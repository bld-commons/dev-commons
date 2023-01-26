/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.json.annotations.JsonDateFilter.java
 */
package com.bld.commons.utils.json.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.bld.commons.utils.json.annotations.deserialize.DateDeserializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * The Interface JsonDateFilter.
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
@JacksonAnnotationsInside
@JsonDeserialize(using = DateDeserializer.class)
//@JsonSerialize(using=CustomDateSerializer.class)
@JsonInclude(Include.NON_NULL)
public @interface JsonDateFilter {

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
	
	/**
	 * Adds the year.
	 *
	 * @return the int
	 */
	public int addYear() default 0;
	
	/**
	 * Adds the month.
	 *
	 * @return the int
	 */
	public int addMonth() default 0;
	
	/**
	 * Adds the week.
	 *
	 * @return the int
	 */
	public int addWeek() default 0;
	
	/**
	 * Adds the day.
	 *
	 * @return the int
	 */
	public int addDay() default 0;
	
	/**
	 * Adds the hour.
	 *
	 * @return the int
	 */
	public int addHour() default 0;
	
	/**
	 * Adds the minute.
	 *
	 * @return the int
	 */
	public int addMinute() default 0;
	
	/**
	 * Adds the second.
	 *
	 * @return the int
	 */
	public int addSecond() default 0;
	
}
