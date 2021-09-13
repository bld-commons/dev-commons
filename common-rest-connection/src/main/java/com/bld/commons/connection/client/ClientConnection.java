package com.bld.commons.connection.client;

import com.bld.commons.connection.model.BasicRequest;

/**
 * The Interface ClientConnection.
 */
public interface ClientConnection {

	public final static String CONTENT_TYPE="Content-Type";
	
	
	public <T,K>T getRestTemplate(BasicRequest<K> basicRequest,Class<T>classe) throws Exception;

	
}
