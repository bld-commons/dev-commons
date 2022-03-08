/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.model.ObjectRequest.java
 */
package com.bld.commons.connection.model;

import org.springframework.http.HttpMethod;

/**
 * The Class ObjectRequest.
 *
 * @param <T> the generic type
 */
public class ObjectRequest<T> extends BasicRequest<T>{

	/**
	 * Instantiates a new object request.
	 *
	 * @param url the url
	 * @param method the method
	 */
	private ObjectRequest(String url, HttpMethod method) {
		super(url, method);
	}

	/**
	 * New instance post.
	 *
	 * @param <T> the generic type
	 * @param url the url
	 * @return the object request
	 */
	public static <T> ObjectRequest<T> newInstancePost(String url) {
		return new ObjectRequest<T>(url, HttpMethod.POST);
	}
	
	/**
	 * New instance put.
	 *
	 * @param <T> the generic type
	 * @param url the url
	 * @return the object request
	 */
	public static <T> ObjectRequest<T> newInstancePut(String url) {
		return new ObjectRequest<T>(url, HttpMethod.PUT);
	}
	
	/**
	 * New instance patch.
	 *
	 * @param <T> the generic type
	 * @param url the url
	 * @return the object request
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
		this.data=data;
	}
}
