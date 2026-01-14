/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.annotations.serialize.CustomDateSerializer.java
 */
package com.bld.commons.utils.json.annotations.serialize;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.AbstractEnvironment;

import com.bld.commons.utils.DateUtils;
import com.bld.commons.utils.json.annotations.DateTimeZone;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

/**
 * The Class CustomDateSerializer.
 *
 * @param <T> the generic type
 */
@SuppressWarnings("serial")
@JacksonStdImpl
public class DateSerializer<T> extends StdScalarSerializer<T> implements ContextualSerializer {

	/** The env. */
	@Autowired
	private AbstractEnvironment env = null;

	/** The date time zone. */
	protected DateTimeZone dateTimeZone = null;

	/**
	 * Instantiates a new custom date serializer.
	 */
	public DateSerializer() {
		this(null);
	}

	private DateSerializer(Class<T> t) {
		super(t);
	}

	/**
	 * Instantiates a new custom date serializer.
	 *
	 * @param t the t
	 */
	private DateSerializer(Class<T> t, AbstractEnvironment env) {
		super(t);
		this.env = env;
	}

	/**
	 * Instantiates a new custom date serializer.
	 *
	 * @param classDate        the class date
	 * @param dateTimeZone     the date time zone
	 */
	private DateSerializer(Class<T> classDate, DateTimeZone dateTimeZone, AbstractEnvironment env) {
		super(classDate);
		this.dateTimeZone = dateTimeZone;
		this.env = env;
	}

	/**
	 * Format date.
	 *
	 * @param date the date
	 * @return the string
	 */
	public String formatDate(T date,SimpleDateFormat simpleDateFormat) {
		String dateString = null;
		if (date instanceof Calendar)
			dateString = simpleDateFormat.format(DateUtils.calendarToDate((Calendar) date));
		else if (date instanceof Date)
			dateString = simpleDateFormat.format((Date) date);
		else if (date instanceof Timestamp)
			dateString = simpleDateFormat.format(DateUtils.timestampToDate((Timestamp) date));
		return dateString;
	}

	/**
	 * Serialize.
	 *
	 * @param date     the date
	 * @param gen      the gen
	 * @param provider the provider
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void serialize(T date, JsonGenerator gen, SerializerProvider provider) throws IOException {
		SimpleDateFormat simpleDateFormat=this.getSimpleDateFormat(this.dateTimeZone);
		gen.writeString(formatDate(date,simpleDateFormat));
	}


	private SimpleDateFormat getSimpleDateFormat(TimeZone timeZone, String format) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		simpleDateFormat.setTimeZone(timeZone);
		return simpleDateFormat;
	}

	/**
	 * Creates the contextual.
	 *
	 * @param prov     the prov
	 * @param property the property
	 * @return the json serializer
	 * @throws JsonMappingException the json mapping exception
	 */
	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
		DateTimeZone dateTimeZone = property.getAnnotation(DateTimeZone.class);
		if (property.getType() != null && property.getType().getRawClass() != null)
			return new DateSerializer<>(property.getType().getRawClass(), dateTimeZone, this.env);
		else
			return this;
	}

	
	private SimpleDateFormat getSimpleDateFormat(DateTimeZone dateTimeZone) {
		String dateFormat = dateTimeZone.format();
		TimeZone timeZone = TimeZone.getDefault();
		if (dateFormat.startsWith("${") && dateFormat.endsWith("}")) {
			dateFormat = this.env.resolvePlaceholders(dateFormat);
			if (StringUtils.isBlank(dateFormat))
				dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
		}
		if (dateTimeZone.timeZone().startsWith("${") && dateTimeZone.timeZone().endsWith("}")) {
			final String tz = this.env.resolvePlaceholders(dateTimeZone.timeZone());
			if (StringUtils.isNotBlank(tz) && !tz.equals(dateTimeZone.timeZone()))
				timeZone = TimeZone.getTimeZone(tz);
			
		} else
			timeZone = TimeZone.getTimeZone(dateTimeZone.timeZone());
		SimpleDateFormat simpleDateFormat=this.getSimpleDateFormat(timeZone, dateFormat);
		return simpleDateFormat;
	}
	

}
