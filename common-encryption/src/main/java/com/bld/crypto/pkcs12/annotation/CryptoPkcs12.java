/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pkcs12.annotation.CryptoPkcs12.java
 */
package com.bld.crypto.pkcs12.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;


/**
 * Field-level annotation that transparently encrypts a value on Jackson serialisation
 * and decrypts it on deserialisation using the X25519 + AES-256-GCM hybrid scheme
 * provided by {@link com.bld.crypto.pkcs12.CryptoPkcs12Utils}.
 *
 * <p>Example:
 * <pre>{@code
 * @CryptoPkcs12(value = "mySecretField")
 * private String sensitiveData;
 * }</pre>
 */
@Retention(RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE_USE})
@JacksonAnnotationsInside
//@JsonDeserialize(using = DecryptPkcs12Deserializer.class)
//@JsonSerialize(using = EncryptPkcs12Serializer.class)
public @interface CryptoPkcs12 {

	/**
	 * When {@code true} the encrypted output receives an additional Base64 encoding
	 * layer so that it is safe for use in URL path segments or query parameters.
	 *
	 * @return {@code true} to enable URL-safe double encoding
	 */
	public boolean url() default false;

	/**
	 * Logical key identifier embedded inside the encrypted payload.  Used to verify
	 * that the decrypted value belongs to the expected field, preventing accidental
	 * cross-field substitution.
	 *
	 * @return the key identifier string
	 */
	public String value() default "";

}
