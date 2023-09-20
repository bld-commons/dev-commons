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
import com.bld.context.annotation.config.EnableContextAnnotatation;



/**
 * The Class EnableCommonUtilsConfiguration.
 */
@Configuration
@EnableContextAnnotatation
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
	
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatterForFieldAnnotation(new DateTimeZoneAnnotationFormatterFactory(env));
        registry.addFormatterForFieldAnnotation(new DateFilterAnnotationFormatterFactory(env));
        registry.addFormatterForFieldAnnotation(new UpperLowerAnnotationFormatterFactory());
        registry.addFormatterForFieldAnnotation(new ClobAnnotationFormatterFactory());
        WebMvcConfigurer.super.addFormatters(registry);
    }
	
}
