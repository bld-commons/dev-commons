"""``@CryptoPkcs12`` equivalent. Port of ``com.bld.crypto.pkcs12.annotation.CryptoPkcs12``."""

from __future__ import annotations

from dataclasses import dataclass

from ..annotation import CryptoAnnotation


@dataclass(frozen=True)
class CryptoPkcs12(CryptoAnnotation):
    """Transparent X25519 + AES-256-GCM encryption on a dataclass field.

    Args:
        value: logical key identifier embedded in the encrypted payload and verified on
            decryption, preventing cross-field substitution.
        url: add the extra Base64 layer so the token is safe in a URL path segment or
            query parameter.

    Example:
        ``id_user: Annotated[int | None, CryptoPkcs12("idUser", url=True)] = None``
    """

    def encrypt(self, plain: str) -> str | None:
        from ..config import CryptoConfig

        utils = CryptoConfig.require_pkcs12()
        return utils.encrypt_key_uri(self.value, plain) if self.url else utils.encrypt_key_value(self.value, plain)

    def decrypt(self, token: str) -> str | None:
        from ..config import CryptoConfig

        utils = CryptoConfig.require_pkcs12()
        return utils.decrypt_key_uri(self.value, token) if self.url else utils.decrypt_key_value(self.value, token)
