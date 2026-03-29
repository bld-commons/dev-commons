/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.config.annotation.EnableRestConnection.java
 */
package com.bld.commons.utils.config.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.bld.commons.utils.config.EnableCommonUtilsConfiguration;

/**
 * Annotation that enables the Common Utils module for a Spring Boot application.
 *
 * <p>Place this annotation on a {@code @Configuration} class (or directly on the
 * Spring Boot main class) to import {@link EnableCommonUtilsConfiguration}, which
 * registers all common formatters, validators, and Spring beans provided by this
 * module.</p>
 *
 * <pre>{@code
 * @SpringBootApplication
 * @EnableCommonUtils
 * public class MyApplication { ... }
 * }</pre>
 */
@Configuration
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Import(EnableCommonUtilsConfiguration.class)
public @interface EnableCommonUtils {

}
