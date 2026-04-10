package com.bld.context.annotation.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.SpringHandlerInstantiator;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;

/**
 * Spring {@link Configuration} that registers the beans required to enable dependency
 * injection inside custom Jackson serializers and deserializers.
 *
 * <p>This configuration is imported automatically when the {@link EnableContextAnnotation}
 * annotation is placed on a {@code @Configuration} class. It creates:
 * <ul>
 *   <li>A {@link HandlerInstantiator} ({@value #CONTEXT_HANDLER_INSTANTIATOR}) backed by
 *       Spring's {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory},
 *       so custom handlers receive {@code @Autowired} fields.</li>
 *   <li>A {@link Jackson2ObjectMapperBuilderCustomizer} that applies the
 *       {@link HandlerInstantiator} to the <em>primary</em> Spring Boot
 *       {@link Jackson2ObjectMapperBuilder}, ensuring the default JSON converter also
 *       supports Spring-injected serializers/deserializers.</li>
 *   <li>A secondary {@link Jackson2ObjectMapperBuilder}
 *       ({@value #CONTEXT_JACKSON2_OBJECT_MAPPER_BUILDER}) and its
 *       {@link MappingJackson2HttpMessageConverter}
 *       ({@value #CONTEXT_MAPPING_JACKSON2_HTTP_MESSAGE_CONVERTER}) for JSON.</li>
 *   <li>A YAML {@link MappingJackson2HttpMessageConverter}
 *       ({@value #CONTEXT_YAML_MAPPING_JACKSON2_HTTP_MESSAGE_CONVERTER}), registered only
 *       when {@code jackson-dataformat-yaml} is on the classpath, so that
 *       {@code Accept: application/yaml} responses also go through the
 *       {@link HandlerInstantiator}.</li>
 * </ul>
 *
 * @see EnableContextAnnotation
 * @see SpringHandlerInstantiator
 */
@Configuration
@ComponentScan(basePackages = "com.bld.context.annotation")
public class EnableContextAnnotationConfiguration {

	/** Bean name for the {@link HandlerInstantiator} that integrates Spring DI with Jackson. */
	public static final String CONTEXT_HANDLER_INSTANTIATOR = "contextHandlerInstantiator";

	/** Bean name for the {@link Jackson2ObjectMapperBuilder} configured with Spring DI support. */
	public static final String CONTEXT_JACKSON2_OBJECT_MAPPER_BUILDER = "contextJackson2ObjectMapperBuilder";

	/** Bean name for the JSON {@link MappingJackson2HttpMessageConverter} built from the configured mapper builder. */
	public static final String CONTEXT_MAPPING_JACKSON2_HTTP_MESSAGE_CONVERTER = "contextMappingJackson2HttpMessageConverter";

	/** Bean name for the YAML {@link MappingJackson2HttpMessageConverter} (active only when {@code jackson-dataformat-yaml} is present). */
	public static final String CONTEXT_YAML_MAPPING_JACKSON2_HTTP_MESSAGE_CONVERTER = "contextYamlMappingJackson2HttpMessageConverter";

	/**
	 * Creates a {@link HandlerInstantiator} that uses Spring's
	 * {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory} to
	 * instantiate Jackson handlers, enabling {@code @Autowired} injection inside custom
	 * serializers and deserializers.
	 *
	 * @param context the Spring {@link ApplicationContext}
	 * @return a {@link SpringHandlerInstantiator} wired to the application context
	 */
	@Bean(CONTEXT_HANDLER_INSTANTIATOR)
	HandlerInstantiator contextHandlerInstantiator(ApplicationContext context) {
		return new SpringHandlerInstantiator(context.getAutowireCapableBeanFactory());
	}

	/**
	 * Registers a {@link Jackson2ObjectMapperBuilderCustomizer} that applies the
	 * Spring-aware {@link HandlerInstantiator} to the <em>primary</em>
	 * {@link Jackson2ObjectMapperBuilder} used by Spring Boot's auto-configuration.
	 *
	 * <p>Without this customizer only the secondary converters registered by this
	 * configuration would carry the {@link HandlerInstantiator}, while the default
	 * {@link MappingJackson2HttpMessageConverter} (built from the primary
	 * {@code ObjectMapper}) would not — leaving {@code @Autowired} fields in custom
	 * serializers/deserializers as {@code null}.
	 *
	 * @param handlerInstantiator the Spring-aware handler instantiator bean
	 * @return a customizer that sets the handler instantiator on the primary builder
	 */
	@Bean
	Jackson2ObjectMapperBuilderCustomizer handlerInstantiatorCustomizer(@Qualifier(CONTEXT_HANDLER_INSTANTIATOR) HandlerInstantiator handlerInstantiator) {
		return builder -> builder.handlerInstantiator(handlerInstantiator);
	}

	/**
	 * Creates a {@link Jackson2ObjectMapperBuilder} pre-configured with the Spring-aware
	 * {@link HandlerInstantiator} and all registered
	 * {@link Jackson2ObjectMapperBuilderCustomizer} beans.
	 *
	 * @param handlerInstantiator the Spring-aware handler instantiator bean
	 * @param applicationContext  the Spring {@link ApplicationContext}
	 * @param customizers         all {@link Jackson2ObjectMapperBuilderCustomizer} beans
	 * @return a fully configured {@link Jackson2ObjectMapperBuilder}
	 */
	@Bean(CONTEXT_JACKSON2_OBJECT_MAPPER_BUILDER)
	Jackson2ObjectMapperBuilder contextJackson2ObjectMapperBuilder(@Qualifier(CONTEXT_HANDLER_INSTANTIATOR) HandlerInstantiator handlerInstantiator, ApplicationContext applicationContext,
			List<Jackson2ObjectMapperBuilderCustomizer> customizers) {
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		builder.handlerInstantiator(handlerInstantiator);
		builder.applicationContext(applicationContext);
		for (Jackson2ObjectMapperBuilderCustomizer customizer : customizers) {
			customizer.customize(builder);
		}
		return builder;
	}

	/**
	 * Creates a JSON {@link MappingJackson2HttpMessageConverter} that uses the
	 * Spring-DI-aware {@link Jackson2ObjectMapperBuilder}.
	 *
	 * @param objectMapperBuilder the {@link Jackson2ObjectMapperBuilder} bean
	 * @return a {@link MappingJackson2HttpMessageConverter} built from the provided builder
	 */
	@Bean(CONTEXT_MAPPING_JACKSON2_HTTP_MESSAGE_CONVERTER)
	MappingJackson2HttpMessageConverter contextMappingJackson2HttpMessageConverter(@Qualifier(CONTEXT_JACKSON2_OBJECT_MAPPER_BUILDER) Jackson2ObjectMapperBuilder objectMapperBuilder) {
		return new MappingJackson2HttpMessageConverter(objectMapperBuilder.build());
	}

	/**
	 * Inner configuration that registers a YAML-capable
	 * {@link MappingJackson2HttpMessageConverter} with {@link SpringHandlerInstantiator},
	 * active only when {@code jackson-dataformat-yaml} is on the classpath.
	 *
	 * <p>Without this converter, requests with {@code Accept: application/yaml} would be
	 * served by a YAML converter that has no {@link HandlerInstantiator}, leaving
	 * {@code @Autowired} fields in custom serializers as {@code null}.
	 */
	@Configuration
	@ConditionalOnClass(YAMLFactory.class)
	static class YamlConverterConfiguration {

		/**
		 * Creates a YAML {@link MappingJackson2HttpMessageConverter} backed by a
		 * {@link Jackson2ObjectMapperBuilder} that carries the {@link SpringHandlerInstantiator}.
		 *
		 * <p>Supports the {@code application/yaml}, {@code application/x-yaml} and
		 * {@code text/yaml} media types.
		 *
		 * @param handlerInstantiator the Spring-aware handler instantiator bean
		 * @param applicationContext  the Spring {@link ApplicationContext}
		 * @param customizers         all {@link Jackson2ObjectMapperBuilderCustomizer} beans
		 * @return a YAML {@link MappingJackson2HttpMessageConverter}
		 */
		@Bean(CONTEXT_YAML_MAPPING_JACKSON2_HTTP_MESSAGE_CONVERTER)
		MappingJackson2HttpMessageConverter contextYamlMappingJackson2HttpMessageConverter(
				@Qualifier(CONTEXT_HANDLER_INSTANTIATOR) HandlerInstantiator handlerInstantiator,
				ApplicationContext applicationContext,
				List<Jackson2ObjectMapperBuilderCustomizer> customizers) {
			Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
			builder.factory(new YAMLFactory());
			builder.handlerInstantiator(handlerInstantiator);
			builder.applicationContext(applicationContext);
			for (Jackson2ObjectMapperBuilderCustomizer customizer : customizers) {
				customizer.customize(builder);
			}
			MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(builder.build());
			converter.setSupportedMediaTypes(List.of(
				new MediaType("application", "yaml"),
				new MediaType("application", "x-yaml"),
				new MediaType("text", "yaml")
			));
			return converter;
		}
	}
}
