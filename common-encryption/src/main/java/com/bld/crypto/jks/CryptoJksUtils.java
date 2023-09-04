package com.bld.crypto.jks;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import com.bld.crypto.bean.CryptoKeyUtils;
import com.bld.crypto.jks.config.data.CipherJks;
import com.bld.crypto.type.CryptoType;
import com.bld.crypto.type.InstanceType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Component
public class CryptoJksUtils extends CryptoKeyUtils {

	public final static int SPLIT_STRING = 128;



	public final static String BEARER = "Bearer ";

	@Autowired
	private CipherJks cipherJks;
	
	public String encryptValue(String value) {
		return super.encryptValue(value, cipherJks.getPrivateKey());
	}

	public String decryptValue(String value) {
		return super.decryptValue(value, cipherJks.getPublicKey());
	}
	
	public String encryptObject(Object value) throws JsonProcessingException {
		return super.encryptValue(this.objMapper.writeValueAsString(value), cipherJks.getPrivateKey());
	}

	public<T> T decryptObject(String value,Class<T> response) throws JsonProcessingException {
		String json=this.decryptValue(value);
		return this.objMapper.readValue(json, response);
	}

	public String encryptUri(String value,CryptoType cryptoType) {
		Key key = getEncryptKey(cryptoType);
		String valueEncrypted = encryptValue(value,key);
		return encodeValue(valueEncrypted);
	}
	
	public String encryptObjectUri(Object value,CryptoType cryptoType) throws JsonProcessingException {
		return this.encryptUri(this.objMapper.writeValueAsString(value),cryptoType);
	}

	private Key getEncryptKey(CryptoType cryptoType) {
		Key key=this.cipherJks.getPrivateKey();
		if(CryptoType.publicKey.equals(cryptoType))
			key=this.cipherJks.getPublicKey();
		return key;
	}
	
	
	public String decryptUri(String value,CryptoType cryptoType) {
		if (StringUtils.isBlank(value))
			return null;
		Key key = getDecryptKey(cryptoType);
		String decode=UriUtils.decode(value.replace(_252F, _2F).replace(_252F.toLowerCase(), _2F.toLowerCase()), StandardCharsets.UTF_8);
		return decryptValue(decode,key);
	}

	private Key getDecryptKey(CryptoType cryptoType) {
		Key key=this.cipherJks.getPublicKey();
		if(CryptoType.privateKey.equals(cryptoType))
			key=this.cipherJks.getPrivateKey();
		return key;
	}
	
	public String decryptValue(String value,CryptoType cryptoType) {
		return super.decryptValue(value, getDecryptKey(cryptoType));
	}
	
	public String encryptValue(String value,CryptoType cryptoType) {
		return super.encryptValue(value, getEncryptKey(cryptoType));
	}
	
	public <T> T decryptObject(String value,Class<T>response,CryptoType cryptoType) throws JsonProcessingException {
		String json=this.decryptValue(value, cryptoType);
		return this.objMapper.readValue(json, response);
	}
	
	public String encryptObject(Object value,CryptoType cryptoType) throws JsonProcessingException {
		return this.encryptValue(this.objMapper.writeValueAsString(value), cryptoType);
	}

	public String encryptUri(String value) {
		return super.encryptUri(value, null);
	}
	
	public String encryptObjectUri(Object value) throws JsonProcessingException {
		return super.encryptUri(this.objMapper.writeValueAsString(value), null);
	}
	
	public String decryptUri(String value) {		
		return super.decryptUri(value, null);
	}
	
	public <T> T decryptObjectUri(String value,Class<T>response) throws JsonMappingException, JsonProcessingException {
		String json=this.decryptUri(value);
		return this.objMapper.readValue(json, response);
	}

	@Override
	protected String encryptValue(String value, String key) {
		return this.encryptValue(value);
	}

	@Override
	protected String decryptValue(String value, String key) {
		return this.decryptValue(value);
	}

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
