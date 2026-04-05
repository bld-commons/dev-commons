/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.model.MapSoapHeader.java
 */
package com.bld.commons.connection.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Specialisation of {@link SoapHeader} that uses a {@code Map<String,Object>} as the SOAP
 * header body. Implements {@link BasicMapRequest} for fluent field management.
 */
public class MapSoapHeader extends SoapHeader<Map<String, Object>> implements BasicMapRequest {

	/**
	 * Instantiates a new map soap header.
	 *
	 * @param operationName the root element name of the header
	 * @param namespace     the XML namespace of the header root element
	 */
	public MapSoapHeader(String operationName, String namespace) {
		super(operationName, namespace);
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
