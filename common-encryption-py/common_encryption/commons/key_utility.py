import json

from ..exception import CryptoException
from ..type import InstanceType


def build_envelope(key: str, value: str) -> str:
    """Wrap ``value`` in the ``{"key":"<key>","value":"<value>"}`` JSON envelope.

    Binds an encrypted payload to its logical type (an entity id, a field name), so that
    a token cannot be substituted across fields. Mirrors
    ``com.bld.crypto.commons.KeyUtility.buildEnvelope``: compact separators, insertion
    order, non-ASCII left unescaped, which is what Jackson produces by default.
    """
    return json.dumps({"key": key, "value": value}, separators=(",", ":"), ensure_ascii=False)


def open_envelope(envelope: str, expected_key: str) -> str:
    """Parse an envelope, check the key, and return the wrapped value.

    Mirrors ``KeyUtility.openEnvelope``.
    """
    try:
        parsed = json.loads(envelope)
    except ValueError as error:
        raise CryptoException(error) from error
    if not isinstance(parsed, dict):
        raise CryptoException("The encrypted payload is not a {key, value} envelope")
    key = parsed.get("key")
    if (expected_key or "") != key:
        raise CryptoException(
            f'The "{expected_key}" value does not match the original "{key}" value'
        )
    return parsed.get("value")


def get_public_key(public_key: str, instance_type: InstanceType) -> object | None:
    """Decode a Base64 X.509 ``SubjectPublicKeyInfo`` into a ``cryptography`` key.

    Mirrors ``KeyUtility.getPublicKey``: returns ``None`` for a blank input, so that a
    keystore holding only a private key (PKCS12 exported with ``-nocerts``) still works.
    """
    import base64

    from cryptography.hazmat.primitives.serialization import load_der_public_key

    if not public_key or not public_key.strip():
        return None
    try:
        return load_der_public_key(base64.b64decode(public_key))
    except Exception as error:
        raise CryptoException(error) from error


def get_private_key(private_key: str, instance_type: InstanceType) -> object | None:
    """Decode a Base64 PKCS#8 private key. Mirrors ``JksKey.setPrivateKey``."""
    import base64

    from cryptography.hazmat.primitives.serialization import load_der_private_key

    if not private_key or not private_key.strip():
        return None
    try:
        return load_der_private_key(base64.b64decode(private_key), password=None)
    except Exception as error:
        raise CryptoException(error) from error


class KeyUtility:
    """Namespace holder mirroring ``com.bld.crypto.commons.KeyUtility``."""

    build_envelope = staticmethod(build_envelope)
    open_envelope = staticmethod(open_envelope)
    get_public_key = staticmethod(get_public_key)
    get_private_key = staticmethod(get_private_key)
