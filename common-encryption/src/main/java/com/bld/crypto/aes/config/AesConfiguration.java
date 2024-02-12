/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.aes.config.AesConfiguration.java
 */
package com.bld.crypto.aes.config;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.collections4.MapUtils;
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

	/** The Constant SIZES. */
	private final static List<Integer> SIZES=Arrays.asList(16,24,32);
	
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
	CipherAesSecret cipherAesSecret() throws Exception {
		Map<String,SecretKey> map=new HashMap<>();
		for(Entry<String, Aes> key:aesProperties.getKeys().entrySet()) {
			Aes aes=key.getValue();
			byte[] keyArray =null;
			if(aes.getSalt()!=null && aes.getKeyLength()!=null) {
				KeySpec spec =  new PBEKeySpec(aes.getPassword().toCharArray(), aes.getSalt().getBytes(StandardCharsets.UTF_8), INTERATION_COUNT, aes.getKeyLength());
				SecretKeyFactory f = SecretKeyFactory.getInstance(PBKDF2_WITH_HMAC_SHA1);
				keyArray = f.generateSecret(spec).getEncoded();
				map.put(key.getKey(),  new SecretKeySpec(keyArray, InstanceType.AES.name()));
			}else {
				if(!SIZES.contains(aes.getPassword().length()))
					throw new CryptoException("TThe password must be 16, 24, or 32 bytes long");
				map.put(key.getKey(),  new SecretKeySpec(aes.getPassword().getBytes(StandardCharsets.UTF_8), InstanceType.AES.name()));
			}
				
					
			
		}
		if(MapUtils.isEmpty(map))
			throw new CryptoException("The secret keys is empty");
		return new CipherAesSecret(map);
	}
}
