package com.bld.context.annotation.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.SpringHandlerInstantiator;

import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;

@Configuration
@ComponentScan(basePackages = "com.bld.context.annotation")
public class EnableContextAnnotationConfiguration {

	
	public static final String CONTEXT_HANDLER_INSTANTIATOR = "contextHandlerInstantiator";
	public static final String CONTEXT_JACKSON2_OBJECT_MAPPER_BUILDER = "contextJackson2ObjectMapperBuilder";
	public static final String CONTEXT_MAPPING_JACKSON2_HTTP_MESSAGE_CONVERTER = "contextMappingJackson2HttpMessageConverter";

	@Bean(CONTEXT_HANDLER_INSTANTIATOR)
	HandlerInstantiator contextHandlerInstantiator(ApplicationContext context) {
		return new SpringHandlerInstantiator(context.getAutowireCapableBeanFactory());
	}

	@Bean(CONTEXT_JACKSON2_OBJECT_MAPPER_BUILDER)
	Jackson2ObjectMapperBuilder contextJackson2ObjectMapperBuilder(@Qualifier(CONTEXT_HANDLER_INSTANTIATOR) HandlerInstantiator handlerInstantiator,ApplicationContext applicationContext,
			List<Jackson2ObjectMapperBuilderCustomizer> customizers) {
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		builder.handlerInstantiator(handlerInstantiator);
		builder.applicationContext(applicationContext);
		for (Jackson2ObjectMapperBuilderCustomizer customizer : customizers) {
			customizer.customize(builder);
		}
		return builder;
	}

	@Bean(CONTEXT_MAPPING_JACKSON2_HTTP_MESSAGE_CONVERTER)
	MappingJackson2HttpMessageConverter contextMappingJackson2HttpMessageConverter(@Qualifier(CONTEXT_JACKSON2_OBJECT_MAPPER_BUILDER) Jackson2ObjectMapperBuilder objectMapperBuilder) {
		return new MappingJackson2HttpMessageConverter(objectMapperBuilder.build());
	}
}
