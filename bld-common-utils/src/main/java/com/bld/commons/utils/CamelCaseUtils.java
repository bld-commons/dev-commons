/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.persistence.reflection.utils.CamelCaseUtils.java
 */
package com.bld.commons.utils;


import org.apache.commons.text.WordUtils;

import com.bld.commons.utils.types.UpperLowerType;

/**
 * The Class CamelCaseUtils.
 */
public class CamelCaseUtils {

	/** The Constant P_UPPER. */
	private static final String P_UPPER = "(?=\\p{Upper})";

	/**
	 * Reverse camel case.
	 *
	 * @param javaField the java field
	 * @param upperLowerType the upper lower type
	 * @return the string
	 */
	public static String reverseCamelCase(String javaField, UpperLowerType upperLowerType) {
		String dbField = "";
		String[] splitJavaFields = javaField.split(P_UPPER);
		for (String splitJavaField : splitJavaFields) {
			dbField += splitJavaField + " ";
		}
		dbField = dbField.trim();
		dbField = dbField.replace(" ", "_");
		if (upperLowerType == null)
			dbField = Character.toUpperCase(dbField.charAt(0)) + dbField.substring(1);
		else {
			if (UpperLowerType.UPPER.equals(upperLowerType))
				dbField = dbField.toUpperCase();
			else if(UpperLowerType.LOWER.equals(upperLowerType))
				dbField = dbField.toLowerCase();
				
		}

		return dbField;
	}
	
	
	/**
	 * Camel case.
	 *
	 * @param tableName the table name
	 * @param firstCharacterLowerCase the first character lower case
	 * @return the string
	 */
	public static String camelCase(String tableName,boolean firstCharacterLowerCase) {
		String className = tableName.replace("_", " ");
		className = (WordUtils.capitalizeFully(className)).replace(" ", "");
		if(firstCharacterLowerCase)
			className = Character.toLowerCase(className.charAt(0)) + className.substring(1);
		return className;
	}

	/**
	 * Camel case.
	 *
	 * @param tableName the table name
	 * @return the string
	 */
	public static String camelCase(String tableName) {
		return camelCase(tableName, false);
	}
	

}
