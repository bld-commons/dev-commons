"""JKS: RSA with an explicit ``CryptoType`` per direction."""

from __future__ import annotations

import base64

import pytest

from common_encryption import CryptoException, CryptoType

SECRET = "città € 1"


def test_default_roundtrip_encrypt_with_public(jks_utils):
    """The annotation defaults: ``encrypt = publicKey``, ``decrypt = privateKey``."""
    token = jks_utils.encrypt_value(SECRET)
    assert token != SECRET
    assert jks_utils.decrypt_value(token) == SECRET


def test_inverted_roundtrip_encrypt_with_private(jks_utils):
    """``CryptoType.private_key`` on encrypt, ``public_key`` on decrypt."""
    token = jks_utils.encrypt_value(SECRET, CryptoType.private_key)
    assert jks_utils.decrypt_value(token, CryptoType.public_key) == SECRET


def test_mismatched_direction_does_not_recover(jks_utils):
    token = jks_utils.encrypt_value(SECRET)
    with pytest.raises(CryptoException):
        jks_utils.decrypt_value(token, CryptoType.public_key)


def test_unicode_survives(jks_utils):
    assert jks_utils.decrypt_value(jks_utils.encrypt_value(SECRET)) == SECRET
    assert jks_utils.decrypt_uri(jks_utils.encrypt_uri(SECRET)) == SECRET


def test_envelope_roundtrip_and_binding(jks_utils):
    token = jks_utils.encrypt_key_value("idUser", SECRET)
    assert jks_utils.decrypt_key_value("idUser", token) == SECRET
    with pytest.raises(CryptoException):
        jks_utils.decrypt_key_value("other", token)


def test_uri_roundtrip(jks_utils):
    token = jks_utils.encrypt_uri(SECRET)
    assert jks_utils.decrypt_uri(token) == SECRET
    assert base64.b64decode(token).decode().count(".") != 2, "extra Base64 layer is not a PKCS12 token"


def test_object_roundtrip(jks_utils):
    payload = {"id": 1, "tags": ["a", "b"]}
    assert jks_utils.decrypt_object(jks_utils.encrypt_object(payload)) == payload
    assert jks_utils.decrypt_object_uri(jks_utils.encrypt_object_uri(payload)) == payload


def test_blank_input_returns_none(jks_utils):
    assert jks_utils.encrypt_value("") is None
    assert jks_utils.encrypt_value(None) is None
    assert jks_utils.decrypt_value("") is None


def test_public_key_base64(jks_utils):
    der = base64.b64decode(jks_utils.public_key_base64())
    assert der.startswith(b"\x30"), "X.509 SubjectPublicKeyInfo"


def test_jks_rsa_keys_are_not_shared_with_pkcs12(jks_utils, rsa_pkcs12_utils):
    """The RSA key inside the PKCS12 fixture is a different key pair from the JKS one."""
    assert jks_utils.public_key_base64() != rsa_pkcs12_utils.public_key_base64()
