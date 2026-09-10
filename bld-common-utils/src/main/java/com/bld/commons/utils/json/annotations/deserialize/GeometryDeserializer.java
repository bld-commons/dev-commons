/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.annotations.deserialize.CustomByteDeserializer.java
 */
package com.bld.commons.utils.json.annotations.deserialize;

import java.io.IOException;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;

import com.bld.commons.utils.GeometryUtils;
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

// TODO: Auto-generated Javadoc
/**
 * The Class GeometryDeserializer.
 */
public class GeometryDeserializer extends JsonDeserializer<Geometry> implements ContextualDeserializer {

	/** The spatial type. */
	private SpatialType spatialType;

	/** The obj mapper. */
	@Autowired
	private ObjectMapper objMapper;

	/** The geometry utils. */
	@Autowired
	private GeometryUtils geometryUtils;

	/**
	 * Instantiates a new geometry deserializer.
	 */
	public GeometryDeserializer() {
		super();
	}

	/**
	 * Instantiates a new geometry deserializer.
	 *
	 * @param spatialType the spatial type
	 * @param objMapper the obj mapper
	 * @param geometryUtils the geometry utils
	 */
	public GeometryDeserializer(SpatialType spatialType, ObjectMapper objMapper, GeometryUtils geometryUtils) {
		super();
		this.spatialType = spatialType;
		this.objMapper = objMapper;
		this.geometryUtils = geometryUtils;
	}

	/**
	 * Creates the contextual.
	 *
	 * @param ctxt the ctxt
	 * @param property the property
	 * @return the json deserializer
	 * @throws JsonMappingException the json mapping exception
	 */
	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
		GeometryPostgis postgisGeometry = property.getAnnotation(GeometryPostgis.class);
		return new GeometryDeserializer(postgisGeometry.value(), this.objMapper, this.geometryUtils);
	}

	/**
	 * Deserialize.
	 *
	 * @param p the p
	 * @param ctxt the ctxt
	 * @return the geometry
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws JacksonException the jackson exception
	 */
	@Override
	public Geometry deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
		TreeNode treeNode = p.readValueAsTree();
		if (treeNode == null) return null;
		return this.geometryUtils.parse(treeNode.toString(), this.spatialType);
	}

}
