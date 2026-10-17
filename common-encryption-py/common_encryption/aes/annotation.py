"""``@CryptoAes`` equivalent. Port of ``com.bld.crypto.aes.annotation.CryptoAes``."""

from __future__ import annotations

from dataclasses import dataclass

from ..annotation import CryptoAnnotation


@dataclass(frozen=True)
class CryptoAes(CryptoAnnotation):
    """Transparent AES encryption on a dataclass field.

    The ``value`` attribute is the logical key name, resolved against
    ``com.bld.crypto.aes.keys.<name>``.

    Args:
        value: name of the configured AES key, looked up under
            ``com.bld.crypto.aes.keys.<value>``. Unlike the keystore annotations it is
            not sealed into the token, so no envelope is produced.
        url: add the extra Base64 layer so the token is safe in a URL path segment or
            query parameter.

    Example:
        ``code: Annotated[str, CryptoAes("myAesKey", url=True)] = None``
    """

    def encrypt(self, plain: str) -> str | None:
        from ..config import CryptoConfig

        utils = CryptoConfig.require_aes()
        return utils.encrypt_uri(plain, self.value) if self.url else utils.encrypt_value(plain, self.value)

    def decrypt(self, token: str) -> str | None:
        from ..config import CryptoConfig

        utils = CryptoConfig.require_aes()
        return utils.decrypt_uri(token, self.value) if self.url else utils.decrypt_value(token, self.value)
