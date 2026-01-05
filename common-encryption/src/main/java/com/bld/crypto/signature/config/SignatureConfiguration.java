/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.jks.config.CryptoJksConfiguration.java
 */
package com.bld.crypto.signature.config;

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

import com.bld.crypto.key.JksKey;
import com.bld.crypto.signature.SignatureKeyUtils;
import com.bld.crypto.signature.config.properties.SignatureKeyProperties;


/**
 * The Class EnableCryptoJksConfiguration.
 */
@Configuration
@ConditionalOnProperty("com.bld.crypto.signature.file")
@ComponentScan(basePackages = {"com.bld.crypto.signature","com.bld.crypto.key"})
public class SignatureConfiguration{

	
	/** The jks properties. */
	@Autowired
	private SignatureKeyProperties signatureProperties;
	
	/**
	 * Cipher jks.
	 *
	 * @return the cipher jks
	 * @throws Exception the exception
	 */
	@Bean
	SignatureKeyUtils signatureKeyUtils() throws Exception {
		KeyStore store = KeyStore.getInstance(this.signatureProperties.getInstanceJks());
		InputStream inputStream = this.signatureProperties.getFile().getInputStream();
		final char[] password = this.signatureProperties.getPassword().toCharArray();
		store.load(inputStream, password);
		PrivateKey privateKey = (PrivateKey) store.getKey(this.signatureProperties.getAlias(), password);
		Certificate cert = store.getCertificate(this.signatureProperties.getAlias());
		PublicKey publicKey = cert.getPublicKey();
		return new SignatureKeyUtils(new JksKey(privateKey, publicKey), this.signatureProperties);
	}


	
	
}
