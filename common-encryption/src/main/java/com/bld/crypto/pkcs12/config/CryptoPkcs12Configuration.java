/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pkcs12.config.CryptoPkcs12Configuration.java
 */
package com.bld.crypto.pkcs12.config;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.bld.crypto.key.JksKey;
import com.bld.crypto.pkcs12.CryptoPkcs12Utils;
import com.bld.crypto.pkcs12.config.properties.Pkcs12KeyProperties;


/**
 * Spring {@link Configuration} that creates the {@link CryptoPkcs12Utils} bean when the
 * property {@code com.bld.crypto.pkcs12.file} is present in the application context.
 *
 * <p>Supports two PKCS12 layouts:
 * <ul>
 *   <li><strong>With certificate</strong> – the public key is extracted from the
 *       certificate associated with the alias.</li>
 *   <li><strong>Without certificate</strong> – the public key is derived from the
 *       X25519 private key using BouncyCastle. This is the case for keystores
 *       created with OpenSSL:
 *       <pre>
 *       openssl genpkey -algorithm X25519 -out private.key
 *       openssl pkcs12 -export -nocerts \
 *           -inkey private.key \
 *           -out mase-x25519.p12 \
 *           -name "mase-x25519"
 *       </pre>
 *   </li>
 * </ul>
 */
@Configuration
@ConditionalOnProperty("com.bld.crypto.pkcs12.file")
@ComponentScan(basePackages = {"com.bld.crypto.pkcs12", "com.bld.crypto.bean", "com.bld.crypto.key"})
public class CryptoPkcs12Configuration {

	/** Bound configuration properties for the PKCS12 keystore. */
	@Autowired
	private Pkcs12KeyProperties pkcs12KeyProperties;


	/**
	 * Creates the {@link CryptoPkcs12Utils} bean by loading the PKCS12 keystore and
	 * resolving the X25519 key pair.
	 *
	 * <p>If a certificate is present for the configured alias the public key is read
	 * directly from it; otherwise the public key is derived from the private key via
	 * BouncyCastle's X25519 scalar multiplication.
	 *
	 * @return a fully initialised {@link CryptoPkcs12Utils}
	 * @throws Exception if the keystore cannot be loaded or the keys cannot be extracted
	 */
	@Bean
	CryptoPkcs12Utils cryptoPkcs12Utils() throws Exception {
		KeyStore store = KeyStore.getInstance(this.pkcs12KeyProperties.getInstanceJks());
		InputStream inputStream = this.pkcs12KeyProperties.getFile().getInputStream();
		final char[] password = this.pkcs12KeyProperties.getPassword().toCharArray();
		store.load(inputStream, password);

		PrivateKey privateKey = (PrivateKey) store.getKey(this.pkcs12KeyProperties.getAlias(), password);
		PublicKey publicKey = resolvePublicKey(store, privateKey);

		return new CryptoPkcs12Utils(new JksKey(privateKey, publicKey));
	}


	/**
	 * Resolves the X25519 public key either from the keystore certificate or, when no
	 * certificate is present, by deriving it from the private key using BouncyCastle.
	 *
	 * @param store      the loaded {@link KeyStore}
	 * @param privateKey the X25519 private key extracted from the keystore
	 * @return the corresponding X25519 {@link PublicKey}
	 * @throws Exception if derivation or key reconstruction fails
	 */
	private PublicKey resolvePublicKey(KeyStore store, PrivateKey privateKey) throws Exception {
		Certificate cert = store.getCertificate(this.pkcs12KeyProperties.getAlias());
		if (cert != null)
			return cert.getPublicKey();

		// No certificate: derive the X25519 public key from the private key with BouncyCastle
		X25519PrivateKeyParameters privParams =
				(X25519PrivateKeyParameters) PrivateKeyFactory.createKey(privateKey.getEncoded());
		X25519PublicKeyParameters pubParams = privParams.generatePublicKey();
		byte[] pubKeyDer = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(pubParams).getEncoded();
		return KeyFactory.getInstance("XDH").generatePublic(new X509EncodedKeySpec(pubKeyDer));
	}

}
