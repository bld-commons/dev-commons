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
import org.locationtech.jts.io.WKTReader;

import com.bld.commons.utils.json.annotations.TextGeometry;
import com.bld.commons.utils.types.SridType;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

/**
 * The Class GeometryDeserializer.
 */
public class GeometryDeserializer extends JsonDeserializer<Geometry> implements ContextualDeserializer{

	/** The srid type. */
	private SridType sridType;
	
	
	/**
	 * Instantiates a new geometry deserializer.
	 */
	public GeometryDeserializer() {
		super();
	}
	
	

	/**
	 * Instantiates a new geometry deserializer.
	 *
	 * @param sridType the srid type
	 */
	public GeometryDeserializer(SridType sridType) {
		super();
		this.sridType = sridType;
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
		TextGeometry textGeometry=property.getAnnotation(TextGeometry.class);
		return new GeometryDeserializer(textGeometry.value());
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
		String textGeometry = p.getText();
		Geometry geometry=null;
		if(StringUtils.isNotBlank(textGeometry)) {
			try {
				int index=0;
				if(textGeometry.indexOf(";")>=0)
					index=textGeometry.indexOf(";")+1;
				geometry = new WKTReader().read(textGeometry.substring(index));
				geometry.setSRID(this.sridType.value());
			} catch (ParseException e) {
				throw new IOException(e);
			}
		}
			
		return geometry;
	}

	
}
