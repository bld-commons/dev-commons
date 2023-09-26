/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.aes.config.AesFormatterConfiguration.java
 */
package com.bld.crypto.aes.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.bld.crypto.aes.CryptoAesUtils;
import com.bld.crypto.aes.formatter.CryptoAesAnnotationFormatterFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class AesFormatterConfiguration.
 */
@Configuration
public class AesFormatterConfiguration implements WebMvcConfigurer {

	/** The crypto aes utils. */
	@Autowired
	private CryptoAesUtils cryptoAesUtils;
	
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
		registry.addFormatterForFieldAnnotation(new CryptoAesAnnotationFormatterFactory(cryptoAesUtils, objMapper));
		WebMvcConfigurer.super.addFormatters(registry);
	}
	
	
	
}
