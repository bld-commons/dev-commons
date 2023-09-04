/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pubkey.annotations.CryptoPubKey.java
 */
package com.bld.crypto.pubkey.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.bld.crypto.pubkey.deserializer.DecryptPubKeyDeserializer;
import com.bld.crypto.pubkey.serializer.EncryptPubKeySerializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * The Interface CryptoPubKey.
 */
@Retention(RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD,ElementType.PARAMETER})
@JacksonAnnotationsInside
@JsonDeserialize(using = DecryptPubKeyDeserializer.class)
@JsonSerialize(using = EncryptPubKeySerializer.class)
@JsonInclude(Include.NON_NULL)
public @interface CryptoPubKey {

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
