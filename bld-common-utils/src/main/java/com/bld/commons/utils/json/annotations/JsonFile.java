/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.json.annotations.JsonFile.java
 */
package com.bld.commons.utils.json.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.bld.commons.utils.json.annotations.deserialize.ByteDeserializer;
import com.bld.commons.utils.json.annotations.serialize.ByteSerializer;
import com.bld.commons.utils.types.MimeType;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * The Interface JsonFile.
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
@JacksonAnnotationsInside
@JsonDeserialize(using = ByteDeserializer.class)
@JsonSerialize(using = ByteSerializer.class)
@JsonInclude(Include.NON_NULL)
public @interface JsonFile {

	/**
	 * Mime type.
	 *
	 * @return the mime type
	 */
	public MimeType mimeType() default MimeType.none;
	
	
}
