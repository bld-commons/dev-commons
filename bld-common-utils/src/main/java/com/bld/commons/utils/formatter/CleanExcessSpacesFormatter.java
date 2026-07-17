package com.bld.commons.utils.formatter;

import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.format.Formatter;

import com.bld.commons.utils.json.annotations.CleanExcessSpaces;

public final class CleanExcessSpacesFormatter implements Formatter<String> {

	private CleanExcessSpaces cleanExcessSpaces;

	public CleanExcessSpacesFormatter(CleanExcessSpaces cleanExcessSpaces) {
		super();
		this.cleanExcessSpaces = cleanExcessSpaces;
	}

	@Override
	public String print(String object, Locale locale) {
		return getText(object);
	}

	private String getText(String text) {
		if (StringUtils.isNotEmpty(text)) {
			if (cleanExcessSpaces.removeAllSpaceType())
				text = text.replaceAll("\\s+", "");
			else {
				if (cleanExcessSpaces.removeEndline())
					text = text.replace("\n", "");
				if (cleanExcessSpaces.removeTab())
					text = text.replace("\t", "");
				if (cleanExcessSpaces.trim())
					text = text.trim();
				String space = "";
				for (int i = 0; i < cleanExcessSpaces.consecutive(); i++)
					space += " ";
				text = removeSpace(space + " ", space, text);
			}

			switch (cleanExcessSpaces.upperLowerType()) {
			case LOWER:
				text = text.toLowerCase();
				break;
			case UPPER:
				text = text.toUpperCase();
				break;
			case NONE:
			default:
				break;
			}
		}
		return text;
	}

	private String removeSpace(String remveText, String replaceText, String text) {
		if (text.contains(remveText))
			text = removeSpace(remveText, replaceText, text.replace(remveText, replaceText));
		return text;
	}

	@Override
	public String parse(String text, Locale locale) throws ParseException {
		return getText(text);
	}

}
