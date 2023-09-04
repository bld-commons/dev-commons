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
import com.bld.commons.utils.json.annotations.JsonDateTimeZone;
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
	protected JsonDateTimeZone dateTimeZone = null;

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
	private DateSerializer(Class<T> t,AbstractEnvironment env) {
		super(t);
		this.env=env;
	}

	/**
	 * Instantiates a new custom date serializer.
	 *
	 * @param classDate the class date
	 * @param dateTimeZone the date time zone
	 * @param simpleDateFormat the simple date format
	 */
	private DateSerializer(Class<T> classDate, JsonDateTimeZone dateTimeZone, SimpleDateFormat simpleDateFormat,AbstractEnvironment env) {
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
		String dateString=null;
		if(date instanceof Calendar)
			dateString=this.simpleDateFormat.format(DateUtils.calendarToDate((Calendar)date));
		else if(date instanceof Date)
			dateString=this.simpleDateFormat.format((Date)date);
		else if(date instanceof Timestamp)
			dateString=this.simpleDateFormat.format(DateUtils.timestampToDate((Timestamp)date));
		return dateString;
	}
	
	

	/**
	 * Serialize.
	 *
	 * @param date the date
	 * @param gen the gen
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
	 * @param format the format
	 */
	private void setSimpleDateFormat(TimeZone timeZone, String format) {
		this.simpleDateFormat = new SimpleDateFormat(format);
		this.simpleDateFormat.setTimeZone(timeZone);
	}

	/**
	 * Creates the contextual.
	 *
	 * @param prov the prov
	 * @param property the property
	 * @return the json serializer
	 * @throws JsonMappingException the json mapping exception
	 */
	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
		this.dateTimeZone = property.getAnnotation(JsonDateTimeZone.class);
		if (this.dateTimeZone.timeZone().startsWith("${") && this.dateTimeZone.timeZone().endsWith("}")) {
			TimeZone timeZone=TimeZone.getDefault();
			final String tz=this.env.resolvePlaceholders(this.dateTimeZone.timeZone());
			if(StringUtils.isNotBlank(tz) && !tz.equals(this.dateTimeZone.timeZone()))
				timeZone=TimeZone.getTimeZone(tz);
			this.setSimpleDateFormat(timeZone, this.dateTimeZone.format());
		} else 
			this.setSimpleDateFormat(TimeZone.getTimeZone(this.dateTimeZone.timeZone()), this.dateTimeZone.format());
		if (property.getType() != null && property.getType().getRawClass() != null)
			return new DateSerializer<>(property.getType().getRawClass(), this.dateTimeZone, this.simpleDateFormat,this.env);
		else
			return this;
	}

}
