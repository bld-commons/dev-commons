package com.bld.commons.utils.data;

import com.bld.commons.utils.types.SpatialType;

public class WKBGeometry extends PostgisGeometry<byte[]>{

	public WKBGeometry() {
		super();
	}

	public WKBGeometry(SpatialType spatialType, byte[] geometry, Integer srid) {
		super(spatialType, geometry, srid);
	}
	
	

}
