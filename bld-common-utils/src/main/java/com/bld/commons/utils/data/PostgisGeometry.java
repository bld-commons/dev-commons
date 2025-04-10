package com.bld.commons.utils.data;

import com.bld.commons.utils.types.SpatialType;

public abstract class PostgisGeometry<T> {

	private SpatialType spatialType;
	
	private T geometry;
	
	private Integer srid;


	public T getGeometry() {
		return geometry;
	}

	public void setGeometry(T geometry) {
		this.geometry = geometry;
	}

	public Integer getSrid() {
		return srid;
	}

	public void setSrid(Integer srid) {
		this.srid = srid;
	}

	public SpatialType getSpatialType() {
		return spatialType;
	}

	public void setSpatialType(SpatialType spatialType) {
		this.spatialType = spatialType;
	}

	public PostgisGeometry(SpatialType spatialType,T geometry, Integer srid) {
		super();
		this.spatialType=spatialType;
		this.geometry = geometry;
		this.srid = srid;
	}

	public PostgisGeometry() {
		super();
	}

	
	
}
