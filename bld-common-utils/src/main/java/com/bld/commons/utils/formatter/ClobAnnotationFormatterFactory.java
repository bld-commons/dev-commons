package com.bld.commons.utils.formatter;

import java.sql.Clob;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

import com.bld.commons.utils.json.annotations.TextClob;

/**
 * Spring {@link AnnotationFormatterFactory} that creates {@link ClobFormatter} instances
 * for fields annotated with {@link TextClob}.
 *
 * <p>Registers support for {@link Clob} field types, enabling Spring MVC to convert
 * between {@code Clob} objects and their textual representation when binding form parameters.</p>
 *
 * @author Francesco Baldi
 */
public final class ClobAnnotationFormatterFactory implements AnnotationFormatterFactory<TextClob> {

	private final static Set<Class<?>> FIELD_TYPES=new HashSet<>(Arrays.asList(Clob.class));


	/**
	 * Returns the set of field types supported by this factory (i.e., {@link Clob}).
	 *
	 * @return a set containing {@code Clob.class}
	 */
	@Override
	public Set<Class<?>> getFieldTypes() {
		return FIELD_TYPES;
	}

	/**
	 * Returns a {@link ClobFormatter} to print (serialise) a {@link Clob} value.
	 *
	 * @param annotation the {@link TextClob} annotation present on the field
	 * @param fieldType  the runtime type of the annotated field
	 * @return a new {@link ClobFormatter} instance
	 */
	@Override
	public Printer<?> getPrinter(TextClob annotation, Class<?> fieldType) {
		return new ClobFormatter();
	}

	/**
	 * Returns a {@link ClobFormatter} to parse (deserialise) a text value into a {@link Clob}.
	 *
	 * @param annotation the {@link TextClob} annotation present on the field
	 * @param fieldType  the runtime type of the annotated field
	 * @return a new {@link ClobFormatter} instance
	 */
	@Override
	public Parser<?> getParser(TextClob annotation, Class<?> fieldType) {
		return new ClobFormatter();
	}


}
