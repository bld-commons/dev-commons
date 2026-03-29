/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.persistence.reflection.utils.CamelCaseUtils.java
 */
package com.bld.commons.utils;


import org.apache.commons.text.WordUtils;

import com.bld.commons.utils.types.UpperLowerType;

/**
 * Utility class for converting between camelCase and underscore-separated naming conventions.
 *
 * <p>Provides static helper methods to convert Java camelCase field names into
 * database-style underscore-separated names (e.g., {@code myField} &rarr; {@code MY_FIELD})
 * and to perform the reverse conversion.</p>
 *
 * @author Francesco Baldi
 */
public class CamelCaseUtils {

	/** The Constant P_UPPER. */
	private static final String P_UPPER = "(?=\\p{Upper})";

	/**
	 * Converts a camelCase Java field name into an underscore-separated database column name.
	 *
	 * <p>For example, {@code myFieldName} becomes {@code My_Field_Name},
	 * {@code MY_FIELD_NAME}, or {@code my_field_name} depending on the
	 * {@code upperLowerType} parameter.</p>
	 *
	 * @param javaField      the camelCase Java field name to convert
	 * @param upperLowerType the case transformation to apply ({@code UPPER}, {@code LOWER},
	 *                       or {@code null} to capitalise only the first character)
	 * @return the underscore-separated column name with the requested case applied
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
	 * Converts an underscore-separated name (e.g., a database table or column name)
	 * into a camelCase string.
	 *
	 * <p>For example, {@code my_table_name} becomes {@code MyTableName} when
	 * {@code firstCharacterLowerCase} is {@code false}, or {@code myTableName}
	 * when it is {@code true}.</p>
	 *
	 * @param tableName             the underscore-separated name to convert
	 * @param firstCharacterLowerCase {@code true} to make the first character lowercase
	 *                              (suitable for field names); {@code false} to capitalise it
	 *                              (suitable for class names)
	 * @return the camelCase representation of the input name
	 */
	public static String camelCase(String tableName,boolean firstCharacterLowerCase) {
		String className = tableName.replace("_", " ");
		className = (WordUtils.capitalizeFully(className)).replace(" ", "");
		if(firstCharacterLowerCase)
			className = Character.toLowerCase(className.charAt(0)) + className.substring(1);
		return className;
	}

	/**
	 * Converts an underscore-separated name into a camelCase string with an
	 * uppercase first character (suitable for use as a Java class name).
	 *
	 * <p>Delegates to {@link #camelCase(String, boolean)} with
	 * {@code firstCharacterLowerCase = false}.</p>
	 *
	 * @param tableName the underscore-separated name to convert
	 * @return the PascalCase representation of the input name
	 */
	public static String camelCase(String tableName) {
		return camelCase(tableName, false);
	}
	

}
