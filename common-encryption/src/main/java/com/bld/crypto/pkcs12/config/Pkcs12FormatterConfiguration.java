/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pkcs12.config.Pkcs12FormatterConfiguration.java
 */
package com.bld.crypto.pkcs12.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.bld.crypto.pkcs12.CryptoPkcs12Utils;
import com.bld.crypto.pkcs12.formatter.CryptoPkcs12AnnotationFormatterFactory;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Spring MVC {@link Configuration} that registers the {@link CryptoPkcs12AnnotationFormatterFactory}
 * so that {@link com.bld.crypto.pkcs12.annotation.CryptoPkcs12 @CryptoPkcs12}-annotated
 * request parameters are transparently encrypted/decrypted during Spring MVC binding.
 */
@Configuration
public class Pkcs12FormatterConfiguration implements WebMvcConfigurer {

	/** The PKCS12 encryption utility bean. */
	@Autowired
	private CryptoPkcs12Utils cryptoPkcs12Utils;

	/** Jackson {@link ObjectMapper} used by the formatter for POJO conversion. */
	@Autowired
	private ObjectMapper objMapper;


	/**
	 * Registers the {@link CryptoPkcs12AnnotationFormatterFactory} with the MVC formatter registry.
	 *
	 * @param registry the Spring {@link FormatterRegistry}
	 */
	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addFormatterForFieldAnnotation(new CryptoPkcs12AnnotationFormatterFactory(cryptoPkcs12Utils, objMapper));
		WebMvcConfigurer.super.addFormatters(registry);
	}

}
