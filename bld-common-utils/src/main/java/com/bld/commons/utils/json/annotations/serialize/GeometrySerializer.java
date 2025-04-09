/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.annotations.serialize.CustomDateSerializer.java
 */
package com.bld.commons.utils.json.annotations.serialize;

import java.io.IOException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;

import com.bld.commons.utils.json.annotations.TextGeometry;
import com.bld.commons.utils.types.SridType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

// TODO: Auto-generated Javadoc
/**
 * The Class GeometrySerializer.
 */
@SuppressWarnings("serial")
@JacksonStdImpl
public class GeometrySerializer extends StdScalarSerializer<Geometry>  implements ContextualSerializer {

	/** The srid type. */
	private SridType sridType;
	
	/**
	 * Instantiates a new geometry serializer.
	 */
	protected GeometrySerializer() {
		super(Geometry.class);
	}


	/**
	 * Instantiates a new geometry serializer.
	 *
	 * @param t the t
	 * @param sridType the srid type
	 */
	protected GeometrySerializer(Class<Geometry> t, SridType sridType) {
		super(t);
		this.sridType = sridType;
	}


	/**
	 * Serialize.
	 *
	 * @param value the value
	 * @param gen the gen
	 * @param provider the provider
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void serialize(Geometry value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		String json=null;
		if(value!=null) {
			WKTWriter writer = new WKTWriter();
	        json = writer.write(value);
	        if(!SridType.NONE.equals(this.sridType))
	        	json="SRID="+sridType.value()+";"+json;
		}
		gen.writeString(json);
	}


	/**
	 * Creates the contextual.
	 *
	 * @param prov the prov
	 * @param property the property
	 * @return the json serializer
	 * @throws JsonMappingException the json mapping exception
	 */
	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
		TextGeometry textGeometry=property.getAnnotation(TextGeometry.class);
		return new GeometrySerializer(Geometry.class,textGeometry.value());
	}



}
