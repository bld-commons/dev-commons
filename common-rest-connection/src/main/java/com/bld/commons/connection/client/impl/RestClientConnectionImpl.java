/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.client.impl.RestClientConnectionImpl.java
 */
package com.bld.commons.connection.client.impl;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.bld.commons.connection.client.RestClientConnection;
import com.bld.commons.connection.model.MapRequest;
import com.bld.commons.connection.model.ObjectRequest;
import com.bld.commons.connection.model.RestBasicRequest;
import com.bld.commons.connection.model.SoapRequest;
import com.bld.commons.connection.utils.RestConnectionMapper;
import com.bld.commons.connection.utils.SoapXmlBuilder;
import com.bld.commons.connection.utils.XmlNodeConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import jakarta.annotation.PostConstruct;

/**
 * Default implementation of {@link RestClientConnection}.
 * Uses Spring {@link RestTemplate} to execute REST and SOAP calls.
 * A new {@link RestTemplate} instance is created for each call so that per-request
 * settings (e.g. timeout) never leak across concurrent calls.
 * Message converters and proxy configuration are built once at startup via {@link PostConstruct}.
 * Proxy support is configurable via {@code com.bld.connection.proxy.ip} and
 * {@code com.bld.connection.proxy.port} properties.
 */
@Component
public class RestClientConnectionImpl implements RestClientConnection {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(RestClientConnectionImpl.class);

	/** The port proxy. */
	@Value("${com.bld.connection.proxy.port:}")
	private Integer portProxy;

	/** The proxy. */
	@Value("${com.bld.connection.proxy.ip:}")
	private String proxy;

	/** HTTP methods that carry a request body. */
	private static final List<HttpMethod> HTTP_METHODS = Arrays.asList(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH);

	/** The Constant OBJECT_MAPPER. */
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
			logger.info("RestClientConnection - proxy enabled: {}:{}", this.proxy, this.portProxy);
			this.configuredProxy = new Proxy(Type.HTTP, new InetSocketAddress(this.proxy, this.portProxy));
		} else {
			logger.debug("RestClientConnection - no proxy configured");
		}
		this.messageConverters = buildMessageConverters();
	}

	/**
	 * Executes a REST call using a {@link MapRequest} and returns a single entity.
	 *
	 * @param <T>           the response type
	 * @param mapRequest    the map request
	 * @param responseClass the response class
	 * @return the response entity
	 * @throws Exception if the call fails
	 */
	@Override
	public <T> T entityRestTemplate(MapRequest mapRequest, Class<T> responseClass) throws Exception {
		MapUtils.unmodifiableMap(mapRequest.getHttpHeaders());
		HttpEntity<?> request = new HttpEntity<>(mapRequest.getHttpHeaders());
		String url = mapRequest.getUrl();
		if (HTTP_METHODS.contains(mapRequest.getMethod())) {
			Object body = RestConnectionMapper.mapToMultiValueMap(mapRequest.getData());
			request = new HttpEntity<>(body, mapRequest.getHttpHeaders());
		} else {
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(mapRequest.getUrl());
			RestConnectionMapper.builderQuery(builder, mapRequest.getData());
			url = builder.toUriString();
		}
		RestBuilder restBuilder = new RestBuilder(url, request);
		ResponseEntity<T> response = this.getResponseEntity(mapRequest, restBuilder, responseClass);
		return response.getBody();
	}

	/**
	 * Executes a REST call using an {@link ObjectRequest} and returns a single entity.
	 *
	 * @param <T>           the response type
	 * @param objectRequest the object request
	 * @param responseClass the response class
	 * @return the response entity
	 * @throws Exception if the call fails
	 */
	@Override
	public <T> T entityRestTemplate(ObjectRequest<?> objectRequest, Class<T> responseClass) throws Exception {
		HttpEntity<?> request = new HttpEntity<>(objectRequest.getData(), objectRequest.getHttpHeaders());
		RestBuilder restBuilder = new RestBuilder(objectRequest.getUrl(), request);
		ResponseEntity<T> response = this.getResponseEntity(objectRequest, restBuilder, responseClass);
		return response.getBody();
	}

	/**
	 * Executes a REST call using a {@link MapRequest} and returns a list of entities.
	 *
	 * @param <T>           the element type
	 * @param mapRequest    the map request
	 * @param responseClass the array response class
	 * @return the list of response entities
	 * @throws Exception if the call fails
	 */
	@Override
	public <T> List<T> listRestTemplate(MapRequest mapRequest, Class<T[]> responseClass) throws Exception {
		MapUtils.unmodifiableMap(mapRequest.getHttpHeaders());
		HttpEntity<?> request = new HttpEntity<>(mapRequest.getHttpHeaders());
		String url = mapRequest.getUrl();
		if (HTTP_METHODS.contains(mapRequest.getMethod())) {
			Object body = RestConnectionMapper.mapToMultiValueMap(mapRequest.getData());
			request = new HttpEntity<>(body, mapRequest.getHttpHeaders());
		} else {
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(mapRequest.getUrl());
			RestConnectionMapper.builderQuery(builder, mapRequest.getData());
			url = builder.toUriString();
		}
		RestBuilder restBuilder = new RestBuilder(url, request);
		ResponseEntity<T[]> response = this.getResponseEntity(mapRequest, restBuilder, responseClass);
		return Arrays.asList(response.getBody());
	}

	/**
	 * Executes a REST call using an {@link ObjectRequest} and returns a list of entities.
	 *
	 * @param <T>           the element type
	 * @param objectRequest the object request
	 * @param responseClass the array response class
	 * @return the list of response entities
	 * @throws Exception if the call fails
	 */
	@Override
	public <T> List<T> listRestTemplate(ObjectRequest<?> objectRequest, Class<T[]> responseClass) throws Exception {
		HttpEntity<?> request = new HttpEntity<>(objectRequest.getData(), objectRequest.getHttpHeaders());
		RestBuilder restBuilder = new RestBuilder(objectRequest.getUrl(), request);
		ResponseEntity<T[]> response = this.getResponseEntity(objectRequest, restBuilder, responseClass);
		return Arrays.asList(response.getBody());
	}

	/**
	 * Executes a SOAP 1.1 call via POST with Content-Type {@code text/xml}.
	 * Builds the SOAP envelope (from Map or JAXB object), sends it and unmarshals the first
	 * child element of the {@code soap:Body} into type {@code T}.
	 * If {@code responseClass} is {@code String.class} the raw envelope XML is returned.
	 *
	 * @param <T>           the response type
	 * @param soapRequest   the SOAP request
	 * @param responseClass the target class for unmarshalling the response
	 * @return the response object of type T unmarshalled from the soap:Body
	 * @throws Exception if a network, marshalling or unmarshalling error occurs
	 */
	@Override
	public <T> T soapRestTemplate(SoapRequest<?, ?> soapRequest, Class<T> responseClass) throws Exception {
		String soapActionValue = StringUtils.isNotEmpty(soapRequest.getSoapAction())
				? "\"" + soapRequest.getSoapAction() + "\""
				: "\"\"";
		soapRequest.getHttpHeaders().set("SOAPAction", soapActionValue);
		String envelope = SoapXmlBuilder.buildEnvelope(soapRequest);
		logger.info("[SOAP] POST {} - SOAPAction: {}", soapRequest.getUrl(), soapActionValue);
		logger.debug("[SOAP] Request headers: {}", soapRequest.getHttpHeaders());
		logger.debug("[SOAP] Request envelope:\n{}", envelope);
		HttpEntity<String> request = new HttpEntity<>(envelope, soapRequest.getHttpHeaders());
		ResponseEntity<String> response = this.buildRestTemplate(soapRequest.getTimeout()).exchange(
				soapRequest.getUrl(),
				soapRequest.getMethod(),
				request,
				String.class);
		logger.info("[SOAP] POST {} -> {}", soapRequest.getUrl(), response.getStatusCode());
		logger.debug("[SOAP] Response headers: {}", response.getHeaders());
		logger.debug("[SOAP] Response body:\n{}", response.getBody());
		return SoapXmlBuilder.unmarshalBody(response.getBody(), responseClass);
	}

	/**
	 * Executes the actual HTTP exchange via a fresh {@link RestTemplate}.
	 * If {@code responseClass} is {@link JsonNode} the response is first retrieved as a raw string:
	 * if the body starts with {@code <} it is converted via {@link XmlNodeConverter#fromXml(String)},
	 * otherwise it is parsed as JSON.
	 *
	 * @param <T>           the response type
	 * @param basicRequest  the REST request
	 * @param restBuilder   the builder holding the resolved URL and HTTP entity
	 * @param responseClass the response class
	 * @return the response entity
	 * @throws Exception if the XML or JSON conversion fails
	 */
	@SuppressWarnings("unchecked")
	private <T> ResponseEntity<T> getResponseEntity(RestBasicRequest<?> basicRequest, RestBuilder restBuilder, Class<T> responseClass) throws Exception {
		RestTemplate rt = this.buildRestTemplate(basicRequest.getTimeout());
		logger.info("[REST] {} {}", basicRequest.getMethod(), restBuilder.getUrl());
		logger.debug("[REST] Request headers: {}", basicRequest.getHttpHeaders());
		if (responseClass == JsonNode.class) {
			ResponseEntity<String> raw = rt.exchange(restBuilder.getUrl(), basicRequest.getMethod(), restBuilder.getRequest(), String.class, basicRequest.getUriParams());
			logger.info("[REST] {} {} -> {}", basicRequest.getMethod(), restBuilder.getUrl(), raw.getStatusCode());
			logger.debug("[REST] Response headers: {}", raw.getHeaders());
			String body = raw.getBody();
			T result = null;
			if (body != null) {
				result = body.trim().startsWith("<")
						? (T) XmlNodeConverter.fromXml(body)
						: (T) OBJECT_MAPPER.readTree(body);
			}
			return ResponseEntity.status(raw.getStatusCode()).body(result);
		}
		ResponseEntity<T> response = rt.exchange(restBuilder.getUrl(), basicRequest.getMethod(), restBuilder.getRequest(), responseClass, basicRequest.getUriParams());
		logger.info("[REST] {} {} -> {}", basicRequest.getMethod(), restBuilder.getUrl(), response.getStatusCode());
		logger.debug("[REST] Response headers: {}", response.getHeaders());
		return response;
	}

	/**
	 * Creates a new {@link RestTemplate} for a single call.
	 * Applies the optional timeout and the proxy configured at startup.
	 * Message converters initialised at startup are reused.
	 *
	 * @param timeout the read/connect timeout in milliseconds, or {@code null} for no timeout
	 * @return a new RestTemplate instance ready for use
	 */
	private RestTemplate buildRestTemplate(Integer timeout) {
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
		MappingJackson2HttpMessageConverter yamlConverter = new MappingJackson2HttpMessageConverter(new YAMLMapper());
		yamlConverter.setSupportedMediaTypes(List.of(
				new MediaType("application", "yaml"),
				new MediaType("application", "x-yaml"),
				new MediaType("text", "yaml")));
		converters.add(yamlConverter);
		StringHttpMessageConverter xmlStringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
		xmlStringConverter.setSupportedMediaTypes(List.of(
				new MediaType("text", "xml"),
				new MediaType("application", "xml"),
				new MediaType("application", "soap+xml")));
		converters.add(xmlStringConverter);
		return converters;
	}

	/**
	 * The Class RestBuilder.
	 */
	private class RestBuilder {

		/** The url. */
		private String url;

		/** The request. */
		private HttpEntity<?> request;

		/**
		 * Instantiates a new rest builder.
		 *
		 * @param url     the url
		 * @param request the request
		 */
		private RestBuilder(String url, HttpEntity<?> request) {
			super();
			this.url = url;
			this.request = request;
		}

		/**
		 * Gets the url.
		 *
		 * @return the url
		 */
		private String getUrl() {
			return url;
		}

		/**
		 * Gets the request.
		 *
		 * @return the request
		 */
		private HttpEntity<?> getRequest() {
			return request;
		}

	}

}
