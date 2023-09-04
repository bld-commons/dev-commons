package com.bld.crypto.pubkey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bld.crypto.bean.CryptoKeyUtils;
import com.bld.crypto.pubkey.config.data.CipherPublicKeys;
import com.bld.crypto.type.InstanceType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Component
public class CryptoPublicKeyUtils extends CryptoKeyUtils {

	public final static int SPLIT_STRING = 128;



	public final static String BEARER = "Bearer ";

	@Autowired
	private CipherPublicKeys cipherPublicKeys;

	@Override
	public String encryptValue(String value,final String key) {
		return super.encryptValue(value, cipherPublicKeys.getPublicKey(key));
	}
	
	public String encryptObject(Object value,final String key) throws JsonProcessingException {
		return this.encryptValue(this.objMapper.writeValueAsString(value), key);
	}
	
	@Override
	public String decryptValue(String value,final String key) {
		return super.decryptValue(value, cipherPublicKeys.getPublicKey(key));
	}

	public <T>T decryptObject(String value,Class<T> response,final String key) throws JsonMappingException, JsonProcessingException {
		String json=this.decryptValue(value, key);
		return this.objMapper.readValue(json, response);
	}

	@Override
	public String encryptUri(String value,final String key) {
		return super.encryptUri(value, key);
	}
	
	public String encryptObjectUri(Object value,final String key) throws JsonProcessingException {
		return this.encryptUri(this.objMapper.writeValueAsString(value), key);
	}
	
	@Override
	public String decryptUri(String value,final String key) {
		return super.decryptUri(value, key);
	}
	
	public <T>T decryptObjectUri(String value,Class<T>response,final String key) throws JsonMappingException, JsonProcessingException {
		String json=this.decryptUri(value, key);
		return this.objMapper.readValue(json, response);
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
