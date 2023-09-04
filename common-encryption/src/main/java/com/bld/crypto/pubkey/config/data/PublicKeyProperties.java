package com.bld.crypto.pubkey.config.data;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "com.bld.crypto.public-key",ignoreUnknownFields = true)
public final class PublicKeyProperties {

	private final Map<String,Resource> keys;
	
	

	public PublicKeyProperties(Map<String, Resource> keys) {
		super();
		this.keys = keys;
	}


	public Map<String, Resource> getKeys() {
		return keys;
	}

	

	
	
	
}
