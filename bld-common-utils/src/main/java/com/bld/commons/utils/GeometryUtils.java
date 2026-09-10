package com.bld.commons.utils;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.locationtech.jts.io.kml.KMLReader;
import org.locationtech.jts.io.kml.KMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bld.commons.utils.data.GeoJsonGeometry;
import com.bld.commons.utils.data.KMLGeometry;
import com.bld.commons.utils.data.PostgisGeometry;
import com.bld.commons.utils.data.WKBGeometry;
import com.bld.commons.utils.data.WKTGeometry;
import com.bld.commons.utils.types.SpatialType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class GeometryUtils {

	@Autowired
	private ObjectMapper objMapper;

	public Geometry parseWkt(String wkt) {
		if (wkt == null || wkt.isBlank()) return null;
		try {
			return new WKTReader().read(wkt.trim());
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid WKT: " + wkt, e);
		}
	}

	public Geometry parseWkb(byte[] wkb) {
		if (wkb == null || wkb.length == 0) return null;
		try {
			return new WKBReader().read(wkb);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid WKB", e);
		}
	}

	public Geometry parseGeoJson(String geoJson) {
		if (geoJson == null || geoJson.isBlank()) return null;
		try {
			JsonNode node = this.objMapper.readTree(geoJson);
			GeoJsonGeometry geoJsonGeometry = this.objMapper.treeToValue(node, GeoJsonGeometry.class);
			Geometry geometry = new GeoJsonReader().read(geoJsonGeometry.geoJson(this.objMapper));
			setSRID(geometry, geoJsonGeometry);
			return geometry;
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid GeoJSON: " + geoJson, e);
		}
	}

	public Geometry parseKml(String kml) {
		if (kml == null || kml.isBlank()) return null;
		try {
			KMLGeometry kmlGeometry = this.objMapper.readValue(kml, KMLGeometry.class);
			Geometry geometry = new KMLReader().read(kmlGeometry.getGeometry());
			setSRID(geometry, kmlGeometry);
			return geometry;
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid KML: " + kml, e);
		}
	}

	public Geometry parse(String text, SpatialType type) {
		if (text == null || type == null) return null;
		switch (type) {
		case WKT:
			return parseWkt(text);
		case WKB:
			return parseWkb(java.util.Base64.getDecoder().decode(text.trim()));
		case GeoJSON:
			return parseGeoJson(text);
		case KML:
			return parseKml(text);
		default:
			throw new IllegalArgumentException("Unsupported SpatialType: " + type);
		}
	}

	public PostgisGeometry<?> toWkt(Geometry geometry) {
		if (geometry == null) return null;
		return new WKTGeometry(SpatialType.WKT, new WKTWriter().write(geometry), geometry.getSRID());
	}

	public PostgisGeometry<?> toWkb(Geometry geometry) {
		if (geometry == null) return null;
		return new WKBGeometry(SpatialType.WKB, new WKBWriter().write(geometry), geometry.getSRID());
	}

	public PostgisGeometry<?> toGeoJson(Geometry geometry) {
		if (geometry == null) return null;
		try {
			JsonNode node = this.objMapper.readTree(new GeoJsonWriter().write(geometry));
			return new GeoJsonGeometry(SpatialType.GeoJSON, node, geometry.getSRID());
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to serialize Geometry to GeoJSON", e);
		}
	}

	public PostgisGeometry<?> toKml(Geometry geometry) {
		if (geometry == null) return null;
		return new KMLGeometry(SpatialType.KML, new KMLWriter().write(geometry), geometry.getSRID());
	}

	public PostgisGeometry<?> serialize(Geometry geometry, SpatialType type) {
		if (geometry == null || type == null) return null;
		switch (type) {
		case WKT:
			return toWkt(geometry);
		case WKB:
			return toWkb(geometry);
		case GeoJSON:
			return toGeoJson(geometry);
		case KML:
			return toKml(geometry);
		default:
			throw new IllegalArgumentException("Unsupported SpatialType: " + type);
		}
	}

	private void setSRID(Geometry geometry, PostgisGeometry<?> postgisGeometry) {
		if (postgisGeometry.getSrid() != null)
			geometry.setSRID(postgisGeometry.getSrid());
	}
}
