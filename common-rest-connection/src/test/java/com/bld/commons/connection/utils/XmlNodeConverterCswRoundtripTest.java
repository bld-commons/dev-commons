package com.bld.commons.connection.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import com.bld.commons.connection.client.RestClientConnection;
import com.bld.commons.connection.config.EnableRestConnectionConfiguration;
import com.bld.commons.connection.model.MapRequest;
import com.fasterxml.jackson.databind.JsonNode;

@SpringBootTest
@ContextConfiguration(classes = EnableRestConnectionConfiguration.class)
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
class XmlNodeConverterCswRoundtripTest {

    private static final String CSW_URL =
            "https://catalogosgi.isprambiente.it/geoportal2/csw";
    
    private final static Logger logger=LoggerFactory.getLogger(XmlNodeConverterCswRoundtripTest.class);

    @Autowired
    private RestClientConnection restClientConnection;

    @Test
    void cswXmlRoundtrip_matchesOriginalStructure() throws Exception {
        MapRequest request = MapRequest.newInstanceGet(CSW_URL);
        request.addData("SERVICE", "CSW");
        request.addData("VERSION", "2.0.2");
        request.addData("REQUEST", "GetRecords");
        request.addData("RESULTTYPE", "results");
        request.addData("TYPENAMES", "csw:Record");
        request.addData("OUTPUTSCHEMA", "http://www.isotc211.org/2005/gmd");
        request.addData("ELEMENTSETNAME", "full");
        request.addData("STARTPOSITION", "1");
        request.addData("MAXRECORDS", "1");

        String originalXml = restClientConnection.entityRestTemplate(request, String.class);
        assertNotNull(originalXml);
        assertTrue(originalXml.contains("csw:GetRecordsResponse"));

        // originalXml → JSON → canonical XML → JSON (pretty-print to match formatting)
        JsonNode jsonFromOriginal = XmlNodeConverter.fromXmlNormalized(originalXml);
        logger.info(jsonFromOriginal.toPrettyString());
        String canonical = XmlNodeConverter.toXml(jsonFromOriginal, true);
        JsonNode jsonFromCanonical = XmlNodeConverter.fromXmlNormalized(canonical);

        // Perfect rewrite: both roundtrips must produce identical JsonNode
        // (proves XML is perfectly preserved through hierarchical #index)
        assertEquals(jsonFromOriginal, jsonFromCanonical,
                "originalXml→JSON→XML→JSON: perfect reconstruction via hierarchical #index");

        // XMLUnit validation: canonical and original are semantically identical
        Diff diff = DiffBuilder.compare(originalXml)
                .withTest(canonical)
                .normalizeWhitespace()
                .ignoreWhitespace()
                .build();
        assertFalse(diff.hasDifferences(),
                "XMLUnit: canonical XML is semantically identical to original");
    }
}
