"""Hybrid asymmetric/symmetric encryption. Port of ``com.bld.crypto.pkcs12.CryptoPkcs12Utils``.

Scheme, per operation:

1. generate a fresh ephemeral X25519 key pair;
2. ECDH between the ephemeral private key and the recipient's static X25519 public key;
3. HKDF-SHA256 derives a 256-bit AES key, using the random 12-byte GCM nonce as salt and
   ``b"CryptoPkcs12"`` as info;
4. AES-256-GCM encrypts the payload (128-bit authentication tag);
5. emit ``Base64(ephemeralPublicKey).Base64(gcmNonce).Base64(ciphertext + tag)``.

Byte-for-byte compatible with the Java original: X25519 raw 32-byte shared secret, the
same HKDF inputs, and the same ciphertext-then-tag layout that
``Cipher.doFinal`` produces for ``AES/GCM/NoPadding``.
"""

from __future__ import annotations

import base64
import json
import os
from dataclasses import asdict, is_dataclass
from enum import Enum
from typing import Any

from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric.x25519 import X25519PrivateKey
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives.kdf.hkdf import HKDF
from cryptography.hazmat.primitives.serialization import (
    Encoding,
    PublicFormat,
    load_der_public_key,
)

from ..bean.crypto_key_utils import _is_blank, decode_value, encode_value
from ..commons.key_utility import build_envelope, open_envelope
from ..exception import CryptoException
from ..key.jks_key import JksKey

GCM_TAG_LENGTH_BITS = 128
GCM_IV_LENGTH_BYTES = 12
AES_KEY_LENGTH_BYTES = 32
DELIMITER = "."
HKDF_INFO = b"CryptoPkcs12"
UTF8 = "utf-8"

__all__ = ["CryptoPkcs12Utils", "json_default", "object_from_json"]


class CryptoPkcs12Utils:
    """X25519 + AES-256-GCM encryption backed by a PKCS12 keystore."""

    def __init__(self, jks_key: JksKey) -> None:
        self.jks_key = jks_key

    # ------------------------------------------------------------------ strings

    def encrypt_value(self, value: str) -> str | None:
        """Encrypt plain text. Returns ``None`` when ``value`` is blank.

        Output: ``Base64(ephemeralPublicKey).Base64(gcmNonce).Base64(ciphertext+tag)``.
        """
        if _is_blank(value):
            return None
        try:
            ephemeral_private = X25519PrivateKey.generate()
            shared_secret = ephemeral_private.exchange(self.jks_key.public_key)

            nonce = os.urandom(GCM_IV_LENGTH_BYTES)
            aes_key = _hkdf(shared_secret, nonce)

            ciphertext = AESGCM(aes_key).encrypt(nonce, str(value).encode(UTF8), None)

            ephemeral_public = ephemeral_private.public_key().public_bytes(
                Encoding.DER, PublicFormat.SubjectPublicKeyInfo
            )
            return DELIMITER.join(
                (
                    base64.b64encode(ephemeral_public).decode(),
                    base64.b64encode(nonce).decode(),
                    base64.b64encode(ciphertext).decode(),
                )
            )
        except Exception as error:
            raise CryptoException(error) from error

    def decrypt_value(self, value: str) -> str | None:
        """Decrypt a string produced by :meth:`encrypt_value`. Returns ``None`` when blank."""
        if _is_blank(value):
            return None
        try:
            parts = value.split(DELIMITER)
            if len(parts) != 3:
                raise CryptoException(
                    "Invalid PKCS12 encrypted value: expected 3 dot-separated Base64 components"
                )
            ephemeral_public_der = base64.b64decode(parts[0])
            nonce = base64.b64decode(parts[1])
            ciphertext = base64.b64decode(parts[2])

            ephemeral_public = load_der_public_key(ephemeral_public_der)
            shared_secret = self.jks_key.private_key.exchange(ephemeral_public)

            aes_key = _hkdf(shared_secret, nonce)
            return AESGCM(aes_key).decrypt(nonce, ciphertext, None).decode(UTF8)
        except CryptoException:
            raise
        except Exception as error:
            raise CryptoException(error) from error

    # ------------------------------------------------------------------- uris

    def encrypt_uri(self, value: str) -> str | None:
        """Encrypt, then add an extra Base64 layer so the token is URL-safe."""
        return encode_value(self.encrypt_value(value))

    def decrypt_uri(self, value: str) -> str | None:
        """Strip the extra Base64 layer, then decrypt."""
        if _is_blank(value):
            return None
        return self.decrypt_value(decode_value(value))

    # ----------------------------------------------------------------- objects

    def encrypt_object(self, value: Any) -> str | None:
        """Serialise ``value`` to JSON, then encrypt."""
        return self.encrypt_value(json.dumps(value, separators=(",", ":"), ensure_ascii=False, default=json_default))

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

    # ------------------------------------------------------- key-bound tokens

    def encrypt_key_value(self, key: str, value: str) -> str | None:
        """Wrap ``value`` in a ``{key, value}`` envelope and encrypt it.

        The key identifier binds the token to its logical field, so a token cannot be
        replayed into a different field. Same format as ``@CryptoPkcs12(value = key)``.
        """
        return self.encrypt_value(build_envelope(key, value))

    def encrypt_key_uri(self, key: str, value: str) -> str | None:
        """URL-safe variant of :meth:`encrypt_key_value`."""
        return self.encrypt_uri(build_envelope(key, value))

    def decrypt_key_value(self, key: str, token: str) -> str | None:
        """Decrypt a token from :meth:`encrypt_key_value`, verifying it belongs to ``key``."""
        if _is_blank(token):
            return None
        return open_envelope(self.decrypt_value(token), key)

    def decrypt_key_uri(self, key: str, token: str) -> str | None:
        """URL-safe variant of :meth:`decrypt_key_value`."""
        if _is_blank(token):
            return None
        return open_envelope(self.decrypt_uri(token), key)

    # ------------------------------------------------------------------ extras

    def public_key_base64(self) -> str | None:
        """Base64 X.509 public key, to hand to an external encryptor."""
        return self.jks_key.public_key_base64()


def _hkdf(input_key_material: bytes, salt: bytes) -> bytes:
    return HKDF(
        algorithm=hashes.SHA256(),
        length=AES_KEY_LENGTH_BYTES,
        salt=salt,
        info=HKDF_INFO,
    ).derive(input_key_material)


def json_default(value: Any) -> Any:
    """``json.dumps`` hook: dataclasses become dicts, enums become their value."""
    if is_dataclass(value) and not isinstance(value, type):
        return asdict(value)
    if isinstance(value, Enum):
        return value.value
    raise TypeError(f"Object of type {type(value).__name__} is not JSON serialisable")


def object_from_json(payload: str | None, response: Any) -> Any:
    """Rebuild ``response`` from a JSON payload.

    ``response`` may be a dataclass type, a plain container such as ``dict``/``list``, or
    any callable decoder.
    """
    if payload is None:
        return None
    parsed = json.loads(payload)
    if response is dict or response is None:
        return parsed
    if is_dataclass(response) and isinstance(parsed, dict):
        return response(**parsed)
    if callable(response):
        return response(parsed)
    return parsed
