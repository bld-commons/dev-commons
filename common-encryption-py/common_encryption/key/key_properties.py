"""Keystore loading, the equivalent of ``CryptoJksConfiguration`` / ``CryptoPkcs12Configuration``.

The Java module builds a ``KeyStore`` from ``instance-jks`` (``PKCS12`` or ``JKS``),
loads the ``file`` resource with ``password``, then reads the entry named by ``alias``.
:func:`load_jks_key` does the same and derives the public key exactly where Java does.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path

from cryptography.hazmat.primitives.asymmetric.x25519 import X25519PrivateKey
from cryptography.hazmat.primitives.serialization import load_der_private_key
from cryptography.hazmat.primitives.serialization.pkcs12 import load_key_and_certificates
from cryptography.x509 import load_der_x509_certificate

from ..commons import jks_store
from ..exception import CryptoException
from .jks_key import JksKey

DEFAULT_INSTANCE_JKS = "PKCS12"

# ASN.1 OID 1.2.840.113549.1.9.20 friendlyName, present in the attribute bags that keytool
# and OpenSSL write for every PKCS12 entry. Used only to decide whether an alias can be
# validated at all; the value itself is never parsed.
_FRIENDLY_NAME_OID = bytes.fromhex("06092a864886f70d010914")


@dataclass
class KeyProperties:
    """Keystore coordinates. Port of ``com.bld.crypto.key.KeyProperties``.

    Maps 1:1 onto the Spring properties::

        com.bld.crypto.jks.file=/etc/keystore/app.p12
        com.bld.crypto.jks.password=changeit
        com.bld.crypto.jks.alias=mase-rsa
        com.bld.crypto.jks.instance-jks=PKCS12
    """

    file: str | Path | bytes | None = None
    password: str = ""
    alias: str = ""
    instance_jks: str = DEFAULT_INSTANCE_JKS

    def read_bytes(self) -> bytes:
        """Read the keystore bytes from a path, a ``file:``/``classpath:`` URI, or raw bytes.

        A Java ``classpath:`` resource is resolved by the JVM; here the caller passes the
        resource path directly, so only the scheme prefix is stripped.
        """
        if isinstance(self.file, bytes):
            return self.file
        location = str(self.file or "")
        for scheme in ("classpath:", "file:"):
            if location.startswith(scheme):
                location = location[len(scheme) :]
        if not location:
            raise CryptoException("No keystore file configured")
        return Path(location).expanduser().read_bytes()

    def load_key_pair(self) -> JksKey:
        """Load the configured alias into a :class:`JksKey`."""
        return load_jks_key(self.read_bytes(), self.password, self.alias, self.instance_jks)


def load_jks_key(data: bytes, password: str, alias: str, instance_jks: str = DEFAULT_INSTANCE_JKS) -> JksKey:
    """Load a key pair from JKS or PKCS12 ``data``.

    Mirrors the Java configuration classes: JKS requires a certificate and takes the
    public key from it, while PKCS12 falls back to deriving the public key from the
    private key when the keystore was exported with ``-nocerts`` (no certificate), which
    is what ``openssl pkcs12 -export -nocerts`` produces for X25519.
    """
    kind = (instance_jks or DEFAULT_INSTANCE_JKS).upper()
    try:
        if kind == "JKS":
            return _load_jks(data, password, alias)
        if kind in ("PKCS12", "PKCS#12", "P12", "PFX"):
            return _load_pkcs12(data, password, alias)
    except CryptoException:
        raise
    except Exception as error:
        raise CryptoException(error) from error
    raise CryptoException(f'Unsupported keystore type "{instance_jks}": expected PKCS12 or JKS')


def _load_pkcs12(data: bytes, password: str, alias: str) -> JksKey:
    _assert_alias_present(data, alias)
    key, certificate, _ = load_key_and_certificates(data, password.encode() or None)
    if key is None:
        raise CryptoException(f'Alias "{alias}" does not exist in the keystore')
    if certificate is not None:
        return JksKey(key, certificate.public_key())
    # No certificate (-nocerts): recover the public key from the private key, the same
    # fallback as CryptoPkcs12Configuration.resolvePublicKey.
    return JksKey(key, _derive_public_key(key))


def _assert_alias_present(data: bytes, alias: str) -> None:
    """Reject an unknown alias instead of silently returning the store's only key pair.

    ``cryptography`` hands back a single key from a PKCS12 file and offers no alias
    lookup, so the alias is checked against the ``friendlyName`` attributes when the store
    records them, in both the BMPString and UTF-8 encodings that keytool and OpenSSL use.
    Stores written without friendly names carry no alias information to check against and
    are accepted.
    """
    if not alias or _FRIENDLY_NAME_OID not in data:
        return
    if alias.encode("utf-16-be") in data or alias.encode() in data:
        return
    raise CryptoException(f'Alias "{alias}" is not present in the PKCS12 keystore')


def _derive_public_key(private_key):
    """Derive the public half of an X25519 private key.

    Port of the BouncyCastle derivation in ``CryptoPkcs12Configuration.resolvePublicKey``
    (``X25519PrivateKeyParameters.generatePublicKey`` + ``SubjectPublicKeyInfoFactory``).
    """
    if isinstance(private_key, X25519PrivateKey):
        return private_key.public_key()
    return private_key.public_key() if hasattr(private_key, "public_key") else None


def _load_jks(data: bytes, password: str, alias: str) -> JksKey:
    store = jks_store.load_jks_store(data, password)
    private_der, chain = store.key_entry(alias, password)
    if not chain:
        raise CryptoException(f'Alias "{alias}" has no certificate, cannot derive the public key')
    certificate = load_der_x509_certificate(chain[0])
    return JksKey(load_der_private_key(private_der, password=None), certificate.public_key())
