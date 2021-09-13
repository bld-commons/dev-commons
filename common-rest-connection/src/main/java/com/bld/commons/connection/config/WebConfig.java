package com.bld.commons.connection.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.bld.commons.connection.interceptor.RestConnectionInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {


	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new RestConnectionInterceptor());
		WebMvcConfigurer.super.addInterceptors(registry);
	}

	
	
	
}
