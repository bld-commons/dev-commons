
package com.bld.commons.utils.data;

import com.bld.commons.utils.types.SpatialType;

/**
 * The Class KMLGeometry.
 */
public class KMLGeometry extends PostgisGeometry<String> {

	/**
	 * Instantiates a new KML geometry.
	 */
	public KMLGeometry() {
		super();
	}

	/**
	 * Instantiates a new KML geometry.
	 *
	 * @param spatialType the spatial type
	 * @param geometry the geometry
	 * @param srid the srid
	 */
	public KMLGeometry(SpatialType spatialType, String geometry, Integer srid) {
		super(spatialType, geometry, srid);
	}

	
}
