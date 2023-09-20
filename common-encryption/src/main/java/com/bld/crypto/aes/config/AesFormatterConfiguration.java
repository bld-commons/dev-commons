package com.bld.crypto.aes.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.bld.crypto.aes.CryptoAesUtils;
import com.bld.crypto.aes.formatter.CryptoAesAnnotationFormatterFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class AesFormatterConfiguration implements WebMvcConfigurer {

	@Autowired
	private CryptoAesUtils cryptoAesUtils;
	
	@Autowired
	private ObjectMapper objMapper;

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addFormatterForFieldAnnotation(new CryptoAesAnnotationFormatterFactory(cryptoAesUtils, objMapper));
		WebMvcConfigurer.super.addFormatters(registry);
	}
	
	
	
}
