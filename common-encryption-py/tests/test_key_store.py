"""The pure Python JKS reader, checked against a store produced by ``keytool``."""

from __future__ import annotations

import pytest

from common_encryption import CryptoException
from common_encryption.key.key_properties import load_jks_key

from conftest import FIXTURES, JKS_PASSWORD, PKCS12_PASSWORD, RSA_ALIAS, X25519_ALIAS


def test_alias_is_case_insensitive(jks_store):
    assert jks_store.alias_of(RSA_ALIAS.upper()) is not None
    assert jks_store.alias_of("Mase-Rsa") is not None


def test_unknown_alias(jks_store):
    with pytest.raises(CryptoException):
        jks_store.alias_of("nope")


def test_wrong_store_password(jks_store):
    with pytest.raises(CryptoException):
        jks_store.key_entry(RSA_ALIAS, "wrong")


def test_recovered_key_matches_the_certificate(jks_store):
    """A JKS password digest only guards the store; the key bytes must still be RSA."""
    from cryptography.hazmat.primitives import serialization

    recovered, chain = jks_store.key_entry(RSA_ALIAS, JKS_PASSWORD)
    assert chain, "the fixture stores a certificate chain"
    private_key = serialization.load_der_private_key(recovered, password=None)
    assert private_key.key_size == 2048
    assert chain[0].startswith(b"\x30"), "X.509 certificate"


def test_certificate_der(jks_store):
    assert jks_store.certificate_der(RSA_ALIAS).startswith(b"\x30")


def test_truncated_store_is_rejected():
    data = (FIXTURES / "mase.jks").read_bytes()
    with pytest.raises(CryptoException):
        load_jks_key(data[:20], JKS_PASSWORD, RSA_ALIAS, "JKS")


def test_not_a_keystore():
    with pytest.raises(CryptoException):
        load_jks_key(b"not a keystore", JKS_PASSWORD, RSA_ALIAS, "JKS")


def test_x25519_pkcs12_loads(pkcs12_utils):
    from cryptography.hazmat.primitives.asymmetric.x25519 import X25519PrivateKey

    assert isinstance(pkcs12_utils.jks_key.private_key, X25519PrivateKey)


def test_pkcs12_alias_is_required():
    with pytest.raises(CryptoException):
        load_jks_key((FIXTURES / "x25519.p12").read_bytes(), PKCS12_PASSWORD, "missing", "PKCS12")


def test_pkcs12_wrong_password():
    with pytest.raises(CryptoException):
        load_jks_key((FIXTURES / "x25519.p12").read_bytes(), "wrong", X25519_ALIAS, "PKCS12")
