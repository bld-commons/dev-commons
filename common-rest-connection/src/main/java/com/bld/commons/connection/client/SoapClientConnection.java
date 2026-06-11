/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.client.SoapClientConnection.java
 */
package com.bld.commons.connection.client;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.bld.commons.connection.model.SoapRequest;

/**
 * The Interface SoapClientConnection.
 */
public interface SoapClientConnection {

	/**
	 * Executes a SOAP 1.1 call via POST with Content-Type {@code text/xml} and returns a single entity.
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
	public <T> T entitySoapTemplate(SoapRequest<?, ?> soapRequest, Class<T> responseClass) throws Exception;

	/**
	 * Executes a SOAP 1.1 call via POST with Content-Type {@code text/xml} and returns the full
	 * {@link ResponseEntity}. The body is unmarshalled into type {@code T} as in
	 * {@link #entitySoapTemplate(SoapRequest, Class)}, while status code and headers of the
	 * underlying HTTP response are preserved.
	 *
	 * @param <T>           the response type
	 * @param soapRequest   the SOAP request with url, operationName, namespace and body
	 * @param responseClass the target class for unmarshalling the response
	 * @return the response entity including status code and headers
	 * @throws Exception if a network, marshalling or unmarshalling error occurs
	 */
	public <T> ResponseEntity<T> responseEntity(SoapRequest<?, ?> soapRequest, Class<T> responseClass) throws Exception;

	/**
	 * Executes a SOAP 1.1 call via POST with Content-Type {@code text/xml} and returns a list of entities.
	 * The response body is unmarshalled as an array of type {@code T} and converted to a {@link List}.
	 *
	 * @param <T>           the element type
	 * @param soapRequest   the SOAP request with url, operationName, namespace and body
	 * @param responseClass the array response class (e.g. {@code MyType[].class})
	 * @return the list of response entities unmarshalled from the soap:Body
	 * @throws Exception if a network, marshalling or unmarshalling error occurs
	 */
	public <T> List<T> listSoapTemplate(SoapRequest<?, ?> soapRequest, Class<T[]> responseClass) throws Exception;

	/**
	 * Executes a SOAP 1.1 call via POST with Content-Type {@code text/xml} and returns the full
	 * {@link ResponseEntity} wrapping a list of entities. The body is unmarshalled as an array of
	 * type {@code T} and converted to a {@link List}, while status code and headers of the
	 * underlying HTTP response are preserved.
	 *
	 * @param <T>           the element type
	 * @param soapRequest   the SOAP request with url, operationName, namespace and body
	 * @param responseClass the array response class (e.g. {@code MyType[].class})
	 * @return the response entity including status code and headers wrapping the list
	 * @throws Exception if a network, marshalling or unmarshalling error occurs
	 */
	public <T> ResponseEntity<List<T>> responseListEntity(SoapRequest<?, ?> soapRequest, Class<T[]> responseClass) throws Exception;

}
