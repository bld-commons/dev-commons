"""Low level encrypt/decrypt shared by every key strategy. Port of ``com.bld.crypto.bean.CryptoKeyUtils``.

The Java original calls ``Cipher.getInstance(instanceType.name())`` with no explicit
transformation, so the cipher is whatever the JCA default is. Those defaults are what
this port reproduces, because they define the wire format:

============  ==========================  =============================
InstanceType  JCA default transformation   Python cipher
============  ==========================  =============================
``AES``       AES/ECB/PKCS5Padding          AES-ECB + PKCS7
``RSA``       RSA/ECB/PKCS1Padding         RSA + PKCS1 v1.5
============  ==========================  =============================
"""

from __future__ import annotations

import base64

from cryptography.hazmat.primitives import padding as symmetric_padding
from cryptography.hazmat.primitives.asymmetric import padding as asymmetric_padding
from cryptography.hazmat.primitives.asymmetric.rsa import RSAPrivateKey, RSAPublicKey
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes

from ..exception import CryptoException
from ..type import InstanceType

UTF8 = "utf-8"


def _is_blank(value) -> bool:
    """``True`` for ``None``, empty and whitespace-only values, as ``StringUtils.isBlank``."""
    return value is None or not str(value).strip()


def _not_blank(value) -> bool:
    return not _is_blank(value)


def encrypt_value(value: str, key, instance_type: InstanceType) -> str | None:
    """Encrypt ``value`` and return Base64 cipher text, or ``None`` when blank.

    Mirrors ``CryptoKeyUtils.encryptValue(String, Key, InstanceType)``.
    """
    if not _not_blank(value):
        return None
    try:
        return base64.b64encode(_do_encrypt(str(value).encode(UTF8), key, instance_type)).decode()
    except CryptoException:
        raise
    except Exception as error:
        raise CryptoException(error) from error


def decrypt_value(value: str, key, instance_type: InstanceType) -> str | None:
    """Decrypt Base64 cipher text and return the plain text, or ``None`` when blank.

    Mirrors ``CryptoKeyUtils.decryptValue(String, Key, InstanceType)``.
    """
    if not _not_blank(value):
        return None
    try:
        return _do_decrypt(base64.b64decode(value), key, instance_type).decode(UTF8)
    except CryptoException:
        raise
    except Exception as error:
        raise CryptoException(error) from error


def _do_encrypt(payload: bytes, key, instance_type: InstanceType) -> bytes:
    if instance_type is InstanceType.rsa:
        if isinstance(key, RSAPrivateKey):
            # JCA hands an RSAPrivateKey to ENCRYPT_MODE and runs the private operation over
            # PKCS#1 type 1 padding (00 01 FF..FF 00 || M), not the public one.
            return _rsa_private_operation(payload, key)
        return key.encrypt(payload, asymmetric_padding.PKCS1v15())
    if instance_type is InstanceType.aes:
        padder = symmetric_padding.PKCS7(algorithms.AES.block_size).padder()
        encryptor = Cipher(algorithms.AES(key), modes.ECB()).encryptor()
        return encryptor.update(padder.update(payload) + padder.finalize()) + encryptor.finalize()
    raise CryptoException(f'Cipher "{instance_type}" is not an encryption algorithm in this port')


def _do_decrypt(ciphertext: bytes, key, instance_type: InstanceType) -> bytes:
    if instance_type is InstanceType.rsa:
        if isinstance(key, RSAPublicKey):
            # Counterpart of the private operation above: raw public exponent, then unpad.
            return _rsa_public_operation(ciphertext, key)
        return key.decrypt(ciphertext, asymmetric_padding.PKCS1v15())
    if instance_type is InstanceType.aes:
        decryptor = Cipher(algorithms.AES(key), modes.ECB()).decryptor()
        padded = decryptor.update(ciphertext) + decryptor.finalize()
        unpadder = symmetric_padding.PKCS7(algorithms.AES.block_size).unpadder()
        return unpadder.update(padded) + unpadder.finalize()
    raise CryptoException(f'Cipher "{instance_type}" is not an encryption algorithm in this port')


def _rsa_private_operation(payload: bytes, key) -> bytes:
    """``EM^d mod n`` with PKCS#1 type 1 padding, as ``Cipher(ENCRYPT_MODE, RSAPrivateKey)`` does."""
    numbers = key.private_numbers()
    modulus = numbers.public_numbers.n
    size = (modulus.bit_length() + 7) // 8
    if len(payload) > size - 11:
        raise CryptoException(f"Data too large for an RSA operation: {len(payload)} > {size - 11}")
    padded = b"\x00\x01" + b"\xff" * (size - len(payload) - 3) + b"\x00" + payload
    return pow(int.from_bytes(padded, "big"), numbers.d, modulus).to_bytes(size, "big")


def _rsa_public_operation(ciphertext: bytes, key) -> bytes:
    """``c^e mod n`` with PKCS#1 type 1 unpadding, as ``Cipher(DECRYPT_MODE, RSAPublicKey)`` does."""
    numbers = key.public_numbers()
    modulus = numbers.n
    size = (modulus.bit_length() + 7) // 8
    padded = pow(int.from_bytes(ciphertext, "big"), numbers.e, modulus).to_bytes(size, "big")
    if padded[0] != 0x00 or padded[1] != 0x01:
        raise CryptoException("RSA block does not carry PKCS#1 type 1 padding")
    separator = padded.find(b"\x00", 2)
    if separator < 10:
        raise CryptoException("RSA block has too short a padding string")
    return padded[separator + 1 :]


def encode_value(value_encrypted: str | None) -> str | None:
    """Add the outer Base64 layer used by the ``url = true`` option.

    Mirrors ``CryptoKeyUtils.encodeValue``: standard (not URL-safe) Base64, which is
    what ``Base64.getEncoder()`` produces. Kept byte compatible with the Java output.
    """
    if value_encrypted:
        return base64.b64encode(value_encrypted.encode(UTF8)).decode()
    return None


def decode_value(value: str) -> str:
    """Strip the outer Base64 layer added by :func:`encode_value`."""
    return base64.b64decode(value).decode(UTF8)


class CryptoKeyUtils:
    """Abstract base mirroring ``com.bld.crypto.bean.CryptoKeyUtils``.

    Subclasses resolve a logical key name to a concrete key; the cipher work stays here.
    """

    obj_mapper = None

    @staticmethod
    def encrypt_value(value: str, key, instance_type: InstanceType) -> str | None:
        return encrypt_value(value, key, instance_type)

    def decrypt_value_with(self, value: str, key, instance_type: InstanceType) -> str | None:
        return decrypt_value(value, key, instance_type)

    def encrypt_value_by_name(self, value: str, key_name: str) -> str:  # pragma: no cover - abstract
        raise NotImplementedError

    def decrypt_value_by_name(self, value: str, key_name: str) -> str:  # pragma: no cover - abstract
        raise NotImplementedError

    def encrypt_uri(self, value: str, key_name: str) -> str | None:
        """Encrypt then add the URL-safe Base64 layer."""
        return encode_value(self.encrypt_value_by_name(value, key_name))

    def decrypt_uri(self, value: str, key_name: str) -> str | None:
        """Strip the URL-safe Base64 layer then decrypt."""
        if not _not_blank(value):
            return None
        return self.decrypt_value_by_name(decode_value(value), key_name)
