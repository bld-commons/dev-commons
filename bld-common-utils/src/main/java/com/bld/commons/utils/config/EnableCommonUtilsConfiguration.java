/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.config.EnableRestConnectionConfiguration.java
 */
package com.bld.commons.utils.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.bld.commons.utils.formatter.ClobAnnotationFormatterFactory;
import com.bld.commons.utils.formatter.DateFilterAnnotationFormatterFactory;
import com.bld.commons.utils.formatter.DateTimeZoneAnnotationFormatterFactory;
import com.bld.commons.utils.formatter.UpperLowerAnnotationFormatterFactory;
import com.bld.context.annotation.config.EnableContextAnnotation;



/**
 * Spring MVC configuration class imported by {@link com.bld.commons.utils.config.annotation.EnableCommonUtils}.
 *
 * <p>Registers the annotation-based Spring {@link org.springframework.format.Formatter}s
 * provided by this module so that they participate in data binding for form/request
 * parameters:
 * <ul>
 *   <li>{@link com.bld.commons.utils.formatter.DateTimeZoneAnnotationFormatterFactory} &mdash;
 *       handles {@link com.bld.commons.utils.json.annotations.DateTimeZone} on {@code Date}/{@code Calendar} fields</li>
 *   <li>{@link com.bld.commons.utils.formatter.DateFilterAnnotationFormatterFactory} &mdash;
 *       handles {@link com.bld.commons.utils.json.annotations.DateChange} on {@code Date}/{@code Calendar} fields</li>
 *   <li>{@link com.bld.commons.utils.formatter.UpperLowerAnnotationFormatterFactory} &mdash;
 *       handles {@link com.bld.commons.utils.json.annotations.UpperLowerCase} on {@code String} fields</li>
 *   <li>{@link com.bld.commons.utils.formatter.ClobAnnotationFormatterFactory} &mdash;
 *       handles {@link com.bld.commons.utils.json.annotations.TextClob} on {@code Clob} fields</li>
 * </ul>
 * </p>
 *
 * @author Francesco Baldi
 */
@Configuration
@EnableContextAnnotation
@ComponentScan(basePackages = {"com.bld.commons.utils"})
public class EnableCommonUtilsConfiguration implements WebMvcConfigurer
{

	@Autowired
	private AbstractEnvironment env;
	
	
	
	
//	@Autowired
//	private DateMethodDeserialize dateMethdoDeserialize;
//
//	@Override
//	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
//		resolvers.add(dateMethdoDeserialize);
//		WebMvcConfigurer.super.addArgumentResolvers(resolvers);
//	}
//	
	
    /**
     * Registers all annotation-driven formatters provided by this module into the
     * Spring {@link FormatterRegistry}.
     *
     * @param registry the formatter registry to add formatters to
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatterForFieldAnnotation(new DateTimeZoneAnnotationFormatterFactory(env));
        registry.addFormatterForFieldAnnotation(new DateFilterAnnotationFormatterFactory(env));
        registry.addFormatterForFieldAnnotation(new UpperLowerAnnotationFormatterFactory());
        registry.addFormatterForFieldAnnotation(new ClobAnnotationFormatterFactory());
        WebMvcConfigurer.super.addFormatters(registry);
    }
	
}
