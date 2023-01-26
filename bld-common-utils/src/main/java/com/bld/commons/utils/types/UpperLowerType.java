/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.type.UpperLowerType.java
 */
package com.bld.commons.utils.types;

/**
 * The Enum UpperLowerType.
 */
public enum UpperLowerType {

	/** The none. */
	NONE(""),

	/** The upper. */
	UPPER("upper"),

	/** The lower. */
	LOWER("lower");

	/** The function. */
	private String function;

	/**
	 * Instantiates a new upper lower type.
	 *
	 * @param function the function
	 */
	private UpperLowerType(String function) {
		this.function = function;
	}

	/**
	 * Gets the function.
	 *
	 * @return the function
	 */
	public String getFunction() {
		return function;
	}

}
