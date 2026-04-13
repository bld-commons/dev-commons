/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.utils.XmlNodeConverter.java
 */
package com.bld.commons.connection.utils;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Converts XML to {@link JsonNode}.
 * Accepts either a raw XML string or an already-parsed DOM {@link Element}
 * (used internally by {@link SoapXmlBuilder} to avoid a second parse).
 * Conversion rules:
 * <ul>
 *   <li>The qualified tag name (prefix:localName) becomes the node key.</li>
 *   <li>Attributes are added as direct keys (no nested "attributes" object).</li>
 *   <li>Child elements with the same name are grouped: one → ObjectNode, many → ArrayNode.</li>
 *   <li>Leaf tags with text only produce {@code "value": "text"}.</li>
 *   <li>Namespace declarations ({@code xmlns:*}) are ignored.</li>
 * </ul>
 */
public final class XmlNodeConverter {

    private static final Logger log = LoggerFactory.getLogger(XmlNodeConverter.class);

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
     * Instantiates a new xml node converter.
     */
    private XmlNodeConverter() {
    }

    /**
     * Parses an XML string and converts the root element to a {@link JsonNode}.
     *
     * @param xml the XML string to convert
     * @return the JsonNode corresponding to the root element
     * @throws Exception if the XML cannot be parsed
     */
    public static JsonNode fromXml(String xml) throws Exception {
        DocumentBuilder builder = FACTORY.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));
        return fromElement(doc.getDocumentElement());
    }

    /**
     * Converts an already-parsed DOM {@link Element} to a {@link JsonNode}.
     * Used by {@link SoapXmlBuilder} to avoid re-parsing the SOAP XML.
     *
     * @param element the DOM root element to convert
     * @return the JsonNode corresponding to the element
     */
    public static JsonNode fromElement(Element element) {
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.set(qualifiedName(element), buildNode(element));
        return result;
    }

    /**
     * Recursively builds a Jackson {@link ObjectNode} from a DOM {@link Element}.
     * Attributes are mapped as direct string fields (namespace declarations are skipped).
     * Child elements are grouped by qualified name: a single occurrence produces a nested
     * {@link ObjectNode}, multiple occurrences produce an {@link ArrayNode}.
     * Leaf elements that contain only text produce a {@code "value"} string field.
     *
     * @param element the DOM element to convert
     * @return the ObjectNode representation of the element's children and attributes
     */
    private static ObjectNode buildNode(Element element) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();

        NamedNodeMap attrMap = element.getAttributes();
        for (int i = 0; i < attrMap.getLength(); i++) {
            Node attr = attrMap.item(i);
            String attrName = attr.getNodeName();
            if (!attrName.equals("xmlns") && !attrName.startsWith("xmlns:")) {
                node.put(attrName, attr.getNodeValue());
            }
        }

        List<Element> childElements = new ArrayList<>();
        StringBuilder textContent = new StringBuilder();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                childElements.add((Element) child);
            } else if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.CDATA_SECTION_NODE) {
                String text = child.getNodeValue().trim();
                if (!text.isEmpty()) {
                    textContent.append(text);
                }
            }
        }

        if (!childElements.isEmpty()) {
            Map<String, List<Element>> grouped = new LinkedHashMap<>();
            for (Element child : childElements) {
                grouped.computeIfAbsent(qualifiedName(child), k -> new ArrayList<>()).add(child);
            }
            log.debug("buildNode <{}>: {} distinct child type(s), {} total children",
                    qualifiedName(element), grouped.size(), childElements.size());
            grouped.forEach((childType, elements) -> {
                if (elements.size() == 1) {
                    Element e = elements.get(0);
                    if (isPlainText(e)) {
                        node.put(childType, textContent(e));
                    } else {
                        node.set(childType, buildNode(e));
                    }
                } else {
                    log.debug("buildNode <{}>: '{}' → ArrayNode[{}]", qualifiedName(element), childType, elements.size());
                    boolean allPlainText = elements.stream().allMatch(XmlNodeConverter::isPlainText);
                    ArrayNode arr = JsonNodeFactory.instance.arrayNode();
                    if (allPlainText) {
                        elements.forEach(e -> arr.add(textContent(e)));
                    } else {
                        elements.forEach(e -> arr.add(buildNode(e)));
                    }
                    node.set(childType, arr);
                }
            });
        } else {
            String text = textContent.toString().trim();
            if (!text.isEmpty()) {
                node.put("value", text);
            }
        }

        return node;
    }

    /**
     * Returns the qualified name of an element, combining the namespace prefix and local name.
     * If no prefix is present, the local name is returned as-is.
     * Example: an element {@code soap:Envelope} returns {@code "soap:Envelope"}.
     *
     * @param element the DOM element
     * @return the qualified name in the form {@code prefix:localName} or just {@code localName}
     */
    private static String qualifiedName(Element element) {
        String prefix = element.getPrefix();
        String localName = element.getLocalName();
        return (prefix != null && !prefix.isEmpty()) ? prefix + ":" + localName : localName;
    }

    /**
     * Checks if the element contains only plain text (no child elements, no significant attributes).
     *
     * @param element the element
     * @return true if plain text only
     */
    private static boolean isPlainText(Element element) {
        if (hasSignificantAttributes(element)) return false;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) return false;
        }
        return true;
    }

    /**
     * Checks whether the element has attributes other than namespace declarations.
     *
     * @param element the element
     * @return true if significant attributes are present
     */
    private static boolean hasSignificantAttributes(Element element) {
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            String name = attrs.item(i).getNodeName();
            if (!name.equals("xmlns") && !name.startsWith("xmlns:")) return true;
        }
        return false;
    }

    /**
     * Returns the trimmed text content of an element (TEXT_NODE and CDATA only).
     *
     * @param element the element
     * @return the text content
     */
    private static String textContent(Element element) {
        StringBuilder sb = new StringBuilder();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.CDATA_SECTION_NODE) {
                sb.append(child.getNodeValue());
            }
        }
        return sb.toString().trim();
    }

}
