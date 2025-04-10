/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.annotations.serialize.CustomDateSerializer.java
 */
package com.bld.commons.utils.json.annotations.serialize;

import java.io.IOException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.locationtech.jts.io.kml.KMLWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.bld.commons.utils.data.GeoJsonGeometry;
import com.bld.commons.utils.data.KMLGeometry;
import com.bld.commons.utils.data.PostgisGeometry;
import com.bld.commons.utils.data.WKBGeometry;
import com.bld.commons.utils.data.WKTGeometry;
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
	
	private SpatialType spatialType;
	
	protected GeometrySerializer() {
		super(Geometry.class);
	}


	protected GeometrySerializer(Class<Geometry> t, SpatialType spatialType,ObjectMapper objMapper) {
		super(t);
		this.spatialType = spatialType;
		this.objMapper=objMapper;
	}


	@Override
	public void serialize(Geometry value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		PostgisGeometry<?> spatialModel=null;
		if(value!=null) {
			switch(this.spatialType) {
			case GeoJSON:
				GeoJsonWriter geoJsonWriter = new GeoJsonWriter();
				spatialModel=new GeoJsonGeometry(SpatialType.GeoJSON, this.objMapper.readTree(geoJsonWriter.write(value)),value.getSRID());
				break;
			case WKB:
				WKBWriter wkbWriter = new WKBWriter();
				spatialModel=new WKBGeometry(SpatialType.WKB,wkbWriter.write(value),value.getSRID());
				break;
			case WKT:
				WKTWriter wktWriter = new WKTWriter();
				spatialModel=new WKTGeometry(SpatialType.WKT,wktWriter.write(value),value.getSRID());
				break;
			case KML:
				KMLWriter kmlWriter= new KMLWriter();
				spatialModel=new KMLGeometry(SpatialType.KML, kmlWriter.write(value), value.getSRID());
				break;
			default:
				break;
			
			}
			
		}
		gen.writeObject(spatialModel);
	}


	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
		GeometryPostgis geometryPostgis=property.getAnnotation(GeometryPostgis.class);
		return new GeometrySerializer(Geometry.class,geometryPostgis.value(),this.objMapper);
	}



}
