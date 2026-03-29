/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.model.BasicRequest.java
 */
package com.bld.commons.connection.model;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import jakarta.validation.constraints.NotNull;

/**
 * Base class for all request types (REST and SOAP).
 * Holds the fields common to every call: URL, HTTP method, timeout, body and HTTP headers.
 * REST-specific features (URI params, Content-Type, Accept) are provided by {@link RestBasicRequest}.
 *
 * @param <T> the type of the request body
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

	/** The timeout. */
	private Integer timeout;

	/**
	 * Instantiates a new basic request.
	 *
	 * @param url    the url
	 * @param method the method
	 */
	protected BasicRequest(String url, HttpMethod method) {
		this.url = url;
		this.method = method;
		this.httpHeaders = new HttpHeaders();
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
	 * @param props the header name
	 * @param value the header value
	 */
	public void addHeader(String props, String value) {
		this.httpHeaders.add(props, value);
	}

	/**
	 * Removes the header.
	 *
	 * @param props the header name
	 */
	public void removeHeader(String props) {
		this.httpHeaders.remove(props);
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
	 * @param timeout the new timeout in milliseconds
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
	 * Sets the bearer auth.
	 *
	 * @param token the Bearer token
	 */
	public void setBearerAuth(String token) {
		this.httpHeaders.setBearerAuth(token);
	}

	/**
	 * Sets the basic auth.
	 *
	 * @param username the username
	 * @param password the password
	 */
	public void setBasicAuth(String username, String password) {
		this.httpHeaders.setBasicAuth(username, password);
	}

}
