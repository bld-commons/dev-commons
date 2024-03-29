/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.annotations.deserialize.data.DateFilterDeserializer.java
 */
package com.bld.commons.utils.json.annotations.deserialize.data;

/**
 * The Class DateFilterDeserializer.
 */
public class DateChangeDeserializer {

	/** The time zone. */
	private String timeZone;

	/** The format. */
	private String format;

	

	/** The add year. */
	private int addYear;

	/** The add month. */
	private int addMonth;

	/** The add week. */
	private int addWeek;

	/** The add day. */
	private int addDay;

	/** The add hour. */
	private int addHour;

	/** The add minute. */
	private int addMinute;

	/** The add second. */
	private int addSecond;

	/**
	 * Instantiates a new date filter deserializer.
	 *
	 * @param timeZone the time zone
	 * @param format the format
	 */
	public DateChangeDeserializer(String timeZone, String format) {
		super();
		this.timeZone = timeZone;
		this.format = format;
		this.addDay = 0;
		this.addHour = 0;
		this.addMinute = 0;
		this.addMonth = 0;
		this.addSecond = 0;
		this.addWeek = 0;
		this.addYear = 0;
	}

	/**
	 * Instantiates a new date filter deserializer.
	 *
	 * @param timeZone the time zone
	 * @param format the format
	 * @param addYear the add year
	 * @param addMonth the add month
	 * @param addWeek the add week
	 * @param addDay the add day
	 * @param addHour the add hour
	 * @param addMinute the add minute
	 * @param addSecond the add second
	 */
	public DateChangeDeserializer(String timeZone, String format,  int addYear, int addMonth, int addWeek, int addDay, int addHour, int addMinute, int addSecond) {
		super();
		this.timeZone = timeZone;
		this.format = format;
		this.addYear = addYear;
		this.addMonth = addMonth;
		this.addWeek = addWeek;
		this.addDay = addDay;
		this.addHour = addHour;
		this.addMinute = addMinute;
		this.addSecond = addSecond;
	}

	/**
	 * Gets the time zone.
	 *
	 * @return the time zone
	 */
	public String getTimeZone() {
		return timeZone;
	}

	/**
	 * Gets the format.
	 *
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * Gets the adds the year.
	 *
	 * @return the adds the year
	 */
	public int getAddYear() {
		return addYear;
	}

	/**
	 * Gets the adds the month.
	 *
	 * @return the adds the month
	 */
	public int getAddMonth() {
		return addMonth;
	}

	/**
	 * Gets the adds the week.
	 *
	 * @return the adds the week
	 */
	public int getAddWeek() {
		return addWeek;
	}

	/**
	 * Gets the adds the day.
	 *
	 * @return the adds the day
	 */
	public int getAddDay() {
		return addDay;
	}

	/**
	 * Gets the adds the hour.
	 *
	 * @return the adds the hour
	 */
	public int getAddHour() {
		return addHour;
	}

	/**
	 * Gets the adds the minute.
	 *
	 * @return the adds the minute
	 */
	public int getAddMinute() {
		return addMinute;
	}

	/**
	 * Gets the adds the second.
	 *
	 * @return the adds the second
	 */
	public int getAddSecond() {
		return addSecond;
	}

	/**
	 * @param timeZone the timeZone to set
	 */
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	/**
	 * @param format the format to set
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	

	
}
