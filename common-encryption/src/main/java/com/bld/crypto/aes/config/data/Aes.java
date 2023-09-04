/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.aes.config.data.Aes.java
 */
package com.bld.crypto.aes.config.data;

import com.bld.crypto.type.AesSizeType;

/**
 * The Class Aes.
 */
public class Aes {
	
	/** The password. */
	private String password;
	
	/** The size. */
	private AesSizeType size;
	
	/** The salt. */
	private String salt;
	
	
	/** The key length. */
	private int keyLength;

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
	 * Gets the size.
	 *
	 * @return the size
	 */
	public AesSizeType getSize() {
		return size;
	}

	/**
	 * Sets the size.
	 *
	 * @param size the new size
	 */
	public void setSize(AesSizeType size) {
		this.size = size;
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
	public int getKeyLength() {
		return keyLength;
	}

	/**
	 * Sets the key length.
	 *
	 * @param keyLength the new key length
	 */
	public void setKeyLength(int keyLength) {
		this.keyLength = keyLength;
	}
	
	
	
	
}
