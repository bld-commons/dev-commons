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

/**
 * Spring {@link Configuration} that registers the beans required to enable dependency
 * injection inside custom Jackson serializers and deserializers.
 *
 * <p>This configuration is imported automatically when the {@link EnableContextAnnotation}
 * annotation is placed on a {@code @Configuration} class. It creates three beans:
 * <ol>
 *   <li>A {@link HandlerInstantiator} ({@value #CONTEXT_HANDLER_INSTANTIATOR}) that
 *       delegates handler creation to Spring's
 *       {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory},
 *       allowing custom handlers to receive {@code @Autowired} fields.</li>
 *   <li>A {@link Jackson2ObjectMapperBuilder}
 *       ({@value #CONTEXT_JACKSON2_OBJECT_MAPPER_BUILDER}) pre-configured with the
 *       handler instantiator and any registered
 *       {@link Jackson2ObjectMapperBuilderCustomizer} beans.</li>
 *   <li>A {@link MappingJackson2HttpMessageConverter}
 *       ({@value #CONTEXT_MAPPING_JACKSON2_HTTP_MESSAGE_CONVERTER}) built from the
 *       customized mapper builder, which can be used by Spring MVC / WebFlux.</li>
 * </ol>
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

	/** Bean name for the {@link MappingJackson2HttpMessageConverter} built from the configured mapper builder. */
	public static final String CONTEXT_MAPPING_JACKSON2_HTTP_MESSAGE_CONVERTER = "contextMappingJackson2HttpMessageConverter";

	/**
	 * Creates a {@link HandlerInstantiator} that uses Spring's
	 * {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory} to
	 * instantiate Jackson handlers, enabling {@code @Autowired} injection inside custom
	 * serializers and deserializers.
	 *
	 * @param context the Spring {@link ApplicationContext} from which the
	 *                {@code AutowireCapableBeanFactory} is obtained
	 * @return a {@link SpringHandlerInstantiator} wired to the application context
	 */
	@Bean(CONTEXT_HANDLER_INSTANTIATOR)
	HandlerInstantiator contextHandlerInstantiator(ApplicationContext context) {
		return new SpringHandlerInstantiator(context.getAutowireCapableBeanFactory());
	}

	/**
	 * Creates a {@link Jackson2ObjectMapperBuilder} pre-configured with the Spring-aware
	 * {@link HandlerInstantiator} and all registered
	 * {@link Jackson2ObjectMapperBuilderCustomizer} beans.
	 *
	 * <p>Each customizer is applied in list order so that application-level
	 * customizations (date format, modules, visibility, etc.) are respected alongside
	 * the Spring DI integration.
	 *
	 * @param handlerInstantiator the Spring-aware handler instantiator bean
	 * @param applicationContext  the Spring {@link ApplicationContext}
	 * @param customizers         all {@link Jackson2ObjectMapperBuilderCustomizer} beans
	 *                            registered in the application context
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
	 * Creates a {@link MappingJackson2HttpMessageConverter} that uses the
	 * Spring-DI-aware {@link Jackson2ObjectMapperBuilder}.
	 *
	 * <p>This converter can be registered as an HTTP message converter in Spring MVC or
	 * Spring WebFlux to ensure that JSON serialization and deserialization always goes
	 * through the configured {@link HandlerInstantiator}.
	 *
	 * @param objectMapperBuilder the {@link Jackson2ObjectMapperBuilder} bean
	 *                            ({@value #CONTEXT_JACKSON2_OBJECT_MAPPER_BUILDER})
	 * @return a {@link MappingJackson2HttpMessageConverter} built from the provided builder
	 */
	@Bean(CONTEXT_MAPPING_JACKSON2_HTTP_MESSAGE_CONVERTER)
	MappingJackson2HttpMessageConverter contextMappingJackson2HttpMessageConverter(@Qualifier(CONTEXT_JACKSON2_OBJECT_MAPPER_BUILDER) Jackson2ObjectMapperBuilder objectMapperBuilder) {
		return new MappingJackson2HttpMessageConverter(objectMapperBuilder.build());
	}
}
