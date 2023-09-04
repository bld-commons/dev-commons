/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pubkey.config.CryptoPublicKeyConfiguration.java
 */
package com.bld.crypto.pubkey.config;

import java.io.InputStreamReader;
import java.io.Reader;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map.Entry;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.bld.crypto.exception.CryptoException;
import com.bld.crypto.pubkey.config.data.CipherPublicKeys;
import com.bld.crypto.pubkey.config.data.PublicKeyProperties;
import com.bld.crypto.type.InstanceType;


/**
 * The Class EnableCryptoPublicKeyConfiguration.
 */
@Configuration
@Conditional(PubKeyConditional.class)
//@ConditionalOnProperty(name = "com.bld.crypto.public-key.keys")
@ComponentScan(basePackages = { "com.bld.crypto.pubkey","com.bld.crypto.bean" })
public class CryptoPublicKeyConfiguration {

	/** The public key props. */
	@Autowired
	private PublicKeyProperties publicKeyProps;


	/**
	 * Pipher public keys.
	 *
	 * @return the cipher public keys
	 * @throws Exception the exception
	 */
	@Bean
	public CipherPublicKeys cipherPublicKeys() throws Exception {
		CipherPublicKeys cipherPublicKeys=new CipherPublicKeys();
		for(Entry<String, Resource> key:publicKeyProps.getKeys().entrySet()) {
			KeyFactory factory = KeyFactory.getInstance(InstanceType.RSA.name());
			Reader reader =  new InputStreamReader(key.getValue().getInputStream());
			PemReader pemReader = new PemReader(reader);
			PemObject pemObject = pemReader.readPemObject();
			byte[] content = pemObject.getContent();
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(content);
			PublicKey publicKey = factory.generatePublic(pubKeySpec);
			cipherPublicKeys.addPublicKey(key.getKey(), publicKey);
		}
		if(cipherPublicKeys.isEmpty())
			throw new CryptoException("The public keys is empty");
		return cipherPublicKeys;
	}

}