package com.bld.commons.utils.data;

import com.bld.commons.utils.types.SpatialType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

public class WKTGeometry extends PostgisGeometry<String> {

	public WKTGeometry() {
		super();
	}

	public WKTGeometry(SpatialType spatialType, String geometry, Integer srid) {
		super(spatialType, geometry, srid);
	}

	@JsonProperty(value = "sridGeometry",access = Access.READ_ONLY)
	public String sridGeometry() {
		String sridGeometry=this.getGeometry();
		if(this.getSrid()!=null && this.getSrid()>0)
			sridGeometry="SRID="+this.getSrid()+";"+sridGeometry;
		return sridGeometry;
	}
}
