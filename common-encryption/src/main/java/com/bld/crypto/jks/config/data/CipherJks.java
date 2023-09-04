/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.jks.config.data.CipherJks.java
 */
package com.bld.crypto.jks.config.data;

import java.security.PrivateKey;
import java.security.PublicKey;

import io.jsonwebtoken.SignatureAlgorithm;


/**
 * The Class CipherJks.
 */
public final class CipherJks {

	/** The private key. */
	private final PrivateKey privateKey;

	/** The public key. */
	private final PublicKey publicKey;

	/** The algorithm. */
	private final SignatureAlgorithm algorithm;

	/**
	 * Instantiates a new cipher jks.
	 *
	 * @param privateKey the private key
	 * @param publicKey the public key
	 * @param algorithm the algorithm
	 */
	public CipherJks(PrivateKey privateKey, PublicKey publicKey, SignatureAlgorithm algorithm) {
		super();
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.algorithm = algorithm;
	}

	/**
	 * Gets the private key.
	 *
	 * @return the private key
	 */
	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	/**
	 * Gets the public key.
	 *
	 * @return the public key
	 */
	public PublicKey getPublicKey() {
		return publicKey;
	}

	/**
	 * Gets the algorithm.
	 *
	 * @return the algorithm
	 */
	public SignatureAlgorithm getAlgorithm() {
		return algorithm;
	}

}
