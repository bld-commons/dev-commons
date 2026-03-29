package com.bld.crypto.commons;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;

import com.bld.crypto.exception.CryptoException;
import com.bld.crypto.type.InstanceType;

/**
 * Utility class that provides static helper methods for converting raw key material
 * (Base64-encoded DER bytes) into {@link java.security.PublicKey} instances.
 */
public class KeyUtility {

	/**
	 * Decodes a Base64-encoded X.509 public key and returns a {@link PublicKey} for
	 * the specified algorithm.
	 *
	 * @param publicKey    the Base64-encoded DER representation of the public key;
	 *                     returns {@code null} if blank
	 * @param instanceType the algorithm used to build the key factory (e.g. RSA, EC)
	 * @return the decoded {@link PublicKey}, or {@code null} if {@code publicKey} is blank
	 * @throws CryptoException if the algorithm is not available or the key spec is invalid
	 */
	public static PublicKey getPublicKey(String publicKey,InstanceType instanceType) {
		try {
			if (StringUtils.isNotBlank(publicKey)) {
				byte[] keyBytes = Base64.getDecoder().decode(publicKey);
				X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
				KeyFactory keyFactory = KeyFactory.getInstance(instanceType.name());
				return keyFactory.generatePublic(spec);
			}
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new CryptoException(e);
		}
		return null;
	}
	
}
