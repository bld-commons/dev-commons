package com.bld.commons.utils.data;

import com.bld.commons.utils.types.SpatialType;

// TODO: Auto-generated Javadoc
/**
 * The Class WKBGeometry.
 */
public class WKBGeometry extends PostgisGeometry<byte[]>{

	/**
	 * Instantiates a new WKB geometry.
	 */
	public WKBGeometry() {
		super();
	}

	/**
	 * Instantiates a new WKB geometry.
	 *
	 * @param spatialType the spatial type
	 * @param geometry the geometry
	 * @param srid the srid
	 */
	public WKBGeometry(SpatialType spatialType, byte[] geometry, Integer srid) {
		super(spatialType, geometry, srid);
	}
	
	

}
