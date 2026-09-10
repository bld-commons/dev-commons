/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.annotations.serialize.CustomDateSerializer.java
 */
package com.bld.commons.utils.json.annotations.serialize;

import java.io.IOException;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;

import com.bld.commons.utils.GeometryUtils;
import com.bld.commons.utils.data.PostgisGeometry;
import com.bld.commons.utils.json.annotations.GeometryPostgis;
import com.bld.commons.utils.types.SpatialType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

@SuppressWarnings("serial")
@JacksonStdImpl
public class GeometrySerializer extends StdScalarSerializer<Geometry>  implements ContextualSerializer {

	@Autowired
	private ObjectMapper objMapper;

	/** The geometry utils. */
	@Autowired
	private GeometryUtils geometryUtils;

	private SpatialType spatialType;
	
	protected GeometrySerializer() {
		super(Geometry.class);
	}


	protected GeometrySerializer(Class<Geometry> t, SpatialType spatialType,ObjectMapper objMapper, GeometryUtils geometryUtils) {
		super(t);
		this.spatialType = spatialType;
		this.objMapper=objMapper;
		this.geometryUtils=geometryUtils;
	}


	@Override
	public void serialize(Geometry value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		if (value == null) {
			gen.writeObject(null);
			return;
		}
		PostgisGeometry<?> spatialModel = this.geometryUtils.serialize(value, this.spatialType);
		gen.writeObject(spatialModel);
	}


	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
		GeometryPostgis geometryPostgis=property.getAnnotation(GeometryPostgis.class);
		return new GeometrySerializer(Geometry.class,geometryPostgis.value(),this.objMapper,this.geometryUtils);
	}



}
