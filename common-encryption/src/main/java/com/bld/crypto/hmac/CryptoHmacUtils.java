/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.hmac.CryptoHmacUtils.java
 */
package com.bld.crypto.hmac;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bld.crypto.bean.CryptoUtils;
import com.bld.crypto.exception.CryptoException;
import com.bld.crypto.hmac.config.data.CryptoHmacSecret;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility bean that signs and verifies values using HMAC.
 *
 * <p>The produced token has the format:
 * <pre>
 *   Base64(envelopeJson) + "." + Base64(hmacBytes)
 * </pre>
 * where {@code envelopeJson} is {@code {"key":"&lt;fieldKey&gt;","value":"&lt;plainValue&gt;"}}.
 * URL-safe mode uses Base64Url encoding without padding.
 *
 * <p>The {@code fieldKey} embedded in the envelope is validated on verification to
 * prevent cross-field substitution (using a token generated for field A on field B).
 * HMAC comparison is performed in constant time via {@link MessageDigest#isEqual}.
 */
@Component
public class CryptoHmacUtils extends CryptoUtils {

	private static final String SEPARATOR = ".";

	private static final Logger logger = LoggerFactory.getLogger(CryptoHmacUtils.class);

	@Autowired
	private CryptoHmacSecret cryptoHmacSecret;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Signs {@code value} and returns a standard Base64 token.
	 *
	 * @param fieldKey  the logical field identifier embedded in the envelope
	 * @param value     the plain-text value to sign
	 * @return the signed token
	 */
	public String signValue(String fieldKey, String value) {
		return sign(fieldKey, value, Base64.getEncoder(), Base64.getEncoder());
	}

	/**
	 * Verifies a standard Base64 token and returns the original plain-text value.
	 *
	 * @param token     the signed token produced by {@link #signValue}
	 * @param fieldKey  the expected logical field identifier
	 * @return the plain-text value
	 * @throws CryptoException if the MAC is invalid or the field key does not match
	 */
	public String verifyValue(String token, String fieldKey) {
		return verify(token, fieldKey, Base64.getDecoder());
	}

	/**
	 * Signs {@code value} and returns a URL-safe Base64 token (no padding).
	 *
	 * @param fieldKey  the logical field identifier embedded in the envelope
	 * @param value     the plain-text value to sign
	 * @return the URL-safe signed token
	 */
	public String signUri(String fieldKey, String value) {
		Base64.Encoder urlEncoder = Base64.getUrlEncoder().withoutPadding();
		return sign(fieldKey, value, urlEncoder, urlEncoder);
	}

	/**
	 * Verifies a URL-safe Base64 token and returns the original plain-text value.
	 *
	 * @param token     the signed token produced by {@link #signUri}
	 * @param fieldKey  the expected logical field identifier
	 * @return the plain-text value
	 * @throws CryptoException if the MAC is invalid or the field key does not match
	 */
	public String verifyUri(String token, String fieldKey) {
		return verify(token, fieldKey, Base64.getUrlDecoder());
	}

	// --- private helpers ---

	private String sign(String fieldKey, String value, Base64.Encoder envelopeEncoder, Base64.Encoder macEncoder) {
		try {
			byte[] envelopeBytes = buildEnvelope(fieldKey, value).getBytes(StandardCharsets.UTF_8);
			byte[] macBytes = computeMac(envelopeBytes);
			return envelopeEncoder.encodeToString(envelopeBytes) + SEPARATOR + macEncoder.encodeToString(macBytes);
		} catch (JsonProcessingException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new CryptoException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private String verify(String token, String fieldKey, Base64.Decoder decoder) {
		int dotIndex = token.lastIndexOf(SEPARATOR);
		if (dotIndex < 0) {
			String errorMessage = "Invalid HMAC token format for field \"" + fieldKey + "\"";
			logger.error(errorMessage);
			throw new CryptoException(errorMessage);
		}
		try {
			byte[] envelopeBytes = decoder.decode(token.substring(0, dotIndex));
			byte[] receivedMac = decoder.decode(token.substring(dotIndex + 1));
			byte[] expectedMac = computeMac(envelopeBytes);

			if (!MessageDigest.isEqual(expectedMac, receivedMac)) {
				String errorMessage = "HMAC verification failed for field \"" + fieldKey + "\"";
				logger.error(errorMessage);
				throw new CryptoException(errorMessage);
			}

			String envelopeJson = new String(envelopeBytes, StandardCharsets.UTF_8);
			Map<String, String> map = objectMapper.readValue(envelopeJson, Map.class);
			String key = map.get("key");
			if (!fieldKey.equals(key)) {
				String errorMessage = "The \"" + fieldKey + "\" value does not match the original \"" + key + "\" value";
				logger.error(errorMessage);
				throw new CryptoException(errorMessage);
			}
			return map.get("value");
		} catch (JsonProcessingException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new CryptoException(e);
		}
	}

	private String buildEnvelope(String fieldKey, String value) throws JsonProcessingException {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("key", fieldKey);
		map.put("value", value);
		return objectMapper.writeValueAsString(map);
	}

	private byte[] computeMac(byte[] data) {
		try {
			Mac mac = Mac.getInstance(cryptoHmacSecret.getAlgorithm());
			mac.init(cryptoHmacSecret.getSecretKey());
			return mac.doFinal(data);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new CryptoException(e);
		}
	}
}
