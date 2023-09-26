package com.bld.commons.utils.formatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.format.Formatter;

import com.bld.commons.utils.DateUtils;
import com.bld.commons.utils.json.annotations.DateChange;
import com.bld.commons.utils.json.annotations.DateTimeZone;
import com.bld.commons.utils.json.annotations.deserialize.data.DateChangeDeserializer;

@SuppressWarnings("unchecked")
public final class DateFormatter<T> implements Formatter<T> {

	private AbstractEnvironment env;

	/** The date filter deserializer. */
	private DateChangeDeserializer dateChangeDeserializer = null;

	/** The simple date format. */
	private SimpleDateFormat simpleDateFormat = null;

	private Class<T> fieldType;

	public DateFormatter(DateTimeZone dateTimeZone, AbstractEnvironment env, Class<T> fieldType) {
		super();
		this.env = env;
		this.fieldType = fieldType;
		this.dateFilter(dateTimeZone, null);
	}

	public DateFormatter(DateChange dateChange, AbstractEnvironment env, Class<T> fieldType) {
		super();
		this.env = env;
		this.fieldType = fieldType;
		this.dateFilter(null, dateChange);
	}

	@Override
	public String print(T object, Locale locale) {
		if (object != null) {
			if (Calendar.class.isAssignableFrom(this.fieldType))
				return this.simpleDateFormat.format(DateUtils.calendarToDate((Calendar) object));
			return this.simpleDateFormat.format(object);
		}
		return null;
	}

	@Override
	public T parse(String text, Locale locale) throws ParseException {
		if (StringUtils.isNotBlank(text)) {
			// dateAnnotation();
			if (Calendar.class.isAssignableFrom(this.fieldType))
				return (T) DateUtils.dateToCalendar(this.simpleDateFormat.parse(text));
			return (T) this.simpleDateFormat.parse(text);
		}
		return null;
	}

	private void dateFilter(DateTimeZone dateTimeZone, DateChange dateFilter) {
		if (dateTimeZone != null)
			this.dateChangeDeserializer = new DateChangeDeserializer(dateTimeZone.timeZone(), dateTimeZone.format());
		else if (dateFilter != null)
			this.dateChangeDeserializer = new DateChangeDeserializer(dateFilter.timeZone(), dateFilter.format(), dateFilter.addYear(), dateFilter.addMonth(), dateFilter.addWeek(), dateFilter.addDay(), dateFilter.addHour(), dateFilter.addMinute(),
					dateFilter.addSecond());

		if (this.dateChangeDeserializer.getTimeZone().startsWith("${") && this.dateChangeDeserializer.getTimeZone().endsWith("}")) {
			TimeZone timeZone = TimeZone.getDefault();
			final String tz = this.env.resolvePlaceholders(this.dateChangeDeserializer.getTimeZone());
			if (StringUtils.isNotBlank(tz) && !tz.equals(this.dateChangeDeserializer.getTimeZone()))
				timeZone = TimeZone.getTimeZone(tz);
			this.setSimpleDateFormat(timeZone, this.dateChangeDeserializer.getFormat());
		} else
			this.setSimpleDateFormat(TimeZone.getTimeZone(this.dateChangeDeserializer.getTimeZone()), this.dateChangeDeserializer.getFormat());
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

}
