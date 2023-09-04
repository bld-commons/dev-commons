/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.aes.config.AesConfiguration.java
 */
package com.bld.crypto.aes.config;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Map.Entry;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.bld.crypto.aes.config.data.Aes;
import com.bld.crypto.aes.config.data.AesProperties;
import com.bld.crypto.aes.config.data.CipherAesSecret;
import com.bld.crypto.exception.CryptoException;
import com.bld.crypto.type.InstanceType;

/**
 * The Class AesConfiguration.
 */
@Configuration
@Conditional(AesConditional.class)
@ComponentScan(basePackages = { "com.bld.crypto.aes","com.bld.crypto.bean" })
public class AesConfiguration {

	/** The Constant INTERATION_COUNT. */
	private static final int INTERATION_COUNT=65536;
	
	/** The Constant PBKDF2_WITH_HMAC_SHA1. */
	private static final String PBKDF2_WITH_HMAC_SHA1 = "PBKDF2WithHmacSHA1";
	
	/** The aes properties. */
	@Autowired
	private AesProperties aesProperties;
	
	/**
	 * Pipher public keys.
	 *
	 * @return the cipher public keys
	 * @throws Exception the exception
	 */
	@Bean
	public CipherAesSecret cipherAesSecret() throws Exception {
		CipherAesSecret cipherAesSecret=new CipherAesSecret();
		for(Entry<String, Aes> key:aesProperties.getKeys().entrySet()) {
			Aes aes=key.getValue();
			KeySpec spec =  new PBEKeySpec(aes.getPassword().toCharArray(), aes.getSalt().getBytes(StandardCharsets.UTF_8), INTERATION_COUNT, aes.getKeyLength());
			SecretKeyFactory f = SecretKeyFactory.getInstance(PBKDF2_WITH_HMAC_SHA1);
			byte[] keyArray = f.generateSecret(spec).getEncoded();
			cipherAesSecret.addSecretKey(key.getKey(),  new SecretKeySpec(keyArray, InstanceType.AES.name()));
		}
		if(cipherAesSecret.isEmpty())
			throw new CryptoException("The secret keys is empty");
		return cipherAesSecret;
	}
}
