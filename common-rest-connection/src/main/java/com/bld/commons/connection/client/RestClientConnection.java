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
 * The Interface ClientConnection.
 */
public interface RestClientConnection {

	/**
	 * Entity rest template.
	 *
	 * @param <T> the generic type
	 * @param mapRequest the map request
	 * @param responseClass the response class
	 * @return the t
	 * @throws Exception the exception
	 */
	public <T> T entityRestTemplate(MapRequest mapRequest, Class<T> responseClass) throws Exception;

	/**
	 * Entity rest template.
	 *
	 * @param <T> the generic type
	 * @param objectRequest the object request
	 * @param responseClass the response class
	 * @return the t
	 * @throws Exception the exception
	 */
	public <T> T entityRestTemplate(ObjectRequest<?> objectRequest, Class<T> responseClass) throws Exception;

	/**
	 * List rest template.
	 *
	 * @param <T> the generic type
	 * @param mapRequest the map request
	 * @param responseClass the response class
	 * @return the list
	 * @throws Exception the exception
	 */
	public <T> List<T> listRestTemplate(MapRequest mapRequest, Class<T[]> responseClass) throws Exception;

	/**
	 * List rest template.
	 *
	 * @param <T> the generic type
	 * @param objectRequest the object request
	 * @param responseClass the response class
	 * @return the list
	 * @throws Exception the exception
	 */
	public <T> List<T> listRestTemplate(ObjectRequest<?> objectRequest, Class<T[]> responseClass) throws Exception;
	
	


	
}
