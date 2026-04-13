/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pubkey.annotations.DecryptPubKey.java
 */
package com.bld.crypto.pubkey.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.bld.crypto.pubkey.deserializer.DecryptPubKeyDeserializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


/**
 * The Interface DecryptPubKey.
 */
@Retention(RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD,ElementType.PARAMETER})
@JacksonAnnotationsInside
@JsonDeserialize(using = DecryptPubKeyDeserializer.class)
@JsonInclude(Include.NON_NULL)
public @interface DecryptPubKey {

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
