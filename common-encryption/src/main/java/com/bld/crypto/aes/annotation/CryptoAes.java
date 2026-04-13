/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.aes.annotation.CryptoAes.java
 */
package com.bld.crypto.aes.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.bld.crypto.aes.deserializer.DecryptAesDeserializer;
import com.bld.crypto.aes.serializer.EncryptAesSerializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * The Interface CryptoJks.
 */
@Retention(RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD,ElementType.PARAMETER})
@JacksonAnnotationsInside
@JsonDeserialize(using = DecryptAesDeserializer.class)
@JsonSerialize(using = EncryptAesSerializer.class)
@JsonInclude(Include.NON_NULL)
public @interface CryptoAes {

	/**
	 * Url.
	 *
	 * @return true, if successful
	 */
	public boolean url() default false;
	
	/**
	 * Value.
	 *
	 * @return the string
	 */
	public String value();
	
}
