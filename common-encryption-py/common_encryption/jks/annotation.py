"""``@CryptoJks`` equivalent. Port of ``com.bld.crypto.jks.annotation.CryptoJks``."""

from __future__ import annotations

from dataclasses import dataclass

from ..annotation import CryptoAnnotation
from ..type import CryptoType


@dataclass(frozen=True)
class CryptoJks(CryptoAnnotation):
    """Transparent RSA encryption on a dataclass field, keyed from a JKS keystore.

    Args:
        value: logical key identifier embedded in the encrypted payload and verified on
            decryption.
        url: add the extra Base64 layer so the token is safe in a URL path segment or
            query parameter.
        encrypt: key used to encrypt. ``publicKey`` (default) for the standard
            asymmetric direction, ``privateKey`` to sign-then-decrypt with the pair.
        decrypt: key used to decrypt, ``privateKey`` by default.

    Example:
        ``code: Annotated[str, CryptoJks("myJksKey", url=True)] = None``
    """

    encrypt_key: CryptoType = CryptoType.public_key
    decrypt_key: CryptoType = CryptoType.private_key

    def encrypt(self, plain: str) -> str | None:
        from ..config import CryptoConfig

        utils = CryptoConfig.require_jks()
        if self.url:
            return utils.encrypt_key_uri(self.value, plain, self.encrypt_key)
        return utils.encrypt_key_value(self.value, plain, self.encrypt_key)

    def decrypt(self, token: str) -> str | None:
        from ..config import CryptoConfig

        utils = CryptoConfig.require_jks()
        if self.url:
            return utils.decrypt_key_uri(self.value, token, self.decrypt_key)
        return utils.decrypt_key_value(self.value, token, self.decrypt_key)
