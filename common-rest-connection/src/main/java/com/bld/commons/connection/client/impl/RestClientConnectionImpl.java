/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.client.impl.RestClientConnectionImpl.java
 */
package com.bld.commons.connection.client.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.bld.commons.connection.client.AbstractClientConnection;
import com.bld.commons.connection.client.RestClientConnection;
import com.bld.commons.connection.model.MapRequest;
import com.bld.commons.connection.model.ObjectRequest;
import com.bld.commons.connection.model.RestBasicRequest;
import com.bld.commons.connection.utils.RestConnectionMapper;
import com.bld.commons.connection.utils.XmlNodeConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Default implementation of {@link RestClientConnection}.
 * Uses Spring {@link RestTemplate} to execute REST calls.
 * A new {@link RestTemplate} instance is created for each call so that per-request
 * settings (e.g. timeout) never leak across concurrent calls.
 */
@Component
public class RestClientConnectionImpl extends AbstractClientConnection implements RestClientConnection {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(RestClientConnectionImpl.class);

	/** HTTP methods that carry a request body. */
	private static final List<HttpMethod> HTTP_METHODS = Arrays.asList(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH);

	/** The object mapper. */
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * {@inheritDoc}
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
	 * {@inheritDoc}
	 */
	@Override
	public <T> T entityRestTemplate(ObjectRequest<?> objectRequest, Class<T> responseClass) throws Exception {
		HttpEntity<?> request = new HttpEntity<>(objectRequest.getData(), objectRequest.getHttpHeaders());
		RestBuilder restBuilder = new RestBuilder(objectRequest.getUrl(), request);
		ResponseEntity<T> response = this.getResponseEntity(objectRequest, restBuilder, responseClass);
		return response.getBody();
	}

	/**
	 * {@inheritDoc}
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
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> listRestTemplate(ObjectRequest<?> objectRequest, Class<T[]> responseClass) throws Exception {
		HttpEntity<?> request = new HttpEntity<>(objectRequest.getData(), objectRequest.getHttpHeaders());
		RestBuilder restBuilder = new RestBuilder(objectRequest.getUrl(), request);
		ResponseEntity<T[]> response = this.getResponseEntity(objectRequest, restBuilder, responseClass);
		return Arrays.asList(response.getBody());
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
						: (T) objectMapper.readTree(body);
			}
			return ResponseEntity.status(raw.getStatusCode()).body(result);
		}
		ResponseEntity<T> response = rt.exchange(restBuilder.getUrl(), basicRequest.getMethod(), restBuilder.getRequest(), responseClass, basicRequest.getUriParams());
		logger.info("[REST] {} {} -> {}", basicRequest.getMethod(), restBuilder.getUrl(), response.getStatusCode());
		logger.debug("[REST] Response headers: {}", response.getHeaders());
		return response;
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
