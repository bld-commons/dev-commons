/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.model.MapSoapRequest.java
 */
package com.bld.commons.connection.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Specialisation of {@link SoapRequest} that uses a {@code Map<String,Object>} as the SOAP
 * operation body. Implements {@link BasicMapRequest} for fluent field management.
 *
 * @param <H> the type of the {@code soap:Header} data
 */
public class MapSoapRequest<H> extends SoapRequest<Map<String, Object>, H> implements BasicMapRequest {

	/**
	 * Instantiates a new map soap request.
	 *
	 * @param url           the SOAP endpoint URL
	 * @param operationName the SOAP operation name
	 * @param namespace     the XML namespace
	 */
	public MapSoapRequest(String url, String operationName, String namespace) {
		super(url, operationName, namespace);
		super.data = new HashMap<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addData(String key, Object value) {
		super.data.put(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeData(String key) {
		super.data.remove(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearData() {
		super.data = new HashMap<>();
	}

}
