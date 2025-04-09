package com.bld.commons.utils.types;


public enum SridType {

	NONE(0),
	WGS_84(4326),
	WEB_MERCATOR(3857),
	NAD83(4269),
	OSGB_1936(27700),
	WGS_84_UTM_ZONE_33N(32633),
	ETRS89_UTM_ZONE_32N(25832),
	ED50_UTM_ZONE_32N(23032),
	ITALY_ZONE_1(3003),
	ITALY_ZONE_2(3004);

	private int srid;
	
	
	private SridType(int srid) {
		this.srid=srid;
	}


	public int value() {
		return srid;
	}
	
	
}
