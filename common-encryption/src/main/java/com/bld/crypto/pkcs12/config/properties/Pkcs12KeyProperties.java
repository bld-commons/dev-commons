/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pkcs12.config.properties.Pkcs12KeyProperties.java
 */
package com.bld.crypto.pkcs12.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.bld.crypto.key.KeyProperties;


/**
 * Configuration properties for the PKCS12 hybrid-encryption keystore.
 *
 * <p>Bind the following keys in {@code application.properties} / {@code application.yml}:
 * <pre>
 * com.bld.crypto.pkcs12.file=classpath:keystore.p12
 * com.bld.crypto.pkcs12.password=changeit
 * com.bld.crypto.pkcs12.alias=myXdhKey
 * com.bld.crypto.pkcs12.instance-jks=PKCS12
 * </pre>
 *
 * <p><strong>Note:</strong> the keystore must contain an <em>XDH / X25519</em> key pair
 * with an associated certificate (self-signed is sufficient).  The private key is used
 * for decryption (ECDH recipient side) and the public key extracted from the certificate
 * is used for encryption (ECDH sender side).
 */
@Configuration
@ConfigurationProperties(prefix = "com.bld.crypto.pkcs12")
public class Pkcs12KeyProperties extends KeyProperties {

}
