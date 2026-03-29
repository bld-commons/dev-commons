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
 * Meta-annotation that activates the {@code common-encryption} module.
 *
 * <p>Place {@code @EnableCrypto} on any Spring {@code @Configuration} class to import
 * all encryption sub-configurations:
 * <ul>
 *   <li>{@link CryptoConfiguration} – base configuration (enables context annotations)</li>
 *   <li>{@link CryptoJksConfiguration} – RSA/JKS bidirectional encryption (conditional on property)</li>
 *   <li>{@link CryptoPublicKeyConfiguration} – public-key-only encryption (conditional on property)</li>
 *   <li>{@link AesConfiguration} – AES symmetric encryption (conditional on property)</li>
 *   <li>{@link SignatureConfiguration} – digital signature (conditional on property)</li>
 * </ul>
 *
 * <p>Example:
 * <pre>{@code
 * @Configuration
 * @EnableCrypto
 * public class AppConfig { }
 * }</pre>
 */
@Configuration
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Import({CryptoConfiguration.class,CryptoJksConfiguration.class,CryptoPublicKeyConfiguration.class,AesConfiguration.class,SignatureConfiguration.class})
public @interface EnableCrypto {

}
