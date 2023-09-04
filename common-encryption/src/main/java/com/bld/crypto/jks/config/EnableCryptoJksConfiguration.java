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

import com.bld.crypto.jks.config.data.CipherJks;
import com.bld.crypto.jks.config.properties.JksProperties;

@Configuration
@ConditionalOnProperty("com.bld.crypto.jks.file")
@ComponentScan(basePackages = {"com.bld.crypto.jks","com.bld.crypto.bean"})
public class EnableCryptoJksConfiguration {

	
	@Autowired
	private JksProperties jksProperties;
	
	@Bean
	public CipherJks cipherJks() throws Exception {
		KeyStore store = KeyStore.getInstance(this.jksProperties.getInstanceJks());
		InputStream inputStream = this.jksProperties.getFile().getInputStream();
		final char[] password = this.jksProperties.getPassword().toCharArray();
		store.load(inputStream, password);
		PrivateKey privateKey = (PrivateKey) store.getKey(this.jksProperties.getAlias(), password);
		Certificate cert = store.getCertificate(this.jksProperties.getAlias());
		PublicKey publicKey = cert.getPublicKey();
		return new CipherJks(privateKey, publicKey,this.jksProperties.getSignatureAlgorithm());
	}
	
	
}
