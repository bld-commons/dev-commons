/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.client.AbstractClientConnection.java
 */
package com.bld.commons.connection.client;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import jakarta.annotation.PostConstruct;

/**
 * Abstract base class for REST and SOAP client connections.
 * Handles proxy configuration and {@link RestTemplate} construction,
 * shared by both {@link RestClientConnection} and {@link SoapClientConnection} implementations.
 */
public abstract class AbstractClientConnection {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(AbstractClientConnection.class);

	/** The port proxy. */
	@Value("${com.bld.connection.proxy.port:}")
	private Integer portProxy;

	/** The proxy. */
	@Value("${com.bld.connection.proxy.ip:}")
	private String proxy;

	/**
	 * Message converters built once at startup and reused in every RestTemplate instance.
	 * Includes default Spring converters plus YAML and XML/SOAP string converters.
	 */
	private List<HttpMessageConverter<?>> messageConverters;

	/**
	 * Proxy instance built once at startup from {@code com.bld.connection.proxy.ip}
	 * and {@code com.bld.connection.proxy.port}. {@code null} when no proxy is configured.
	 */
	private Proxy configuredProxy;

	/**
	 * Initialises the message converters and the proxy after {@code @Value} injection.
	 * Called automatically by Spring after the bean is constructed.
	 */
	@PostConstruct
	public void init() {
		if (StringUtils.isNotEmpty(this.proxy) && this.portProxy != null) {
			logger.info("ClientConnection - proxy enabled: {}:{}", this.proxy, this.portProxy);
			this.configuredProxy = new Proxy(Type.HTTP, new InetSocketAddress(this.proxy, this.portProxy));
		} else {
			logger.debug("ClientConnection - no proxy configured");
		}
		this.messageConverters = buildMessageConverters();
	}

	/**
	 * Creates a new {@link RestTemplate} for a single call.
	 * Applies the optional timeout and the proxy configured at startup.
	 * Message converters initialised at startup are reused.
	 *
	 * @param timeout the read/connect timeout in milliseconds, or {@code null} for no timeout
	 * @return a new RestTemplate instance ready for use
	 */
	protected RestTemplate buildRestTemplate(Integer timeout) {
		SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
		if (timeout != null) {
			rf.setReadTimeout(timeout);
			rf.setConnectTimeout(timeout);
		}
		if (this.configuredProxy != null) {
			rf.setProxy(this.configuredProxy);
		}
		RestTemplate rt = new RestTemplate(rf);
		rt.setMessageConverters(new ArrayList<>(this.messageConverters));
		rt.setErrorHandler(new DefaultResponseErrorHandler());
		return rt;
	}

	/**
	 * Builds the list of message converters once at startup.
	 * Starts from the default Spring converters and adds YAML and XML/SOAP string converters.
	 *
	 * @return the list of message converters
	 */
	private List<HttpMessageConverter<?>> buildMessageConverters() {
		List<HttpMessageConverter<?>> converters = new ArrayList<>(new RestTemplate().getMessageConverters());
		try {
			MappingJackson2HttpMessageConverter yamlConverter = new MappingJackson2HttpMessageConverter(new YAMLMapper());
			yamlConverter.setSupportedMediaTypes(List.of(
					new MediaType("application", "yaml"),
					new MediaType("application", "x-yaml"),
					new MediaType("text", "yaml")));
			converters.add(yamlConverter);
		} catch (Throwable e) {
			logger.warn("ClientConnection - YAML message converter not configured: the application will start normally but YAML media types (application/yaml, application/x-yaml, text/yaml) will not be supported. Cause: {}", e.getMessage());
		}
		try {
			StringHttpMessageConverter xmlStringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
			xmlStringConverter.setSupportedMediaTypes(List.of(
					new MediaType("text", "xml"),
					new MediaType("application", "xml"),
					new MediaType("application", "soap+xml")));
			converters.add(xmlStringConverter);
		} catch (Throwable e) {
			logger.warn("ClientConnection - XML/SOAP message converter not configured: the application will start normally but XML media types (text/xml, application/xml, application/soap+xml) will not be supported. Cause: {}", e.getMessage());
		}
		return converters;
	}

}
