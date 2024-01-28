/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.jks.CryptoJksUtils.java
 */
package com.bld.crypto.jks;

import java.security.Key;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bld.crypto.bean.CryptoKeyUtils;
import com.bld.crypto.jks.config.data.CipherJks;
import com.bld.crypto.type.CryptoType;
import com.bld.crypto.type.InstanceType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;


/**
 * The Class CryptoJksUtils.
 */
@Component
public class CryptoJksUtils extends CryptoKeyUtils {

	/** The Constant SPLIT_STRING. */
	public final static int SPLIT_STRING = 128;



	/** The Constant BEARER. */
	public final static String BEARER = "Bearer ";

	/** The cipher jks. */
	@Autowired
	private CipherJks cipherJks;
	
	/**
	 * Encrypt value.
	 *
	 * @param value the value
	 * @return the string
	 */
	public String encryptValue(String value) {
		return super.encryptValue(value, cipherJks.getPublicKey());
	}

	/**
	 * Decrypt value.
	 *
	 * @param value the value
	 * @return the string
	 */
	public String decryptValue(String value) {
		return super.decryptValue(value, cipherJks.getPrivateKey());
	}
	
	/**
	 * Encrypt object.
	 *
	 * @param value the value
	 * @return the string
	 * @throws JsonProcessingException the json processing exception
	 */
	public String encryptObject(Object value) throws JsonProcessingException {
		return super.encryptValue(this.objMapper.writeValueAsString(value), cipherJks.getPublicKey());
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
		String valueEncrypted = encryptValue(value,key);
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
		Key key=this.cipherJks.getPublicKey();
		if(CryptoType.privateKey.equals(cryptoType))
			key=this.cipherJks.getPrivateKey();
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
		return decryptValue(decode,key);
	}

	/**
	 * Gets the decrypt key.
	 *
	 * @param cryptoType the crypto type
	 * @return the decrypt key
	 */
	private Key getDecryptKey(CryptoType cryptoType) {
		Key key=this.cipherJks.getPrivateKey();
		if(CryptoType.publicKey.equals(cryptoType))
			key=this.cipherJks.getPublicKey();
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
		return super.decryptValue(value, getDecryptKey(cryptoType));
	}
	
	/**
	 * Encrypt value.
	 *
	 * @param value the value
	 * @param cryptoType the crypto type
	 * @return the string
	 */
	public String encryptValue(String value,CryptoType cryptoType) {
		return super.encryptValue(value, getEncryptKey(cryptoType));
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
	 * Instance type.
	 *
	 * @return the instance type
	 */
	@Override
	protected InstanceType instanceType() {
		return InstanceType.RSA;
	}

//	public  void cookie(String token, HttpServletResponse response) {
//		int j=0;
//		token=BEARER+token;
//		for(int i=0 ; i<token.length();i+=CryptoUtils.SPLIT_STRING) {
//			String itemToken=null;
//			if(i+CryptoUtils.SPLIT_STRING>=token.length())
//				itemToken=token.substring(i);
//			else
//				itemToken=token.substring(i,i+CryptoUtils.SPLIT_STRING);
//			itemToken=this.encryptUri(itemToken);
//			CookieUtils.addCookie(response, j+"-"+HttpHeaders.AUTHORIZATION, itemToken);
//			j++;
//		}
//	}

//	public void headerAuthorization(String token, HttpServletResponse response) {
//		token=BEARER+token;
//		response.setHeader(HttpHeaders.AUTHORIZATION, this.encryptUri(token));
//	}

}
