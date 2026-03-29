/*
 * @auth: Francesco Baldi
 * @email: francesco.baldi1987@gmail.com
 * @class: com.bld.crypto.type.InstanceType.java
 */
package com.bld.crypto.type;

/**
 * Enumeration of the cryptographic algorithm identifiers used when obtaining a
 * {@link javax.crypto.Cipher} or {@link java.security.KeyFactory} instance.
 *
 * <p>The enum name is used directly as the argument to
 * {@link javax.crypto.Cipher#getInstance(String)} and
 * {@link java.security.KeyFactory#getInstance(String)}, so each constant must match
 * the algorithm name accepted by the JCA/JCE providers.
 */
public enum InstanceType {

	/** Advanced Encryption Standard symmetric algorithm. */
	AES,
	/** RSA asymmetric algorithm (used for JKS and public-key strategies). */
	RSA,
	/** Elliptic Curve algorithm. */
	EC;

}
