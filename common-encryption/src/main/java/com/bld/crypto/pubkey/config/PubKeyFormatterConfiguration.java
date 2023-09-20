package com.bld.crypto.pubkey.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.bld.crypto.pubkey.CryptoPublicKeyUtils;
import com.bld.crypto.pubkey.formatter.CryptoPubKeyAnnotationFormatterFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class PubKeyFormatterConfiguration implements WebMvcConfigurer{

	@Autowired
	private CryptoPublicKeyUtils cryptoPubKeyUtils;
	
	@Autowired
	private ObjectMapper objMapper;
	
	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addFormatterForFieldAnnotation(new CryptoPubKeyAnnotationFormatterFactory(cryptoPubKeyUtils, objMapper));
		WebMvcConfigurer.super.addFormatters(registry);
	}
	
}
