/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.annotations.deserialize.CustomByteDeserializer.java
 */
package com.bld.commons.utils.json.annotations.deserialize;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;

import com.bld.commons.utils.data.PostgisGeometry;
import com.bld.commons.utils.data.WKBGeometry;
import com.bld.commons.utils.data.WKTGeometry;
import com.bld.commons.utils.json.annotations.GeometryPostgis;
import com.bld.commons.utils.types.SpatialType;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

public class GeometryDeserializer extends JsonDeserializer<Geometry> implements ContextualDeserializer {

	private SpatialType spatialType;

	@Autowired
	private ObjectMapper objMapper;

	public GeometryDeserializer() {
		super();
	}

	public GeometryDeserializer(SpatialType spatialType, ObjectMapper objMapper) {
		super();
		this.spatialType = spatialType;
		this.objMapper = objMapper;
	}

	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
		GeometryPostgis postgisGeometry = property.getAnnotation(GeometryPostgis.class);
		return new GeometryDeserializer(postgisGeometry.value(), this.objMapper);
	}

	@Override
	public Geometry deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
		TreeNode treeNode = p.readValueAsTree();
		Geometry geometry=null;
		if (treeNode!=null) {
			String textGeometry=treeNode.toString();
			try {

				switch (this.spatialType) {
				case WKB:
					WKBReader wkbReader=new WKBReader();
					WKBGeometry wkbGeometry=this.objMapper.readValue(textGeometry,WKBGeometry.class);
					geometry=wkbReader.read(wkbGeometry.getGeometry());
					setSRID(geometry, wkbGeometry);
					break;
				case WKT:
					WKTReader wktReader = new WKTReader();
					WKTGeometry wktGeometry = this.objMapper.readValue(textGeometry, WKTGeometry.class);
					geometry = wktReader.read(wktGeometry.getGeometry());
					setSRID(geometry, wktGeometry);
					break;
				default:
					break;

				}
			} catch (ParseException e) {
				throw new IOException(e);
			}
		}

		return geometry;
	}

	private void setSRID(Geometry geometry,PostgisGeometry<?>postgisGeometry) {
		if (postgisGeometry.getSrid() != null)
			geometry.setSRID(postgisGeometry.getSrid());
	}
	
}
