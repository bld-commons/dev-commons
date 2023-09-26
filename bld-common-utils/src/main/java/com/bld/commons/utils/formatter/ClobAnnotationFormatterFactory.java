package com.bld.commons.utils.formatter;

import java.sql.Clob;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

import com.bld.commons.utils.json.annotations.TextClob;

public final class ClobAnnotationFormatterFactory implements AnnotationFormatterFactory<TextClob> {

	private final static Set<Class<?>> FIELD_TYPES=new HashSet<>(Arrays.asList(Clob.class));
	
	
	@Override
	public Set<Class<?>> getFieldTypes() {
		return FIELD_TYPES;
	}

	@Override
	public Printer<?> getPrinter(TextClob annotation, Class<?> fieldType) {
		return new ClobFormatter();
	}

	@Override
	public Parser<?> getParser(TextClob annotation, Class<?> fieldType) {
		return new ClobFormatter();
	}

	
}
