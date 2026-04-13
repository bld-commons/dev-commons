/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.hmac.annotation.CryptoHmac.java
 */
package com.bld.crypto.hmac.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.bld.crypto.hmac.deserializer.DecryptHmacDeserializer;
import com.bld.crypto.hmac.serializer.EncryptHmacSerializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Field-level annotation that transparently signs a value on Jackson serialisation
 * and verifies it on deserialisation using HMAC.
 *
 * <p>The produced token embeds the plain-text value inside a JSON envelope together
 * with the {@link #value()} field identifier, so cross-field substitution is detected
 * at verification time.
 *
 * <p>Example:
 * <pre>{@code
 * @CryptoHmac(value = "serviceDataSourceId")
 * private Integer id;
 * }</pre>
 */
@Retention(RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@JacksonAnnotationsInside
@JsonDeserialize(using = DecryptHmacDeserializer.class)
@JsonSerialize(using = EncryptHmacSerializer.class)
@JsonInclude(Include.NON_NULL)
public @interface CryptoHmac {

	/**
	 * When {@code true} the token uses URL-safe Base64 encoding (no padding),
	 * suitable for path segments or query parameters.
	 *
	 * @return {@code true} to enable URL-safe encoding
	 */
	boolean url() default false;

	/**
	 * Logical field identifier embedded inside the signed token. Used to verify
	 * that the token belongs to the expected field, preventing cross-field substitution.
	 *
	 * @return the field identifier string
	 */
	String value();
}
