/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.exception.CryptoException.java
 */
package com.bld.crypto.exception;


/**
 * Unchecked exception thrown whenever a cryptographic operation fails inside the
 * {@code common-encryption} module.
 *
 * <p>This exception wraps lower-level checked exceptions (e.g.
 * {@link java.security.GeneralSecurityException}, {@link java.io.IOException}) so that
 * callers do not have to declare checked exceptions on every method that performs
 * encryption or decryption.
 */
@SuppressWarnings("serial")
public class CryptoException extends RuntimeException {

	/**
	 * Constructs a new {@code CryptoException} with no detail message.
	 */
	public CryptoException() {
		super();
	}

	/**
	 * Constructs a new {@code CryptoException} with the specified detail message, cause,
	 * suppression flag, and writable stack trace flag.
	 *
	 * @param message            the detail message
	 * @param cause              the cause of this exception
	 * @param enableSuppression  whether suppression is enabled or disabled
	 * @param writableStackTrace whether the stack trace should be writable
	 */
	public CryptoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Constructs a new {@code CryptoException} with the specified detail message and cause.
	 *
	 * @param message the detail message
	 * @param cause   the cause of this exception
	 */
	public CryptoException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new {@code CryptoException} with the specified detail message.
	 *
	 * @param message the detail message
	 */
	public CryptoException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@code CryptoException} with the specified cause.
	 *
	 * @param cause the cause of this exception
	 */
	public CryptoException(Throwable cause) {
		super(cause);
	}

	
	
	

}
