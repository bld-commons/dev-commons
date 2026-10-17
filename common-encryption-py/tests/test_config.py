"""Configuration binding for the AES, HMAC, public-key and signature sections."""

from __future__ import annotations

import base64

import pytest

from common_encryption import CryptoException, SecretSpec
from common_encryption.config import CryptoConfig
from conftest import FIXTURES, JKS_PASSWORD, RSA_ALIAS


@pytest.fixture(autouse=True)
def _clean_registry():
    yield
    CryptoConfig.clear()


# ------------------------------------------------------------------- field names


@pytest.mark.parametrize(
    "field,expected",
    [("keyLength", "key_length"), ("key-length", "key_length"),
     ("instanceSignature", "instance_signature"), ("instance-jks", "instance_jks")],
)
def test_property_names_are_relaxed(field, expected):
    """Spring accepts camelCase, kebab-case and snake_case for the same property."""
    from common_encryption.config import _normalise

    assert _normalise(field) == expected


def test_flat_camelcase_properties():
    config = CryptoConfig.from_properties(
        {
            "com.bld.crypto.hmac.secret": "hmac-secret",
            "com.bld.crypto.hmac.algorithm": "HmacSHA512",
            "com.bld.crypto.hmac.keyLength": "256",
            "com.bld.crypto.hmac.salt": "salty",
        },
        install=False,
    )
    secret = config.hmac.crypto_hmac_secret
    assert secret.algorithm == "HmacSHA512"
    assert len(secret.secret_key) == 32, "PBKDF2 with a 256 bit key length"


def test_nested_camelcase_keystores():
    config = CryptoConfig.from_properties(
        {
            "com": {
                "bld": {
                    "crypto": {
                        "jks": {
                            "file": str(FIXTURES / "mase.jks"),
                            "password": JKS_PASSWORD,
                            "alias": RSA_ALIAS,
                            "instanceJks": "JKS",
                        },
                        "signature": {
                            "file": str(FIXTURES / "mase.jks"),
                            "password": JKS_PASSWORD,
                            "alias": RSA_ALIAS,
                            "instanceSignature": "SHA256withRSA",
                        },
                    }
                }
            }
        },
        install=False,
    )
    assert config.jks.decrypt_value(config.jks.encrypt_value("probe")) == "probe"
    assert config.signature.verify(config.signature.sign("probe"), "probe") is True


@pytest.mark.parametrize(
    "section,fixture",
    [("jks", "mase.jks"), ("pkcs12", "x25519.p12"), ("signature", "mase.jks")],
)
def test_keystore_type_defaults_per_section(section, fixture):
    """``jks`` defaults to JKS, ``pkcs12`` to PKCS12, ``signature`` to JKS."""
    from conftest import PKCS12_PASSWORD, X25519_ALIAS

    password, alias = (
        (PKCS12_PASSWORD, X25519_ALIAS) if section == "pkcs12" else (JKS_PASSWORD, RSA_ALIAS)
    )
    config = CryptoConfig.from_properties(
        {
            f"com.bld.crypto.{section}.file": str(FIXTURES / fixture),
            f"com.bld.crypto.{section}.password": password,
            f"com.bld.crypto.{section}.alias": alias,
        },
        install=False,
    )
    assert getattr(config, section) is not None


# --------------------------------------------------------------------- aes keys


def test_aes_keys_flat_properties():
    config = CryptoConfig.from_properties(
        {"com.bld.crypto.aes.keys.k1.password": "0123456789abcdef"}, install=False
    )
    assert config.aes.decrypt_value(config.aes.encrypt_value("x", "k1"), "k1") == "x"


def test_aes_keys_nested_with_derivation():
    config = CryptoConfig.from_properties(
        {
            "com.bld": {
                "crypto": {
                    "aes": {
                        "keys": {
                            "k1": {
                                "password": "password",
                                "salt": "salty",
                                "keyLength": "256",
                            }
                        }
                    }
                }
            }
        },
        install=False,
    )
    # Same inputs must give the same key whichever binding shape supplied them.
    from common_encryption.config import _aes_secrets

    expected = SecretSpec("password", "salty", 256).key_bytes()
    assert _aes_secrets(
        {"com.bld.crypto.aes.keys.k1.password": "password",
         "com.bld.crypto.aes.keys.k1.salt": "salty",
         "com.bld.crypto.aes.keys.k1.key-length": "256"}
    ).get_secret_key("k1") == expected
    assert config.aes.decrypt_value(config.aes.encrypt_value("x", "k1"), "k1") == "x"


def test_aes_key_length_must_be_usable():
    """The length arrives as a string from ``.properties``, and JCA wants whole bytes."""
    with pytest.raises(CryptoException):
        SecretSpec("p", "s", "abc")
    with pytest.raises(CryptoException):
        SecretSpec("p", "s", 12)
    with pytest.raises(CryptoException):
        SecretSpec("p", "s", 0)


def test_hmac_key_length_is_not_an_aes_length():
    """Unlike AES, an HMAC key may be any size, so no 16/24/32 rule applies."""
    from common_encryption import CryptoHmacUtils

    utils = CryptoConfig.from_properties(
        {"com.bld.crypto.hmac.secret": "twenty-byte-secret!!"}, install=False
    ).hmac
    assert isinstance(utils, CryptoHmacUtils)
    assert len(utils.crypto_hmac_secret.secret_key) == 20
    assert utils.verify_value(utils.sign_value("f", "x"), "f") == "x"


# ------------------------------------------------------------------ public keys


def test_public_key_flat_properties():
    pem = _certificate_pem()
    config = CryptoConfig.from_properties(
        {"com.bld.crypto.public-key.keys.mase": pem}, install=False
    )
    assert config.jks is None, "only the public key section was configured"
    token = config.pub_key.encrypt_value("probe", "mase")
    from common_encryption import CryptoJksUtils, load_jks_key

    key = load_jks_key((FIXTURES / "mase.jks").read_bytes(), JKS_PASSWORD, RSA_ALIAS, "JKS")
    assert CryptoJksUtils(key).decrypt_value(token) == "probe"


def test_public_key_accepts_raw_der():
    der = base64.b64decode(
        _spki_base64()
    )
    config = CryptoConfig.from_properties(
        {"com.bld.crypto.public-key.keys.mase": base64.b64encode(der).decode()}, install=False
    )
    from common_encryption import CryptoJksUtils, load_jks_key

    key = load_jks_key((FIXTURES / "mase.jks").read_bytes(), JKS_PASSWORD, RSA_ALIAS, "JKS")
    assert CryptoJksUtils(key).decrypt_value(config.pub_key.encrypt_value("probe", "mase")) == "probe"


# ------------------------------------------------------------------- environment


def test_env_maps_the_hmac_section():
    config = CryptoConfig.from_env(
        {"BLD_CRYPTO_HMAC_SECRET": "s", "BLD_CRYPTO_HMAC_ALGORITHM": "HmacSHA512"}, install=False
    )
    assert config.hmac.crypto_hmac_secret.algorithm == "HmacSHA512"


def test_env_has_no_single_aes_secret():
    """Java binds AES as a named map, so there is no one-valued variable to mirror."""
    config = CryptoConfig.from_env({"BLD_CRYPTO_AES_SECRET": "0123456789abcdef"}, install=False)
    assert config.aes is None


def _spki_base64() -> str:
    from cryptography.hazmat.primitives.serialization import Encoding, PublicFormat

    from common_encryption import load_jks_key

    key = load_jks_key((FIXTURES / "mase.jks").read_bytes(), JKS_PASSWORD, RSA_ALIAS, "JKS")
    der = key.public_key.public_bytes(Encoding.DER, PublicFormat.SubjectPublicKeyInfo)
    return base64.b64encode(der).decode()


def _certificate_pem() -> str:
    import textwrap

    from common_encryption.commons.jks_store import load_jks_store

    store = load_jks_store((FIXTURES / "mase.jks").read_bytes(), JKS_PASSWORD)
    _, chain = store.key_entry(RSA_ALIAS, JKS_PASSWORD)
    body = textwrap.fill(base64.b64encode(chain[0]).decode(), 64)
    return f"-----BEGIN CERTIFICATE-----\n{body}\n-----END CERTIFICATE-----\n"
