/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.jks.CryptoJksUtils.java
 */
package com.bld.crypto.jks;

import java.security.Key;
import java.security.PublicKey;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;

import com.bld.crypto.bean.CryptoKeyUtils;
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


	public final PublicKey getPublicKey() {
		return this.jksKey.getPublicKey();
	}
	
	public final String publicKey() {
		return this.jksKey.publicKey();
	}


}
