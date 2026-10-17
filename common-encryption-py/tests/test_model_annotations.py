"""Transparent ``Annotated`` fields for AES, HMAC, public keys and signatures."""

from __future__ import annotations

import base64
import json
from dataclasses import dataclass
from typing import Annotated

import pytest

from common_encryption import (
    CryptoAes,
    CryptoConfig,
    CryptoException,
    CryptoHmac,
    CryptoPubKey,
    CryptoPubKeyDecrypt,
    CryptoPubKeyEncrypt,
    from_json,
    to_json,
)
from common_encryption.commons.jks_store import load_jks_store
from conftest import FIXTURES, JKS_PASSWORD, RSA_ALIAS


def _certificate_pem() -> str:
    import textwrap

    store = load_jks_store((FIXTURES / "mase.jks").read_bytes(), JKS_PASSWORD)
    _, chain = store.key_entry(RSA_ALIAS, JKS_PASSWORD)
    body = textwrap.fill(base64.b64encode(chain[0]).decode(), 64)
    return f"-----BEGIN CERTIFICATE-----\n{body}\n-----END CERTIFICATE-----\n"


@pytest.fixture(autouse=True)
def installed():
    CryptoConfig.from_properties(
        {
            "com.bld": {
                "crypto": {
                    "aes": {"keys": {"k1": {"password": "0123456789abcdef"}}},
                    "hmac": {"secret": "0123456789abcdef", "algorithm": "HmacSHA256"},
                    "public-key": {"keys": {"mase": _certificate_pem()}},
                    "signature": {
                        "file": str(FIXTURES / "mase.jks"),
                        "password": JKS_PASSWORD,
                        "alias": RSA_ALIAS,
                        "instance-signature": "SHA256withRSA",
                    },                }
            }
        }
    )
    yield
    CryptoConfig.clear()


@dataclass
class Payload:
    secret_aes: Annotated[str | None, CryptoAes("k1")] = None
    aes_uri: Annotated[str | None, CryptoAes("k1", url=True)] = None
    signed: Annotated[str | None, CryptoHmac("idUser")] = None
    signed_uri: Annotated[str | None, CryptoHmac("idUser", url=True)] = None
    plain: str | None = None


def test_roundtrip():
    original = Payload("a", "b", "c", "d", "clear")
    restored = from_json(Payload, to_json(original))
    assert restored == original


def test_only_annotated_fields_are_tokens():
    body = json.loads(to_json(Payload("a", "b", "c", "d", "clear")))
    assert body["plain"] == "clear"
    for name in ("secret_aes", "aes_uri", "signed", "signed_uri"):
        assert body[name] != "a" and body[name]


def test_hmac_token_carries_its_field_name():
    """The envelope is what stops a token moving between fields."""
    body = json.loads(to_json(Payload(signed="value")))
    envelope, _, _ = body["signed"].partition(".")
    assert json.loads(base64.b64decode(envelope)) == {"key": "idUser", "value": "value"}


def test_pubkey_token_is_bare():
    """Public key tokens have no envelope, unlike the keystore and HMAC ones."""

    @dataclass
    class Outbound:
        mailbox: Annotated[str | None, CryptoPubKey("mase")] = None

    body = json.loads(to_json(Outbound("a@b.c")))
    assert "." not in body["mailbox"]
    assert body["mailbox"] != "a@b.c"


def test_outbound_public_key_is_not_readable_by_the_sender():
    """A token made with the public key needs the recipient's private key, so it does not
    come back through ``@CryptoPubKey``: the map utils only hold public keys."""

    @dataclass
    class Outbound:
        mailbox: Annotated[str | None, CryptoPubKey("mase")] = None

    with pytest.raises(CryptoException):
        from_json(Outbound, to_json(Outbound("a@b.c")))


def test_inbound_public_key_is_recovered_from_a_private_key_token():
    """``@DecryptPubKey``: written by the key owner, checked by anyone with the public key."""

    @dataclass
    class Inbound:
        mailbox: Annotated[str | None, CryptoPubKeyDecrypt("mase")] = None

    from common_encryption import CryptoJksUtils, CryptoType, load_jks_key

    key = load_jks_key((FIXTURES / "mase.jks").read_bytes(), JKS_PASSWORD, RSA_ALIAS, "JKS")
    token = CryptoJksUtils(key).encrypt_value("a@b.c", CryptoType.private_key)

    body = json.dumps({"mailbox": token})
    assert from_json(Inbound, body).mailbox == "a@b.c", "the sender wrote it in the clear"


def test_one_directional_public_keys_pass_the_other_way():
    """Java registers only one Jackson hook per annotation, so the other side is a no-op."""

    @dataclass
    class Outbound:
        mailbox: Annotated[str | None, CryptoPubKeyEncrypt("mase")] = None

    @dataclass
    class Inbound:
        mailbox: Annotated[str | None, CryptoPubKeyDecrypt("mase")] = None

    body = to_json(Outbound("a@b.c"))
    token = json.loads(body)["mailbox"]
    assert token != "a@b.c"
    assert from_json(Outbound, body).mailbox == token, (
        "an outbound field is never decrypted on the way in"
    )
    assert json.loads(to_json(Inbound("a@b.c")))["mailbox"] == "a@b.c", (
        "an inbound field is never encrypted"
    )


def test_unknown_aes_key_is_reported():
    @dataclass
    class Bad:
        value: Annotated[str | None, CryptoAes("missing")] = None

    with pytest.raises(CryptoException):
        to_json(Bad("x"))


def test_unknown_public_key_is_reported():
    @dataclass
    class Bad:
        value: Annotated[str | None, CryptoPubKey("missing")] = None

    with pytest.raises(CryptoException):
        to_json(Bad("x"))


def test_signature_has_no_field_annotation():
    """Java ships none either: a detached signature cannot be checked on the way back in.

    The token replaces the value, so the text to verify against is gone. Signing a field
    would need a ``signature + "." + value`` token Java cannot read, so ``SignatureKeyUtils``
    stays an explicit call, exactly as in Java.
    """
    import common_encryption

    assert not hasattr(common_encryption, "CryptoSignature")
    assert not hasattr(common_encryption.CryptoAnnotation, "CryptoSignature")
