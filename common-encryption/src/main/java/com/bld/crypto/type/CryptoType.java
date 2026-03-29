/*
 * @auth: Francesco Baldi
 * @email: francesco.baldi1987@gmail.com
 * @class: com.bld.crypto.type.CryptoType.java
 */
package com.bld.crypto.type;

/**
 * Enumeration that selects which key of an asymmetric key pair (private or public)
 * is used for a specific cryptographic operation in the JKS strategy.
 *
 * <p>JKS-based encryption is bidirectional: the caller can choose to encrypt with
 * the public key (default) or with the private key, and to decrypt with the private
 * key (default) or with the public key.  This enum is used as an attribute on the
 * {@link com.bld.crypto.jks.annotation.CryptoJks} annotation.
 */
public enum CryptoType {

	/** Selects the private key for the operation. */
	privateKey,
	/** Selects the public key for the operation. */
	publicKey;
}
