class CryptoException(Exception):
    """Unchecked exception raised by every cryptographic operation of this module.

    Mirrors ``com.bld.crypto.exception.CryptoException``: it wraps lower level errors
    (key material problems, GCM tag failures, envelope mismatches) so that callers do
    not have to declare checked exceptions on every encrypt/decrypt call.
    """
