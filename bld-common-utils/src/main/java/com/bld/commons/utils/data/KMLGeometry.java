
package com.bld.commons.utils.data;

import com.bld.commons.utils.types.SpatialType;

/**
 * PostGIS geometry holder that stores the spatial data as a KML string.
 *
 * <p>Used in conjunction with {@link com.bld.commons.utils.json.annotations.GeometryPostgis}
 * (with {@code value = SpatialType.KML}) to deserialise and serialise KML geometry
 * representations to and from JTS {@link org.locationtech.jts.geom.Geometry} objects.</p>
 *
 * @author Francesco Baldi
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
