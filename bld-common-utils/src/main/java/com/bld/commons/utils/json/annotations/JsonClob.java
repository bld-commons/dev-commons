/*
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.json.annotations.JsonClob.java 
 */
package com.bld.commons.utils.json.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.bld.commons.utils.json.annotations.deserialize.ClobDeserializer;
import com.bld.commons.utils.json.annotations.serialize.ClobSerializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * The Interface JsonClob.
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD,PARAMETER })
@JacksonAnnotationsInside
@JsonDeserialize(using = ClobDeserializer.class)
@JsonSerialize(using=ClobSerializer.class)
public @interface JsonClob {

}
