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

/**
 * Spring {@link AnnotationFormatterFactory} that creates {@link DateFormatter} instances
 * for fields annotated with {@link DateTimeZone}.
 *
 * <p>Supports {@link Date} and {@link Calendar} field types.  The Spring environment is
 * injected so that placeholder values (e.g., {@code ${spring.jackson.time-zone}}) in the
 * annotation attributes can be resolved at runtime.</p>
 *
 * @author Francesco Baldi
 */
public final class DateFilterAnnotationFormatterFactory implements AnnotationFormatterFactory<DateTimeZone> {

	private final static Set<Class<?>> FIELD_TYPES=new HashSet<>(Arrays.asList(Date.class,Calendar.class));

	private AbstractEnvironment env;


	/**
	 * Constructs a new factory with the given Spring environment.
	 *
	 * @param env the Spring {@link AbstractEnvironment} used to resolve property placeholders
	 */
	public DateFilterAnnotationFormatterFactory(AbstractEnvironment env) {
		super();
		this.env=env;
	}

	/**
	 * Returns the field types supported by this factory ({@link Date} and {@link Calendar}).
	 *
	 * @return a set containing {@code Date.class} and {@code Calendar.class}
	 */
	@Override
	public Set<Class<?>> getFieldTypes() {
		return FIELD_TYPES;
	}

	/**
	 * Returns a {@link DateFormatter} printer configured from the given {@link DateTimeZone} annotation.
	 *
	 * @param annotation the {@link DateTimeZone} annotation present on the field
	 * @param fieldType  the runtime type of the annotated field
	 * @return a {@link DateFormatter} instance
	 */
	@Override
	public Printer<?> getPrinter(DateTimeZone annotation, Class<?> fieldType) {
		return new DateFormatter<>(annotation, env, fieldType);
	}

	/**
	 * Returns a {@link DateFormatter} parser configured from the given {@link DateTimeZone} annotation.
	 *
	 * @param annotation the {@link DateTimeZone} annotation present on the field
	 * @param fieldType  the runtime type of the annotated field
	 * @return a {@link DateFormatter} instance
	 */
	@Override
	public Parser<?> getParser(DateTimeZone annotation, Class<?> fieldType) {
		return new DateFormatter<>(annotation, env, fieldType);
	}


}
