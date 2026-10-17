"""Public key encryption and detached signatures."""

from __future__ import annotations

import base64

import pytest

from common_encryption import (
    CryptoException,
    CryptoJksUtils,
    CryptoMapPublicKeyUtils,
    CryptoPublicKeyUtils,
    SignatureKeyUtils,
)
from common_encryption.type import CryptoType
from common_encryption.signature.signature_key_utils import sign, verify

SECRET = "città € 1"


@pytest.fixture(scope="module")
def pair(jks_utils):
    return CryptoPublicKeyUtils(jks_utils.jks_key)


@pytest.fixture(scope="module")
def sender(pair):
    """Public key only, the position a sender is in."""
    map_utils = CryptoMapPublicKeyUtils()
    map_utils.add_public_key("mase", pair.jks_key.public_key)
    return map_utils


# ---------------------------------------------------------------------- public key


def test_pair_roundtrip(pair):
    assert pair.decrypt_value(pair.encrypt_value(SECRET)) == SECRET
    assert pair.decrypt_uri(pair.encrypt_uri(SECRET)) == SECRET
    assert pair.decrypt_object(pair.encrypt_object({"a": [1, 2]})) == {"a": [1, 2]}
    assert pair.decrypt_object_uri(pair.encrypt_object_uri({"a": 1})) == {"a": 1}


def test_sender_holds_only_the_public_key(pair, sender):
    token = sender.encrypt_value(SECRET, "mase")
    assert pair.decrypt_value(token) == SECRET


def test_public_key_direction_is_checkable_everywhere(pair, sender):
    """A token made with the private key is recovered with the public key alone.

    This is the ``@DecryptPubKey`` case: only the key owner can produce the token, so
    anyone holding the public key can confirm it came from the owner.
    """
    from common_encryption import CryptoJksUtils

    token = CryptoJksUtils(pair.jks_key).encrypt_value(SECRET, CryptoType.private_key)
    assert sender.decrypt_value(token, "mase") == SECRET


def test_unknown_public_key_name(sender):
    with pytest.raises(CryptoException):
        sender.encrypt_value(SECRET, "missing")


def test_public_key_blank_is_rejected():
    utils = CryptoMapPublicKeyUtils()
    with pytest.raises(CryptoException):
        utils.add_public_key_der("empty", "")


# ---------------------------------------------------------------------- signature


def test_signature_roundtrip(signature_utils):
    signed = signature_utils.sign("payload")
    assert signature_utils.verify(signed, "payload") is True


def test_signature_rejects_other_text(signature_utils):
    signed = signature_utils.sign("payload")
    assert signature_utils.verify(signed, "other") is False


def test_signature_rejects_other_payload(signature_utils):
    signed = signature_utils.sign("payload")
    forged = base64.b64encode(b"\x00" * 256).decode()
    assert signature_utils.verify(forged, "payload") is False


def test_signature_is_deterministic_for_pkcs1(signature_utils):
    """RSASSA-PKCS1-v1_5 is deterministic, unlike PSS."""
    assert signature_utils.sign("payload") == signature_utils.sign("payload")


def test_signature_public_key_is_exported(signature_utils):
    """Java's ``publicKey()`` returns the encoded key; ``publicKeyBase64`` the Base64 form."""
    assert base64.b64decode(signature_utils.public_key()).startswith(b"\x30")
    assert signature_utils.get_public_key() is signature_utils.jks_key.public_key


def test_static_helpers_match_the_instance(signature_utils):
    key = signature_utils.jks_key
    assert sign("payload", key.private_key, "SHA256withRSA") == signature_utils.sign("payload")
    assert verify(key.public_key, signature_utils.sign("payload"), "payload", "SHA256withRSA")


def test_unsupported_signature_instance(signature_utils):
    with pytest.raises(CryptoException):
        SignatureKeyUtils(signature_utils.jks_key, "MD5withRSA").sign("x")
    with pytest.raises(CryptoException):
        SignatureKeyUtils(signature_utils.jks_key, "SHA256").sign("x")
