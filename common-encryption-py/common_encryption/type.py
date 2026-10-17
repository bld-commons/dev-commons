from enum import Enum


class CryptoType(Enum):
    """Selects which half of an asymmetric key pair is used for an operation.

    Port of ``com.bld.crypto.type.CryptoType``. JKS based encryption is bidirectional:
    encrypt with the public key (default) or the private key, decrypt with the private
    key (default) or the public key.
    """

    private_key = "privateKey"
    public_key = "publicKey"

    def __str__(self) -> str:
        return self.value


class InstanceType(Enum):
    """Cipher algorithm identifiers.

    Port of ``com.bld.crypto.type.InstanceType``. Each name matches the algorithm
    accepted by the JCA, and therefore the cipher selected in the Python port:

    ========  =========================
    name      cipher
    ========  =========================
    ``AES``   AES/ECB/PKCS5Padding
    ``RSA``   RSA/ECB/PKCS1Padding
    ``EC``    key agreement / signature only
    ========  =========================
    """

    aes = "AES"
    rsa = "RSA"
    ec = "EC"

    def __str__(self) -> str:
        return self.value


class AesSizeType(Enum):
    """Valid AES key sizes in bytes. Port of ``com.bld.crypto.type.AesSizeType``."""

    sz16 = 16
    sz24 = 24
    sz32 = 32

    def __init__(self, size: int) -> None:
        self.size = size
