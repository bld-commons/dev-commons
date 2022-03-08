/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.client.impl.RestClientConnectionImpl.java
 */
package com.bld.commons.connection.client.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.bld.commons.connection.client.RestClientConnection;
import com.bld.commons.connection.model.BasicRequest;
import com.bld.commons.connection.model.MapRequest;
import com.bld.commons.connection.model.ObjectRequest;
import com.bld.commons.connection.utils.RestConnectionMapper;

/**
 * The Class ClientConnectionImpl.
 */
@Component
public class RestClientConnectionImpl implements RestClientConnection {

	/** The rest template. */
	@Autowired
	private RestTemplate restTemplate;
	
	/** The Constant HTTP_METHODS. */
	private final static List<HttpMethod> HTTP_METHODS=Arrays.asList(HttpMethod.POST,HttpMethod.PUT,HttpMethod.PATCH);




	/**
	 * Entity rest template.
	 *
	 * @param <T> the generic type
	 * @param mapRequest the map request
	 * @param responseClass the response class
	 * @return the t
	 * @throws Exception the exception
	 */
	@Override
	public <T> T entityRestTemplate(MapRequest mapRequest, Class<T> responseClass) throws Exception {
		MapUtils.unmodifiableMap(mapRequest.getHttpHeaders());
		HttpEntity<?> request = new HttpEntity<>(mapRequest.getHttpHeaders());
		String url=mapRequest.getUrl();
		if(HTTP_METHODS.contains(mapRequest.getMethod())) {
			Object body = RestConnectionMapper.mapToMultiValueMap(mapRequest.getData());
			request = new HttpEntity<>(body, mapRequest.getHttpHeaders());
		}else {
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(mapRequest.getUrl());
			RestConnectionMapper.builderQuery(builder, mapRequest.getData());
			url = builder.toUriString();
		}
		RestBuilder restBuilder = new RestBuilder(url, request);
		ResponseEntity<T> response=this.getResponseEntity(mapRequest, restBuilder, responseClass);
		return response.getBody();
	}
	
	/**
	 * Entity rest template.
	 *
	 * @param <T> the generic type
	 * @param objectRequest the object request
	 * @param responseClass the response class
	 * @return the t
	 * @throws Exception the exception
	 */
	@Override
	public <T> T entityRestTemplate(ObjectRequest<?> objectRequest, Class<T> responseClass) throws Exception {
		HttpEntity<?> request = new HttpEntity<>(objectRequest.getData(), objectRequest.getHttpHeaders());
		RestBuilder restBuilder = new RestBuilder(objectRequest.getUrl(), request);
		ResponseEntity<T> response=this.getResponseEntity(objectRequest, restBuilder, responseClass);
		return response.getBody();
	}
	
	/**
	 * List rest template.
	 *
	 * @param <T> the generic type
	 * @param mapRequest the map request
	 * @param responseClass the response class
	 * @return the list
	 * @throws Exception the exception
	 */
	@Override
	public <T> List<T> listRestTemplate(MapRequest mapRequest, Class<T[]> responseClass) throws Exception {
		MapUtils.unmodifiableMap(mapRequest.getHttpHeaders());
		HttpEntity<?> request = new HttpEntity<>(mapRequest.getHttpHeaders());
		String url=mapRequest.getUrl();
		if(HTTP_METHODS.contains(mapRequest.getMethod())) {
			Object body = RestConnectionMapper.mapToMultiValueMap(mapRequest.getData());
			request = new HttpEntity<>(body, mapRequest.getHttpHeaders());
		}else {
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(mapRequest.getUrl());
			RestConnectionMapper.builderQuery(builder, mapRequest.getData());
			url = builder.toUriString();
		}
		RestBuilder restBuilder = new RestBuilder(url, request);
		
		ResponseEntity<T[]> response=this.getResponseEntity(mapRequest, restBuilder, responseClass);
		return Arrays.asList(response.getBody());
	}
	
	/**
	 * List rest template.
	 *
	 * @param <T> the generic type
	 * @param objectRequest the object request
	 * @param responseClass the response class
	 * @return the list
	 * @throws Exception the exception
	 */
	@Override
	public <T> List<T> listRestTemplate(ObjectRequest<?> objectRequest, Class<T[]> responseClass) throws Exception {
		HttpEntity<?> request = new HttpEntity<>(objectRequest.getData(), objectRequest.getHttpHeaders());
		RestBuilder restBuilder = new RestBuilder(objectRequest.getUrl(), request);
		ResponseEntity<T[]> response=this.getResponseEntity(objectRequest, restBuilder, responseClass);
		return Arrays.asList(response.getBody());
	}
	
	
	/**
	 * Gets the response entity.
	 *
	 * @param <T> the generic type
	 * @param basicRequest the basic request
	 * @param restBuilder the rest builder
	 * @param responseClass the response class
	 * @return the response entity
	 */
	private <T> ResponseEntity<T> getResponseEntity(BasicRequest<?> basicRequest,RestBuilder restBuilder,Class<T> responseClass) {
		this.setTimeout(basicRequest);
		return this.restTemplate.exchange(restBuilder.getUrl(), basicRequest.getMethod(), restBuilder.getRequest(), responseClass, basicRequest.getUriParams());
	}
	


	/**
	 * Sets the timeout.
	 *
	 * @param <K> the key type
	 * @param basicRequest the new timeout
	 */
	private <K> void setTimeout(BasicRequest<K> basicRequest) {
		restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());
		if (basicRequest.getTimeout() != null) {
			SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
			rf.setReadTimeout(basicRequest.getTimeout());
			rf.setConnectTimeout(basicRequest.getTimeout());
		}

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
		 * @param url the url
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
