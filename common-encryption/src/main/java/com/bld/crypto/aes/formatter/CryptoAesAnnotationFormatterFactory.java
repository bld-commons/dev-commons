/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.aes.formatter.CryptoAesAnnotationFormatterFactory.java
 */
package com.bld.crypto.aes.formatter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

import com.bld.crypto.aes.CryptoAesUtils;
import com.bld.crypto.aes.annotation.CryptoAes;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A factory for creating CryptoAesAnnotationFormatter objects.
 */
public final class CryptoAesAnnotationFormatterFactory implements AnnotationFormatterFactory<CryptoAes> {

	/** The Constant FIELD_TYPES. */
	private final static Set<Class<?>> FIELD_TYPES=new HashSet<>(Arrays.asList(String.class,Number.class));
	
	/** The crypto aes utils. */
	private CryptoAesUtils cryptoAesUtils;
	
	/** The obj mapper. */
	private ObjectMapper objMapper;
	

	
	
	/**
	 * Instantiates a new crypto aes annotation formatter factory.
	 *
	 * @param cryptoAesUtils the crypto aes utils
	 * @param objMapper the obj mapper
	 */
	public CryptoAesAnnotationFormatterFactory(CryptoAesUtils cryptoAesUtils, ObjectMapper objMapper) {
		super();
		this.cryptoAesUtils = cryptoAesUtils;
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
	public Printer<?> getPrinter(CryptoAes annotation, Class<?> fieldType) {
		return new CryptoAesFormatter<>(objMapper, fieldType, annotation, cryptoAesUtils);
	}

	/**
	 * Gets the parser.
	 *
	 * @param annotation the annotation
	 * @param fieldType the field type
	 * @return the parser
	 */
	@Override
	public Parser<?> getParser(CryptoAes annotation, Class<?> fieldType) {
		return new CryptoAesFormatter<>(objMapper, fieldType, annotation, cryptoAesUtils);
	}

	
}
