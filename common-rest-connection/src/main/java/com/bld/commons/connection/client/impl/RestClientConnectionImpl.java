/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.client.impl.RestClientConnectionImpl.java
 */
package com.bld.commons.connection.client.impl;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

	/** The Constant OBJECT_MAPPER. */
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/** Matches a JSON content type, including vendor suffixes (e.g. {@code application/hal+json}). */
	private static final Pattern JSON_CONTENT_TYPE = Pattern.compile(".*/(.*\\+)?json", Pattern.CASE_INSENSITIVE);

	/** Matches an XML content type, including vendor suffixes (e.g. {@code application/soap+xml}). */
	private static final Pattern XML_CONTENT_TYPE = Pattern.compile(".*/(.*\\+)?xml", Pattern.CASE_INSENSITIVE);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T entityRestTemplate(MapRequest mapRequest, Class<T> responseClass) throws Exception {
		return this.responseEntity(mapRequest, responseClass).getBody();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> ResponseEntity<T> responseEntity(MapRequest mapRequest, Class<T> responseClass) throws Exception {
		MapUtils.unmodifiableMap(mapRequest.getHttpHeaders());
		HttpEntity<?> request = new HttpEntity<>(mapRequest.getHttpHeaders());
		String url = mapRequest.getUrl();
		if (HTTP_METHODS.contains(mapRequest.getMethod())) {
			String callUrl = mapRequest.getUrl();
			if (MapUtils.isNotEmpty(mapRequest.getQueryParams())) {
				UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(mapRequest.getUrl());
				RestConnectionMapper.builderQuery(builder, mapRequest.getQueryParams());
				callUrl = builder.toUriString();
			}
			url = callUrl;
			Object body = isFormLike(mapRequest)
					? RestConnectionMapper.mapToMultiValueMap(mapRequest.getData())
					: mapRequest.getData();
			request = new HttpEntity<>(body, mapRequest.getHttpHeaders());
		} else {
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(mapRequest.getUrl());
			RestConnectionMapper.builderQuery(builder, mapRequest.getData());
			url = builder.toUriString();
		}
		RestBuilder restBuilder = new RestBuilder(url, request);
		return this.getResponseEntity(mapRequest, restBuilder, responseClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T entityRestTemplate(ObjectRequest<?> objectRequest, Class<T> responseClass) throws Exception {
		return this.entityResponseRestTemplate(objectRequest, responseClass).getBody();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> ResponseEntity<T> entityResponseRestTemplate(ObjectRequest<?> objectRequest, Class<T> responseClass) throws Exception {
		HttpEntity<?> request = new HttpEntity<>(objectRequest.getData(), objectRequest.getHttpHeaders());
		RestBuilder restBuilder = new RestBuilder(objectRequest.getUrl(), request);
		return this.getResponseEntity(objectRequest, restBuilder, responseClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> listRestTemplate(MapRequest mapRequest, Class<T[]> responseClass) throws Exception {
		return this.responseListEntity(mapRequest, responseClass).getBody();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> ResponseEntity<List<T>> responseListEntity(MapRequest mapRequest, Class<T[]> responseClass) throws Exception {
		MapUtils.unmodifiableMap(mapRequest.getHttpHeaders());
		HttpEntity<?> request = new HttpEntity<>(mapRequest.getHttpHeaders());
		String url = mapRequest.getUrl();
		if (HTTP_METHODS.contains(mapRequest.getMethod())) {
			String callUrl = mapRequest.getUrl();
			if (MapUtils.isNotEmpty(mapRequest.getQueryParams())) {
				UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(mapRequest.getUrl());
				RestConnectionMapper.builderQuery(builder, mapRequest.getQueryParams());
				callUrl = builder.toUriString();
			}
			url = callUrl;
			Object body = isFormLike(mapRequest)
					? RestConnectionMapper.mapToMultiValueMap(mapRequest.getData())
					: mapRequest.getData();
			request = new HttpEntity<>(body, mapRequest.getHttpHeaders());
		} else {
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(mapRequest.getUrl());
			RestConnectionMapper.builderQuery(builder, mapRequest.getData());
			url = builder.toUriString();
		}
		RestBuilder restBuilder = new RestBuilder(url, request);
		ResponseEntity<T[]> response = this.getResponseEntity(mapRequest, restBuilder, responseClass);
		return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(Arrays.asList(response.getBody()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> listRestTemplate(ObjectRequest<?> objectRequest, Class<T[]> responseClass) throws Exception {
		return this.listResponseRestTemplate(objectRequest, responseClass).getBody();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> ResponseEntity<List<T>> listResponseRestTemplate(ObjectRequest<?> objectRequest, Class<T[]> responseClass) throws Exception {
		HttpEntity<?> request = new HttpEntity<>(objectRequest.getData(), objectRequest.getHttpHeaders());
		RestBuilder restBuilder = new RestBuilder(objectRequest.getUrl(), request);
		ResponseEntity<T[]> response = this.getResponseEntity(objectRequest, restBuilder, responseClass);
		return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(Arrays.asList(response.getBody()));
	}

	/**
	 * Returns {@code true} when the request content type is a form-like
	 * media type: {@code application/x-www-form-urlencoded} or
	 * {@code multipart/form-data}.
	 * Multi-value mapping is required only for form bodies; JSON bodies
	 * must keep the original {@code Map<String,Object>} so values are not
	 * serialised as single-element arrays.
	 * <p>
	 * Senza questo, un body {@code Map} con content-type
	 * {@code multipart/form-data} fallisce con
	 * {@code No HttpMessageConverter for java.util.HashMap and content type "multipart/form-data"}
	 * (RestTemplate serializza i form-data solo da una
	 * {@code org.springframework.util.MultiValueMap} via {@code FormHttpMessageConverter}).
	 *
	 * @param mapRequest the REST request
	 * @return {@code true} if the content type is form-like
	 */
	private static boolean isFormLike(MapRequest mapRequest) {
		MediaType contentType = mapRequest.getHttpHeaders().getContentType();
		return contentType != null && (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)
				|| MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType));
	}

	/**
	 * Executes the actual HTTP exchange via a fresh {@link RestTemplate}.
	 * If {@code responseClass} is {@link JsonNode} the response is first retrieved as a raw string:
	 * if the body starts with {@code <} it is converted via {@link XmlNodeConverter#fromXmlNormalized(String)},
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
			boolean xml = body != null && body.trim().startsWith("<");
			if (body != null) {
				if (xml) {
					result = (T) XmlNodeConverter.fromXmlNormalized(body);
				} else {
					result = (T) OBJECT_MAPPER.readTree(body);
				}
			}
			HttpHeaders headers = resolveContentType(raw.getHeaders(), xml);
			return ResponseEntity.status(raw.getStatusCode()).headers(headers).body(result);
		}
		ResponseEntity<T> response = rt.exchange(restBuilder.getUrl(), basicRequest.getMethod(), restBuilder.getRequest(), responseClass, basicRequest.getUriParams());
		logger.info("[REST] {} {} -> {}", basicRequest.getMethod(), restBuilder.getUrl(), response.getStatusCode());
		logger.debug("[REST] Response headers: {}", response.getHeaders());
		return response;
	}

	/**
	 * Returns a copy of {@code source} whose {@code Content-Type} is guaranteed to match the
	 * actual body format detected by sniffing. When the original {@code Content-Type} is missing
	 * or does not match the detected format (server bug), it is replaced with the standard media
	 * type ({@link MediaType#APPLICATION_XML} or {@link MediaType#APPLICATION_JSON}); otherwise the
	 * original value (including any charset) is preserved.
	 *
	 * @param source the original response headers
	 * @param xml    {@code true} when the body was detected as XML, {@code false} for JSON
	 * @return a new {@link HttpHeaders} with a coherent {@code Content-Type}
	 */
	private HttpHeaders resolveContentType(HttpHeaders source, boolean xml) {
		HttpHeaders headers = new HttpHeaders();
		headers.putAll(source);
		MediaType actual = source.getContentType();
		Pattern expected = xml ? XML_CONTENT_TYPE : JSON_CONTENT_TYPE;
		boolean coherent = actual != null && expected.matcher(actual.toString()).matches();
		if (!coherent) {
			headers.setContentType(xml ? MediaType.APPLICATION_XML : MediaType.APPLICATION_JSON);
		}
		return headers;
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
