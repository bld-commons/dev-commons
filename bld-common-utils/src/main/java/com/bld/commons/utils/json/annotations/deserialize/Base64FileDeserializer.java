/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.annotations.deserialize.CustomByteDeserializer.java
 */
package com.bld.commons.utils.json.annotations.deserialize;

import java.io.IOException;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * The Class CustomByteDeserializer.
 */
public class Base64FileDeserializer extends JsonDeserializer<byte[]> {

	/**
	 * Deserialize.
	 *
	 * @param p the p
	 * @param ctxt the ctxt
	 * @return the byte[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws JsonProcessingException the json processing exception
	 */
	@Override
	public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		String file=p.getText();
		String partSeparator = ",";
		if(file.contains(partSeparator))
			file=file.substring(file.indexOf(partSeparator)+1);
		return Base64.getDecoder().decode(file);
	}

}
