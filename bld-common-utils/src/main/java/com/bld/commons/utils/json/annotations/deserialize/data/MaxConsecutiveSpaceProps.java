/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.annotations.deserialize.data.MaxConsecutiveSpaceProps.java
 */
package com.bld.commons.utils.json.annotations.deserialize.data;

import com.bld.commons.utils.types.UpperLowerType;

/**
 * The Class MaxConsecutiveSpaceProps.
 */
public class MaxConsecutiveSpaceProps {
	
	/** The consecutive. */
	private int consecutive;

	/** The trim. */
	private boolean trim;
	
	/** The remove endline. */
	private boolean removeEndline;
	
	/** The remove all space type. */
	private boolean removeAllSpaceType;
	
	/** The upper lower type. */
	private UpperLowerType upperLowerType;
	
	/** The remove tab. */
	public boolean removeTab;



	

	/**
	 * Instantiates a new max consecutive space props.
	 *
	 * @param consecutive the consecutive
	 * @param trim the trim
	 * @param removeEndline the remove endline
	 * @param removeAllSpaceType the remove all space type
	 * @param upperLowerType the upper lower type
	 * @param removeTab the remove tab
	 */
	public MaxConsecutiveSpaceProps(int consecutive, boolean trim, boolean removeEndline, boolean removeAllSpaceType, UpperLowerType upperLowerType, boolean removeTab) {
		super();
		this.consecutive = consecutive;
		this.trim = trim;
		this.removeEndline = removeEndline;
		this.removeAllSpaceType = removeAllSpaceType;
		this.upperLowerType = upperLowerType;
		this.removeTab = removeTab;
	}

	/**
	 * Gets the consecutive.
	 *
	 * @return the consecutive
	 */
	public int getConsecutive() {
		return consecutive;
	}

	/**
	 * Checks if is trim.
	 *
	 * @return true, if is trim
	 */
	public boolean isTrim() {
		return trim;
	}

	/**
	 * Checks if is removes the endline.
	 *
	 * @return true, if is removes the endline
	 */
	public boolean isRemoveEndline() {
		return removeEndline;
	}

	/**
	 * Checks if is removes the all space type.
	 *
	 * @return true, if is removes the all space type
	 */
	public boolean isRemoveAllSpaceType() {
		return removeAllSpaceType;
	}

	/**
	 * Gets the upper lower type.
	 *
	 * @return the upper lower type
	 */
	public UpperLowerType getUpperLowerType() {
		return upperLowerType;
	}

	/**
	 * Checks if is removes the tab.
	 *
	 * @return true, if is removes the tab
	 */
	public boolean isRemoveTab() {
		return removeTab;
	}

	
}
