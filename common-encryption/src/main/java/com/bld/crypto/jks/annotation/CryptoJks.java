/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.jks.annotations.CryptoJks.java
 */
package com.bld.crypto.jks.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.bld.crypto.jks.deserializer.DecryptJksDeserializer;
import com.bld.crypto.jks.serializer.EncryptJksSerializer;
import com.bld.crypto.type.CryptoType;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * The Interface CryptoJks.
 */
@Retention(RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD,ElementType.PARAMETER})
@JacksonAnnotationsInside
@JsonDeserialize(using = DecryptJksDeserializer.class)
@JsonSerialize(using = EncryptJksSerializer.class)
public @interface CryptoJks {

	/**
	 * Url.
	 *
	 * @return true, if successful
	 */
	public boolean url() default false;
	
	/**
	 * Encrypt.
	 *
	 * @return the crypto type
	 */
	public CryptoType encrypt() default CryptoType.publicKey;
	
	/**
	 * Decrypt.
	 *
	 * @return the crypto type
	 */
	public CryptoType decrypt() default CryptoType.privateKey;
	
	
	public String value() default "";
	
	
}
