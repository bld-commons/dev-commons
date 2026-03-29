/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.model.ObjectRequest.java
 */
package com.bld.commons.connection.model;

import org.springframework.http.HttpMethod;

/**
 * REST request whose body is a typed object.
 *
 * @param <T> the type of the request body
 */
public class ObjectRequest<T> extends RestBasicRequest<T> {

	/**
	 * Instantiates a new object request.
	 *
	 * @param url    the url
	 * @param method the method
	 */
	private ObjectRequest(String url, HttpMethod method) {
		super(url, method);
	}

	/**
	 * Creates a new {@link ObjectRequest} for an HTTP POST call.
	 * Set the body with {@link #setData(Object)} before dispatching.
	 *
	 * @param <T> the type of the request body
	 * @param url the target URL
	 * @return a new ObjectRequest configured for POST
	 */
	public static <T> ObjectRequest<T> newInstancePost(String url) {
		return new ObjectRequest<T>(url, HttpMethod.POST);
	}

	/**
	 * Creates a new {@link ObjectRequest} for an HTTP PUT call.
	 * Set the body with {@link #setData(Object)} before dispatching.
	 *
	 * @param <T> the type of the request body
	 * @param url the target URL
	 * @return a new ObjectRequest configured for PUT
	 */
	public static <T> ObjectRequest<T> newInstancePut(String url) {
		return new ObjectRequest<T>(url, HttpMethod.PUT);
	}

	/**
	 * Creates a new {@link ObjectRequest} for an HTTP PATCH call.
	 * Set the body with {@link #setData(Object)} before dispatching.
	 *
	 * @param <T> the type of the request body
	 * @param url the target URL
	 * @return a new ObjectRequest configured for PATCH
	 */
	public static <T> ObjectRequest<T> newInstancePatch(String url) {
		return new ObjectRequest<T>(url, HttpMethod.PATCH);
	}

	/**
	 * Sets the data.
	 *
	 * @param data the new data
	 */
	public void setData(T data) {
		this.data = data;
	}

}
