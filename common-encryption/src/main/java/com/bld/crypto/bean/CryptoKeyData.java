/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.bean.CryptoKeyData.java
 */
package com.bld.crypto.bean;


/**
 * The Class CryptoPubKeyData.
 */
public class CryptoKeyData {

	/** The name. */
	private String name;
	
	/** The url. */
	private boolean url;

	/**
	 * Instantiates a new crypto pub key data.
	 *
	 * @param name the name
	 * @param url the url
	 */
	public CryptoKeyData(String name, boolean url) {
		super();
		this.name = name;
		this.url = url;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Checks if is url.
	 *
	 * @return true, if is url
	 */
	public boolean isUrl() {
		return url;
	}
	
	
	
}
