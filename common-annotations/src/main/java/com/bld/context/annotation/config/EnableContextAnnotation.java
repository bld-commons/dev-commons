/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.context.annotation.config.EnableContextAnnotation.java
 */
package com.bld.context.annotation.config;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Annotation that enables Spring dependency injection support inside custom Jackson
 * serializers and deserializers.
 *
 * <p>Place this annotation on any {@code @Configuration} class to activate the
 * {@link EnableContextAnnotationConfiguration} auto-configuration, which registers a
 * {@link com.fasterxml.jackson.databind.cfg.HandlerInstantiator} backed by
 * Spring's {@link org.springframework.http.converter.json.SpringHandlerInstantiator}.
 * This allows custom Jackson handlers (serializers, deserializers, type handlers, etc.)
 * to receive Spring-managed beans via {@code @Autowired} or constructor injection.
 *
 * <p>Usage example:
 * <pre>{@code
 * @Configuration
 * @EnableContextAnnotation
 * public class AppConfig {
 *     // custom serializers/deserializers can now @Autowired Spring beans
 * }
 * }</pre>
 *
 * @see EnableContextAnnotationConfiguration
 */
@Configuration
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Import(EnableContextAnnotationConfiguration.class)
public @interface EnableContextAnnotation {

}
