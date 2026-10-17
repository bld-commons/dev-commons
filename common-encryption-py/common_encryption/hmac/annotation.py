"""``@CryptoHmac`` equivalent. Port of ``com.bld.crypto.hmac.annotation.CryptoHmac``."""

from __future__ import annotations

from dataclasses import dataclass

from ..annotation import CryptoAnnotation


@dataclass(frozen=True)
class CryptoHmac(CryptoAnnotation):
    """Integrity protection on a dataclass field: the value is signed, not encrypted.

    Args:
        value: logical key identifier sealed into the signed envelope and checked on
            verification, so a token cannot be replayed into another field.
        url: use the URL-safe Base64 alphabet for the token.

    Example:
        ``id_user: Annotated[int | None, CryptoHmac("idUser")] = None``
    """

    def encrypt(self, plain: str) -> str | None:
        from ..config import CryptoConfig

        utils = CryptoConfig.require_hmac()
        return utils.sign_uri(self.value, plain) if self.url else utils.sign_value(self.value, plain)

    def decrypt(self, token: str) -> str | None:
        from ..config import CryptoConfig

        utils = CryptoConfig.require_hmac()
        return utils.verify_uri(token, self.value) if self.url else utils.verify_value(token, self.value)
