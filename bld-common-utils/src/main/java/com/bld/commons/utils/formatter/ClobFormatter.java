package com.bld.commons.utils.formatter;

import java.io.BufferedReader;
import java.sql.Clob;
import java.text.ParseException;
import java.util.Locale;

import javax.sql.rowset.serial.SerialClob;

import org.apache.commons.lang3.StringUtils;
import org.springframework.format.Formatter;

/**
 * Spring {@link Formatter} that converts between {@link Clob} objects and their plain-text
 * {@link String} representation.
 *
 * <p>ANSI escape sequences are stripped from the text during both printing and parsing.
 * Used by {@link ClobAnnotationFormatterFactory} for form-binding on fields annotated
 * with {@link com.bld.commons.utils.json.annotations.TextClob}.</p>
 *
 * @author Francesco Baldi
 */
public class ClobFormatter implements Formatter<Clob> {

	/**
	 * Converts a {@link Clob} to its {@link String} representation.
	 *
	 * <p>Reads all lines from the CLOB character stream, concatenating them with
	 * newline separators, then strips any ANSI escape sequences.</p>
	 *
	 * @param object the {@link Clob} to print; may be {@code null}
	 * @param locale the current locale (not used)
	 * @return the textual content of the CLOB, or {@code null} if the input is {@code null}
	 * @throws RuntimeException if an SQL or I/O error occurs while reading the CLOB
	 */
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

	/**
	 * Parses a plain-text {@link String} into a {@link Clob}.
	 *
	 * <p>ANSI escape sequences are stripped before the {@link SerialClob} is created.</p>
	 *
	 * @param text   the text to convert; may be blank
	 * @param locale the current locale (not used)
	 * @return a {@link SerialClob} wrapping the sanitised text, or {@code null} if the
	 *         input is empty/blank
	 * @throws ParseException   never thrown directly (declared by the interface)
	 * @throws RuntimeException if the {@link SerialClob} cannot be created
	 */
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
