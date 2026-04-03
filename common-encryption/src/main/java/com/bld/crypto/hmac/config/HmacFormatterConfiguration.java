/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.hmac.config.HmacFormatterConfiguration.java
 */
package com.bld.crypto.hmac.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.bld.crypto.hmac.CryptoHmacUtils;
import com.bld.crypto.hmac.formatter.CryptoHmacAnnotationFormatterFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Registers the {@link CryptoHmacAnnotationFormatterFactory} with Spring MVC
 * so that {@code @CryptoHmac}-annotated {@code @PathVariable} and
 * {@code @RequestParam} parameters are automatically signed and verified.
 */
@Configuration
public class HmacFormatterConfiguration implements WebMvcConfigurer {

	@Autowired
	private CryptoHmacUtils cryptoHmacUtils;

	@Autowired
	private ObjectMapper objMapper;

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addFormatterForFieldAnnotation(new CryptoHmacAnnotationFormatterFactory(cryptoHmacUtils, objMapper));
		WebMvcConfigurer.super.addFormatters(registry);
	}
}
