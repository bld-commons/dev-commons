package com.bld.commons.connection.config;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;


@Configuration
@ComponentScan(basePackages = {"com.bld.commons.connection"})
public class EnableRestConnectionConfiguration {

	/** The port proxy. */
	@Value("${com.dxc.connection.proxy.port:}")
	private Integer portProxy;

	/** The proxy. */
	@Value("${connection.proxy.proxy.ip:}")
	private String proxy;
	
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = null;
		if (StringUtils.isNotEmpty(this.proxy) && this.portProxy != null) {
			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(this.proxy, this.portProxy));
			requestFactory.setProxy(proxy);
			restTemplate = new RestTemplate(requestFactory);
		} else {
			restTemplate = new RestTemplate();
			restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
		}
		return restTemplate;
	}
	
	
	
}
