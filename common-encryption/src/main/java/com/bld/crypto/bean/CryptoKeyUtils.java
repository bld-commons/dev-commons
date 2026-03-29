/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.bean.CryptoKeyUtils.java
 */

package com.bld.crypto.bean;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bld.crypto.exception.CryptoException;
import com.bld.crypto.type.InstanceType;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Abstract base class that provides low-level encrypt/decrypt operations using the
 * Java {@link javax.crypto.Cipher} API.
 *
 * <p>Concrete subclasses supply the key-lookup strategy (AES secret key, RSA JKS key,
 * or RSA public key) and delegate the actual cipher work to the protected static helpers
 * defined here.  The class also exposes convenience URL-encoding variants that wrap the
 * cipher output in an additional Base64 layer so that the result is safe for use in
 * URL path segments or query parameters.
 */
public abstract class CryptoKeyUtils {



	/** Logger for this class. */
	private final static Logger logger=LoggerFactory.getLogger(CryptoKeyUtils.class);

	/** Jackson {@link ObjectMapper} injected by Spring for JSON serialization/deserialization. */
	@Autowired
	protected ObjectMapper objMapper;


	/**
	 * Creates and initialises a {@link Cipher} instance for the given mode and key.
	 *
	 * @param mode         the cipher mode, e.g. {@link javax.crypto.Cipher#ENCRYPT_MODE} or
	 *                     {@link javax.crypto.Cipher#DECRYPT_MODE}
	 * @param key          the cryptographic key
	 * @param instanceType the algorithm identifier (AES, RSA, EC …)
	 * @return an initialised {@link Cipher}
	 * @throws NoSuchAlgorithmException if the requested algorithm is not available
	 * @throws NoSuchPaddingException   if the requested padding is not available
	 * @throws InvalidKeyException      if the key is inappropriate for the cipher
	 */
	protected static Cipher getCipher(int mode,Key key,InstanceType instanceType) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		Cipher cipher = Cipher.getInstance(instanceType.name());
		cipher.init(mode, key);
		return cipher;
	}



	/**
	 * Encrypts a plain-text string using the supplied key and algorithm, then encodes
	 * the resulting bytes as a Base64 string.
	 *
	 * @param value        the plain-text value to encrypt; ignored if blank
	 * @param key          the cryptographic key
	 * @param instanceType the algorithm identifier (AES, RSA, EC …)
	 * @return the Base64-encoded cipher text, or {@code null} if {@code value} is blank
	 * @throws CryptoException if any cryptographic error occurs
	 */
	public static String encryptValue(final String value,final Key key,InstanceType instanceType) {
		String valueEncrypted = null;
		if (StringUtils.isNotBlank(value)) {
			try {
				Cipher cipher = CryptoKeyUtils.getCipher(Cipher.ENCRYPT_MODE,key,instanceType);
				byte[] encrypt = cipher.doFinal(value.getBytes());
				valueEncrypted = Base64.getEncoder().encodeToString(encrypt);
			} catch (Exception e) {
				logger.error(ExceptionUtils.getStackTrace(e));
				throw new CryptoException(e);
			}

		}
		return valueEncrypted;
	}
	
	
	
	/**
	 * Decrypts a Base64-encoded cipher-text string using the supplied key and algorithm.
	 *
	 * @param value        the Base64-encoded cipher text to decrypt; ignored if blank
	 * @param key          the cryptographic key
	 * @param instanceType the algorithm identifier (AES, RSA, EC …)
	 * @return the plain-text string, or {@code null} if {@code value} is blank
	 * @throws CryptoException if any cryptographic error occurs
	 */
	public String decryptValue(final String value,final Key key,InstanceType instanceType) {
		String valueDecripted = null;
		if (StringUtils.isNotBlank(value)) {
			try {
				Cipher cipher = CryptoKeyUtils.getCipher(Cipher.DECRYPT_MODE,key,instanceType);
				byte[] decrypt = Base64.getDecoder().decode(value);
				valueDecripted = new String(cipher.doFinal(decrypt));
			} catch (Exception e) {
				logger.error(ExceptionUtils.getStackTrace(e));
				throw new CryptoException(e);
			}
		}
		return valueDecripted;
	}
	

	/**
	 * Encrypts a plain-text value using the named key.  Subclasses resolve the logical
	 * key name to the concrete {@link java.security.Key} and delegate to
	 * {@link #encryptValue(String, Key, InstanceType)}.
	 *
	 * @param value the plain-text value to encrypt
	 * @param key   the logical key name used to look up the actual cryptographic key
	 * @return the encrypted, Base64-encoded string
	 */
	protected abstract String encryptValue(String value,final String key);


	/**
	 * Decrypts a Base64-encoded cipher text using the named key.  Subclasses resolve the
	 * logical key name to the concrete {@link java.security.Key} and delegate to
	 * {@link #decryptValue(String, Key, InstanceType)}.
	 *
	 * @param value the Base64-encoded cipher text to decrypt
	 * @param key   the logical key name used to look up the actual cryptographic key
	 * @return the plain-text string
	 */
	protected abstract String decryptValue(String value,final String key);



	/**
	 * Encrypts a value and then encodes the result with an additional Base64 layer so
	 * that it is safe for use in URL path segments or query parameters.
	 *
	 * @param value the plain-text value to encrypt
	 * @param key   the logical key name
	 * @return the doubly-encoded cipher text suitable for embedding in a URL
	 */
	protected String encryptUri(String value,final String key) {
		String valueEncrypted = encryptValue(value,key);
		return encodeValue(valueEncrypted);
	}


	/**
	 * Applies an additional Base64 encoding to an already-encrypted value, making it
	 * safe for embedding in a URL.
	 *
	 * @param valueEncrypted the encrypted (Base64) string to encode further
	 * @return the URL-safe double-encoded string, or {@code null} if the input is empty
	 */
	protected String encodeValue(String valueEncrypted) {
		if (StringUtils.isNotEmpty(valueEncrypted))
			return Base64.getEncoder().encodeToString(valueEncrypted.getBytes());
		else
			return null;
	}

	/**
	 * Decodes the outer URL-safe Base64 layer and then decrypts the inner cipher text
	 * using the named key.
	 *
	 * @param value the URL-safe double-encoded cipher text
	 * @param key   the logical key name
	 * @return the plain-text string, or {@code null} if {@code value} is blank
	 */
	protected String decryptUri(String value,final String key) {
		if (StringUtils.isBlank(value))
			return null;
		String decode=new String(Base64.getDecoder().decode(value));
		return decryptValue(decode,key);
	}


}
