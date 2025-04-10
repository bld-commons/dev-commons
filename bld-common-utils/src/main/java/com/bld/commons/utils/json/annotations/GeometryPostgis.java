/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.json.annotations.JsonDateTimeZone.java
 */
package com.bld.commons.utils.json.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.bld.commons.utils.json.annotations.deserialize.GeometryDeserializer;
import com.bld.commons.utils.json.annotations.serialize.GeometrySerializer;
import com.bld.commons.utils.types.SpatialType;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * The Interface JsonDateTimeZone.
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD,PARAMETER })
@JacksonAnnotationsInside
@JsonDeserialize(using = GeometryDeserializer.class)
@JsonSerialize(using=GeometrySerializer.class)
public @interface GeometryPostgis {

	/**
	 * Value.
	 *
	 * @return the spatial type
	 */
	public SpatialType value() default SpatialType.WKT;
	
	
}
