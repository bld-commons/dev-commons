"""Shared fixtures.

The keystores under ``fixtures/`` are real files produced by ``keytool`` and ``openssl``,
not ones this package generated, so the tests exercise genuine JCA/OpenSSL artefacts.
``java_vectors.json`` holds payloads encrypted by the Java reference implementation and is
regenerated with the command documented in the README.
"""

from __future__ import annotations

import json
from pathlib import Path

import pytest

from common_encryption import CryptoJksUtils, CryptoPkcs12Utils
from common_encryption.commons.jks_store import load_jks_store
from common_encryption.key.key_properties import load_jks_key

FIXTURES = Path(__file__).parent / "fixtures"

JKS_PASSWORD = "jkspass"
PKCS12_PASSWORD = "pkcs12pass"
RSA_ALIAS = "mase-rsa"
X25519_ALIAS = "mase-x25519"


def _load(name: str, password: str, alias: str, instance: str):
    return load_jks_key((FIXTURES / name).read_bytes(), password, alias, instance)


@pytest.fixture(scope="session")
def jks_utils() -> CryptoJksUtils:
    return CryptoJksUtils(_load("mase.jks", JKS_PASSWORD, RSA_ALIAS, "JKS"))


@pytest.fixture(scope="session")
def rsa_pkcs12_utils() -> CryptoPkcs12Utils:
    return CryptoPkcs12Utils(_load("rsa.p12", PKCS12_PASSWORD, RSA_ALIAS, "PKCS12"))


@pytest.fixture(scope="session")
def pkcs12_utils() -> CryptoPkcs12Utils:
    return CryptoPkcs12Utils(_load("x25519.p12", PKCS12_PASSWORD, X25519_ALIAS, "PKCS12"))


@pytest.fixture(scope="session")
def jks_store():
    return load_jks_store((FIXTURES / "mase.jks").read_bytes(), JKS_PASSWORD)


@pytest.fixture(scope="session")
def java_vectors() -> dict[str, str]:
    return json.loads((FIXTURES / "java_vectors.json").read_text())


AES_KEY = "0123456789abcdef"
HMAC_KEY = "0123456789abcdef"


@pytest.fixture(scope="session")
def aes_utils():
    from common_encryption import AesSecrets, CryptoAesUtils, SecretSpec

    return CryptoAesUtils(AesSecrets({"k1": SecretSpec(AES_KEY)}))


@pytest.fixture(scope="session")
def hmac_utils():
    from common_encryption import CryptoHmacUtils, HmacSecret

    return CryptoHmacUtils(HmacSecret(HMAC_KEY.encode(), "HmacSHA256"))


@pytest.fixture(scope="session")
def signature_utils(jks_utils):
    from common_encryption import SignatureKeyUtils

    return SignatureKeyUtils(jks_utils.jks_key, "SHA256withRSA")
