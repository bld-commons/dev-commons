"""Detached signatures. Port of ``com.bld.crypto.signature.SignatureKeyUtils``.

The Java class hands the configured ``instance-signature`` straight to
``Signature.getInstance``, for example ``SHA256withRSA``. The equivalent ``cryptography``
name is looked up by that same string, so the configuration carries over unchanged.
"""

from __future__ import annotations

import base64

from cryptography.exceptions import InvalidSignature
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import ec, padding

from ..exception import CryptoException
from ..key.jks_key import JksKey

DEFAULT_INSTANCE_SIGNATURE = "SHA256withRSA"

_HASHES = {
    "SHA1": hashes.SHA1,
    "SHA224": hashes.SHA224,
    "SHA256": hashes.SHA256,
    "SHA384": hashes.SHA384,
    "SHA512": hashes.SHA512,
}


def _split_instance(instance: str) -> tuple[type[hashes.HashAlgorithm], bool]:
    """Split ``SHA256withRSA`` into its hash and whether the key is RSA."""
    parts = instance.split("with")
    if len(parts) != 2:
        raise CryptoException(f'Unsupported signature instance "{instance}"')
    algorithm, key_algorithm = parts[0].strip(), parts[1].strip().upper()
    digest = _HASHES.get(algorithm.upper())
    if digest is None:
        raise CryptoException(f'Unsupported signature digest in "{instance}"')
    if key_algorithm not in ("RSA", "EC", "ECDSA"):
        raise CryptoException(f'Unsupported signature key algorithm in "{instance}"')
    return digest(), key_algorithm == "RSA"


def _padding(digest, is_rsa: bool):
    """Java's ``SHA256withRSA`` is RSASSA-PKCS1-v1_5, so PKCS1v15 rather than PSS.

    Both branches produce DER encoded output, matching ``java.security.Signature``.
    """
    return padding.PKCS1v15() if is_rsa else padding.ECDSA(ec.ECDSA(digest))


class SignatureKeyUtils:
    """Sign and verify with the configured key pair."""

    def __init__(
        self,
        jks_key: JksKey,
        instance_signature: str = DEFAULT_INSTANCE_SIGNATURE,
    ) -> None:
        self.jks_key = jks_key
        self.instance_signature = instance_signature

    # ---------------------------------------------------------------- instance

    def sign(self, text: str) -> str:
        """Sign ``text`` and return the Base64 signature."""
        return sign(text, self.jks_key.private_key, self.instance_signature)

    def verify(self, signed: str, check_text: str) -> bool:
        """Return whether ``signed`` is a valid signature over ``check_text``."""
        return verify(self.jks_key.public_key, signed, check_text, self.instance_signature)

    def get_public_key(self):
        """The public key object, as ``SignatureKeyUtils.getPublicKey()``."""
        return self.jks_key.public_key

    def public_key(self) -> str:
        """Base64 X.509 public key, as ``SignatureKeyUtils.publicKey()``."""
        return self.jks_key.public_key_base64()


def sign(text: str, private_key, instance: str = DEFAULT_INSTANCE_SIGNATURE) -> str:
    """Sign ``text``, mirroring the static ``SignatureKeyUtils.sign``."""
    digest, is_rsa = _split_instance(instance)
    payload = text.encode()
    try:
        raw = private_key.sign(payload, _padding(digest, is_rsa), digest)
        return base64.b64encode(raw).decode()
    except Exception as error:
        raise CryptoException(error) from error


def verify(public_key, signed: str, check_text: str, instance: str = DEFAULT_INSTANCE_SIGNATURE) -> bool:
    """Verify a signature, mirroring the static ``SignatureKeyUtils.verify``."""
    digest, is_rsa = _split_instance(instance)
    try:
        public_key.verify(
            base64.b64decode(signed), check_text.encode(), _padding(digest, is_rsa), digest
        )
        return True
    except InvalidSignature:
        return False
    except Exception as error:
        raise CryptoException(error) from error


__all__ = ["DEFAULT_INSTANCE_SIGNATURE", "SignatureKeyUtils", "sign", "verify"]
