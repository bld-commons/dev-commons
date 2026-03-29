/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.client.RestClientConnection.java
 */
package com.bld.commons.connection.client;

import java.util.List;

import com.bld.commons.connection.model.MapRequest;
import com.bld.commons.connection.model.ObjectRequest;
import com.bld.commons.connection.model.SoapRequest;

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

	/**
	 * Executes a SOAP 1.1 call via POST with Content-Type {@code text/xml}.
	 * The operation body is built from {@code soapRequest.getData()}:
	 * if it is a {@code Map<String,Object>} the entries are serialised as child XML tags;
	 * if it is a JAXB object annotated with {@code @XmlRootElement} it is marshalled directly.
	 * The response is unmarshalled into type {@code T} by extracting the first child element
	 * of the {@code soap:Body}. If {@code responseClass} is {@code String.class} the raw
	 * envelope XML is returned.
	 *
	 * @param <T>           the response type
	 * @param soapRequest   the SOAP request with url, operationName, namespace and body
	 * @param responseClass the target class for unmarshalling the response
	 * @return the response object of type T unmarshalled from the soap:Body
	 * @throws Exception if a network, marshalling or unmarshalling error occurs
	 */
	public <T> T soapRestTemplate(SoapRequest<?, ?> soapRequest, Class<T> responseClass) throws Exception;

}
