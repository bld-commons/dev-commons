package com.bld.commons.utils;

public class CommonUtility {

	
	public static boolean isAssignableFrom(Class<?>classObj,Class<?>... classes) {
		for(Class<?>clazz:classes) {
			if(clazz.isAssignableFrom(classObj))
				return true;
		}
		return false;
	}
	
	public static boolean isAssignableFrom(Object obj,Class<?>... classes) {
		boolean assignableFrom=false;
		if(obj!=null)
			assignableFrom=isAssignableFrom(obj.getClass(), classes);
		return assignableFrom;
	}
}
