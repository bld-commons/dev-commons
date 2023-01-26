/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.utils.StaticApplicationContext.java
 */
package com.bld.commons.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * The Class StaticApplicationContext.
 */
@Component
public class StaticApplicationContext  implements ApplicationContextAware{
	
	/** The application context. */
	private static ApplicationContext applicationContext;

	/**
	 * Sets the application context.
	 *
	 * @param ac the new application context
	 * @throws BeansException the beans exception
	 */
	@Override
	public void setApplicationContext(ApplicationContext ac) throws BeansException {
		applicationContext=ac;
	}
	
	/**
	 * Gets the bean.
	 *
	 * @param <T> the generic type
	 * @param classBean the class bean
	 * @return the bean
	 */
	public static <T> T getBean(Class<T>classBean) {
		return applicationContext.getBean(classBean);
	}
	
	/**
	 * Gets the bean.
	 *
	 * @param <T> the generic type
	 * @param className the class name
	 * @param classBean the class bean
	 * @return the bean
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String className,Class<T>classBean) {
		return (T)applicationContext.getBean(className);
	}
	
}