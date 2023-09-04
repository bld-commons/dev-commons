package com.bld.crypto.jks.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.bld.crypto.jks.deserializer.DecryptJksDeserializer;
import com.bld.crypto.jks.serializer.EncryptJksSerializer;
import com.bld.crypto.type.CryptoType;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Retention(RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD,ElementType.PARAMETER})
@JacksonAnnotationsInside
@JsonDeserialize(using = DecryptJksDeserializer.class)
@JsonSerialize(using = EncryptJksSerializer.class)
@JsonInclude(Include.NON_NULL)
public @interface CryptoJks {

	public boolean url() default false;
	
	public CryptoType encrypt() default CryptoType.privateKey;
	
	public CryptoType decrypt() default CryptoType.publicKey;
	
}
