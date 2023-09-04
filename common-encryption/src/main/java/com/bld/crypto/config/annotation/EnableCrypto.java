/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.config.annotation.EnableRestConnection.java
 */
package com.bld.crypto.config.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.bld.crypto.config.EnableCryptoConfiguration;
import com.bld.crypto.jks.config.EnableCryptoJksConfiguration;
import com.bld.crypto.pubkey.config.EnableCryptoPublicKeyConfiguration;

/**
 * The Interface EnableRestConnection.
 */
@Configuration
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Import({EnableCryptoConfiguration.class,EnableCryptoJksConfiguration.class,EnableCryptoPublicKeyConfiguration.class})
public @interface EnableCrypto {

}
