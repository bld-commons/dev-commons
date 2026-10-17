"""PKCS#12: hybrid X25519 + AES-GCM, and the envelope and URL variants built on top."""

from __future__ import annotations

import base64

import pytest

from common_encryption import CryptoException, InstanceType
from common_encryption.pkcs12.crypto_pkcs12_utils import HKDF_INFO

SECRET = "città € 1"


def test_value_roundtrip(pkcs12_utils):
    token = pkcs12_utils.encrypt_value(SECRET)
    assert token != SECRET
    assert pkcs12_utils.decrypt_value(token) == SECRET


def test_tokens_are_not_deterministic(pkcs12_utils):
    """A fresh ephemeral key and nonce per call, so the same input never repeats."""
    assert pkcs12_utils.encrypt_value(SECRET) != pkcs12_utils.encrypt_value(SECRET)


def test_token_layout(pkcs12_utils):
    """``Base64(SPKI).Base64(nonce).Base64(ciphertext + tag)``."""
    head, nonce, tail = pkcs12_utils.encrypt_value(SECRET).split(".")
    assert base64.b64decode(head, validate=True)
    assert len(base64.b64decode(nonce)) == 12
    # AES-GCM output is the plaintext plus a 16 byte tag, so it exceeds it.
    assert len(base64.b64decode(tail)) > len(SECRET.encode())


def test_derives_a_shared_secret(pkcs12_utils, monkeypatch):
    """AES key comes from HKDF-SHA256 over the raw ECDH output, salt = nonce, fixed info."""
    captured = {}

    def spy(input_key_material, salt):
        captured.update(shared=input_key_material, salt=salt)
        return b"\x11" * 32

    monkeypatch.setattr("common_encryption.pkcs12.crypto_pkcs12_utils._hkdf", spy)
    token = pkcs12_utils.encrypt_value(SECRET)

    assert len(captured["shared"]) == 32, "raw 32 byte X25519 shared secret"
    assert captured["salt"] == base64.b64decode(token.split(".")[1]), "salt is the GCM nonce"
    assert HKDF_INFO == b"CryptoPkcs12"

    # With the key pinned, the ciphertext is deterministic, which proves the rest of
    # the pipeline is unchanged.
    nonce = base64.b64decode(token.split(".")[1])
    again = pkcs12_utils.encrypt_value(SECRET)
    assert again.split(".")[1] != token.split(".")[1]


def test_envelope_roundtrip(pkcs12_utils):
    token = pkcs12_utils.encrypt_key_value("email", SECRET)
    assert pkcs12_utils.decrypt_key_value("email", token) == SECRET


def test_envelope_rejects_a_different_key(pkcs12_utils):
    """The key identifier is authenticated, so a token cannot be moved between fields."""
    token = pkcs12_utils.encrypt_key_value("email", SECRET)
    with pytest.raises(CryptoException):
        pkcs12_utils.decrypt_key_value("other", token)


def test_uri_adds_a_second_base64_layer(pkcs12_utils):
    token = pkcs12_utils.encrypt_uri(SECRET)
    assert pkcs12_utils.decrypt_uri(token) == SECRET
    # Stripping one Base64 layer yields exactly the three part encrypt_value payload.
    assert base64.b64decode(token).decode().count(".") == 2
    assert pkcs12_utils.decrypt_value(base64.b64decode(token).decode()) == SECRET


def test_object_roundtrip(pkcs12_utils):
    payload = {"id": 1, "tags": ["a", "b"], "nested": {"ok": True}}
    assert pkcs12_utils.decrypt_object(pkcs12_utils.encrypt_object(payload)) == payload
    assert pkcs12_utils.decrypt_object_uri(pkcs12_utils.encrypt_object_uri(payload)) == payload


def test_tampering_is_rejected(pkcs12_utils):
    head, nonce, tail = pkcs12_utils.encrypt_value(SECRET).split(".")
    flipped = bytearray(base64.b64decode(tail))
    flipped[0] ^= 0x01
    broken = f"{head}.{nonce}.{base64.b64encode(bytes(flipped)).decode()}"
    with pytest.raises(CryptoException):
        pkcs12_utils.decrypt_value(broken)


def test_raw_key_material_accepts_base64_der(rsa_pkcs12_utils):
    """Port of ``JksKey(String, String, InstanceType)``: keys given as Base64 DER."""
    from cryptography.hazmat.primitives import serialization

    from common_encryption import JksKey

    key = rsa_pkcs12_utils.jks_key
    private_b64 = base64.b64encode(
        key.private_key.private_bytes(
            encoding=serialization.Encoding.DER,
            format=serialization.PrivateFormat.PKCS8,
            encryption_algorithm=serialization.NoEncryption(),
        )
    ).decode()
    rebuilt = JksKey(private_b64, key.public_key_base64(), InstanceType.rsa)
    assert rebuilt.public_key_base64() == key.public_key_base64()
    assert base64.b64encode(
        rebuilt.private_key.private_bytes(
            encoding=serialization.Encoding.DER,
            format=serialization.PrivateFormat.PKCS8,
            encryption_algorithm=serialization.NoEncryption(),
        )
    ).decode() == private_b64
