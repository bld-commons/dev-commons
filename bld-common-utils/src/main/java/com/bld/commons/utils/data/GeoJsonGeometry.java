package com.bld.commons.utils.data;

import com.bld.commons.utils.types.SpatialType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * The Class GeoJsonGeometry.
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
	 * Geo json.
	 *
	 * @param objMapper the obj mapper
	 * @return the string
	 * @throws JsonProcessingException the json processing exception
	 */
	@JsonIgnore
	public String geoJson(ObjectMapper objMapper) throws JsonProcessingException {
		return objMapper.writeValueAsString(this.getGeometry());
	}
	
}
