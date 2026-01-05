/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.jks.config.CryptoJksConfiguration.java
 */
package com.bld.crypto.jks.config;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.bld.crypto.jks.CryptoJksUtils;
import com.bld.crypto.jks.config.properties.JksKeyProperties;
import com.bld.crypto.key.JksKey;


/**
 * The Class EnableCryptoJksConfiguration.
 */
@Configuration
@ConditionalOnProperty("com.bld.crypto.jks.file")
@ComponentScan(basePackages = {"com.bld.crypto.jks","com.bld.crypto.bean","com.bld.crypto.key"})
public class CryptoJksConfiguration{

	
	/** The jks properties. */
	@Autowired
	private JksKeyProperties jksKeyProperties;
	
	/**
	 * Cipher jks.
	 *
	 * @return the cipher jks
	 * @throws Exception the exception
	 */
	@Bean
	CryptoJksUtils cryptoJksUtils() throws Exception {
		KeyStore store = KeyStore.getInstance(this.jksKeyProperties.getInstanceJks());
		InputStream inputStream = this.jksKeyProperties.getFile().getInputStream();
		final char[] password = this.jksKeyProperties.getPassword().toCharArray();
		store.load(inputStream, password);
		PrivateKey privateKey = (PrivateKey) store.getKey(this.jksKeyProperties.getAlias(), password);
		Certificate cert = store.getCertificate(this.jksKeyProperties.getAlias());
		PublicKey publicKey = cert.getPublicKey();
		return new CryptoJksUtils(new JksKey(privateKey, publicKey));
	}


	
	
}
