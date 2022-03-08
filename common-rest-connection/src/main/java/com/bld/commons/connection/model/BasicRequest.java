/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.model.BasicRequest.java
 */
package com.bld.commons.connection.model;

import javax.validation.constraints.NotNull;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * The Class BasicRequest.
 *
 * @param <T> the generic type
 */
public abstract class BasicRequest<T> {

	/** The data. */
	protected T data;

	/** The url. */
	@NotNull
	private String url;

	/** The method. */
	@NotNull
	private HttpMethod method;

	/** The http headers. */
	private HttpHeaders httpHeaders;

	/** The uri params. */
	private Object[] uriParams;

	/** The timeout. */
	private Integer timeout;
	
	/**
	 * Instantiates a new basic request.
	 *
	 * @param url the url
	 * @param method the method
	 * @param mediaType the media type
	 */
	protected BasicRequest(String url, HttpMethod method, MediaType mediaType) {
		super();
		this.url = url;
		this.method = method;
		init();
		this.setContentType(mediaType);
	}


	/**
	 * Inits the.
	 */
	private void init() {
		this.httpHeaders = new HttpHeaders();
		this.uriParams = new Object[] {};
	}
	

	/**
	 * Instantiates a new basic request.
	 *
	 * @param url the url
	 * @param method the method
	 */
	protected BasicRequest(String url, HttpMethod method) {
		super();
		this.url = url;
		this.method = method;
		init();
	}

	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the url.
	 *
	 * @param url the new url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Gets the method.
	 *
	 * @return the method
	 */
	public HttpMethod getMethod() {
		return method;
	}

	/**
	 * Gets the http headers.
	 *
	 * @return the http headers
	 */
	public HttpHeaders getHttpHeaders() {
		return httpHeaders;
	}

	/**
	 * Adds the header.
	 *
	 * @param props the props
	 * @param value the value
	 */
	public void addHeader(String props, String value) {
		this.httpHeaders.add(props, value);
	}
	
	/**
	 * Removes the haeder.
	 *
	 * @param props the props
	 */
	public void removeHaeder(String props) {
		this.httpHeaders.remove(props);
	}

	/**
	 * Gets the uri params.
	 *
	 * @return the uri params
	 */
	public Object[] getUriParams() {
		return uriParams;
	}

	/**
	 * Adds the uri params.
	 *
	 * @param uriParams the uri params
	 */
	public void addUriParams(Object... uriParams) {
		int i = this.uriParams.length;
		for (Object uriParam : uriParams)
			this.uriParams[i++] = uriParam;
	}
	
	/**
	 * Clear uri paramas.
	 */
	public void clearUriParamas() {
		this.uriParams = new Object[] {};
	}

	/**
	 * Gets the timeout.
	 *
	 * @return the timeout
	 */
	public Integer getTimeout() {
		return timeout;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the new timeout
	 */
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	public T getData() {
		return data;
	}
	
	/**
	 * Sets the content type.
	 *
	 * @param mediaType the new content type
	 */
	public void setContentType(MediaType mediaType) {
		this.httpHeaders.setContentType(mediaType);
	}
	
	
	/**
	 * Sets the bearer auth.
	 *
	 * @param token the new bearer auth
	 */
	public void setBearerAuth(String token) {
		this.httpHeaders.setBearerAuth(token);
	}
	

}
