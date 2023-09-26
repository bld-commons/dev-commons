/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.jks.config.JksFormatterConfiguration.java
 */
package com.bld.crypto.jks.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.bld.crypto.jks.CryptoJksUtils;
import com.bld.crypto.jks.formatter.CryptoJksAnnotationFormatterFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class JksFormatterConfiguration.
 */
@Configuration
public class JksFormatterConfiguration implements WebMvcConfigurer{

	/** The crypto jks utils. */
	@Autowired
	private CryptoJksUtils cryptoJksUtils;
	
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
		registry.addFormatterForFieldAnnotation(new CryptoJksAnnotationFormatterFactory(cryptoJksUtils, objMapper));
		WebMvcConfigurer.super.addFormatters(registry);
	}
	
	
	
}
