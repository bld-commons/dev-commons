package com.bld.crypto.bean;

import java.nio.charset.StandardCharsets;
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
import org.springframework.web.util.UriUtils;

import com.bld.crypto.exception.CryptoException;
import com.bld.crypto.type.InstanceType;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class CryptoKeyUtils {
	
	protected static final String _252F = "_252F25_";

	protected static final String _2F = "%2F";

	protected abstract InstanceType instanceType();
	
	private final static Logger logger=LoggerFactory.getLogger(CryptoKeyUtils.class);
	
	@Autowired
	protected ObjectMapper objMapper;
	
	protected Cipher getCipher(int mode,Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		Cipher cipher = Cipher.getInstance(this.instanceType().name());
		cipher.init(mode, key);
		return cipher;
	}
	
	
	public String encryptValue(final String value,final Key key) {
		String valueEncrypted = null;
		if (StringUtils.isNotBlank(value)) {
			try {
				Cipher cipher = getCipher(Cipher.ENCRYPT_MODE,key);
				byte[] encrypt = cipher.doFinal(value.getBytes());
				valueEncrypted = Base64.getEncoder().encodeToString(encrypt);
			} catch (Exception e) {
				logger.error(ExceptionUtils.getStackTrace(e));
				throw new CryptoException(e);
			}

		}
		return valueEncrypted;
	}
	
	
	public String decryptValue(final String value,final Key key) {
		String valueDecripted = null;
		if (StringUtils.isNotBlank(value)) {
			try {
				
				Cipher cipher = getCipher(Cipher.DECRYPT_MODE,key);
				byte[] decrypt = Base64.getDecoder().decode(value);
				valueDecripted = new String(cipher.doFinal(decrypt));
			} catch (Exception e) {
				logger.error(ExceptionUtils.getStackTrace(e));
				throw new CryptoException(e);
			}
		}
		return valueDecripted;
	}
	
	protected abstract String encryptValue(String value,final String key);

	protected abstract String decryptValue(String value,final String key);
	
	
	protected String encryptUri(String value,final String key) {
		String valueEncrypted = encryptValue(value,key);
		return encodeValue(valueEncrypted);
	}


	protected String encodeValue(String valueEncrypted) {
		if (StringUtils.isNotEmpty(valueEncrypted))
			return UriUtils.encode(valueEncrypted, StandardCharsets.UTF_8).replace(_2F, _252F).replace(_2F.toLowerCase(), _252F.toLowerCase());
		else
			return null;
	}
	
	protected String decryptUri(String value,final String key) {
		if (StringUtils.isBlank(value))
			return null;
		String decode=UriUtils.decode(value.replace(_252F, _2F).replace(_252F.toLowerCase(), _2F.toLowerCase()), StandardCharsets.UTF_8);
		return decryptValue(decode,key);
	}


}
