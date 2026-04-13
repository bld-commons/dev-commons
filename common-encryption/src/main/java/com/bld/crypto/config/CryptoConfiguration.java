/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.config.CryptoConfiguration.java
 */
package com.bld.crypto.config;

import org.springframework.context.annotation.Configuration;

import com.bld.context.annotation.config.EnableContextAnnotation;

/**
 * Root Spring configuration class for the {@code common-encryption} module.
 *
 * <p>This class enables the context-annotation processing infrastructure
 * (via {@code @EnableContextAnnotation}) that allows Jackson serializers and
 * deserializers registered by this module to be Spring-aware beans.
 */
@Configuration
@EnableContextAnnotation
public class CryptoConfiguration {


}
