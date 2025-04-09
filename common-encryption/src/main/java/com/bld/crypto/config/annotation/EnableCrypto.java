/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.config.annotation.EnableCrypto.java
 */
package com.bld.crypto.config.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.bld.crypto.aes.config.AesConfiguration;
import com.bld.crypto.config.CryptoConfiguration;
import com.bld.crypto.jks.config.CryptoJksConfiguration;
import com.bld.crypto.pubkey.config.CryptoPublicKeyConfiguration;
import com.bld.crypto.signature.config.SignatureConfiguration;

/**
 * The Interface EnableRestConnection.
 */
@Configuration
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Import({CryptoConfiguration.class,CryptoJksConfiguration.class,CryptoPublicKeyConfiguration.class,AesConfiguration.class,SignatureConfiguration.class})
public @interface EnableCrypto {

}
