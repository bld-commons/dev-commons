"""Transparent field encryption: the Python stand-in for the Jackson annotations."""

from __future__ import annotations

import json
from dataclasses import dataclass, field
from typing import Annotated

import pytest

from common_encryption import CryptoConfig, CryptoException, CryptoJks, CryptoPkcs12, from_json, to_json

from conftest import FIXTURES, JKS_PASSWORD, PKCS12_PASSWORD, RSA_ALIAS, X25519_ALIAS

JKS = {
    "file": str(FIXTURES / "mase.jks"),
    "password": JKS_PASSWORD,
    "alias": RSA_ALIAS,
    "instance-jks": "JKS",
}
PKCS12 = {
    "file": str(FIXTURES / "x25519.p12"),
    "password": PKCS12_PASSWORD,
    "alias": X25519_ALIAS,
    "instance-jks": "PKCS12",
}


@pytest.fixture(autouse=True)
def installed():
    CryptoConfig.from_properties({"com.bld": {"crypto": {"jks": JKS, "pkcs12": PKCS12}}})
    yield
    CryptoConfig.clear()


@dataclass
class Filter:
    id_user: Annotated[int | None, CryptoPkcs12("idUser", url=True)] = None
    roles: Annotated[list[int] | None, CryptoPkcs12("roles")] = None
    name: Annotated[str | None, CryptoJks("name")] = None
    active: Annotated[bool | None, CryptoPkcs12("active")] = None
    plain: str | None = None
    nested: "Filter | None" = None
    tags: Annotated[list[str], CryptoPkcs12("tags")] = field(default_factory=list)


def test_roundtrip_keeps_types():
    original = Filter(1, [10, 20], "ann", True, "clear", None, ["a", "b"])
    restored = from_json(Filter, to_json(original))
    assert restored == original


def test_encrypted_fields_are_opaque():
    original = Filter(4242, [10], "annamaria", True, "clear")
    body = to_json(original)
    fields = json.loads(body)

    assert fields["plain"] == "clear", "unannotated fields stay readable"
    assert "annamaria" not in body, "JKS plaintext must not appear anywhere"
    assert "4242" not in body
    assert fields["active"] != "True"

    # each list element is a token of its own
    assert isinstance(fields["roles"], list) and len(fields["roles"]) == 1
    assert fields["roles"][0].count(".") == 2


def test_tokens_differ_per_call():
    """A fresh ephemeral key per operation, so two equal fields produce different tokens."""
    one = to_json(Filter(name="same"))
    two = to_json(Filter(name="same"))
    assert one != two


def test_none_fields_stay_null():
    body = to_json(Filter())
    assert from_json(Filter, body) == Filter()
    assert '"id_user":null' in body


def test_empty_list_roundtrip():
    original = Filter(tags=[])
    assert from_json(Filter, to_json(original)).tags == []


def test_token_cannot_move_between_fields():
    """A token issued for one key identifier is refused by another."""

    @dataclass
    class FieldA:
        v: Annotated[str, CryptoPkcs12("a")] = ""

    @dataclass
    class FieldB:
        v: Annotated[str, CryptoPkcs12("b")] = ""

    stolen = json.loads(to_json(FieldA("secret")))["v"]
    body = json.dumps({"v": stolen})
    with pytest.raises(CryptoException):
        from_json(FieldB, body)


def test_blank_annotated_field_still_produces_a_token():
    """Java builds the {key, value} envelope before encrypting, so "" is not skipped.

    Reading it back yields None, because a blank word has no value to coerce.
    """

    @dataclass
    class Two:
        a: Annotated[str, CryptoPkcs12("a")] = ""

    body = to_json(Two(a=""))
    assert json.loads(body)["a"] is not None
    assert from_json(Two, body).a is None


def test_json_is_compact_and_unicode_friendly():
    @dataclass
    class One:
        v: Annotated[str, CryptoPkcs12("v")] = ""

    body = to_json(One("città"))
    assert ", " not in body and '": ' not in body
    assert from_json(One, body).v == "città"


def test_unconfigured_utility_is_reported():
    CryptoConfig.clear()

    @dataclass
    class One:
        v: Annotated[str, CryptoPkcs12("v")] = ""

    with pytest.raises(CryptoException):
        to_json(One("x"))


@pytest.mark.parametrize(
    "properties",
    [
        pytest.param(
            {"com.bld": {"crypto": {"jks": JKS}}}, id="yaml-dotted-key"
        ),
        pytest.param(
            {"com": {"bld": {"crypto": {"jks": JKS}}}}, id="fully-nested"
        ),
        pytest.param(
            {"com.bld": {"crypto.jks": JKS}}, id="mixed-dots"
        ),
        pytest.param(
            {f"com.bld.crypto.jks.{k.replace('-', '.').replace('instance.jks', 'instance-jks')}": v
             for k, v in JKS.items()},
            id="flat-properties",
        ),
    ],
)
def test_config_accepts_every_spring_shape(properties):
    CryptoConfig.clear()
    config = CryptoConfig.from_properties(properties)
    assert config.jks is not None
    assert config.jks.decrypt_value(config.jks.encrypt_value("probe")) == "probe"


def test_config_from_env():
    CryptoConfig.clear()
    config = CryptoConfig.from_env(
        {
            "BLD_CRYPTO_JKS_FILE": str(FIXTURES / "mase.jks"),
            "BLD_CRYPTO_JKS_PASSWORD": JKS_PASSWORD,
            "BLD_CRYPTO_JKS_ALIAS": RSA_ALIAS,
            "BLD_CRYPTO_JKS_INSTANCE_JKS": "JKS",
        }
    )
    assert config.jks is not None


def test_config_without_file_is_skipped():
    """Mirrors ``@ConditionalOnProperty``: a section with no file is simply absent."""
    CryptoConfig.clear()
    config = CryptoConfig.from_properties({"com.bld": {"crypto": {"jks": {"password": "x"}}}})
    assert config.jks is None
