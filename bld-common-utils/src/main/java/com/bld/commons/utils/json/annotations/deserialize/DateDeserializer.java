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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

// TODO: Auto-generated Javadoc
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
	 * @param env              the env
	 */
	private DateDeserializer(Class<T> classDate, DateChangeDeserializer dateDeserializer, AbstractEnvironment env) {
		super(classDate);
		this.dateFilterDeserializer = dateDeserializer;
		this.env = env;
	}

	/**
	 * Gets the date.
	 *
	 * @param dateString the date string
	 * @return the date
	 */
	protected Date getDate(String dateString) {
		Date date = null;
		try {
			Instant instant = Instant.parse(dateString);
			ZoneId zoneId = ZoneId.of(dateFilterDeserializer.getTimeZone());
			LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zoneId);
			date = Date.from(localDateTime.atZone(zoneId).toInstant());
		} catch (Exception e) {
			try {
				SimpleDateFormat simpleDateFormat=new SimpleDateFormat(this.dateFilterDeserializer.getFormat().replace("/", "-"));
				simpleDateFormat.setTimeZone(TimeZone.getTimeZone(this.dateFilterDeserializer.getTimeZone()));
				date=simpleDateFormat.parse(dateString);
			} catch (ParseException e1) {
				throw new RuntimeException(e1);
			}
		}
		return DateUtils.sumDate(date, this.dateFilterDeserializer.getAddYear(), this.dateFilterDeserializer.getAddMonth(), this.dateFilterDeserializer.getAddWeek(), this.dateFilterDeserializer.getAddDay(), this.dateFilterDeserializer.getAddHour(),
				this.dateFilterDeserializer.getAddMinute(), this.dateFilterDeserializer.getAddSecond());

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
			return new DateDeserializer<>(property.getType().getRawClass(), this.dateFilterDeserializer, this.env);
		else
			return this;
	}

	/**
	 * Date filter.
	 *
	 * @param dateTimeZone the date time zone
	 * @param dateFilter   the date filter
	 */
	private void dateFilter(DateTimeZone dateTimeZone, DateChange dateFilter) {
		if (dateTimeZone != null)
			this.dateFilterDeserializer = new DateChangeDeserializer(dateTimeZone.timeZone(), dateTimeZone.format());
		else if (dateFilter != null)
			this.dateFilterDeserializer = new DateChangeDeserializer(dateFilter.timeZone(), dateFilter.format(), dateFilter.addYear(), dateFilter.addMonth(), dateFilter.addWeek(), dateFilter.addDay(), dateFilter.addHour(), dateFilter.addMinute(),
					dateFilter.addSecond());
		String dateFormat = this.dateFilterDeserializer.getFormat();
		if (dateFormat.startsWith("${") && dateFormat.endsWith("}")) {
			dateFormat = this.env.resolvePlaceholders(this.dateFilterDeserializer.getFormat());
			if (StringUtils.isBlank(dateFormat))
				dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
		}
		String timeZone = this.dateFilterDeserializer.getTimeZone();
		if (timeZone.startsWith("${") && timeZone.endsWith("}"))
			timeZone = this.env.resolvePlaceholders(this.dateFilterDeserializer.getTimeZone());

		if (StringUtils.isBlank(timeZone))
			timeZone = TimeZone.getDefault().getID();

		this.dateFilterDeserializer.setTimeZone(timeZone);
		this.dateFilterDeserializer.setFormat(dateFormat);
	}

}
