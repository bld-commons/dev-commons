package com.bld.commons.utils.data;

import com.bld.commons.utils.types.SpatialType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * PostGIS geometry holder that stores the spatial data as a GeoJSON {@link JsonNode}.
 *
 * <p>Used in conjunction with {@link com.bld.commons.utils.json.annotations.GeometryPostgis}
 * (with {@code value = SpatialType.GeoJSON}) to deserialise incoming GeoJSON payloads
 * and serialise JTS {@link org.locationtech.jts.geom.Geometry} objects back to GeoJSON.</p>
 *
 * @author Francesco Baldi
 */
public class GeoJsonGeometry extends PostgisGeometry<JsonNode> {


	/**
	 * Instantiates a new geo json geometry.
	 */
	public GeoJsonGeometry() {
		super();
	}


	/**
	 * Instantiates a new geo json geometry.
	 *
	 * @param spatialType the spatial type
	 * @param geometry the geometry
	 * @param srid the srid
	 */
	public GeoJsonGeometry(SpatialType spatialType, JsonNode geometry, Integer srid) {
		super(spatialType, geometry, srid);
	}

	/**
	 * Serialises the contained GeoJSON {@link JsonNode} to its JSON string representation.
	 *
	 * @param objMapper the Jackson {@link ObjectMapper} to use for serialisation
	 * @return a GeoJSON string representing the geometry
	 * @throws JsonProcessingException if the node cannot be serialised
	 */
	@JsonIgnore
	public String geoJson(ObjectMapper objMapper) throws JsonProcessingException {
		return objMapper.writeValueAsString(this.getGeometry());
	}
	
}
