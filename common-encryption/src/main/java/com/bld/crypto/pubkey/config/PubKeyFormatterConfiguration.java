/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pubkey.config.PubKeyFormatterConfiguration.java
 */
package com.bld.crypto.pubkey.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.bld.crypto.pubkey.CryptoPublicKeyUtils;
import com.bld.crypto.pubkey.formatter.CryptoPubKeyAnnotationFormatterFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class PubKeyFormatterConfiguration.
 */
@Configuration
public class PubKeyFormatterConfiguration implements WebMvcConfigurer{

	/** The crypto pub key utils. */
	@Autowired
	private CryptoPublicKeyUtils cryptoPubKeyUtils;
	
	/** The obj mapper. */
	@Autowired
	private ObjectMapper objMapper;
	
	/**
	 * Adds the formatters.
	 *
	 * @param registry the registry
	 */
	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addFormatterForFieldAnnotation(new CryptoPubKeyAnnotationFormatterFactory(cryptoPubKeyUtils, objMapper));
		WebMvcConfigurer.super.addFormatters(registry);
	}
	
}
