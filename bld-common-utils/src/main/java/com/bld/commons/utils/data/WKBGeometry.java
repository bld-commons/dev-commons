package com.bld.commons.utils.data;

import com.bld.commons.utils.types.SpatialType;

/**
 * PostGIS geometry holder that stores the spatial data as a Well-Known Binary (WKB) byte array.
 *
 * <p>Used in conjunction with {@link com.bld.commons.utils.json.annotations.GeometryPostgis}
 * (with {@code value = SpatialType.WKB}) to deserialise and serialise WKB geometry
 * representations to and from JTS {@link org.locationtech.jts.geom.Geometry} objects.</p>
 *
 * @author Francesco Baldi
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
