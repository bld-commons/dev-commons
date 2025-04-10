package com.bld.commons.utils.data;

import com.bld.commons.utils.types.SpatialType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

// TODO: Auto-generated Javadoc
/**
 * The Class WKTGeometry.
 */
public class WKTGeometry extends PostgisGeometry<String> {

	/**
	 * Instantiates a new WKT geometry.
	 */
	public WKTGeometry() {
		super();
	}

	/**
	 * Instantiates a new WKT geometry.
	 *
	 * @param spatialType the spatial type
	 * @param geometry the geometry
	 * @param srid the srid
	 */
	public WKTGeometry(SpatialType spatialType, String geometry, Integer srid) {
		super(spatialType, geometry, srid);
	}

	/**
	 * Srid geometry.
	 *
	 * @return the string
	 */
	@JsonProperty(value = "sridGeometry",access = Access.READ_ONLY)
	public String sridGeometry() {
		String sridGeometry=this.getGeometry();
		if(this.getSrid()!=null && this.getSrid()>0)
			sridGeometry="SRID="+this.getSrid()+";"+sridGeometry;
		return sridGeometry;
	}
}
