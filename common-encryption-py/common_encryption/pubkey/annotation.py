"""``@CryptoPubKey`` equivalent. Port of ``com.bld.crypto.pubkey.annotations.CryptoPubKey``.

The Java package also declares ``@EncryptPubKey`` and ``@DecryptPubKey``. Jackson registers
only a serializer for the first and only a deserializer for the second, so those two are
one directional here too; ``CryptoPubKey`` is the bidirectional one.
"""

from __future__ import annotations

from dataclasses import dataclass

from ..annotation import CryptoAnnotation


@dataclass(frozen=True)
class CryptoPubKey(CryptoAnnotation):
    """Transparent RSA encryption against a configured public key.

    Args:
        value: name of the key under ``com.bld.crypto.public-key.keys.<value>``. Unlike
            the keystore annotations it is not sealed into the token, so no envelope is
            produced.
        url: add the extra Base64 layer so the token is safe in a URL path segment or
            query parameter.

    Example:
        ``email: Annotated[str, CryptoPubKey("mase", url=True)] = None``
    """

    def encrypt(self, plain: str) -> str | None:
        from ..config import CryptoConfig

        utils = CryptoConfig.require_pub_key()
        return utils.encrypt_uri(plain, self.value) if self.url else utils.encrypt_value(plain, self.value)

    def decrypt(self, token: str) -> str | None:
        from ..config import CryptoConfig

        utils = CryptoConfig.require_pub_key()
        return utils.decrypt_uri(token, self.value) if self.url else utils.decrypt_value(token, self.value)


__all__ = ["CryptoPubKey", "CryptoPubKeyDecrypt", "CryptoPubKeyEncrypt"]


@dataclass(frozen=True)
class CryptoPubKeyEncrypt(CryptoPubKey):
    """``@EncryptPubKey``: encrypt on write, pass the stored value through on read.

    Java registers only a serializer for this annotation, so nothing decrypts the field.
    """

    def decrypt(self, token: str) -> str | None:
        return token


@dataclass(frozen=True)
class CryptoPubKeyDecrypt(CryptoPubKey):
    """``@DecryptPubKey``: decrypt on read, pass the incoming value through on write."""

    def encrypt(self, plain: str) -> str | None:
        return plain
