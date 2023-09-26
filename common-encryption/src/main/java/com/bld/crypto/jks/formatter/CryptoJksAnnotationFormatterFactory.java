/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.jks.formatter.CryptoJksAnnotationFormatterFactory.java
 */
package com.bld.crypto.jks.formatter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

import com.bld.crypto.jks.CryptoJksUtils;
import com.bld.crypto.jks.annotation.CryptoJks;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A factory for creating CryptoJksAnnotationFormatter objects.
 */
public final class CryptoJksAnnotationFormatterFactory implements AnnotationFormatterFactory<CryptoJks> {

	/** The Constant FIELD_TYPES. */
	private final static Set<Class<?>> FIELD_TYPES=new HashSet<>(Arrays.asList(String.class,Number.class));
	
	/** The crypto jks utils. */
	private CryptoJksUtils cryptoJksUtils;
	
	/** The obj mapper. */
	private ObjectMapper objMapper;
	

	
	
	/**
	 * Instantiates a new crypto jks annotation formatter factory.
	 *
	 * @param cryptoJksUtils the crypto jks utils
	 * @param objMapper the obj mapper
	 */
	public CryptoJksAnnotationFormatterFactory(CryptoJksUtils cryptoJksUtils, ObjectMapper objMapper) {
		super();
		this.cryptoJksUtils = cryptoJksUtils;
		this.objMapper = objMapper;
	}

	/**
	 * Gets the field types.
	 *
	 * @return the field types
	 */
	@Override
	public Set<Class<?>> getFieldTypes() {
		return FIELD_TYPES;
	}

	/**
	 * Gets the printer.
	 *
	 * @param annotation the annotation
	 * @param fieldType the field type
	 * @return the printer
	 */
	@Override
	public Printer<?> getPrinter(CryptoJks annotation, Class<?> fieldType) {
		return new CryptoJksFormatter<>(annotation, this.cryptoJksUtils, this.objMapper, fieldType);
	}

	/**
	 * Gets the parser.
	 *
	 * @param annotation the annotation
	 * @param fieldType the field type
	 * @return the parser
	 */
	@Override
	public Parser<?> getParser(CryptoJks annotation, Class<?> fieldType) {
		return new CryptoJksFormatter<>(annotation, this.cryptoJksUtils, this.objMapper, fieldType);
	}

	
}
