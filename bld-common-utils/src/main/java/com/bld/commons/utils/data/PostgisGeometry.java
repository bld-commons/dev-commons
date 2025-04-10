package com.bld.commons.utils.data;

import com.bld.commons.utils.types.SpatialType;

/**
 * The Class PostgisGeometry.
 *
 * @param <T> the generic type
 */
public abstract class PostgisGeometry<T> {

	/** The spatial type. */
	private SpatialType spatialType;
	
	/** The geometry. */
	private T geometry;
	
	/** The srid. */
	private Integer srid;


	/**
	 * Gets the geometry.
	 *
	 * @return the geometry
	 */
	public T getGeometry() {
		return geometry;
	}

	/**
	 * Sets the geometry.
	 *
	 * @param geometry the new geometry
	 */
	public void setGeometry(T geometry) {
		this.geometry = geometry;
	}

	/**
	 * Gets the srid.
	 *
	 * @return the srid
	 */
	public Integer getSrid() {
		return srid;
	}

	/**
	 * Sets the srid.
	 *
	 * @param srid the new srid
	 */
	public void setSrid(Integer srid) {
		this.srid = srid;
	}

	/**
	 * Gets the spatial type.
	 *
	 * @return the spatial type
	 */
	public SpatialType getSpatialType() {
		return spatialType;
	}

	/**
	 * Sets the spatial type.
	 *
	 * @param spatialType the new spatial type
	 */
	public void setSpatialType(SpatialType spatialType) {
		this.spatialType = spatialType;
	}

	/**
	 * Instantiates a new postgis geometry.
	 *
	 * @param spatialType the spatial type
	 * @param geometry the geometry
	 * @param srid the srid
	 */
	public PostgisGeometry(SpatialType spatialType,T geometry, Integer srid) {
		super();
		this.spatialType=spatialType;
		this.geometry = geometry;
		this.srid = srid;
	}

	/**
	 * Instantiates a new postgis geometry.
	 */
	public PostgisGeometry() {
		super();
	}

	
	
}
