"""Symmetric encryption. Port of ``com.bld.crypto.aes.CryptoAesUtils``.

``Cipher.getInstance("AES")`` in Java resolves to AES in ECB mode with PKCS5 padding, so
that is what this reproduces: it fits the keyed fields the Java module uses it for, but it
is not a general purpose cipher.
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
from ..commons.secret import AesSecrets
from ..exception import CryptoException
from ..pkcs12.crypto_pkcs12_utils import json_default, object_from_json
from ..type import InstanceType


class CryptoAesUtils:
    """AES encryption against one of the named keys in :class:`AesSecrets`.

    Port of ``CryptoAesUtils``, which resolves the logical key name through
    ``CipherAesSecret`` and then delegates to ``CryptoKeyUtils`` with ``InstanceType.AES``.
    """

    def __init__(self, cipher_aes_secret: AesSecrets) -> None:
        self.cipher_aes_secret = cipher_aes_secret

    # ------------------------------------------------------------------ strings

    def encrypt_value(self, value: str, key: str) -> str | None:
        """Encrypt with the named AES key. Returns ``None`` when ``value`` is blank."""
        return encrypt_value(value, self._key(key), InstanceType.aes)

    def decrypt_value(self, value: str, key: str) -> str | None:
        """Decrypt a value produced by :meth:`encrypt_value`."""
        return decrypt_value(value, self._key(key), InstanceType.aes)

    # ------------------------------------------------------------------- uris

    def encrypt_uri(self, value: str, key: str) -> str | None:
        """Encrypt, then add the extra Base64 layer used for URL-safe tokens."""
        return encode_value(self.encrypt_value(value, key))

    def decrypt_uri(self, value: str, key: str) -> str | None:
        """Strip the extra Base64 layer, then decrypt."""
        if not _not_blank(value):
            return None
        return self.decrypt_value(decode_value(value), key)

    # ----------------------------------------------------------------- objects

    def encrypt_object(self, value: Any, key: str) -> str | None:
        """Serialise ``value`` to JSON, then encrypt."""
        return self.encrypt_value(self._to_json(value), key)

    def decrypt_object(self, value: str, key: str, response: Any = dict) -> Any:
        """Decrypt, then rebuild ``response`` from the JSON payload."""
        return object_from_json(self.decrypt_value(value, key), response)

    def encrypt_object_uri(self, value: Any, key: str) -> str | None:
        """Serialise to JSON, encrypt, then add the extra Base64 layer."""
        return self.encrypt_uri(self._to_json(value), key)

    def decrypt_object_uri(self, value: str, key: str, response: Any = dict) -> Any:
        """Strip the extra Base64 layer, decrypt, then rebuild ``response``."""
        return object_from_json(self.decrypt_uri(value, key), response)

    # ----------------------------------------------------------------- private

    def _key(self, key: str) -> bytes:
        try:
            return self.cipher_aes_secret.get_secret_key(key)
        except CryptoException:
            raise
        except Exception as error:
            raise CryptoException(error) from error

    @staticmethod
    def _to_json(value: Any) -> str:
        return json.dumps(value, separators=(",", ":"), ensure_ascii=False, default=json_default)


__all__ = ["CryptoAesUtils"]
