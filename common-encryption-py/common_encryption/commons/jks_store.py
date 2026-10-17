"""Pure Python reader for the JKS keystore format (``KeyStore.getInstance("JKS")``).

There is no JKS support in the ``cryptography`` library, which only handles PKCS#8,
PKCS#12 and DER blobs. JKS is a JDK proprietary container, so the format is parsed here.

Layout (big endian throughout, see ``sun.security.provider.JavaKeyStore``)::

    magic     u32   0xFEEDFEED
    version   u32   1 or 2
    count     u32
    entries   count x {
        tag        u32   1 = private key entry, 2 = trusted certificate entry
        alias      UTF   u16 length prefix + bytes (always lower case in JKS)
        timestamp  u64   milliseconds since epoch
        # tag 1 only
        key_len    u32
        key        key_len bytes  (DER EncryptedPrivateKeyInfo, see _recover_private_key)
        chain_len  u32
        chain      chain_len x { type UTF, len u32, der }
    }
    digest    20 bytes  SHA-1( UTF-16BE(password) || "Mighty Aphrodite" || everything above )

Private key protection is the Sun ``KeyProtector`` XOR keystream scheme: the stored
``encryptedData`` is ``salt(20) || ciphertext || SHA-1(password || plaintext_key)(20)``
and the keystream is the iterated digest ``d(n+1) = SHA-1(password || d(n))`` seeded with
the salt.
"""

from __future__ import annotations

import hashlib
import struct
from dataclasses import dataclass, field
from typing import Iterator

from ..exception import CryptoException

MAGIC = 0xFEEDFEED
_SALT_LEN = 20
_DIGEST_LEN = 20
_TAG_PRIVATE_KEY = 1
_TAG_CERTIFICATE = 2
_KEY_PROTECTOR_OID_DER = bytes.fromhex("060a2b060104012a02110101")  # 1.3.6.1.4.1.42.2.17.1.1


@dataclass(frozen=True)
class _Entry:
    alias: str
    tag: int
    key: bytes | None = None
    chain: list[bytes] = field(default_factory=list)


@dataclass(frozen=True)
class JksStore:
    """In-memory view of a JKS file."""

    entries: list[_Entry]

    def alias_of(self, alias: str) -> str:
        """Resolve an alias the way ``JavaKeyStore.JKS`` does: case insensitively.

        The JKS engine lower cases every alias on write and on lookup, so an alias
        stored as ``MaseRSA`` is reachable as ``masersa`` but not as ``MaseRSA``.
        """
        wanted = alias.lower()
        for entry in self.entries:
            if entry.alias.lower() == wanted:
                return entry.alias
        raise CryptoException(f"Alias \"{alias}\" does not exist in the keystore")

    def _find(self, alias: str) -> _Entry:
        resolved = self.alias_of(alias)
        for entry in self.entries:
            if entry.alias == resolved:
                return entry
        raise CryptoException(f"Alias \"{alias}\" does not exist in the keystore")

    def key_entry(self, alias: str, password: str) -> tuple[bytes, list[bytes]]:
        """Return ``(pkcs8_private_key_der, certificate_chain_der)`` for a key entry."""
        entry = self._find(alias)
        if entry.tag != _TAG_PRIVATE_KEY:
            raise CryptoException(f"Alias \"{alias}\" is not a private key entry")
        return _recover_private_key(entry.key or b"", password), list(entry.chain)

    def certificate_der(self, alias: str) -> bytes:
        """Return the DER certificate of an alias, for deriving its public key."""
        entry = self._find(alias)
        if not entry.chain:
            raise CryptoException(f"Alias \"{alias}\" has no certificate")
        return entry.chain[0]


class _Reader:
    """Minimal big-endian cursor over the keystore bytes."""

    def __init__(self, data: bytes) -> None:
        self.data = data
        self.pos = 0

    def take(self, count: int) -> bytes:
        chunk = self.data[self.pos : self.pos + count]
        if len(chunk) != count:
            raise CryptoException("Truncated JKS keystore")
        self.pos += count
        return chunk

    def u32(self) -> int:
        return struct.unpack(">I", self.take(4))[0]

    def u64(self) -> int:
        return struct.unpack(">Q", self.take(8))[0]

    def utf(self) -> str:
        return self.take(self.u16()).decode("utf-8")

    def u16(self) -> int:
        return struct.unpack(">H", self.take(2))[0]


def load_jks_store(data: bytes, password: str | None = None) -> JksStore:
    """Parse JKS ``data``, optionally verifying the store integrity digest."""
    reader = _Reader(data)
    if reader.u32() != MAGIC:
        raise CryptoException("Not a JKS keystore (bad magic): expected 0xFEEDFEED")
    version = reader.u32()
    if version not in (1, 2):
        raise CryptoException(f"Unsupported JKS version {version}")

    entries: list[_Entry] = []
    for _ in range(reader.u32()):
        tag = reader.u32()
        alias = reader.utf()
        reader.u64()  # creation timestamp, unused
        if tag == _TAG_PRIVATE_KEY:
            key = reader.take(reader.u32())
            chain = [_read_cert(reader) for _ in range(reader.u32())]
            entries.append(_Entry(alias=alias, tag=tag, key=key, chain=chain))
        elif tag == _TAG_CERTIFICATE:
            entries.append(_Entry(alias=alias, tag=tag, chain=[_read_cert(reader)]))
        else:
            raise CryptoException(f"Unsupported JKS entry tag {tag}")

    body_end = reader.pos
    digest = reader.take(_DIGEST_LEN)
    if password is not None and digest != _store_digest(password, data[:body_end]):
        raise CryptoException("Keystore was tampered with, or password was incorrect")
    return JksStore(entries=entries)


def aliases(data: bytes) -> Iterator[str]:
    """Iterate over the aliases contained in JKS ``data``."""
    for entry in load_jks_store(data).entries:
        yield entry.alias


def _read_cert(reader: _Reader) -> bytes:
    reader.utf()  # certificate type, always "X.509"
    return reader.take(reader.u32())


def _password_bytes(password: str) -> bytes:
    """UTF-16BE encoding, matching ``JavaKeyStore.convertToBytes``."""
    return password.encode("utf-16-be")


def _store_digest(password: str, body: bytes) -> bytes:
    digest = hashlib.sha1()
    digest.update(_password_bytes(password))
    digest.update(b"Mighty Aphrodite")
    digest.update(body)
    return digest.digest()


def _keystream(password: bytes, salt: bytes, length: int) -> bytes:
    """Iterated ``SHA-1(password || previous)`` keystream, truncated to ``length``."""
    out = bytearray()
    previous = salt
    while len(out) < length:
        digest = hashlib.sha1(password + previous).digest()
        out += digest
        previous = digest
    return bytes(out[:length])


def _read_der_tlv(data: bytes, pos: int) -> tuple[int, bytes, int]:
    """Read one DER tag/length/value triple, returning ``(tag, value, next_pos)``."""
    tag = data[pos]
    length = data[pos + 1]
    pos += 2
    if length & 0x80:
        count = length & 0x7F
        length = int.from_bytes(data[pos : pos + count], "big")
        pos += count
    return tag, data[pos : pos + length], pos + length


def _recover_private_key(encrypted_private_key_info: bytes, password: str) -> bytes:
    """Undo ``sun.security.provider.KeyProtector`` protection, returning PKCS#8 DER."""
    tag, algorithm, pos = _read_der_tlv(encrypted_private_key_info, 0)
    if tag != 0x30:
        raise CryptoException("Private key not stored as PKCS#8 EncryptedPrivateKeyInfo")
    algorithm_tag, algorithm_value, pos = _read_der_tlv(algorithm, 0)
    if algorithm_tag != 0x30 or _KEY_PROTECTOR_OID_DER not in algorithm_value:
        raise CryptoException("Unsupported JKS key protection algorithm")

    payload_tag, payload, _ = _read_der_tlv(algorithm, pos)
    if payload_tag != 0x04:
        raise CryptoException("Private key not stored as PKCS#8 EncryptedPrivateKeyInfo")

    password_bytes = _password_bytes(password)
    salt = payload[:_SALT_LEN]
    ciphertext = payload[_SALT_LEN : len(payload) - _DIGEST_LEN]
    expected = payload[len(payload) - _DIGEST_LEN :]

    plain = bytes(a ^ b for a, b in zip(ciphertext, _keystream(password_bytes, salt, len(ciphertext))))
    digest = hashlib.sha1(password_bytes + plain).digest()
    if digest != expected:
        raise CryptoException("Cannot recover key: keystore password is incorrect")

    return plain
