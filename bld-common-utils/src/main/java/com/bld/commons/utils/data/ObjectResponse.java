/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.model.ObjectResponse.java
 */
package com.bld.commons.utils.data;

import java.io.Serializable;

import jakarta.validation.Valid;

/**
 * Generic response wrapper for a single data payload.
 *
 * <p>Wraps an arbitrary result object in a consistent API response envelope.
 * The wrapped value is validated via Jakarta Bean Validation constraints
 * ({@link jakarta.validation.Valid}) when the response is used as a request body.</p>
 *
 * @param <T> the type of the wrapped data object
 *
 * @author Francesco Baldi
 */
@SuppressWarnings("serial")
public class ObjectResponse<T> implements Serializable{

	/** The data. */
	@Valid
	private T data;
	
	/**
	 * Instantiates a new object response.
	 */
	public ObjectResponse() {
		super();
	}
	
	/**
	 * Instantiates a new object response.
	 *
	 * @param data the data
	 */
	public ObjectResponse(T data) {
		super();
		this.data = data;
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
	 * Sets the data.
	 *
	 * @param data the new data
	 */
	public void setData(T data) {
		this.data = data;
	}
	
	
	
}
