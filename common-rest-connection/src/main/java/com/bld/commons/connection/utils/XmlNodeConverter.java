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
 * Single-shape XML &lt;-&gt; {@link JsonNode} converter.
 *
 * <p>Forward conversion is exposed by {@link #fromXmlNormalized(String)} and
 * {@link #fromElementNormalized(Element)}; the inverse is {@link #toXml(JsonNode)}.
 * A round-trip {@code XML -> JsonNode -> XML} preserves the original document order
 * of the children, which is required when the target XSD declares an
 * {@code xs:sequence} of heterogeneous elements (e.g. {@code <a/><b/><a/><b/>}).
 *
 * <p>Shape rules:
 * <ul>
 *   <li>Qualified tag name {@code prefix:localName} is used as key (or just
 *       {@code localName} when no prefix);</li>
 *   <li>attributes become string fields prefixed with {@code @}
 *       (e.g. {@code "@scheme"});</li>
 *   <li>text content is always stored under the {@code value} key;</li>
 *   <li>single child elements become an {@link ObjectNode};</li>
 *   <li>repeated child elements with the same qualified name become an
 *       {@link ArrayNode} of {@link ObjectNode}s;</li>
 *   <li>every child {@link ObjectNode} (single or array item) carries a
 *       {@code #index} integer field with its 0-based position among the
 *       siblings of the parent element &mdash; this is what lets
 *       {@link #toXml(JsonNode)} restore the original ordering;</li>
 *   <li>any field whose name starts with {@code #} is metadata and is
 *       <em>skipped</em> by {@link #toXml(JsonNode)} when emitting XML;</li>
 *   <li>namespace declarations ({@code xmlns:*}) are discarded.</li>
 * </ul>
 */
public final class XmlNodeConverter {

    /** Prefix used for XML attributes in the JsonNode shape. */
    private static final String ATTR_PREFIX = "@";

    /** Key used for the textual content of an element. */
    private static final String TEXT_KEY = "value";

    /**
     * Key used to record the document-order position of an element among its
     * siblings (local index, scoped to the parent element). The leading
     * {@code #} marks this field as metadata, so {@link #toXml(JsonNode)} never
     * emits it as an XML node.
     */
    private static final String INDEX_KEY = "#index";

    /** Generic prefix marking metadata fields that must not be serialized to XML. */
    private static final String META_PREFIX = "#";

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
     * Any {@code <!DOCTYPE>} declaration in the input is stripped before parsing
     * to avoid DTD resolution.
     *
     * @param xml the XML string to convert
     * @return the JsonNode corresponding to the root element
     * @throws Exception if the XML cannot be parsed
     */
    public static JsonNode fromXmlNormalized(String xml) throws Exception {
        String sanitized = xml.replaceFirst("(?s)<!DOCTYPE\\b[^\\[>]*(?:\\[[^\\]]*])?[^>]*>", "");
        DocumentBuilder builder = FACTORY.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(sanitized)));
        return fromElementNormalized(doc.getDocumentElement());
    }

    /**
     * Converts an already-parsed DOM {@link Element} to a {@link JsonNode}.
     * Used by {@link SoapXmlBuilder} to avoid re-parsing the SOAP XML.
     *
     * @param element the DOM root element to convert
     * @return the JsonNode corresponding to the element
     */
    public static JsonNode fromElementNormalized(Element element) {
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.set(qualifiedName(element), buildNode(element));
        return result;
    }

    /**
     * Recursively builds a Jackson {@link ObjectNode} from a DOM {@link Element}.
     * See the class-level Javadoc for the shape rules. Each child ObjectNode
     * receives a {@code #index} carrying its 0-based document-order position
     * among the siblings of {@code element}.
     *
     * @param element the DOM element to convert
     * @return the ObjectNode representation, with #index on every child
     */
    private static ObjectNode buildNode(Element element) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();

        NamedNodeMap attrMap = element.getAttributes();
        for (int i = 0; i < attrMap.getLength(); i++) {
            Node attr = attrMap.item(i);
            String attrName = attr.getNodeName();
            if (!attrName.equals("xmlns") && !attrName.startsWith("xmlns:")) {
                node.put(ATTR_PREFIX + attrName, attr.getNodeValue());
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
            Map<String, List<ObjectNode>> grouped = new LinkedHashMap<>();
            for (int i = 0; i < childElements.size(); i++) {
                Element child = childElements.get(i);
                ObjectNode childNode = buildNode(child);
                childNode.put(INDEX_KEY, i);
                grouped.computeIfAbsent(qualifiedName(child), k -> new ArrayList<>()).add(childNode);
            }
            log.debug("buildNode <{}>: {} distinct child type(s), {} total children",
                    qualifiedName(element), grouped.size(), childElements.size());
            grouped.forEach((childType, nodes) -> {
                if (nodes.size() == 1) {
                    node.set(childType, nodes.get(0));
                } else {
                    log.debug("buildNode <{}>: '{}' → ArrayNode[{}]", qualifiedName(element), childType, nodes.size());
                    ArrayNode arr = JsonNodeFactory.instance.arrayNode();
                    nodes.forEach(arr::add);
                    node.set(childType, arr);
                }
            });
            String text = textContent.toString().trim();
            if (!text.isEmpty()) {
                node.put(TEXT_KEY, text);
            }
        } else {
            String text = textContent.toString().trim();
            if (!text.isEmpty()) {
                node.put(TEXT_KEY, text);
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
     * Serializes a {@link JsonNode} back to an XML string.
     *
     * <p>Conventions consumed:
     * <ul>
     *   <li>the root ObjectNode must have a single property whose key is the
     *       qualified name of the root element;</li>
     *   <li>fields prefixed with {@code @} become XML attributes;</li>
     *   <li>fields prefixed with {@code #} are metadata and are skipped (in
     *       particular {@code #index} is used only to reorder siblings,
     *       never emitted);</li>
     *   <li>the {@code value} field becomes the textual content of the element;</li>
     *   <li>every other field is treated as a child element &mdash; when its
     *       value is an {@link ArrayNode}, the tag is repeated once per array
     *       entry;</li>
     *   <li>siblings are emitted in ascending {@code #index} order; entries
     *       without {@code #index} are appended after the indexed ones, in
     *       encounter order.</li>
     * </ul>
     *
     * <p>The output does <b>not</b> include an XML declaration.
     *
     * @param node the JsonNode to serialize (root must wrap a single named element)
     * @return the XML string
     */
    public static String toXml(JsonNode node) {
        if (node == null || !node.isObject() || node.size() != 1) {
            throw new IllegalArgumentException("Root JsonNode must be an ObjectNode with exactly one property (the root element name).");
        }
        Map.Entry<String, JsonNode> root = node.fields().next();
        StringBuilder sb = new StringBuilder();
        writeElement(sb, root.getKey(), root.getValue());
        return sb.toString();
    }

    /**
     * Writes a single XML element (or a sequence of repeated elements, when value is
     * an ArrayNode) to the builder.
     *
     * @param sb     the output buffer
     * @param name   the qualified element name
     * @param value  the JsonNode carrying attributes, value and children
     */
    private static void writeElement(StringBuilder sb, String name, JsonNode value) {
        if (value.isArray()) {
            for (JsonNode item : value) {
                writeElement(sb, name, item);
            }
            return;
        }
        if (!value.isObject()) {
            sb.append('<').append(name).append('>').append(escapeXml(value.asText())).append("</").append(name).append('>');
            return;
        }
        ObjectNode obj = (ObjectNode) value;

        sb.append('<').append(name);
        obj.fields().forEachRemaining(f -> {
            if (f.getKey().startsWith(ATTR_PREFIX)) {
                sb.append(' ').append(f.getKey().substring(1)).append("=\"").append(escapeXmlAttr(f.getValue().asText())).append('"');
            }
        });

        List<Map.Entry<String, JsonNode>> childEntries = new ArrayList<>();
        obj.fields().forEachRemaining(f -> {
            String k = f.getKey();
            if (k.startsWith(ATTR_PREFIX) || k.startsWith(META_PREFIX)) return;
            if (TEXT_KEY.equals(k) && f.getValue().isValueNode()) return;
            childEntries.add(f);
        });
        String text = null;
        JsonNode textNode = obj.get(TEXT_KEY);
        if (textNode != null && textNode.isValueNode()) {
            text = textNode.asText();
        }

        if (childEntries.isEmpty() && (text == null || text.isEmpty())) {
            sb.append("/>");
            return;
        }
        sb.append('>');

        List<OrderedChild> ordered = new ArrayList<>();
        int fallbackIdx = Integer.MAX_VALUE / 2;
        for (Map.Entry<String, JsonNode> e : childEntries) {
            JsonNode v = e.getValue();
            if (v.isArray()) {
                for (JsonNode item : v) {
                    ordered.add(new OrderedChild(e.getKey(), item, indexOf(item, fallbackIdx++)));
                }
            } else {
                ordered.add(new OrderedChild(e.getKey(), v, indexOf(v, fallbackIdx++)));
            }
        }
        ordered.sort((x, y) -> Integer.compare(x.index, y.index));
        for (OrderedChild oc : ordered) {
            writeElement(sb, oc.name, oc.value);
        }

        if (text != null && !text.isEmpty()) {
            sb.append(escapeXml(text));
        }
        sb.append("</").append(name).append('>');
    }

    /**
     * Extracts the {@code #index} value from a node, or returns the supplied fallback
     * when the node is not an object or has no {@code #index} field.
     *
     * @param n         the node
     * @param fallback  the value to return when no index is present
     * @return the index to use for sorting
     */
    private static int indexOf(JsonNode n, int fallback) {
        if (n != null && n.isObject()) {
            JsonNode idx = n.get(INDEX_KEY);
            if (idx != null && idx.isInt()) return idx.asInt();
        }
        return fallback;
    }

    /**
     * Holder for a child to be emitted, capturing its tag name, its node and the
     * resolved sibling index.
     */
    private static final class OrderedChild {
        final String name;
        final JsonNode value;
        final int index;
        OrderedChild(String name, JsonNode value, int index) {
            this.name = name;
            this.value = value;
            this.index = index;
        }
    }

    /**
     * Escapes the XML predefined entities in element textual content.
     *
     * @param s the raw text
     * @return the escaped text safe for XML PCDATA
     */
    private static String escapeXml(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<': out.append("&lt;"); break;
                case '>': out.append("&gt;"); break;
                case '&': out.append("&amp;"); break;
                default: out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * Escapes characters that are not valid inside an XML attribute value enclosed
     * by double quotes.
     *
     * @param s the raw attribute value
     * @return the escaped attribute value
     */
    private static String escapeXmlAttr(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<': out.append("&lt;"); break;
                case '>': out.append("&gt;"); break;
                case '&': out.append("&amp;"); break;
                case '"': out.append("&quot;"); break;
                default: out.append(c);
            }
        }
        return out.toString();
    }

}
