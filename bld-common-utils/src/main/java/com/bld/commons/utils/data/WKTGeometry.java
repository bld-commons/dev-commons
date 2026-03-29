package com.bld.commons.utils.data;

import com.bld.commons.utils.types.SpatialType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

/**
 * PostGIS geometry holder that stores the spatial data as a Well-Known Text (WKT) string.
 *
 * <p>Used in conjunction with {@link com.bld.commons.utils.json.annotations.GeometryPostgis}
 * (with {@code value = SpatialType.WKT}) to deserialise and serialise WKT geometry
 * representations to and from JTS {@link org.locationtech.jts.geom.Geometry} objects.</p>
 *
 * <p>The {@link #sridGeometry()} convenience method returns the geometry string
 * prefixed with the SRID in the PostGIS extended WKT format (e.g., {@code SRID=4326;POINT(...)}).</p>
 *
 * @author Francesco Baldi
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
	 * Returns the WKT geometry string prefixed with the SRID in PostGIS extended format.
	 *
	 * <p>If the SRID is set and greater than zero, the returned value has the form
	 * {@code SRID=&lt;srid&gt;;&lt;wkt&gt;}; otherwise the plain WKT string is returned.
	 * This property is serialised as the read-only JSON field {@code sridGeometry}.</p>
	 *
	 * @return the SRID-prefixed WKT string, or the plain WKT if no valid SRID is set
	 */
	@JsonProperty(value = "sridGeometry",access = Access.READ_ONLY)
	public String sridGeometry() {
		String sridGeometry=this.getGeometry();
		if(this.getSrid()!=null && this.getSrid()>0)
			sridGeometry="SRID="+this.getSrid()+";"+sridGeometry;
		return sridGeometry;
	}
}
