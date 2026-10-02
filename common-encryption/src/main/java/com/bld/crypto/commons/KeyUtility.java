package com.bld.crypto.commons;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.bld.crypto.exception.CryptoException;
import com.bld.crypto.type.InstanceType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	/**
	 * Wraps {@code value} in the {@code {"key":"<key>","value":"<value>"}} JSON envelope
	 * used to bind an encrypted value to its logical type (e.g. an entity id).
	 *
	 * @param objMapper the Jackson {@link ObjectMapper}
	 * @param key       the logical key identifier
	 * @param value     the plain-text value
	 * @return the JSON envelope
	 * @throws CryptoException if JSON serialisation fails
	 */
	public static String buildEnvelope(ObjectMapper objMapper, String key, String value) {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("key", key);
		map.put("value", value);
		try {
			return objMapper.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			throw new CryptoException(e);
		}
	}

	/**
	 * Parses a JSON envelope produced by {@link #buildEnvelope}, verifies that its key
	 * matches {@code expectedKey} and returns the wrapped value.
	 *
	 * @param objMapper   the Jackson {@link ObjectMapper}
	 * @param envelope    the decrypted JSON envelope
	 * @param expectedKey the expected logical key identifier
	 * @return the plain-text value
	 * @throws CryptoException if the JSON is invalid or the key does not match
	 */
	@SuppressWarnings("unchecked")
	public static String openEnvelope(ObjectMapper objMapper, String envelope, String expectedKey) {
		Map<String, String> map;
		try {
			map = objMapper.readValue(envelope, Map.class);
		} catch (JsonProcessingException e) {
			throw new CryptoException(e);
		}
		String key = map.get("key");
		if (!StringUtils.defaultString(expectedKey).equals(key))
			throw new CryptoException("The \"" + expectedKey + "\" value does not match the original \"" + key + "\" value");
		return map.get("value");
	}

}
