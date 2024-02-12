/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.aes.config.data.Aes.java
 */
package com.bld.crypto.aes.config.data;

import jakarta.validation.constraints.NotNull;

/**
 * The Class Aes.
 */
public class Aes {
	
	
	
	/** The password. */
	@NotNull
	private String password;
	
	
	
	/** The salt. */
	private String salt;
	
	
	/** The key length. */
	private Integer keyLength;

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
	 * Gets the salt.
	 *
	 * @return the salt
	 */
	public String getSalt() {
		return salt;
	}

	/**
	 * Sets the salt.
	 *
	 * @param salt the new salt
	 */
	public void setSalt(String salt) {
		this.salt = salt;
	}



	/**
	 * Gets the key length.
	 *
	 * @return the key length
	 */
	public Integer getKeyLength() {
		return keyLength;
	}

	/**
	 * Sets the key length.
	 *
	 * @param keyLength the new key length
	 */
	public void setKeyLength(Integer keyLength) {
		this.keyLength = keyLength;
	}
	
}
