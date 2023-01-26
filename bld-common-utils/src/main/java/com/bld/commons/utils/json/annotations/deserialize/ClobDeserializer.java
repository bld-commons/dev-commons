package com.bld.commons.utils.json.annotations.deserialize;

import java.io.IOException;
import java.sql.Clob;

import javax.sql.rowset.serial.SerialClob;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

@SuppressWarnings("serial")
@JacksonStdImpl
public class ClobDeserializer extends StdScalarDeserializer<Clob> {

	protected ClobDeserializer() {
		super(Clob.class);
	}

	@Override
	public Clob deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
		String json = p.getText();
		Clob clob = null;
		if (StringUtils.isNotEmpty(json)) {
			try {
				json=json.replaceAll("\u001B\\[[\\d;]*[^\\d;]","");
				clob = new SerialClob(json.toCharArray());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return clob;
	}

}
