"""Public key encryption. Port of ``com.bld.crypto.pubkey``.

The sender holds only the recipient's public key, so this encrypts one way. The
``CryptoType.private_key`` direction is what the ``EncryptPubKey`` / ``DecryptPubKey``
annotation pair selects.
"""

from __future__ import annotations

import json
from typing import Any

from ..bean.crypto_key_utils import (
    _not_blank,
    decode_value,
    decrypt_value,
    encode_value,
    encrypt_value,
)
from ..commons.key_utility import build_envelope, get_public_key, open_envelope
from ..exception import CryptoException
from ..key.jks_key import JksKey
from ..pkcs12.crypto_pkcs12_utils import json_default, object_from_json
from ..type import CryptoType, InstanceType


class CryptoPublicKeyUtils:
    """Encrypt with a single key pair. Port of ``CryptoPublicKeyUtils``.

    Holds the public half only, which is all a sender needs.
    """

    def __init__(self, jks_key: JksKey) -> None:
        self.jks_key = jks_key

    def encrypt_value(self, value: str) -> str | None:
        """Encrypt with the recipient's public key."""
        return encrypt_value(value, self.jks_key.public_key, InstanceType.rsa)

    def decrypt_value(self, value: str) -> str | None:
        """Decrypt with the private key, the only half that can reverse the operation."""
        return decrypt_value(value, self.jks_key.private_key, InstanceType.rsa)

    def encrypt_uri(self, value: str) -> str | None:
        """Encrypt, then add the extra Base64 layer used for URL-safe tokens."""
        return encode_value(self.encrypt_value(value))

    def decrypt_uri(self, value: str) -> str | None:
        """Strip the extra Base64 layer, then decrypt."""
        if not _not_blank(value):
            return None
        return self.decrypt_value(decode_value(value))

    def encrypt_key_value(self, key: str, value: str) -> str | None:
        """Seal ``value`` into a ``{key, value}`` envelope, then encrypt."""
        return self.encrypt_value(build_envelope(key, value))

    def decrypt_key_value(self, key: str, token: str) -> str | None:
        """Decrypt a token from :meth:`encrypt_key_value`, verifying its field."""
        if not _not_blank(token):
            return None
        return open_envelope(self.decrypt_value(token), key)

    def encrypt_object(self, value: Any) -> str | None:
        """Serialise ``value`` to JSON, then encrypt."""
        return self.encrypt_value(
            json.dumps(value, separators=(",", ":"), ensure_ascii=False, default=json_default)
        )

    def decrypt_object(self, value: str, response: Any = dict) -> Any:
        """Decrypt, then rebuild ``response`` from the JSON payload."""
        return object_from_json(self.decrypt_value(value), response)

    def encrypt_object_uri(self, value: Any) -> str | None:
        """Serialise to JSON, encrypt, then add the extra Base64 layer."""
        return self.encrypt_uri(
            json.dumps(value, separators=(",", ":"), ensure_ascii=False, default=json_default)
        )

    def decrypt_object_uri(self, value: str, response: Any = dict) -> Any:
        """Strip the extra Base64 layer, decrypt, then rebuild ``response``."""
        return object_from_json(self.decrypt_uri(value), response)


class CryptoMapPublicKeyUtils:
    """Encrypt against one of several named public keys. Port of ``CryptoMapPublicKeyUtils``."""

    def __init__(self, cipher_public_keys: dict[str, Any] | None = None) -> None:
        self._keys: dict[str, Any] = dict(cipher_public_keys or {})

    def add_public_key(self, key: str, public_key) -> None:
        """Register an already decoded public key."""
        self._keys[key] = public_key

    def add_public_key_der(self, key: str, public_key_base64: str, instance_type=InstanceType.rsa) -> None:
        """Register a public key given as Base64 X.509 ``SubjectPublicKeyInfo``."""
        decoded = get_public_key(public_key_base64, instance_type)
        if decoded is None:
            raise CryptoException(f'Public key "{key}" is empty')
        self._keys[key] = decoded

    def public_key(self, name: str):
        try:
            return self._keys[name]
        except KeyError:
            raise CryptoException(f'No public key configured under the name "{name}"') from None

    # ------------------------------------------------------------------ strings

    def encrypt_value(self, value: str, key: str) -> str | None:
        """Encrypt with the named public key."""
        return encrypt_value(value, self.public_key(key), InstanceType.rsa)

    def decrypt_value(self, value: str, key: str) -> str | None:
        """Decrypt with the named private key, so this is the recipient side."""
        return decrypt_value(value, self.public_key(key), InstanceType.rsa)

    # ------------------------------------------------------------------- uris

    def encrypt_uri(self, value: str, key: str) -> str | None:
        """Encrypt, then add the extra Base64 layer used for URL-safe tokens."""
        return encode_value(self.encrypt_value(value, key))

    def decrypt_uri(self, value: str, key: str) -> str | None:
        """Strip the extra Base64 layer, then decrypt."""
        if not _not_blank(value):
            return None
        return self.decrypt_value(decode_value(value), key)

    # ------------------------------------------------------- key-bound tokens

    def encrypt_key_value(self, field_key: str, value: str, key: str) -> str | None:
        """Seal ``value`` into a ``{field_key, value}`` envelope, then encrypt."""
        return self.encrypt_value(build_envelope(field_key, value), key)

    def decrypt_key_value(self, field_key: str, token: str, key: str) -> str | None:
        """Decrypt a token from :meth:`encrypt_key_value`, verifying its field."""
        if not _not_blank(token):
            return None
        return open_envelope(self.decrypt_value(token, key), field_key)

    # ----------------------------------------------------------------- objects

    def encrypt_object(self, value: Any, key: str) -> str | None:
        """Serialise ``value`` to JSON, then encrypt."""
        return self.encrypt_value(
            json.dumps(value, separators=(",", ":"), ensure_ascii=False, default=json_default), key
        )

    def decrypt_object(self, value: str, response: Any = dict, key: str = "") -> Any:
        """Decrypt, then rebuild ``response`` from the JSON payload."""
        return object_from_json(self.decrypt_value(value, key), response)

    def encrypt_object_uri(self, value: Any, key: str) -> str | None:
        """Serialise to JSON, encrypt, then add the extra Base64 layer."""
        return self.encrypt_uri(
            json.dumps(value, separators=(",", ":"), ensure_ascii=False, default=json_default), key
        )

    def decrypt_object_uri(self, value: str, response: Any = dict, key: str = "") -> Any:
        """Strip the extra Base64 layer, decrypt, then rebuild ``response``."""
        return object_from_json(self.decrypt_uri(value, key), response)


__all__ = ["CryptoMapPublicKeyUtils", "CryptoPublicKeyUtils", "CryptoType"]
