/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.config.EnableRestConnectionConfiguration.java
 */
package com.bld.commons.connection.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that activates component scanning for the
 * {@code com.bld.commons.connection} package.
 * Imported automatically by the {@link com.bld.commons.connection.config.annotation.EnableRestConnection} annotation.
 */
@Configuration
@ComponentScan(basePackages = {"com.bld.commons.connection"})
public class EnableRestConnectionConfiguration {

}
