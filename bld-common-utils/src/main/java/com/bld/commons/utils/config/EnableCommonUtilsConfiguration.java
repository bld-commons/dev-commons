/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.config.EnableRestConnectionConfiguration.java
 */
package com.bld.commons.utils.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.bld.context.annotation.config.EnableContextAnnotatation;



/**
 * The Class EnableCommonUtilsConfiguration.
 */
@Configuration
@EnableContextAnnotatation
@ComponentScan(basePackages = {"com.bld.commons.utils"})
public class EnableCommonUtilsConfiguration {
	
	
}
