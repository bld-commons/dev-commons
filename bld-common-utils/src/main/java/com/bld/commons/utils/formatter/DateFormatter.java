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

/**
 * Spring {@link Formatter} that converts between date/time objects ({@link java.util.Date}
 * or {@link Calendar}) and their string representations, respecting a configurable
 * date format and time zone.
 *
 * <p>Can be configured from either a {@link DateTimeZone} or a {@link DateChange} annotation.
 * Spring property placeholders in the time-zone and format attribute values are resolved via
 * the injected {@link AbstractEnvironment}.</p>
 *
 * @param <T> the date/time type handled by this formatter ({@link java.util.Date} or
 *            {@link Calendar})
 *
 * @author Francesco Baldi
 */
@SuppressWarnings("unchecked")
public final class DateFormatter<T> implements Formatter<T> {

	private AbstractEnvironment env;

	/** The date filter deserializer. */
	private DateChangeDeserializer dateChangeDeserializer = null;

	/** The simple date format. */
	private SimpleDateFormat simpleDateFormat = null;

	private Class<T> fieldType;

	/**
	 * Constructs a {@link DateFormatter} configured from a {@link DateTimeZone} annotation.
	 *
	 * @param dateTimeZone the annotation supplying the time zone and date format
	 * @param env          the Spring environment for resolving property placeholders
	 * @param fieldType    the target field type ({@link java.util.Date} or {@link Calendar})
	 */
	public DateFormatter(DateTimeZone dateTimeZone, AbstractEnvironment env, Class<T> fieldType) {
		super();
		this.env = env;
		this.fieldType = fieldType;
		this.dateFilter(dateTimeZone, null);
	}

	/**
	 * Constructs a {@link DateFormatter} configured from a {@link DateChange} annotation.
	 *
	 * @param dateChange the annotation supplying the time zone, date format, and date offsets
	 * @param env        the Spring environment for resolving property placeholders
	 * @param fieldType  the target field type ({@link java.util.Date} or {@link Calendar})
	 */
	public DateFormatter(DateChange dateChange, AbstractEnvironment env, Class<T> fieldType) {
		super();
		this.env = env;
		this.fieldType = fieldType;
		this.dateFilter(null, dateChange);
	}

	/**
	 * Formats the given date/time object as a string using the configured date format and time zone.
	 *
	 * @param object the date/time value to format
	 * @param locale the current locale (not used for formatting)
	 * @return the formatted date string, or {@code null} if the value is {@code null}
	 */
	@Override
	public String print(T object, Locale locale) {
		if (object != null) {
			if (Calendar.class.isAssignableFrom(this.fieldType))
				return this.simpleDateFormat.format(DateUtils.calendarToDate((Calendar) object));
			return this.simpleDateFormat.format(object);
		}
		return null;
	}

	/**
	 * Parses the given text into a date/time object using the configured date format and time zone.
	 *
	 * @param text   the text to parse; blank values return {@code null}
	 * @param locale the current locale (not used for parsing)
	 * @return the parsed date/time value, or {@code null} if the text is blank
	 * @throws ParseException if the text cannot be parsed
	 */
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

	/**
	 * Initialises the internal {@link SimpleDateFormat} from either a {@link DateTimeZone}
	 * or a {@link DateChange} annotation, resolving property placeholders as needed.
	 *
	 * @param dateTimeZone the {@link DateTimeZone} annotation, or {@code null}
	 * @param dateFilter   the {@link DateChange} annotation, or {@code null}
	 */
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
	 * Creates and configures the internal {@link SimpleDateFormat} with the given time zone
	 * and format pattern.
	 *
	 * @param timeZone the time zone to apply to the formatter
	 * @param format   the date/time format pattern
	 */
	private void setSimpleDateFormat(TimeZone timeZone, String format) {
		this.simpleDateFormat = new SimpleDateFormat(format);
		this.simpleDateFormat.setTimeZone(timeZone);
	}

}
