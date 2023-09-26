package com.bld.commons.utils.formatter;

import java.io.BufferedReader;
import java.sql.Clob;
import java.text.ParseException;
import java.util.Locale;

import javax.sql.rowset.serial.SerialClob;

import org.apache.commons.lang3.StringUtils;
import org.springframework.format.Formatter;

public class ClobFormatter implements Formatter<Clob> {

	@Override
	public String print(Clob object, Locale locale) {
		String json=null;
		if(object!=null) {
			try {
				BufferedReader stringReader = new BufferedReader(object.getCharacterStream());
				String singleLine = null;
				StringBuilder strBuilder = new StringBuilder();
				while ((singleLine = stringReader.readLine()) != null) 
					strBuilder.append(singleLine).append("\n");
				json = strBuilder.toString();
				json=json.replaceAll("\u001B\\[[\\d;]*[^\\d;]","");
			} catch (Exception e) {
				throw new RuntimeException(e); 
			} 
			
		}		
		return json;
	}

	@Override
	public Clob parse(String text, Locale locale) throws ParseException {
		Clob clob = null;
		if (StringUtils.isNotEmpty(text)) {
			try {
				text=text.replaceAll("\u001B\\[[\\d;]*[^\\d;]","");
				clob = new SerialClob(text.toCharArray());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return clob;
	}

}
