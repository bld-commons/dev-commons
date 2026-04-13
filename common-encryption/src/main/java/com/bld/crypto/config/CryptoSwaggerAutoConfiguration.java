/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.config.CryptoSwaggerAutoConfiguration.java
 */
package com.bld.crypto.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import com.bld.crypto.openapi.CryptoModelConverter;

import io.swagger.v3.core.converter.ModelConverter;

/**
 * Auto-configuration that registers {@link CryptoModelConverter} as a Spring bean
 * when {@code swagger-core-jakarta} is present on the classpath.
 *
 * <p>This configuration is discovered automatically via
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * and does not require any explicit {@code @Import} in the application.
 */
@AutoConfiguration
@ConditionalOnClass(ModelConverter.class)
public class CryptoSwaggerAutoConfiguration {

    @Bean
    CryptoModelConverter cryptoModelConverter() {
        return new CryptoModelConverter();
    }
}
