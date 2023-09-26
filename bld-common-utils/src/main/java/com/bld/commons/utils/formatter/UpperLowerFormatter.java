package com.bld.commons.utils.formatter;

import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.format.Formatter;

import com.bld.commons.utils.json.annotations.UpperLowerCase;
import com.bld.commons.utils.types.UpperLowerType;

public final class UpperLowerFormatter implements Formatter<String> {
	
	private UpperLowerCase upperLowerCase;
	
	public UpperLowerFormatter(UpperLowerCase upperLowerCase) {
		super();
		this.upperLowerCase=upperLowerCase;
	}

	@Override
	public String print(String object, Locale locale) {
		return getText(object);
	}

	private String getText(String object) {
		if(StringUtils.isNotBlank(object) && !UpperLowerType.NONE.equals(upperLowerCase.value()))
			if(UpperLowerType.UPPER.equals(upperLowerCase.value()))
				object=object.toUpperCase();
			else
				object=object.toLowerCase();
		return object;
	}

	@Override
	public String parse(String text, Locale locale) throws ParseException {
		return getText(text);
	}

}
