/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.exception.CryptoException.java
 */
package com.bld.crypto.exception;


/**
 * The Class CryptoException.
 */
@SuppressWarnings("serial")
public class CryptoException extends RuntimeException {

	/**
	 * Instantiates a new crypto exception.
	 */
	public CryptoException() {
		super();
	}

	/**
	 * Instantiates a new crypto exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 * @param enableSuppression the enable suppression
	 * @param writableStackTrace the writable stack trace
	 */
	public CryptoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Instantiates a new crypto exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public CryptoException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new crypto exception.
	 *
	 * @param message the message
	 */
	public CryptoException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new crypto exception.
	 *
	 * @param cause the cause
	 */
	public CryptoException(Throwable cause) {
		super(cause);
	}

	
	
	

}
