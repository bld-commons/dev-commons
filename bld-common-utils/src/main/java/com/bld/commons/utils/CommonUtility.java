package com.bld.commons.utils;

/**
 * General-purpose utility class providing helper methods for class-type checks.
 *
 * <p>This class contains static utility methods that simplify common Java
 * reflection operations, such as checking assignability between types.</p>
 *
 * @author Francesco Baldi
 */
public class CommonUtility {

	/**
	 * Checks whether the given class is assignable from (i.e., a subtype of or
	 * the same as) any of the supplied candidate classes.
	 *
	 * @param classObj the class to test
	 * @param classes  one or more candidate classes to check against
	 * @return {@code true} if at least one of the candidate classes is assignable
	 *         from {@code classObj}; {@code false} otherwise
	 */
	public static boolean isAssignableFrom(Class<?>classObj,Class<?>... classes) {
		for(Class<?>clazz:classes) {
			if(clazz.isAssignableFrom(classObj))
				return true;
		}
		return false;
	}

	/**
	 * Checks whether the runtime type of the given object is assignable from any
	 * of the supplied candidate classes.
	 *
	 * <p>Returns {@code false} (without throwing) if {@code obj} is {@code null}.</p>
	 *
	 * @param obj     the object whose class is to be tested; may be {@code null}
	 * @param classes one or more candidate classes to check against
	 * @return {@code true} if {@code obj} is non-null and its class is assignable
	 *         from at least one of the candidate classes; {@code false} otherwise
	 */
	public static boolean isAssignableFrom(Object obj,Class<?>... classes) {
		boolean assignableFrom=false;
		if(obj!=null)
			assignableFrom=isAssignableFrom(obj.getClass(), classes);
		return assignableFrom;
	}
}
