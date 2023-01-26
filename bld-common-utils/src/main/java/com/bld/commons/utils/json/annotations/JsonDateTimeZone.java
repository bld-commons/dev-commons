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

import com.bld.commons.utils.json.annotations.deserialize.DateDeserializer;
import com.bld.commons.utils.json.annotations.serialize.DateSerializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * The Interface JsonDateTimeZone.
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD,PARAMETER })
@JacksonAnnotationsInside
@JsonDeserialize(using = DateDeserializer.class)
@JsonSerialize(using=DateSerializer.class)
//@JsonInclude(Include.NON_NULL)
public @interface JsonDateTimeZone {

	/**
	 * Time zone.
	 *
	 * @return the string
	 */
	public String timeZone() default "${spring.jackson.time-zone}";
	
	
	/**
	 * Format.
	 *
	 * @return the string
	 */
	public String format() default "yyyy-MM-dd'T'HH:mm:ss";
	
}
