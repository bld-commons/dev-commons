import base64

from cryptography.hazmat.primitives.serialization import Encoding, PublicFormat

from ..commons.key_utility import get_private_key, get_public_key
from ..type import InstanceType


class JksKey:
    """Key pair holder. Port of ``com.bld.crypto.key.JksKey``.

    Wraps an asymmetric key pair loaded from a keystore. Accepts either ready
    ``cryptography`` key objects (the keystore path) or Base64 DER strings (the
    raw-material path, as in the Java constructor ``JksKey(String, String, InstanceType)``).
    """

    def __init__(
        self,
        private_key=None,
        public_key=None,
        instance_type: InstanceType = InstanceType.rsa,
    ) -> None:
        self.private_key = (
            private_key
            if not isinstance(private_key, (str, type(None)))
            else get_private_key(private_key, instance_type)
        )
        self.public_key = (
            public_key
            if not isinstance(public_key, (str, type(None)))
            else get_public_key(public_key, instance_type)
        )

    def public_key_base64(self) -> str | None:
        """Base64 of the X.509 encoded public key, or ``None`` when absent.

        Mirrors ``JksKey.publicKey()``; exposed so the key can be handed to an
        external encryptor.
        """
        if self.public_key is None:
            return None
        return base64.b64encode(
            self.public_key.public_bytes(Encoding.DER, PublicFormat.SubjectPublicKeyInfo)
        ).decode()

    def __repr__(self) -> str:  # pragma: no cover - debugging aid
        return f"JksKey(private={self.private_key is not None}, public={self.public_key is not None})"
