/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.model.BasicMapRequest.java
 */
package com.bld.commons.connection.model;

/**
 * Common interface for types that manage the request body as a {@code Map<String,Object>}.
 * Implemented by {@link MapRequest}, {@link MapSoapRequest} and {@link MapSoapHeader}.
 */
public interface BasicMapRequest {

	/**
	 * Adds a key-value pair to the body.
	 *
	 * @param key   the field name
	 * @param value the field value
	 */
	void addData(String key, Object value);

	/**
	 * Removes a key from the body.
	 *
	 * @param key the field name to remove
	 */
	void removeData(String key);

	/**
	 * Clears the body.
	 */
	void clearData();

}
