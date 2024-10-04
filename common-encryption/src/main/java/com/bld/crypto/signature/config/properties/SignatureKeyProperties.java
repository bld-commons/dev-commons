/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.jks.config.properties.JksProperties.java
 */
package com.bld.crypto.signature.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.bld.crypto.key.KeyProperties;


/**
 * The Class JksProperties.
 */
@Configuration
@ConfigurationProperties(prefix = "com.bld.crypto.signature")
public class SignatureKeyProperties extends KeyProperties {

	private String instanceSignature;

	public String getInstanceSignature() {
		return instanceSignature;
	}

	public void setInstanceSignature(String instanceSignature) {
		this.instanceSignature = instanceSignature;
	}
	
	
	
	
}
