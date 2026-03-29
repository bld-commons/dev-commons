/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.model.SoapRequest.java
 */
package com.bld.commons.connection.model;

import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Request for SOAP 1.1 calls.
 * Two generic type parameters:
 * <ul>
 *   <li>{@code B} — type of the SOAP operation body:
 *       {@code Map<String,Object>} or a JAXB class annotated with {@code @XmlRootElement}.</li>
 *   <li>{@code H} — type of the {@code soap:Header} block data:
 *       {@code Map<String,Object>} or a JAXB class. Use {@code Void} when no header is needed.</li>
 * </ul>
 * The HTTP method is always POST. Content-Type and Accept are preset to {@code text/xml};
 * authentication and custom headers can be added via the methods inherited from {@link BasicRequest}.
 *
 * @param <B> the type of the SOAP operation body
 * @param <H> the type of the {@code soap:Header} data
 */
public class SoapRequest<B, H> extends BasicRequest<B> {

	/** The operation name (root element inside the SOAP body, e.g. "CelsiusToFahrenheit"). */
	private final String operationName;

	/** The XML namespace of the operation (e.g. "https://www.w3schools.com/xml/"). */
	private final String namespace;

	/**
	 * Value of the {@code SOAPAction} HTTP header (SOAP 1.1).
	 * Sent as an empty string {@code ""} when {@code null}.
	 */
	private String soapAction;

	/** The {@code soap:Header} block of the envelope. {@code null} means no SOAP header. */
	private SoapHeader<H> header;

	/**
	 * Protected constructor. Content-Type and Accept are set to {@code text/xml}.
	 *
	 * @param url           the SOAP endpoint URL
	 * @param operationName the SOAP operation name
	 * @param namespace     the XML namespace
	 */
	protected SoapRequest(String url, String operationName, String namespace) {
		super(url, HttpMethod.POST);
		this.operationName = operationName;
		this.namespace = namespace;
		this.getHttpHeaders().setContentType(MediaType.TEXT_XML);
		this.getHttpHeaders().setAccept(List.of(MediaType.TEXT_XML));
	}

	/**
	 * Factory method.
	 *
	 * @param <B>           the body type
	 * @param <H>           the header type
	 * @param url           the SOAP endpoint URL
	 * @param operationName the SOAP operation name
	 * @param namespace     the XML namespace
	 * @return a new SoapRequest instance
	 */
	public static <B, H> SoapRequest<B, H> newInstance(String url, String operationName, String namespace) {
		return new SoapRequest<>(url, operationName, namespace);
	}

	/**
	 * Sets the request body.
	 *
	 * @param data the body object (Map or JAXB class)
	 */
	public void setData(B data) {
		super.data = data;
	}

	/**
	 * Sets the SOAP header block of the envelope.
	 *
	 * @param header the {@link SoapHeader} containing the {@code soap:Header} data
	 */
	public void setHeader(SoapHeader<H> header) {
		this.header = header;
	}

	/**
	 * Gets the operation name.
	 *
	 * @return the operation name
	 */
	public String getOperationName() {
		return operationName;
	}

	/**
	 * Gets the namespace.
	 *
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Gets the soap action.
	 *
	 * @return the soap action
	 */
	public String getSoapAction() {
		return soapAction;
	}

	/**
	 * Sets the soap action.
	 *
	 * @param soapAction the SOAPAction header value (e.g. "urn:MyService#myOperation")
	 */
	public void setSoapAction(String soapAction) {
		this.soapAction = soapAction;
	}

	/**
	 * Gets the SOAP header block.
	 *
	 * @return the SOAP header, or {@code null} if not configured
	 */
	public SoapHeader<H> getHeader() {
		return header;
	}

}
