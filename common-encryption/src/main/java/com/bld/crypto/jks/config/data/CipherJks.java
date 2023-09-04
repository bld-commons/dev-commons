package com.bld.crypto.jks.config.data;

import java.security.PrivateKey;
import java.security.PublicKey;

import io.jsonwebtoken.SignatureAlgorithm;

public final class CipherJks {

	private final PrivateKey privateKey;

	private final PublicKey publicKey;

	private final SignatureAlgorithm algorithm;

	public CipherJks(PrivateKey privateKey, PublicKey publicKey, SignatureAlgorithm algorithm) {
		super();
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.algorithm = algorithm;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public SignatureAlgorithm getAlgorithm() {
		return algorithm;
	}

}
