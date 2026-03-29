/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.utils.SoapXmlBuilder.java
 */
package com.bld.commons.connection.utils;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.bld.commons.connection.model.SoapHeader;
import com.bld.commons.connection.model.SoapRequest;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * Builds the XML of a SOAP 1.1 envelope from a {@link SoapRequest}.
 * Supports two strategies for the operation body:
 * <ul>
 *   <li><b>Map&lt;String,Object&gt;</b>: each entry becomes a child XML element;
 *       values of type {@code Map} are nested recursively.</li>
 *   <li><b>JAXB object</b>: the object is marshalled via {@link JAXBContext};
 *       the class must be annotated with {@code @XmlRootElement}.</li>
 * </ul>
 */
public final class SoapXmlBuilder {

    /** The Constant SOAP_NS. */
    private static final String SOAP_NS = "http://schemas.xmlsoap.org/soap/envelope/";

    /** The factory. */
    private static final DocumentBuilderFactory FACTORY = createFactory();

    /**
     * Creates the factory.
     *
     * @return the document builder factory
     */
    private static DocumentBuilderFactory createFactory() {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            f.setExpandEntityReferences(false);
            return f;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Instantiates a new soap xml builder.
     */
    private SoapXmlBuilder() {
    }

    /**
     * Builds the complete SOAP envelope for the given request.
     * If {@link SoapRequest#getHeader()} is set, a {@code soap:Header} block is included
     * before the {@code soap:Body}.
     *
     * @param soapRequest the SOAP request with operationName, namespace, optional header and body
     * @return the XML string of the SOAP envelope
     * @throws Exception if a JAXB marshalling error occurs
     */
    public static String buildEnvelope(SoapRequest<?, ?> soapRequest) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        sb.append("<soap:Envelope xmlns:soap=\"").append(SOAP_NS).append("\">");
        SoapHeader<?> header = soapRequest.getHeader();
        if (header != null && header.getData() != null) {
            sb.append("<soap:Header>");
            sb.append(buildContent(header.getOperationName(), header.getNamespace(), header.getData()));
            sb.append("</soap:Header>");
        }
        sb.append("<soap:Body>");
        sb.append(buildContent(soapRequest.getOperationName(), soapRequest.getNamespace(), soapRequest.getData()));
        sb.append("</soap:Body>");
        sb.append("</soap:Envelope>");
        return sb.toString();
    }

    /**
     * Routes XML element construction to the Map or JAXB strategy.
     *
     * @param operationName the operation name
     * @param namespace     the XML namespace
     * @param data          the body data (Map or JAXB object)
     * @return the XML string for the element
     * @throws Exception if a JAXB marshalling error occurs
     */
    @SuppressWarnings("unchecked")
    private static String buildContent(String operationName, String namespace, Object data) throws Exception {
        if (data instanceof Map) {
            return buildFromMap(operationName, namespace, (Map<String, Object>) data);
        }
        return buildFromJaxb(data);
    }

    /**
     * Builds the operation XML element from a Map.
     * Map entries become child elements; Map values are nested recursively.
     *
     * @param operationName the operation name
     * @param namespace     the XML namespace
     * @param data          the map of field names to values
     * @return the XML string for the operation element
     */
    private static String buildFromMap(String operationName, String namespace, Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(operationName);
        if (StringUtils.isNotEmpty(namespace)) {
            sb.append(" xmlns=\"").append(namespace).append("\"");
        }
        sb.append(">");
        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                appendElement(sb, entry.getKey(), entry.getValue());
            }
        }
        sb.append("</").append(operationName).append(">");
        return sb.toString();
    }

    /**
     * Recursively appends an XML element to the builder.
     * <ul>
     *   <li>{@code List}  — repeats the tag for each list item</li>
     *   <li>{@code Map}   — generates child tags recursively</li>
     *   <li>other         — serialised as the element text value</li>
     * </ul>
     *
     * @param sb    the string builder
     * @param name  the element name
     * @param value the element value
     */
    @SuppressWarnings("unchecked")
    private static void appendElement(StringBuilder sb, String name, Object value) {
        if (value instanceof List) {
            for (Object item : (List<?>) value) {
                appendElement(sb, name, item);
            }
            return;
        }
        sb.append("<").append(name).append(">");
        if (value instanceof Map) {
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
                appendElement(sb, entry.getKey(), entry.getValue());
            }
        } else if (value != null) {
            sb.append(value);
        }
        sb.append("</").append(name).append(">");
    }

    /**
     * Marshals a JAXB object to XML (without the XML declaration).
     * The class must be annotated with {@code @XmlRootElement}.
     *
     * @param data the JAXB object to marshal
     * @return the XML string
     * @throws Exception if marshalling fails
     */
    private static String buildFromJaxb(Object data) throws Exception {
        JAXBContext context = JAXBContext.newInstance(data.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        StringWriter writer = new StringWriter();
        marshaller.marshal(data, writer);
        return writer.toString();
    }

    /**
     * Extracts the first child element of the {@code soap:Body} and unmarshals it to type {@code T}.
     * If {@code responseClass} is {@code String.class} the raw envelope XML is returned without
     * further processing. The target class does not require {@code @XmlRootElement}: {@code @XmlType}
     * is sufficient because unmarshalling is performed directly on the DOM node via
     * {@link Unmarshaller#unmarshal(Node, Class)}.
     *
     * @param <T>           the type of the response object
     * @param soapXml       the raw XML of the SOAP response
     * @param responseClass the target class for unmarshalling
     * @return the unmarshalled object of type T from the SOAP body
     * @throws Exception if the XML cannot be parsed or the type is not JAXB-compatible
     */
    @SuppressWarnings("unchecked")
    public static <T> T unmarshalBody(String soapXml, Class<T> responseClass) throws Exception {
        if (responseClass == String.class) {
            return (T) soapXml;
        }
        DocumentBuilder docBuilder = FACTORY.newDocumentBuilder();
        Document doc = docBuilder.parse(new InputSource(new StringReader(soapXml)));
        NodeList bodyNodes = doc.getElementsByTagNameNS(SOAP_NS, "Body");
        Element bodyElement = (Element) bodyNodes.item(0);
        Node firstChild = bodyElement.getFirstChild();
        while (firstChild != null && firstChild.getNodeType() != Node.ELEMENT_NODE) {
            firstChild = firstChild.getNextSibling();
        }
        if (firstChild == null) {
            return null;
        }
        Element bodyFirstElement = (Element) firstChild;
        if (responseClass == JsonNode.class) {
            return (T) XmlNodeConverter.fromXml(soapXml);
        }
        JAXBContext context = JAXBContext.newInstance(responseClass);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return unmarshaller.unmarshal(bodyFirstElement, responseClass).getValue();
    }

}
