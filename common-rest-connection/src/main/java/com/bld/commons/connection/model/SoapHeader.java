/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.model.SoapHeader.java
 */
package com.bld.commons.connection.model;

/**
 * Represents the {@code soap:Header} block of a SOAP 1.1 envelope.
 * The generic type {@code H} is the type of the header body:
 * it can be a {@code Map<String,Object>} (keys become XML tags)
 * or a JAXB-annotated class.
 *
 * @param <H> the type of the SOAP header data
 */
public class SoapHeader<H> {

	/** The root element name of the header (used only when {@code data} is a Map). */
	private final String operationName;

	/** The XML namespace of the header root element. */
	private final String namespace;

	/** The header data (Map or JAXB object). */
	protected H data;

	/**
	 * Protected constructor.
	 *
	 * @param operationName the root element name of the header
	 * @param namespace     the XML namespace of the header root element
	 */
	protected SoapHeader(String operationName, String namespace) {
		this.operationName = operationName;
		this.namespace = namespace;
	}

	/**
	 * Factory method.
	 *
	 * @param <H>           the type of the header data
	 * @param operationName the root element name of the header
	 * @param namespace     the XML namespace of the header root element
	 * @return a new SoapHeader instance
	 */
	public static <H> SoapHeader<H> newInstance(String operationName, String namespace) {
		return new SoapHeader<>(operationName, namespace);
	}

	/**
	 * Sets the header data.
	 *
	 * @param data the header object (Map or JAXB class)
	 */
	public void setData(H data) {
		this.data = data;
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
	 * Gets the data.
	 *
	 * @return the data
	 */
	public H getData() {
		return data;
	}

}
