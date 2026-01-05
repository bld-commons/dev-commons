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

import org.apache.commons.collections4.MapUtils;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.bld.crypto.exception.CryptoException;
import com.bld.crypto.pubkey.CryptoMapPublicKeyUtils;
import com.bld.crypto.pubkey.config.data.CipherPublicKeys;
import com.bld.crypto.pubkey.config.data.PublicKeyProperties;
import com.bld.crypto.type.InstanceType;


/**
 * The Class EnableCryptoPublicKeyConfiguration.
 */
@Configuration
@Conditional(PubKeyConditional.class)
@ComponentScan(basePackages = { "com.bld.crypto.pubkey","com.bld.crypto.bean" })
public class CryptoPublicKeyConfiguration implements WebMvcConfigurer{

	/** The public key props. */
	@Autowired
	private PublicKeyProperties publicKeyProps;


	/**
	 * Crypto map public key utils.
	 *
	 * @return the crypto map public key utils
	 * @throws Exception the exception
	 */
	@Bean
	CryptoMapPublicKeyUtils cryptoMapPublicKeyUtils() throws Exception {
		CipherPublicKeys cipherPublicKeys=new CipherPublicKeys();
		if(MapUtils.isEmpty(publicKeyProps.getKeys()))
			throw new CryptoException("The public keys is empty");
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
		return new CryptoMapPublicKeyUtils(cipherPublicKeys);
	}



}
