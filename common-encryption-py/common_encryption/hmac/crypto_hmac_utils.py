"""Field signing and verification. Port of ``com.bld.crypto.hmac.CryptoHmacUtils``.

Not encryption: the payload travels in the clear and only its integrity and origin are
proven. A token is ``Base64(envelope).Base64(mac)``, where the envelope is the usual
``{"key":...,"value":...}`` document and the MAC covers its exact bytes.
"""

from __future__ import annotations

import base64
import hmac
import json
from hashlib import sha1, sha256, sha384, sha512

from ..commons.key_utility import build_envelope
from ..commons.secret import HmacSecret
from ..exception import CryptoException

SEPARATOR = "."
UTF8 = "utf-8"

_DIGESTS = {"sha1": sha1, "sha256": sha256, "sha384": sha384, "sha512": sha512}


class CryptoHmacUtils:
    """HMAC signing of ``{key, value}`` envelopes. Port of ``CryptoHmacUtils``."""

    def __init__(self, crypto_hmac_secret: HmacSecret) -> None:
        self.crypto_hmac_secret = crypto_hmac_secret

    # -------------------------------------------------------------- signing

    def sign_value(self, field_key: str, value: str) -> str:
        """Sign ``value`` for ``field_key``: ``Base64(envelope).Base64(mac)``."""
        return self._sign(field_key, value, base64.b64encode, base64.b64encode)

    def sign_uri(self, field_key: str, value: str) -> str:
        """URL-safe variant, using the URL-safe Base64 alphabet for both halves."""
        return self._sign(field_key, value, base64.urlsafe_b64encode, base64.urlsafe_b64encode)

    # ---------------------------------------------------------- verification

    def verify_value(self, token: str, field_key: str) -> str:
        """Verify a token from :meth:`sign_value` and return the original value."""
        return self._verify(token, field_key, base64.b64decode)

    def verify_uri(self, token: str, field_key: str) -> str:
        """Verify a token from :meth:`sign_uri`."""
        return self._verify(token, field_key, base64.urlsafe_b64decode)

    # --------------------------------------------------------------- private

    def _sign(self, field_key: str, value: str, envelope_encoder, mac_encoder) -> str:
        envelope = build_envelope(field_key, value).encode(UTF8)
        return envelope_encoder(envelope).decode() + SEPARATOR + mac_encoder(
            self._mac(envelope)
        ).decode()

    def _verify(self, token: str, field_key: str, decoder) -> str:
        dot = token.rfind(SEPARATOR)
        if dot < 0:
            raise CryptoException(f'Invalid HMAC token format for field "{field_key}"')
        try:
            envelope = decoder(token[:dot])
            received = decoder(token[dot + 1 :])
        except Exception as error:
            raise CryptoException(error) from error

        # Constant time, as MessageDigest.isEqual in the Java original.
        if not hmac.compare_digest(self._mac(envelope), received):
            raise CryptoException(f'HMAC verification failed for field "{field_key}"')

        try:
            parsed = json.loads(envelope.decode(UTF8))
        except Exception as error:
            raise CryptoException(error) from error
        key = parsed.get("key")
        if field_key != key:
            raise CryptoException(
                f'The "{field_key}" value does not match the original "{key}" value'
            )
        return parsed.get("value")

    def _mac(self, data: bytes) -> bytes:
        name = self.crypto_hmac_secret.algorithm.lower().replace("hmac", "").strip("-")
        digest = _DIGESTS.get(name)
        if digest is None:
            raise CryptoException(f'Unsupported HMAC algorithm "{self.crypto_hmac_secret.algorithm}"')
        return hmac.new(self.crypto_hmac_secret.secret_key, data, digest).digest()


__all__ = ["CryptoHmacUtils"]
