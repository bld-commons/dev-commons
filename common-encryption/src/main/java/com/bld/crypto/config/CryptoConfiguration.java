/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.config.CryptoConfiguration.java
 */
package com.bld.crypto.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bld.context.annotation.config.EnableContextAnnotation;
import com.bld.crypto.introspector.CryptoTypeUseAnnotationIntrospector;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;

/**
 * Root Spring configuration class for the {@code common-encryption} module.
 *
 * <p>This class enables the context-annotation processing infrastructure
 * (via {@code @EnableContextAnnotation}) that allows Jackson serializers and
 * deserializers registered by this module to be Spring-aware beans.
 *
 * <p>It also registers a {@link Jackson2ObjectMapperBuilderCustomizer} that appends
 * {@link CryptoTypeUseAnnotationIntrospector} as a secondary introspector so that
 * {@code @Crypto*} annotations placed on type arguments (e.g.
 * {@code List<@CryptoPkcs12("id") Integer>}) are recognised by Jackson.
 */
@Configuration
@EnableContextAnnotation
public class CryptoConfiguration {

	/**
	 * Appends {@link CryptoTypeUseAnnotationIntrospector} as secondary introspector on
	 * every {@code ObjectMapper} built from a {@link org.springframework.http.converter.json.Jackson2ObjectMapperBuilder}
	 * that receives this customizer, including the primary Spring Boot mapper and any
	 * secondary mappers registered by {@link com.bld.context.annotation.config.EnableContextAnnotationConfiguration}.
	 */
	@Bean
	Jackson2ObjectMapperBuilderCustomizer cryptoTypeUseAnnotationIntrospectorCustomizer() {
		return builder -> builder.postConfigurer(mapper -> {
			AnnotationIntrospector existing = mapper.getSerializationConfig().getAnnotationIntrospector();
			mapper.setAnnotationIntrospector(AnnotationIntrospectorPair.pair(existing, new CryptoTypeUseAnnotationIntrospector()));
		});
	}
}
