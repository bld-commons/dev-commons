package com.bld.commons.utils.formatter;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.core.env.AbstractEnvironment;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

import com.bld.commons.utils.json.annotations.DateTimeZone;

public final class DateFilterAnnotationFormatterFactory implements AnnotationFormatterFactory<DateTimeZone> {

	private final static Set<Class<?>> FIELD_TYPES=new HashSet<>(Arrays.asList(Date.class,Calendar.class));
	
	private AbstractEnvironment env;
	
	
	public DateFilterAnnotationFormatterFactory(AbstractEnvironment env) {
		super();
		this.env=env;
	}

	@Override
	public Set<Class<?>> getFieldTypes() {
		return FIELD_TYPES;
	}

	@Override
	public Printer<?> getPrinter(DateTimeZone annotation, Class<?> fieldType) {
		return new DateFormatter<>(annotation, env, fieldType);
	}

	@Override
	public Parser<?> getParser(DateTimeZone annotation, Class<?> fieldType) {
		return new DateFormatter<>(annotation, env, fieldType);
	}

	
}
