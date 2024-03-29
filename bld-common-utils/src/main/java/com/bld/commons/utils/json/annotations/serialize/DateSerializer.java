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

	/** The simple date format. */
	private SimpleDateFormat simpleDateFormat = null;

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
	 * @param simpleDateFormat the simple date format
	 */
	private DateSerializer(Class<T> classDate, DateTimeZone dateTimeZone, SimpleDateFormat simpleDateFormat, AbstractEnvironment env) {
		super(classDate);
		this.dateTimeZone = dateTimeZone;
		this.simpleDateFormat = simpleDateFormat;
		this.env = env;
	}

	/**
	 * Format date.
	 *
	 * @param date the date
	 * @return the string
	 */
	public String formatDate(T date) {
		String dateString = null;
		if (date instanceof Calendar)
			dateString = this.simpleDateFormat.format(DateUtils.calendarToDate((Calendar) date));
		else if (date instanceof Date)
			dateString = this.simpleDateFormat.format((Date) date);
		else if (date instanceof Timestamp)
			dateString = this.simpleDateFormat.format(DateUtils.timestampToDate((Timestamp) date));
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
		gen.writeString(this.formatDate(date));
	}

	/**
	 * Sets the simple date format.
	 *
	 * @param timeZone the time zone
	 * @param format   the format
	 */
	private void setSimpleDateFormat(TimeZone timeZone, String format) {
		this.simpleDateFormat = new SimpleDateFormat(format);
		this.simpleDateFormat.setTimeZone(timeZone);
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
		this.dateTimeZone = property.getAnnotation(DateTimeZone.class);
		TimeZone timeZone = TimeZone.getDefault();
		String dateFormat = this.dateTimeZone.format();
		if (dateFormat.startsWith("${") && dateFormat.endsWith("}")) {
			dateFormat = this.env.resolvePlaceholders(dateFormat);
			if (StringUtils.isBlank(dateFormat))
				dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
		}
		if (this.dateTimeZone.timeZone().startsWith("${") && this.dateTimeZone.timeZone().endsWith("}")) {
			final String tz = this.env.resolvePlaceholders(this.dateTimeZone.timeZone());
			if (StringUtils.isNotBlank(tz) && !tz.equals(this.dateTimeZone.timeZone()))
				timeZone = TimeZone.getTimeZone(tz);
			
		} else
			timeZone = TimeZone.getTimeZone(this.dateTimeZone.timeZone());
		this.setSimpleDateFormat(timeZone, dateFormat);
		if (property.getType() != null && property.getType().getRawClass() != null)
			return new DateSerializer<>(property.getType().getRawClass(), this.dateTimeZone, this.simpleDateFormat, this.env);
		else
			return this;
	}

//	private void dateFilter(DateTimeZone dateTimeZone, DateChange dateFilter) {
//		if (dateTimeZone != null)
//			this.dateFilterDeserializer = new DateChangeDeserializer(dateTimeZone.timeZone(), dateTimeZone.format());
//		else if (dateFilter != null)
//			this.dateFilterDeserializer = new DateChangeDeserializer(dateFilter.timeZone(), dateFilter.format(), dateFilter.addYear(), dateFilter.addMonth(), dateFilter.addWeek(), dateFilter.addDay(), dateFilter.addHour(), dateFilter.addMinute(),
//					dateFilter.addSecond());
//		TimeZone timeZone = TimeZone.getDefault();
//
//		if (this.dateFilterDeserializer.getTimeZone().startsWith("${") && this.dateFilterDeserializer.getTimeZone().endsWith("}")) {
//			final String tz = this.env.resolvePlaceholders(this.dateFilterDeserializer.getTimeZone());
//			if (StringUtils.isNotBlank(tz) && !tz.equals(this.dateFilterDeserializer.getTimeZone()))
//				timeZone = TimeZone.getTimeZone(tz);
//			this.setSimpleDateFormat(timeZone, this.dateFilterDeserializer.getFormat());
//		} else
//			timeZone = TimeZone.getTimeZone(this.dateFilterDeserializer.getTimeZone());
//		String dateFormat = this.dateFilterDeserializer.getFormat();
//		if (dateFormat.startsWith("${") && dateFormat.endsWith("}")) {
//			dateFormat = this.env.resolvePlaceholders(this.dateFilterDeserializer.getFormat());
//			if (StringUtils.isBlank(dateFormat))
//				dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
//		}
//		this.setSimpleDateFormat(timeZone, dateFormat);
//	}

}
