/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.client.RestClientConnection.java
 */
package com.bld.commons.connection.client;

import java.util.List;

import com.bld.commons.connection.model.MapRequest;
import com.bld.commons.connection.model.ObjectRequest;

/**
 * The Interface RestClientConnection.
 */
public interface RestClientConnection {

	/**
	 * Executes a REST call using a {@link MapRequest} and returns a single entity.
	 *
	 * @param <T>           the response type
	 * @param mapRequest    the map request
	 * @param responseClass the response class
	 * @return the response entity
	 * @throws Exception if the call fails
	 */
	public <T> T entityRestTemplate(MapRequest mapRequest, Class<T> responseClass) throws Exception;

	/**
	 * Executes a REST call using an {@link ObjectRequest} and returns a single entity.
	 *
	 * @param <T>            the response type
	 * @param objectRequest  the object request
	 * @param responseClass  the response class
	 * @return the response entity
	 * @throws Exception if the call fails
	 */
	public <T> T entityRestTemplate(ObjectRequest<?> objectRequest, Class<T> responseClass) throws Exception;

	/**
	 * Executes a REST call using a {@link MapRequest} and returns a list of entities.
	 *
	 * @param <T>           the element type
	 * @param mapRequest    the map request
	 * @param responseClass the array response class (e.g. {@code MyType[].class})
	 * @return the list of response entities
	 * @throws Exception if the call fails
	 */
	public <T> List<T> listRestTemplate(MapRequest mapRequest, Class<T[]> responseClass) throws Exception;

	/**
	 * Executes a REST call using an {@link ObjectRequest} and returns a list of entities.
	 *
	 * @param <T>            the element type
	 * @param objectRequest  the object request
	 * @param responseClass  the array response class (e.g. {@code MyType[].class})
	 * @return the list of response entities
	 * @throws Exception if the call fails
	 */
	public <T> List<T> listRestTemplate(ObjectRequest<?> objectRequest, Class<T[]> responseClass) throws Exception;

}
