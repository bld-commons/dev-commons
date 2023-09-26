package com.bld.commons.utils.formatter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

import com.bld.commons.utils.json.annotations.UpperLowerCase;

public final class UpperLowerAnnotationFormatterFactory implements AnnotationFormatterFactory<UpperLowerCase> {

	private final static Set<Class<?>> FIELD_TYPES=new HashSet<>(Arrays.asList(String.class));
	
	
	@Override
	public Set<Class<?>> getFieldTypes() {
		return FIELD_TYPES;
	}

	@Override
	public Printer<?> getPrinter(UpperLowerCase annotation, Class<?> fieldType) {
		return new UpperLowerFormatter(annotation);
	}

	@Override
	public Parser<?> getParser(UpperLowerCase annotation, Class<?> fieldType) {
		return new UpperLowerFormatter(annotation);
	}

	
}
