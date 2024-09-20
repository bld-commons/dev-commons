/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.jks.config.properties.JksProperties.java
 */
package com.bld.crypto.jks.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;


/**
 * The Class JksProperties.
 */
@Configuration
@ConfigurationProperties(prefix = "com.bld.crypto.jks")
public class JksProperties {

	
	/** The file. */
	private Resource file;
	
	/** The password. */
	private String password;

	/** The alias. */
	private String alias;
	
	/** The instance jks. */
	private String instanceJks;


	/**
	 * Gets the file.
	 *
	 * @return the file
	 */
	public Resource getFile() {
		return file;
	}

	/**
	 * Sets the file.
	 *
	 * @param file the new file
	 */
	public void setFile(Resource file) {
		this.file = file;
	}

	/**
	 * Gets the password.
	 *
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password.
	 *
	 * @param password the new password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the alias.
	 *
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * Sets the alias.
	 *
	 * @param alias the new alias
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * Gets the instance jks.
	 *
	 * @return the instance jks
	 */
	public String getInstanceJks() {
		return instanceJks;
	}

	/**
	 * Sets the instance jks.
	 *
	 * @param instanceJks the new instance jks
	 */
	public void setInstanceJks(String instanceJks) {
		this.instanceJks = instanceJks;
	}

	
	
	
}
