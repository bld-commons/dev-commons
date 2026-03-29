package com.bld.commons.utils.data;

import com.bld.commons.utils.types.SpatialType;

/**
 * Abstract base class for PostGIS geometry wrappers.
 *
 * <p>Holds a spatial geometry value of type {@code T} (e.g., a {@code String} for WKT/KML,
 * a {@code byte[]} for WKB, or a {@link com.fasterxml.jackson.databind.JsonNode} for GeoJSON),
 * together with the spatial reference system identifier (SRID) and the
 * {@link com.bld.commons.utils.types.SpatialType} that indicates the serialisation format.</p>
 *
 * <p>Concrete subclasses: {@link WKTGeometry}, {@link WKBGeometry},
 * {@link GeoJsonGeometry}, {@link KMLGeometry}.</p>
 *
 * @param <T> the type used to hold the geometry data
 *
 * @author Francesco Baldi
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
