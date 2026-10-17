/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.jks.CryptoJksUtils.java
 */
package com.bld.crypto.jks;

import java.security.Key;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;

import com.bld.crypto.bean.CryptoKeyUtils;
import com.bld.crypto.commons.KeyUtility;
import com.bld.crypto.key.JksKey;
import com.bld.crypto.type.CryptoType;
import com.bld.crypto.type.InstanceType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;


/**
 * The Class CryptoJksUtils.
 */
public final class CryptoJksUtils extends CryptoKeyUtils {

	/** The Constant SPLIT_STRING. */
	public final static int SPLIT_STRING = 128;



	/** The Constant BEARER. */
	public final static String BEARER = "Bearer ";

	

	/** The jks key. */
	private final JksKey jksKey;
	
	
	
	public CryptoJksUtils(JksKey jksKey) {
		super();
		this.jksKey = jksKey;
	}

	/**
	 * Encrypt value.
	 *
	 * @param value the value
	 * @return the string
	 */
	public String encryptValue(String value) {
		return super.encryptValue(value, jksKey.getPublicKey(),InstanceType.RSA);
	}

	/**
	 * Decrypt value.
	 *
	 * @param value the value
	 * @return the string
	 */
	public String decryptValue(String value) {
		return super.decryptValue(value, jksKey.getPrivateKey(),InstanceType.RSA);
	}
	
	/**
	 * Encrypt object.
	 *
	 * @param value the value
	 * @return the string
	 * @throws JsonProcessingException the json processing exception
	 */
	public String encryptObject(Object value) throws JsonProcessingException {
		return super.encryptValue(this.objMapper.writeValueAsString(value), jksKey.getPublicKey(),InstanceType.RSA);
	}

	/**
	 * Decrypt object.
	 *
	 * @param <T> the generic type
	 * @param value the value
	 * @param response the response
	 * @return the t
	 * @throws JsonProcessingException the json processing exception
	 */
	public<T> T decryptObject(String value,Class<T> response) throws JsonProcessingException {
		String json=this.decryptValue(value);
		return this.objMapper.readValue(json, response);
	}

	/**
	 * Encrypt uri.
	 *
	 * @param value the value
	 * @param cryptoType the crypto type
	 * @return the string
	 */
	public String encryptUri(String value,CryptoType cryptoType) {
		Key key = getEncryptKey(cryptoType);
		String valueEncrypted = encryptValue(value,key,InstanceType.RSA);
		return encodeValue(valueEncrypted);
	}
	
	/**
	 * Encrypt object uri.
	 *
	 * @param value the value
	 * @param cryptoType the crypto type
	 * @return the string
	 * @throws JsonProcessingException the json processing exception
	 */
	public String encryptObjectUri(Object value,CryptoType cryptoType) throws JsonProcessingException {
		return this.encryptUri(this.objMapper.writeValueAsString(value),cryptoType);
	}

	/**
	 * Gets the encrypt key.
	 *
	 * @param cryptoType the crypto type
	 * @return the encrypt key
	 */
	private Key getEncryptKey(CryptoType cryptoType) {
		Key key=this.jksKey.getPublicKey();
		if(CryptoType.privateKey.equals(cryptoType))
			key=this.jksKey.getPrivateKey();
		return key;
	}
	
	
	/**
	 * Decrypt uri.
	 *
	 * @param value the value
	 * @param cryptoType the crypto type
	 * @return the string
	 */
	public String decryptUri(String value,CryptoType cryptoType) {
		if (StringUtils.isBlank(value))
			return null;
		Key key = getDecryptKey(cryptoType);
		String decode=new String(Base64.getDecoder().decode(value));
		return decryptValue(decode,key,InstanceType.RSA);
	}

	/**
	 * Gets the decrypt key.
	 *
	 * @param cryptoType the crypto type
	 * @return the decrypt key
	 */
	private Key getDecryptKey(CryptoType cryptoType) {
		Key key=this.jksKey.getPrivateKey();
		if(CryptoType.publicKey.equals(cryptoType))
			key=this.jksKey.getPublicKey();
		return key;
	}
	
	/**
	 * Decrypt value.
	 *
	 * @param value the value
	 * @param cryptoType the crypto type
	 * @return the string
	 */
	public String decryptValue(String value,CryptoType cryptoType) {
		return super.decryptValue(value, getDecryptKey(cryptoType),InstanceType.RSA);
	}
	
	/**
	 * Encrypt value.
	 *
	 * @param value the value
	 * @param cryptoType the crypto type
	 * @return the string
	 */
	public String encryptValue(String value,CryptoType cryptoType) {
		return super.encryptValue(value, getEncryptKey(cryptoType),InstanceType.RSA);
	}
	
	/**
	 * Decrypt object.
	 *
	 * @param <T> the generic type
	 * @param value the value
	 * @param response the response
	 * @param cryptoType the crypto type
	 * @return the t
	 * @throws JsonProcessingException the json processing exception
	 */
	public <T> T decryptObject(String value,Class<T>response,CryptoType cryptoType) throws JsonProcessingException {
		String json=this.decryptValue(value, cryptoType);
		return this.objMapper.readValue(json, response);
	}
	
	/**
	 * Encrypt object.
	 *
	 * @param value the value
	 * @param cryptoType the crypto type
	 * @return the string
	 * @throws JsonProcessingException the json processing exception
	 */
	public String encryptObject(Object value,CryptoType cryptoType) throws JsonProcessingException {
		return this.encryptValue(this.objMapper.writeValueAsString(value), cryptoType);
	}

	/**
	 * Encrypt uri.
	 *
	 * @param value the value
	 * @return the string
	 */
	public String encryptUri(String value) {
		return super.encryptUri(value, null);
	}
	
	/**
	 * Encrypt object uri.
	 *
	 * @param value the value
	 * @return the string
	 * @throws JsonProcessingException the json processing exception
	 */
	public String encryptObjectUri(Object value) throws JsonProcessingException {
		return super.encryptUri(this.objMapper.writeValueAsString(value), null);
	}
	
	/**
	 * Decrypt uri.
	 *
	 * @param value the value
	 * @return the string
	 */
	public String decryptUri(String value) {		
		return super.decryptUri(value, null);
	}
	
	/**
	 * Decrypt object uri.
	 *
	 * @param <T> the generic type
	 * @param value the value
	 * @param response the response
	 * @return the t
	 * @throws JsonMappingException the json mapping exception
	 * @throws JsonProcessingException the json processing exception
	 */
	public <T> T decryptObjectUri(String value,Class<T>response) throws JsonMappingException, JsonProcessingException {
		String json=this.decryptUri(value);
		return this.objMapper.readValue(json, response);
	}

	/**
	 * Encrypt value.
	 *
	 * @param value the value
	 * @param key the key
	 * @return the string
	 */
	@Override
	protected String encryptValue(String value, String key) {
		return this.encryptValue(value);
	}

	/**
	 * Decrypt value.
	 *
	 * @param value the value
	 * @param key the key
	 * @return the string
	 */
	@Override
	protected String decryptValue(String value, String key) {
		return this.decryptValue(value);
	}


	/**
	 * Wraps {@code value} in a {@code {key, value}} envelope and encrypts it with the public key,
	 * binding the value to its logical type (same format used by {@code @CryptoJks(value = key)}).
	 *
	 * @param key   the logical key identifier
	 * @param value the plain-text value
	 * @return the encrypted token
	 */
	public String encryptKeyValue(String key, String value) {
		return this.encryptKeyValue(key, value, CryptoType.publicKey);
	}

	/**
	 * Wraps {@code value} in a {@code {key, value}} envelope and encrypts it.
	 *
	 * @param key        the logical key identifier
	 * @param value      the plain-text value
	 * @param cryptoType the key used to encrypt
	 * @return the encrypted token
	 */
	public String encryptKeyValue(String key, String value, CryptoType cryptoType) {
		return this.encryptValue(KeyUtility.buildEnvelope(this.objMapper, key, value), cryptoType);
	}

	/**
	 * URL-safe variant of {@link #encryptKeyValue(String, String)}.
	 *
	 * @param key   the logical key identifier
	 * @param value the plain-text value
	 * @return the URL-safe encrypted token
	 */
	public String encryptKeyUri(String key, String value) {
		return this.encryptKeyUri(key, value, CryptoType.publicKey);
	}

	/**
	 * URL-safe variant of {@link #encryptKeyValue(String, String, CryptoType)}.
	 *
	 * @param key        the logical key identifier
	 * @param value      the plain-text value
	 * @param cryptoType the key used to encrypt
	 * @return the URL-safe encrypted token
	 */
	public String encryptKeyUri(String key, String value, CryptoType cryptoType) {
		return this.encryptUri(KeyUtility.buildEnvelope(this.objMapper, key, value), cryptoType);
	}

	/**
	 * Decrypts a token produced by {@link #encryptKeyValue(String, String)} with the private key
	 * and verifies that it belongs to {@code key}.
	 *
	 * @param key   the expected logical key identifier
	 * @param token the encrypted token
	 * @return the plain-text value, or {@code null} if {@code token} is blank
	 * @throws com.bld.crypto.exception.CryptoException if the key does not match
	 */
	public String decryptKeyValue(String key, String token) {
		return this.decryptKeyValue(key, token, CryptoType.privateKey);
	}

	/**
	 * Decrypts a token produced by {@link #encryptKeyValue(String, String, CryptoType)} and
	 * verifies that it belongs to {@code key}.
	 *
	 * @param key        the expected logical key identifier
	 * @param token      the encrypted token
	 * @param cryptoType the key used to decrypt
	 * @return the plain-text value, or {@code null} if {@code token} is blank
	 * @throws com.bld.crypto.exception.CryptoException if the key does not match
	 */
	public String decryptKeyValue(String key, String token, CryptoType cryptoType) {
		if (StringUtils.isBlank(token))
			return null;
		return KeyUtility.openEnvelope(this.objMapper, this.decryptValue(token, cryptoType), key);
	}

	/**
	 * URL-safe variant of {@link #decryptKeyValue(String, String)}.
	 *
	 * @param key   the expected logical key identifier
	 * @param token the URL-safe encrypted token
	 * @return the plain-text value, or {@code null} if {@code token} is blank
	 */
	public String decryptKeyUri(String key, String token) {
		return this.decryptKeyUri(key, token, CryptoType.privateKey);
	}

	/**
	 * URL-safe variant of {@link #decryptKeyValue(String, String, CryptoType)}.
	 *
	 * @param key        the expected logical key identifier
	 * @param token      the URL-safe encrypted token
	 * @param cryptoType the key used to decrypt
	 * @return the plain-text value, or {@code null} if {@code token} is blank
	 */
	public String decryptKeyUri(String key, String token, CryptoType cryptoType) {
		if (StringUtils.isBlank(token))
			return null;
		return KeyUtility.openEnvelope(this.objMapper, this.decryptUri(token, cryptoType), key);
	}




}
