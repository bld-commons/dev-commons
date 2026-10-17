"""Configuration binding. Counterpart of Spring's ``@ConfigurationProperties`` wiring.

The Java module builds its beans from ``application.properties``/``application.yml``:
``CryptoJksConfiguration`` is conditional on ``com.bld.crypto.jks.file`` and
``CryptoPkcs12Configuration`` on ``com.bld.crypto.pkcs12.file``. Here the same properties
are read from a mapping (so a parsed YAML can be reused verbatim) or from environment
variables.

Example:
    >>> config = CryptoConfig.from_properties(yaml.safe_load(open("application.yml")))
    >>> config = CryptoConfig.from_env()          # BLD_CRYPTO_PKCS12_FILE, ...
"""

from __future__ import annotations

import re

import base64
import os
from collections.abc import Mapping
from typing import Any

from cryptography.hazmat.primitives.serialization import Encoding, PublicFormat
from cryptography.x509 import load_pem_x509_certificate

from .aes.crypto_aes_utils import CryptoAesUtils
from .commons.secret import AesSecrets, HmacSecret, SecretSpec
from .exception import CryptoException
from .hmac.crypto_hmac_utils import CryptoHmacUtils
from .jks.crypto_jks_utils import CryptoJksUtils
from .key.key_properties import KeyProperties
from .pkcs12.crypto_pkcs12_utils import CryptoPkcs12Utils
from .pubkey.crypto_public_key_utils import CryptoMapPublicKeyUtils
from .signature.signature_key_utils import DEFAULT_INSTANCE_SIGNATURE, SignatureKeyUtils

PROPERTY_PREFIX = "com.bld.crypto."
ENV_PREFIX = "BLD_CRYPTO_"

_KEY_FIELDS = ("file", "password", "alias", "instance_jks")
_SECRET_FIELDS = ("secret", "salt", "key_length", "algorithm")
_ALL_FIELDS = _KEY_FIELDS + _SECRET_FIELDS + ("instance_signature",)
_SECTIONS = ("pkcs12", "jks", "aes", "hmac", "pub_key", "signature")


class CryptoConfig:
    """Holds the utility instances that the field annotations resolve at call time.

    Acts as the Python stand-in for Spring bean injection: Java autowires
    ``CryptoPkcs12Utils`` / ``CryptoJksUtils`` into the Jackson serializers, here the
    annotations look them up here.
    """

    pkcs12 = None
    jks = None
    aes = None
    hmac = None
    pub_key = None
    signature = None

    def __init__(self, pkcs12=None, jks=None, aes=None, hmac=None, pub_key=None, signature=None) -> None:
        self.pkcs12 = pkcs12
        self.jks = jks
        self.aes = aes
        self.hmac = hmac
        self.pub_key = pub_key
        self.signature = signature

    # ------------------------------------------------------------- construction

    @classmethod
    def from_properties(cls, properties: Mapping[str, Any], install: bool = True) -> "CryptoConfig":
        """Build the utilities from ``com.bld.crypto.*`` properties, the Spring property names.

        Every section is optional, mirroring the ``@ConditionalOnProperty`` behaviour: a
        keystore without ``file``, an ``aes`` section without ``keys`` and a ``hmac``
        section without ``secret`` are skipped rather than raising.
        """
        config = cls()
        for name, factory, default_type in (
            ("pkcs12", CryptoPkcs12Utils, "PKCS12"),
            ("jks", CryptoJksUtils, "JKS"),
        ):
            values = _block(properties, PROPERTY_PREFIX + name)
            if values.get("file"):
                values.setdefault("instance_jks", default_type)
                setattr(config, name, factory(KeyProperties(**_keystore_fields(values)).load_key_pair()))

        aes_secrets = _aes_secrets(properties)
        if aes_secrets:
            config.aes = CryptoAesUtils(aes_secrets)

        hmac_values = _block(properties, PROPERTY_PREFIX + "hmac")
        if hmac_values.get("secret"):
            config.hmac = CryptoHmacUtils(_hmac_secret(hmac_values))

        signature_values = _block(properties, PROPERTY_PREFIX + "signature")
        if signature_values.get("file"):
            instance = signature_values.get("instance_signature") or DEFAULT_INSTANCE_SIGNATURE
            signature_values.setdefault("instance_jks", "JKS")
            key = KeyProperties(**_keystore_fields(signature_values)).load_key_pair()
            config.signature = SignatureKeyUtils(key, instance)

        pub_keys = _public_keys(properties)
        if pub_keys:
            config.pub_key = pub_keys

        if install:
            config.install()
        return config

    @classmethod
    def from_env(cls, environ: Mapping[str, str] | None = None, install: bool = True) -> "CryptoConfig":
        """Build the utilities from ``BLD_CRYPTO_*`` environment variables.

        ``BLD_CRYPTO_HMAC_SECRET`` maps onto the single ``com.bld.crypto.hmac`` section and
        the keystores map one to one onto the property names above. The AES keys are named
        (``aes.keys.<name>``) in Java, so they are configured through properties or YAML only.
        """
        environ = os.environ if environ is None else environ
        properties: dict[str, Any] = {}
        for name in ("pkcs12", "jks", "signature"):
            fields = _KEY_FIELDS + (("instance_signature",) if name == "signature" else ())
            for field in fields:
                key = f"{ENV_PREFIX}{name.upper()}_{field.upper()}"
                if key in environ:
                    properties[f"{PROPERTY_PREFIX}{name}.{field}"] = environ[key]
        # Only hmac: the AES keys are a named map in Java (com.bld.crypto.aes.keys.<name>),
        # which has no single-valued environment binding to mirror.
        for field in _SECRET_FIELDS:
            key = f"{ENV_PREFIX}HMAC_{field.upper()}"
            if key in environ:
                properties[f"{PROPERTY_PREFIX}hmac.{field}"] = environ[key]
        return cls.from_properties(properties, install=install)

    # ------------------------------------------------------------------ registry

    def install(self) -> "CryptoConfig":
        """Make this configuration the one the annotations use."""
        for name in _SECTIONS:
            setattr(CryptoConfig, name, getattr(self, name))
        return self

    @classmethod
    def clear(cls) -> None:
        """Forget every configured utility."""
        for name in _SECTIONS:
            setattr(cls, name, None)

    @classmethod
    def _require(cls, name: str, label: str):
        value = getattr(cls, name)
        if value is None:
            raise CryptoException(
                f"No {label} utility configured: call CryptoConfig.from_env() / "
                "CryptoConfig.from_properties() or install one explicitly"
            )
        return value

    @classmethod
    def require_pkcs12(cls):
        return cls._require("pkcs12", "PKCS12")

    @classmethod
    def require_jks(cls):
        return cls._require("jks", "JKS")

    @classmethod
    def require_aes(cls):
        return cls._require("aes", "AES")

    @classmethod
    def require_hmac(cls):
        return cls._require("hmac", "HMAC")

    @classmethod
    def require_pub_key(cls):
        return cls._require("pub_key", "public key")


def _block(properties: Mapping[str, Any], prefix: str, fields=_ALL_FIELDS) -> dict[str, Any]:
    """Collect the ``com.bld.crypto.<section>.*`` settings, flat or nested.

    Both shapes produced by real Spring configuration are accepted:
        {"com.bld.crypto.jks.file": "..."}                 # application.properties
        {"com.bld": {"crypto": {"jks": {"file": ...}}}}  # yaml.safe_load
    Field names may use dashes or underscores, and only known ``fields`` are kept.
    """
    prefix = prefix if prefix.endswith(".") else prefix + "."
    flat: dict[str, Any] = {}
    for key, value in properties.items():
        if isinstance(key, str) and key.startswith(prefix):
            field = _normalise(key[len(prefix) :])
            if field in fields:
                flat[field] = value

    nested = _descend(properties, prefix[:-1].split("."))
    if isinstance(nested, Mapping):
        for field, value in nested.items():
            if isinstance(field, str) and _normalise(field) in fields:
                flat[_normalise(field)] = value
    return flat


def _descend(source: Any, path: list[str]) -> Any:
    """Walk ``path`` through nested mappings.

    YAML keeps a dotted property path inside a single key (``com.bld:`` rather than
    ``com: {bld:``), so each level may match one segment or a dotted prefix.
    """
    if not path:
        return source
    if not isinstance(source, Mapping):
        return None
    head, rest = path[0], path[1:]
    for key, value in source.items():
        if not isinstance(key, str):
            continue
        parts = key.replace("-", "_").split(".")
        if len(parts) <= len(path) and _same(".".join(parts), ".".join(path[: len(parts)])):
            return _descend(value, path[len(parts) :])
        if _same(parts[0], head):
            return _descend(value, rest)
    return None


def _normalise(field: str) -> str:
    """Property name to Python name: ``keyLength``, ``key-length`` and ``key_length`` agree."""
    field = re.sub(r"(?<=[a-z0-9])(?=[A-Z])", "_", field.replace("-", "_").strip())
    return field.lower()


def _same(left: str, right: str) -> bool:
    """Compare two property segments, ignoring case, dashes and underscores.

    ``public-key``, ``public_key`` and ``publicKey`` are the same Spring property.
    """
    return _squash(left) == _squash(right)


def _squash(text: str) -> str:
    return text.replace("-", "").replace("_", "").lower()


def _flat_map(properties: Mapping[str, Any], prefix: str) -> dict[str, Any]:
    """Collect ``<prefix><name>=value`` style properties into a mapping."""
    flat: dict[str, Any] = {}
    for key, value in properties.items():
        if isinstance(key, str) and key.startswith(prefix):
            name = key[len(prefix) :]
            if name and not name.startswith("."):
                flat[name] = value
    return flat


def _keystore_fields(values: Mapping[str, Any]) -> dict[str, Any]:
    """Keep only the fields ``KeyProperties`` accepts."""
    return {f: values[f] for f in _KEY_FIELDS if f in values}


def _aes_secrets(properties: Mapping[str, Any]) -> AesSecrets:
    """Build the named AES keys from ``com.bld.crypto.aes.keys``.

    Java uses PBKDF2WithHmacSHA1 for AES key derivation.
    """
    keys = _descend(properties, ["com", "bld", "crypto", "aes", "keys"])
    if not keys:
        # Flat property form: com.bld.crypto.aes.keys.<name>[.<field>]=...
        grouped: dict[str, dict[str, Any]] = {}
        for key, value in _flat_map(properties, PROPERTY_PREFIX + "aes.keys.").items():
            name, _, field = key.partition(".")
            grouped.setdefault(name, {})[_normalise(field) if field else "password"] = value
        keys = grouped or None
    secrets = AesSecrets()
    for name, spec in (keys or {}).items():
        if isinstance(spec, str):
            spec = {"password": spec}
        secrets.add(
            name,
            SecretSpec(
                password=spec.get("password", ""),
                salt=spec.get("salt"),
                key_length=spec.get("key_length", spec.get("keyLength")),
            ),
        )
    return secrets


def _hmac_secret(values: Mapping[str, Any]) -> HmacSecret:
    """Build the HMAC key from ``com.bld.crypto.hmac``.

    Java uses PBKDF2WithHmacSHA256 here, unlike the AES keys, so the derived bytes differ.
    """
    algorithm = values.get("algorithm") or "HmacSHA256"
    spec = SecretSpec(
        password=values.get("secret", ""),
        salt=values.get("salt"),
        key_length=values.get("key_length"),
        algorithm="pbkdf2_sha256",
    )
    return HmacSecret(spec.material(), algorithm)


def _public_keys(properties: Mapping[str, Any]) -> CryptoMapPublicKeyUtils | None:
    """Build the named public keys from ``com.bld.crypto.public-key.keys``.

    Each value is a PEM certificate or Base64 ``SubjectPublicKeyInfo``, matching the
    ``Resource`` values the Java configuration binds.
    """
    keys = _descend(properties, ["com", "bld", "crypto", "public", "key"])
    if not keys:
        keys = _descend(properties, ["com", "bld", "crypto", "public-key", "keys"])
    if not keys:
        keys = _flat_map(properties, PROPERTY_PREFIX + "public-key.keys.") or None
    if not keys:
        return None
    utils = CryptoMapPublicKeyUtils()
    for name, material in keys.items():
        if isinstance(material, bytes):
            material = material.decode()
        utils.add_public_key_der(name, _public_key_der(material))
    return utils


def _public_key_der(material: str) -> str:
    """Accept either Base64 DER or a PEM certificate, and return Base64 DER."""
    text = material.strip()
    if "-----BEGIN" not in text:
        return "".join(text.split())
    if "PUBLIC KEY" in text:
        body = text.split("-----BEGIN PUBLIC KEY-----")[1].split("-----END")[0]
    else:
        certificate = load_pem_x509_certificate(text.encode())
        body = base64.b64encode(
            certificate.public_key().public_bytes(Encoding.DER, PublicFormat.SubjectPublicKeyInfo)
        ).decode()
    return "".join(body.split())
