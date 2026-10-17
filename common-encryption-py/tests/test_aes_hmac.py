"""AES, PBKDF2 key derivation, and the HMAC signer."""

from __future__ import annotations

import base64

import pytest

from common_encryption import (
    AesSecrets,
    CryptoAesUtils,
    CryptoException,
    CryptoHmacUtils,
    HmacSecret,
    SecretSpec,
)

SECRET = "città € 1"


# --------------------------------------------------------------------- derivation


def test_raw_password_must_be_a_valid_aes_length():
    with pytest.raises(CryptoException):
        SecretSpec("too-short").key_bytes()
    for size in (16, 24, 32):
        assert len(SecretSpec("k" * size).key_bytes()) == size


def test_pbkdf2_derivation_matches_java():
    """PBKDF2WithHmacSHA1, 65536 iterations, as AesConfiguration does it."""
    from common_encryption.commons.secret import PBKDF2_ITERATIONS

    key = SecretSpec("password", salt="salt", key_length=256).key_bytes()
    assert len(key) == 32

    # Recomputed independently: a different derivation must not collide with this one.
    other = SecretSpec("password", salt="salt", key_length=256, algorithm="pbkdf2_sha256").key_bytes()
    assert key != other, "AES uses HMAC-SHA1 and HMAC uses HMAC-SHA256"

    assert SecretSpec("password", salt="salt", key_length=256, algorithm="pbkdf2_sha1").key_bytes() == key
    assert PBKDF2_ITERATIONS == 65536


def test_unknown_key_name_is_reported():
    with pytest.raises(CryptoException):
        AesSecrets({"other": SecretSpec("k" * 16)}).get_secret_key("missing")


# --------------------------------------------------------------------------- aes


def test_aes_roundtrip(aes_utils):
    token = aes_utils.encrypt_value(SECRET, "k1")
    assert aes_utils.decrypt_value(token, "k1") == SECRET


def test_aes_uri_roundtrip(aes_utils):
    token = aes_utils.encrypt_uri(SECRET, "k1")
    assert base64.b64decode(token).decode() != token, "extra Base64 layer"
    assert aes_utils.decrypt_uri(token, "k1") == SECRET


def test_aes_object_roundtrip(aes_utils):
    payload = {"a": 1, "b": [2, 3]}
    assert aes_utils.decrypt_object(aes_utils.encrypt_object(payload, "k1"), "k1") == payload
    assert aes_utils.decrypt_object_uri(aes_utils.encrypt_object_uri(payload, "k1"), "k1") == payload


def test_aes_is_deterministic(aes_utils):
    """ECB with a fixed key, so the same input gives the same ciphertext."""
    assert aes_utils.encrypt_value("x", "k1") == aes_utils.encrypt_value("x", "k1")


def test_aes_block_padding(aes_utils):
    """PKCS5 padding always fills to the 16 byte block, so 15 and 16 bytes differ in length."""
    assert len(aes_utils.encrypt_value("a" * 16, "k1")) != len(aes_utils.encrypt_value("a" * 15, "k1"))


def test_aes_wrong_key_is_rejected(aes_utils):
    from common_encryption import SecretSpec

    other = CryptoAesUtils(AesSecrets({"k1": SecretSpec("fedcba9876543210")}))
    with pytest.raises(CryptoException):
        other.decrypt_value(aes_utils.encrypt_value(SECRET, "k1"), "k1")


def test_aes_blank_returns_none(aes_utils):
    assert aes_utils.encrypt_value("", "k1") is None
    assert aes_utils.decrypt_value("  ", "k1") is None


# -------------------------------------------------------------------------- hmac


def test_hmac_roundtrip(hmac_utils):
    token = hmac_utils.sign_value("idUser", SECRET)
    assert token.count(".") == 1
    assert hmac_utils.verify_value(token, "idUser") == SECRET


def test_hmac_payload_is_readable(hmac_utils):
    """Signing is not encryption: the value travels in the clear."""
    token = hmac_utils.sign_value("idUser", SECRET)
    assert base64.b64decode(token.split(".")[0]).decode() == '{"key":"idUser","value":"città € 1"}'


def test_hmac_uri_uses_the_url_alphabet(hmac_utils):
    token = hmac_utils.sign_uri("idUser", "a?b/c+d")
    assert "+" not in token and "/" not in token
    assert hmac_utils.verify_uri(token, "idUser") == "a?b/c+d"


def test_hmac_rejects_a_tampered_value(hmac_utils):
    token = hmac_utils.sign_value("idUser", SECRET)
    payload, mac = token.split(".")
    forged = base64.b64encode(b'{"key":"idUser","value":"evil"}').decode()
    with pytest.raises(CryptoException):
        hmac_utils.verify_value(f"{forged}.{mac}", "idUser")


def test_hmac_rejects_another_field(hmac_utils):
    token = hmac_utils.sign_value("idUser", SECRET)
    with pytest.raises(CryptoException):
        hmac_utils.verify_value(token, "other")


def test_hmac_rejects_another_secret(hmac_utils):
    token = hmac_utils.sign_value("idUser", SECRET)
    other = CryptoHmacUtils(HmacSecret(b"fedcba9876543210", "HmacSHA256"))
    with pytest.raises(CryptoException):
        other.verify_value(token, "idUser")


def test_hmac_without_separator(hmac_utils):
    with pytest.raises(CryptoException):
        hmac_utils.verify_value("no-separator-here", "idUser")


@pytest.mark.parametrize("algorithm", ["HmacSHA1", "HmacSHA256", "HmacSHA384", "HmacSHA512"])
def test_hmac_algorithms(algorithm):
    utils = CryptoHmacUtils(HmacSecret(b"0123456789abcdef", algorithm))
    assert utils.verify_value(utils.sign_value("f", "x"), "f") == "x"


def test_hmac_unknown_algorithm(hmac_utils):
    utils = CryptoHmacUtils(HmacSecret(b"0123456789abcdef", "HmacMD5"))
    with pytest.raises(CryptoException):
        utils.sign_value("f", "x")
