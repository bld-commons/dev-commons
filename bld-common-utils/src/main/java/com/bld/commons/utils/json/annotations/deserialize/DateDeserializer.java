/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.annotations.deserialize.CustomDateDeserializer.java
 */
package com.bld.commons.utils.json.annotations.deserialize;

import java.io.IOException;
import java.security.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.AbstractEnvironment;

import com.bld.commons.utils.DateUtils;
import com.bld.commons.utils.json.annotations.DateChange;
import com.bld.commons.utils.json.annotations.DateTimeZone;
import com.bld.commons.utils.json.annotations.deserialize.data.DateChangeDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

/**
 * The Class CustomDateDeserializer.
 *
 * @param <T> the generic type
 */
@SuppressWarnings({ "serial", "unchecked" })
@JacksonStdImpl
public class DateDeserializer<T> extends StdScalarDeserializer<T> implements ContextualDeserializer {

	/** The env. */
	@Autowired
	private AbstractEnvironment env;

	/** The date filter deserializer. */
	private DateChangeDeserializer dateFilterDeserializer = null;

	/** The simple date format. */
	private SimpleDateFormat simpleDateFormat = null;

	/**
	 * Instantiates a new custom date deserializer.
	 */
	public DateDeserializer() {
		super(Object.class);
	}

	/**
	 * Instantiates a new custom date deserializer.
	 *
	 * @param classDate        the class date
	 * @param dateDeserializer the date deserializer
	 * @param simpleDateFormat the simple date format
	 */
	private DateDeserializer(Class<T> classDate, DateChangeDeserializer dateDeserializer, SimpleDateFormat simpleDateFormat, AbstractEnvironment env) {
		super(classDate);
		this.dateFilterDeserializer = dateDeserializer;
		this.simpleDateFormat = simpleDateFormat;
		this.env = env;
	}

	/**
	 * Gets the date.
	 *
	 * @param dateString the date string
	 * @return the date
	 * @throws ParseException the parse exception
	 */
	protected Date getDate(String dateString) {
		try {
			Date date = this.simpleDateFormat.parse(dateString);
			return DateUtils.sumDate(date, this.dateFilterDeserializer.getAddYear(), this.dateFilterDeserializer.getAddMonth(), this.dateFilterDeserializer.getAddWeek(), this.dateFilterDeserializer.getAddDay(),
					this.dateFilterDeserializer.getAddHour(), this.dateFilterDeserializer.getAddMinute(), this.dateFilterDeserializer.getAddSecond());
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

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
	 * Deserialize.
	 *
	 * @param p    the p
	 * @param ctxt the ctxt
	 * @return the t
	 * @throws IOException             Signals that an I/O exception has occurred.
	 * @throws JsonProcessingException the json processing exception
	 */
	@Override
	public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		String dateString = p.getText();
		T value = null;
		Date date = this.getDate(dateString);
		if (Date.class.isAssignableFrom(super._valueClass))
			value = (T) date;
		if (Calendar.class.isAssignableFrom(this._valueClass))
			value = (T) DateUtils.dateToCalendar(date);
		if (Timestamp.class.isAssignableFrom(super._valueClass))
			value = (T) DateUtils.dateToTimestamp(date);
		return value;
	}

	/**
	 * Creates the contextual.
	 *
	 * @param ctxt     the ctxt
	 * @param property the property
	 * @return the json deserializer
	 * @throws JsonMappingException the json mapping exception
	 */
	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
		DateTimeZone dateTimeZone = property.getAnnotation(DateTimeZone.class);
		DateChange dateFilter = property.getAnnotation(DateChange.class);
		dateFilter(dateTimeZone, dateFilter);

		if (property.getType() != null && property.getType().getRawClass() != null)
			return new DateDeserializer<>(property.getType().getRawClass(), this.dateFilterDeserializer, this.simpleDateFormat, this.env);
		else
			return this;
	}

	private void dateFilter(DateTimeZone dateTimeZone, DateChange dateFilter) {
		if (dateTimeZone != null)
			this.dateFilterDeserializer = new DateChangeDeserializer(dateTimeZone.timeZone(), dateTimeZone.format());
		else if (dateFilter != null)
			this.dateFilterDeserializer = new DateChangeDeserializer(dateFilter.timeZone(), dateFilter.format(), dateFilter.addYear(), dateFilter.addMonth(), dateFilter.addWeek(), dateFilter.addDay(), dateFilter.addHour(), dateFilter.addMinute(),
					dateFilter.addSecond());

		if (this.dateFilterDeserializer.getTimeZone().startsWith("${") && this.dateFilterDeserializer.getTimeZone().endsWith("}")) {
			TimeZone timeZone = TimeZone.getDefault();
			final String tz = this.env.resolvePlaceholders(this.dateFilterDeserializer.getTimeZone());
			if (StringUtils.isNotBlank(tz) && !tz.equals(this.dateFilterDeserializer.getTimeZone()))
				timeZone = TimeZone.getTimeZone(tz);
			this.setSimpleDateFormat(timeZone, this.dateFilterDeserializer.getFormat());
		} else
			this.setSimpleDateFormat(TimeZone.getTimeZone(this.dateFilterDeserializer.getTimeZone()), this.dateFilterDeserializer.getFormat());
	}

	

	
}
