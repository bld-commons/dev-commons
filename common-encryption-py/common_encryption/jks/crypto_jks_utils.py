"""RSA encryption from a JKS keystore. Port of ``com.bld.crypto.jks.CryptoJksUtils``.

Every operation defaults to the classic asymmetric direction (encrypt with the public
key, decrypt with the private key); passing a :class:`~common_encryption.type.CryptoType`
overrides that, which is what ``@CryptoJks(encrypt = ..., decrypt = ...)`` does.
"""

from __future__ import annotations

import json
from typing import Any

from ..bean.crypto_key_utils import (
    _is_blank,
    _not_blank,
    decode_value,
    decrypt_value,
    encode_value,
    encrypt_value,
)
from ..commons.key_utility import build_envelope, open_envelope
from ..key.jks_key import JksKey
from ..pkcs12.crypto_pkcs12_utils import json_default, object_from_json
from ..type import CryptoType, InstanceType

class CryptoJksUtils:
    """RSA encryption backed by a JKS (or PKCS12) keystore."""

    def __init__(self, jks_key: JksKey) -> None:
        self.jks_key = jks_key

    # ------------------------------------------------------------ key selection

    def _encrypt_key(self, crypto_type: CryptoType = CryptoType.public_key):
        """Key handed to the cipher, mirroring ``getEncryptKey``.

        ``CryptoType.privateKey`` returns the private key, and the cipher layer then runs
        the RSA private operation, exactly as JCA does for
        ``Cipher.init(ENCRYPT_MODE, RSAPrivateKey)``.
        """
        return (
            self.jks_key.private_key
            if crypto_type is CryptoType.private_key
            else self.jks_key.public_key
        )

    def _decrypt_key(self, crypto_type: CryptoType = CryptoType.private_key):
        """Key handed to the cipher, mirroring ``getDecryptKey``."""
        return (
            self.jks_key.private_key
            if crypto_type is CryptoType.private_key
            else self.jks_key.public_key
        )

    # ----------------------------------------------------------------- strings

    def encrypt_value(self, value: str, crypto_type: CryptoType = CryptoType.public_key) -> str | None:
        """Encrypt with the selected key and return Base64 cipher text."""
        return encrypt_value(value, self._encrypt_key(crypto_type), InstanceType.rsa)

    def decrypt_value(self, value: str, crypto_type: CryptoType = CryptoType.private_key) -> str | None:
        """Decrypt Base64 cipher text with the selected key."""
        return decrypt_value(value, self._decrypt_key(crypto_type), InstanceType.rsa)

    # ------------------------------------------------------------------- uris

    def encrypt_uri(self, value: str, crypto_type: CryptoType = CryptoType.public_key) -> str | None:
        """Encrypt, then add an extra Base64 layer so the token is URL-safe."""
        return encode_value(self.encrypt_value(value, crypto_type))

    def decrypt_uri(self, value: str, crypto_type: CryptoType = CryptoType.private_key) -> str | None:
        """Strip the extra Base64 layer, then decrypt."""
        if not _not_blank(value):
            return None
        return self.decrypt_value(decode_value(value), crypto_type)

    # ----------------------------------------------------------------- objects

    def encrypt_object(self, value: Any, crypto_type: CryptoType = CryptoType.public_key) -> str | None:
        """Serialise ``value`` to JSON, then encrypt."""
        return self.encrypt_value(_to_json(value), crypto_type)

    def decrypt_object(self, value: str, response: Any = dict, crypto_type: CryptoType = CryptoType.private_key) -> Any:
        """Decrypt, then rebuild ``response`` from the JSON payload."""
        return object_from_json(self.decrypt_value(value, crypto_type), response)

    def encrypt_object_uri(self, value: Any, crypto_type: CryptoType = CryptoType.public_key) -> str | None:
        """Serialise to JSON, encrypt, then add the extra Base64 layer."""
        return self.encrypt_uri(_to_json(value), crypto_type)

    def decrypt_object_uri(self, value: str, response: Any = dict, crypto_type: CryptoType = CryptoType.private_key) -> Any:
        """Strip the extra Base64 layer, decrypt, then rebuild ``response``."""
        return object_from_json(self.decrypt_uri(value, crypto_type), response)

    # ------------------------------------------------------- key-bound tokens

    def encrypt_key_value(self, key: str, value: str, crypto_type: CryptoType = CryptoType.public_key) -> str | None:
        """Wrap ``value`` in a ``{key, value}`` envelope and encrypt it.

        Same envelope as ``@CryptoJks(value = key)``: the identifier binds the token to
        its logical field, so it cannot be replayed into a different field.
        """
        return self.encrypt_value(build_envelope(key, value), crypto_type)

    def encrypt_key_uri(self, key: str, value: str, crypto_type: CryptoType = CryptoType.public_key) -> str | None:
        """URL-safe variant of :meth:`encrypt_key_value`."""
        return self.encrypt_uri(build_envelope(key, value), crypto_type)

    def decrypt_key_value(self, key: str, token: str, crypto_type: CryptoType = CryptoType.private_key) -> str | None:
        """Decrypt a token from :meth:`encrypt_key_value`, verifying it belongs to ``key``."""
        if _is_blank(token):
            return None
        return open_envelope(self.decrypt_value(token, crypto_type), key)

    def decrypt_key_uri(self, key: str, token: str, crypto_type: CryptoType = CryptoType.private_key) -> str | None:
        """URL-safe variant of :meth:`decrypt_key_value`."""
        if _is_blank(token):
            return None
        return open_envelope(self.decrypt_uri(token, crypto_type), key)

    # ------------------------------------------------------------------ extras

    def public_key_base64(self) -> str | None:
        """Base64 X.509 public key, to hand to an external encryptor."""
        return self.jks_key.public_key_base64()


def _to_json(value: Any) -> str:
    return json.dumps(value, separators=(",", ":"), ensure_ascii=False, default=json_default)
