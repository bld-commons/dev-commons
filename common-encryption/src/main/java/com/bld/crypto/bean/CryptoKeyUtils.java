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
 * The Class CryptoKeyUtils.
 */
public abstract class CryptoKeyUtils {
	
	
	
	/** The Constant logger. */
	private final static Logger logger=LoggerFactory.getLogger(CryptoKeyUtils.class);
	
	/** The obj mapper. */
	@Autowired
	protected ObjectMapper objMapper;
	

	/**
	 * Gets the cipher.
	 *
	 * @param mode the mode
	 * @param key the key
	 * @param instanceType the instance type
	 * @return the cipher
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws NoSuchPaddingException the no such padding exception
	 * @throws InvalidKeyException the invalid key exception
	 */
	protected static Cipher getCipher(int mode,Key key,InstanceType instanceType) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		Cipher cipher = Cipher.getInstance(instanceType.name());
		cipher.init(mode, key);
		return cipher;
	}
	
	
	
	/**
	 * Encrypt value.
	 *
	 * @param value the value
	 * @param key the key
	 * @param instanceType the instance type
	 * @return the string
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
	 * Decrypt value.
	 *
	 * @param value the value
	 * @param key the key
	 * @param instanceType the instance type
	 * @return the string
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
	 * Encrypt value.
	 *
	 * @param value the value
	 * @param key the key
	 * @return the string
	 */
	protected abstract String encryptValue(String value,final String key);

	
	/**
	 * Decrypt value.
	 *
	 * @param value the value
	 * @param key the key
	 * @return the string
	 */
	protected abstract String decryptValue(String value,final String key);
	
	

	/**
	 * Encrypt uri.
	 *
	 * @param value the value
	 * @param key the key
	 * @return the string
	 */
	protected String encryptUri(String value,final String key) {
		String valueEncrypted = encryptValue(value,key);
		return encodeValue(valueEncrypted);
	}


	/**
	 * Encode value.
	 *
	 * @param valueEncrypted the value encrypted
	 * @return the string
	 */
	protected String encodeValue(String valueEncrypted) {
		if (StringUtils.isNotEmpty(valueEncrypted))
			return Base64.getEncoder().encodeToString(valueEncrypted.getBytes());
		else
			return null;
	}
	
	/**
	 * Decrypt uri.
	 *
	 * @param value the value
	 * @param key the key
	 * @return the string
	 */
	protected String decryptUri(String value,final String key) {
		if (StringUtils.isBlank(value))
			return null;
		String decode=new String(Base64.getDecoder().decode(value));
		return decryptValue(decode,key);
	}


}
