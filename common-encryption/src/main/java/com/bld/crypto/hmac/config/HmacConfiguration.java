/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.hmac.config.HmacConfiguration.java
 */
package com.bld.crypto.hmac.config;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.bld.crypto.hmac.config.data.CryptoHmacSecret;
import com.bld.crypto.hmac.config.data.HmacProperties;

/**
 * Spring configuration for the HMAC module.
 *
 * <p>Active only when {@code com.bld.crypto.hmac.secret} is configured.
 * Derives a {@link SecretKey} via PBKDF2 if {@code salt} and {@code keyLength}
 * are provided, otherwise uses the raw secret bytes directly.
 */
@Configuration
@Conditional(HmacConditional.class)
@ComponentScan(basePackages = { "com.bld.crypto.hmac", "com.bld.crypto.bean" })
public class HmacConfiguration {

	private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";

	private static final int ITERATION_COUNT = 65536;

	@Autowired
	private HmacProperties hmacProperties;

	@Bean
	CryptoHmacSecret cryptoHmacSecret() throws Exception {
		String algorithm = hmacProperties.getAlgorithm();
		SecretKey secretKey;
		if (hmacProperties.getSalt() != null && hmacProperties.getKeyLength() != null) {
			KeySpec spec = new PBEKeySpec(
					hmacProperties.getSecret().toCharArray(),
					hmacProperties.getSalt().getBytes(StandardCharsets.UTF_8),
					ITERATION_COUNT,
					hmacProperties.getKeyLength());
			SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
			byte[] keyBytes = factory.generateSecret(spec).getEncoded();
			secretKey = new SecretKeySpec(keyBytes, algorithm);
		} else {
			secretKey = new SecretKeySpec(
					hmacProperties.getSecret().getBytes(StandardCharsets.UTF_8),
					algorithm);
		}
		return new CryptoHmacSecret(secretKey, algorithm);
	}
}
