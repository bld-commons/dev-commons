/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.persistence.reflection.utils.DateUtils.java
 */
package com.bld.commons.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bld.commons.utils.types.TimeUnitMeasureType;

// TODO: Auto-generated Javadoc
/**
 * The Class DateUtils.
 */
public class DateUtils {

	/** The Constant logger. */
	private static final Log logger = LogFactory.getLog(DateUtils.class);

	/** The Constant PROPS_TIME_ZONE. */
	public final static String PROPS_TIME_ZONE = "spring.jackson.time-zone";

	/** The Constant ENV_TIME_ZONE. */
	public final static String ENV_TIME_ZONE = "${" + PROPS_TIME_ZONE + "}";

	/**
	 * Gets the timezone.
	 *
	 * @param timezone the timezone
	 * @return the timezone
	 */
	public static TimeZone getTimezone(String timezone) {
		return TimeZone.getTimeZone(timezone);
	}

	/**
	 * Calendar to date.
	 *
	 * @param cal the cal
	 * @return the date
	 */
	public static Date calendarToDate(Calendar cal) {
		if (cal != null) {
			return cal.getTime();
		}
		return null;
	}

	/**
	 * Date to calendar.
	 *
	 * @param data the data
	 * @return the calendar
	 */
	public static Calendar dateToCalendar(Date data) {
		Calendar calendario = null;
		if (data != null) {
			calendario = Calendar.getInstance();
			calendario.setTimeInMillis(data.getTime());
		}
		return calendario;
	}

	/**
	 * Current date.
	 *
	 * @return the date
	 */
	public static Date currentDate() {
		Calendar calendario = Calendar.getInstance();
		return calendarToDate(resetHour(calendario));
	}

	/**
	 * String to calendar date.
	 *
	 * @param dateString the date string
	 * @param dateFormat the date format
	 * @return the calendar
	 */
	public static Calendar stringToCalendarDate(String dateString,  String dateFormat) {
		Date date=stringToDate(dateString, dateFormat);
		Calendar calendar=Calendar.getInstance();
		calendar.setTime(date);
		return resetHour(calendar);
	}

	


	/**
	 * String to date.
	 *
	 * @param createTimestamp the create timestamp
	 * @param dateFormat the date format
	 * @return the date
	 */
	public static Date stringToDate(String createTimestamp, String dateFormat) {
		try {
			SimpleDateFormat sdf=new SimpleDateFormat(dateFormat);
			Date date = sdf.parse(createTimestamp);
			return date;
		} catch (Exception e) {
			logger.error("error while converting string to Date - input: '" + createTimestamp + "'", e);
		}
		return null;
	}

	

	/**
	 * Calendar to string.
	 *
	 * @param cal the cal
	 * @param formato the formato
	 * @return the string
	 */
	public static String calendarToString(Calendar cal, String formato) {
		return dateToString(calendarToDate(cal), formato);
	}

	/**
	 * Date to string.
	 *
	 * @param data the data
	 * @param dateFormat the date format
	 * @return the string
	 */
	public static String dateToString(Date data, String dateFormat) {
		String dateToString = null;

		if (data != null && StringUtils.isNotBlank(dateFormat)) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
				dateToString = sdf.format(data.getTime());
			} catch (Exception e) {
				logger.error("error while converting calendar to string", e);
			}
		} else {
			logger.info("Null Parameter -> date:" + data + ", formato: " + dateFormat);
		}
		return dateToString;
	}




	/**
	 * Today.
	 *
	 * @return the calendar
	 */
	public static Calendar today() {
		return resetHour(Calendar.getInstance());
	}



	
	/**
	 * Reset hour.
	 *
	 * @param cal the cal
	 * @return the calendar
	 */
	public static Calendar resetHour(Calendar cal) {
		if (cal == null)
			return null;

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal;
	}


	/**
	 * Reset hour.
	 *
	 * @param date the date
	 * @return the date
	 */
	public static Date resetHour(Date date) {
		if (date == null)
			return null;
		Calendar cal = dateToCalendar(date);
		cal = resetHour(cal);

		return calendarToDate(cal);
	}

	
	/**
	 * Differnece date.
	 *
	 * @param maxDate the max date
	 * @param minDate the min date
	 * @param timeUnitMeasureType the time unit measure type
	 * @return the long
	 */
	public static Long differneceDate(Calendar maxDate, Calendar minDate,TimeUnitMeasureType timeUnitMeasureType) {
		if (maxDate != null && minDate != null) {
			maxDate = resetHour(maxDate);
			minDate = resetHour(minDate);
			long giorni = (maxDate.getTime().getTime() - minDate.getTime().getTime()) / timeUnitMeasureType.getTime();
			return giorni;
		}
		return null;
	}

	/**
	 * Differnece date.
	 *
	 * @param maxDate the max date
	 * @param minDate the min date
	 * @param timeUnitMeasureType the time unit measure type
	 * @return the long
	 */
	public static Long differneceDate(Date maxDate, Date minDate,TimeUnitMeasureType timeUnitMeasureType) {
		if (maxDate != null && minDate != null) {
			return differneceDate(dateToCalendar(maxDate), dateToCalendar(minDate),timeUnitMeasureType);
		}
		return null;
	}

	/**
	 * Gets the current year.
	 *
	 * @return the current year
	 */
	public static int getCurrentYear() {
		return Calendar.getInstance().get(Calendar.YEAR);
	}




	/**
	 * Sum date.
	 *
	 * @param date   the date
	 * @param year   the year
	 * @param month  the month
	 * @param week the week
	 * @param day    the day
	 * @param hour   the hour
	 * @param minute the minute
	 * @param second the second
	 * @return the date
	 */
	public static Date sumDate(Date date,int year, int month, int week, int day,int hour,int minute,int second) {
		Calendar calendar=sumDate(dateToCalendar(date), year, month, week, day,hour,minute,second);
		if(calendar!=null)
			date=calendar.getTime();
		return date;
	}
	
	
	/**
	 * Sum date.
	 *
	 * @param timestamp the timestamp
	 * @param year the year
	 * @param month the month
	 * @param week the week
	 * @param day the day
	 * @param hour the hour
	 * @param minute the minute
	 * @param second the second
	 * @return the timestamp
	 */
	public static Timestamp sumDate(Timestamp timestamp,int year, int month, int week, int day,int hour,int minute,int second) {
		Date date=sumDate(timestampToDate(timestamp), year, month, week, day, hour, minute, second);
		timestamp=dateToTimestamp(date);
		return timestamp;
	}
	
		
	/**
	 * Sum date.
	 *
	 * @param date  the date
	 * @param year  the year
	 * @param month the month
	 * @param week the week
	 * @param day   the day
	 * @return the date
	 */
	public static Date sumDate(Date date,int year, int month, int week, int day) {
		return sumDate(date, year, month,week, day,0,0,0);
	}

	
	/**
	 * Sum date.
	 *
	 * @param calendar the calendar
	 * @param year     the year
	 * @param month    the month
	 * @param week the week
	 * @param day      the day
	 * @return the calendar
	 */
	public static Calendar sumDate(Calendar calendar,int year, int month,int week, int day) {
		return sumDate(calendar, year, month,week, day,0, 0, 0);
	}
	
	
	
	/**
	 * Sum date.
	 *
	 * @param calendar the calendar
	 * @param year     the year
	 * @param month    the month
	 * @param week the week
	 * @param day      the day
	 * @param hour     the hour
	 * @param minute   the minute
	 * @param second   the second
	 * @return the calendar
	 */
	public static Calendar sumDate(Calendar calendar,int year, int month,int week, int day,int hour,int minute,int second) {
		if (calendar != null) {
			calendar.add(Calendar.YEAR, year);
			calendar.add(Calendar.MONTH, month);
			calendar.add(Calendar.DATE, week*7+day);
			calendar.add(Calendar.HOUR, hour);
			calendar.add(Calendar.MINUTE, minute);
			calendar.add(Calendar.SECOND, second);
		}
		return calendar;
	}

	/**
	 * Date to timestamp.
	 *
	 * @param date the date
	 * @return the timestamp
	 */
	public static Timestamp dateToTimestamp(Date date) {
		Timestamp timestamp=null;
		if(date!=null)
			timestamp=new Timestamp(date.getTime());
		return timestamp;
	}
	
	/**
	 * Now.
	 *
	 * @return the timestamp
	 */
	public static Timestamp now() {
		return new Timestamp((new Date()).getTime());
	}
	
	/**
	 * Timestamp to date.
	 *
	 * @param timestamp the timestamp
	 * @return the date
	 */
	public static Date timestampToDate(Timestamp timestamp) {
		Date date =null;
		if(timestamp!=null)
			date=new Date(timestamp.getTime());
		return date;
	}
	
	/**
	 * Gets the year.
	 *
	 * @param date the date
	 * @return the year
	 */
	public static Integer getYear(Calendar date) {
		Integer year=null;
		if(date!=null)
			year=date.get(Calendar.YEAR);
		return year;
	}
	
	/**
	 * Gets the month.
	 *
	 * @param date the date
	 * @return the month
	 */
	public static Integer getMonth(Calendar date) {
		Integer month=null;
		if(date!=null)
			month=date.get(Calendar.MONTH);
		return month;
	}
	
	/**
	 * Gets the day of month.
	 *
	 * @param date the date
	 * @return the day of month
	 */
	public static Integer getDayOfMonth(Calendar date) {
		Integer day=null;
		if(date!=null)
			day=date.get(Calendar.DATE);
		return day;
	}
	
	/**
	 * Gets the year.
	 *
	 * @param date the date
	 * @return the year
	 */
	public static Integer getYear(Date date) {
		return getYear(dateToCalendar(date));
	}
	
	/**
	 * Gets the month.
	 *
	 * @param date the date
	 * @return the month
	 */
	public static Integer getMonth(Date date) {
		return getMonth(dateToCalendar(date));
	}
	
	/**
	 * Gets the day of month.
	 *
	 * @param date the date
	 * @return the day of month
	 */
	public static Integer getDayOfMonth(Date date) {
		return getDayOfMonth(dateToCalendar(date));
	}
	
}
