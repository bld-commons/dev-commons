"""Secret key derivation shared by the AES and HMAC modules.

Port of the ``PBEKeySpec`` blocks in ``AesConfiguration`` and ``HmacConfiguration``:
PBKDF2 when a salt and a key length are configured, otherwise the raw secret bytes.
"""

from __future__ import annotations

from dataclasses import dataclass

from ..exception import CryptoException

AES_SIZES = (16, 24, 32)
PBKDF2_ITERATIONS = 65536


@dataclass(frozen=True)
class SecretSpec:
    """One named secret, as configured under ``com.bld.crypto.aes.keys.<name>``.

    Args:
        password: the raw secret. With ``salt`` and ``key_length`` set it becomes a
            PBKDF2 input, otherwise it must already be a valid AES key length.
        salt: PBKDF2 salt, UTF-8 encoded.
        key_length: PBKDF2 output length in bits.
        algorithm: PBKDF2 variant. Java uses HMAC-SHA1 for AES keys and HMAC-SHA256 for
            HMAC keys, which matters because the derived bytes differ.
    """

    password: str
    salt: str | None = None
    key_length: int | None = None
    algorithm: str = "pbkdf2_sha1"

    def __post_init__(self) -> None:
        """Accept the strings a ``.properties`` file yields, as Spring binding does."""
        if self.key_length is None:
            return
        try:
            length = int(self.key_length)
        except (TypeError, ValueError):
            raise CryptoException(f'Invalid key length "{self.key_length}"') from None
        if length <= 0 or length % 8:
            raise CryptoException(
                f'The key length must be a positive multiple of 8 bits, got {length}'
            )
        object.__setattr__(self, "key_length", length)

    def material(self) -> bytes:
        """Derive the key, or return the raw secret when no PBKDF2 input is configured.

        No length check: an HMAC key may be any size, only AES is restricted.
        """
        if self.salt is not None and self.key_length is not None:
            return derive(self.password, self.salt, self.key_length, self.algorithm)
        return self.password.encode("utf-8")

    def key_bytes(self) -> bytes:
        """Derive the key material, matching the Java configuration class."""
        raw = self.material()
        if len(raw) not in AES_SIZES:
            raise CryptoException(
                "TThe password must be 16, 24, or 32 bytes long"
            )
        return raw


class AesSecrets:
    """Holder for the named AES keys. Port of ``CipherAesSecret``."""

    def __init__(self, keys: dict[str, SecretSpec] | None = None) -> None:
        self._keys = dict(keys or {})

    def add(self, name: str, spec: SecretSpec) -> None:
        self._keys[name] = spec

    def get_secret_key(self, name: str) -> bytes:
        try:
            return self._keys[name].key_bytes()
        except KeyError:
            raise CryptoException(f'No AES key configured under the name "{name}"') from None

    def __bool__(self) -> bool:
        return bool(self._keys)


class HmacSecret:
    """Holder for the HMAC key and algorithm. Port of ``CryptoHmacSecret``."""

    def __init__(self, key: bytes, algorithm: str = "sha256") -> None:
        self._key = key
        self._algorithm = algorithm

    @property
    def secret_key(self) -> bytes:
        return self._key

    @property
    def algorithm(self) -> str:
        """JCA style algorithm name, ``HmacSHA256`` and friends."""
        return self._algorithm


def derive(password: str, salt: str, key_length: int, algorithm: str = "pbkdf2_sha1") -> bytes:
    """PBKDF2 key derivation, byte compatible with ``PBEKeySpec``."""
    from cryptography.hazmat.primitives import hashes
    from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC

    names = {"pbkdf2_sha1": hashes.SHA1(), "pbkdf2_sha256": hashes.SHA256()}
    if algorithm not in names:
        raise CryptoException(f'Unsupported key derivation algorithm "{algorithm}"')
    return PBKDF2HMAC(
        algorithm=names[algorithm],
        length=key_length // 8,
        salt=salt.encode("utf-8"),
        iterations=PBKDF2_ITERATIONS,
    ).derive(password.encode("utf-8"))
