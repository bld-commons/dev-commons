"""Java compatibility: every payload below was encrypted by the Java reference module."""

from __future__ import annotations

import base64

from common_encryption import CryptoType


def test_pkcs12_values(java_vectors, pkcs12_utils):
    assert pkcs12_utils.decrypt_value(java_vectors["pkcs12.value"]) == "ciao mondo"
    assert pkcs12_utils.decrypt_value(java_vectors["pkcs12.value.unicode"]) == "città € 1"


def test_pkcs12_envelope_and_uri(java_vectors, pkcs12_utils):
    assert (
        pkcs12_utils.decrypt_key_value("email", java_vectors["pkcs12.keyValue"])
        == "francesco@baldi.it"
    )
    assert pkcs12_utils.decrypt_uri(java_vectors["pkcs12.uri"]) == "uri-payload"


def test_pkcs12_ephemeral_public_key_matches_java(java_vectors, pkcs12_utils):
    """The X25519 public key derived for a Java payload must be byte identical."""
    assert pkcs12_utils.jks_key.public_key_base64() == java_vectors["pkcs12.publicKey.der.b64"]


def test_jks_public_key_direction(java_vectors, jks_utils):
    assert jks_utils.decrypt_value(java_vectors["jks.value.publicKey"]) == "segreto"


def test_jks_private_key_direction(java_vectors, jks_utils):
    """Java encrypts with the private key here, so the public key is what reverses it."""
    assert (
        jks_utils.decrypt_value(java_vectors["jks.value.privateKey"], CryptoType.public_key)
        == "segreto"
    )


def test_jks_recovered_private_key_is_javas(java_vectors, jks_store):
    """The PKCS#8 key recovered from the JKS matches what Java exported."""
    recovered, _chain = jks_store.key_entry("mase-rsa", "jkspass")
    assert base64.b64encode(recovered).decode() == java_vectors["jks.privateKey.pkcs8.b64"]


def test_jks_envelope_and_uri(java_vectors, jks_utils):
    assert jks_utils.decrypt_key_value("idUser", java_vectors["jks.keyValue"]) == "42"
    assert jks_utils.decrypt_uri(java_vectors["jks.uri"]) == "uri-payload"


def test_aes_vectors(java_vectors, aes_utils):
    """AES/ECB/PKCS5Padding, the JCA default for ``Cipher.getInstance("AES")``."""
    assert aes_utils.decrypt_value(java_vectors["aes.value"], "k1") == "citta euro 1"
    assert aes_utils.decrypt_uri(java_vectors["aes.uri"], "k1") == "uri-payload"


def test_hmac_vectors(java_vectors, hmac_utils):
    assert hmac_utils.verify_value(java_vectors["hmac.value"], "idUser") == "citta euro 1"
    assert hmac_utils.verify_uri(java_vectors["hmac.uri"], "idUser") == "uri-payload"


def test_signature_vector(java_vectors, signature_utils):
    assert signature_utils.verify(java_vectors["signature.value"], "payload") is True
    assert signature_utils.verify(java_vectors["signature.value"], "tampered") is False
