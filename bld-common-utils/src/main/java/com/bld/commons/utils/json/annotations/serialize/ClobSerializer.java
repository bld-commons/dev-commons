package com.bld.commons.utils.json.annotations.serialize;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

@SuppressWarnings("serial")
@JacksonStdImpl
public class ClobSerializer extends StdScalarSerializer<Clob> {

	protected ClobSerializer() {
		super(Clob.class);
	}

	@Override
	public void serialize(Clob value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		String json=null;
		if(value!=null) {
			try {
				BufferedReader stringReader = new BufferedReader(value.getCharacterStream());
				String singleLine = null;
				StringBuilder strBuilder = new StringBuilder();
				while ((singleLine = stringReader.readLine()) != null) 
					strBuilder.append(singleLine).append("\n");
				json = strBuilder.toString();
				json=json.replaceAll("\u001B\\[[\\d;]*[^\\d;]","");
			} catch (SQLException e) {
				throw new RuntimeException(e); 
			}
			
		}		
		gen.writeString(json);
	}

}
