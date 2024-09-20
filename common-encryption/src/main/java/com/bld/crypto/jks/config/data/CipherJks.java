/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.jks.config.data.CipherJks.java
 */
package com.bld.crypto.jks.config.data;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;

import com.bld.crypto.exception.CryptoException;
import com.bld.crypto.type.InstanceType;

/**
 * The Class CipherJks.
 */
public final class CipherJks {

	/** The private key. */
	private final PrivateKey privateKey;

	/** The public key. */
	private final PublicKey publicKey;

	/**
	 * Instantiates a new cipher jks.
	 *
	 * @param privateKey the private key
	 * @param publicKey  the public key
	 */
	public CipherJks(PrivateKey privateKey, PublicKey publicKey) {
		super();
		this.privateKey = privateKey;
		this.publicKey = publicKey;
	}

	public CipherJks(String privateKey, String publicKey) {
		super();
		this.privateKey = this.setPrivateKey(privateKey);
		this.publicKey = this.setPublicKey(publicKey);
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

	public String publicKey() {
		if (this.publicKey != null)
			return Base64.getEncoder().encodeToString(publicKey.getEncoded());
		return null;
	}

	private PrivateKey setPrivateKey(String privateKey) {
		try {
			if(StringUtils.isNotBlank(privateKey)) {
				byte[] keyBytes = Base64.getDecoder().decode(privateKey);
			    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
			    KeyFactory keyFactory = KeyFactory.getInstance(InstanceType.RSA.name());
			    return keyFactory.generatePrivate(spec);
			}
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new CryptoException(e);
		}
		return null;
	}

	private PublicKey setPublicKey(String publicKey) {
		try {
			if (StringUtils.isNotBlank(publicKey)) {
				byte[] keyBytes = Base64.getDecoder().decode(publicKey);
				X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
				KeyFactory keyFactory = KeyFactory.getInstance(InstanceType.RSA.name());
				return keyFactory.generatePublic(spec);
			}
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new CryptoException(e);
		}
		return null;
	}

}
