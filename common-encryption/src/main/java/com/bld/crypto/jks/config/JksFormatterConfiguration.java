package com.bld.crypto.jks.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.bld.crypto.jks.CryptoJksUtils;
import com.bld.crypto.jks.formatter.CryptoJksAnnotationFormatterFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class JksFormatterConfiguration implements WebMvcConfigurer{

	/** The crypto jks utils. */
	@Autowired
	private CryptoJksUtils cryptoJksUtils;
	@Autowired
	private ObjectMapper objMapper;
	
	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addFormatterForFieldAnnotation(new CryptoJksAnnotationFormatterFactory(cryptoJksUtils, objMapper));
		WebMvcConfigurer.super.addFormatters(registry);
	}
	
	
	
}
