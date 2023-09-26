/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pubkey.formatter.CryptoPubKeyAnnotationFormatterFactory.java
 */
package com.bld.crypto.pubkey.formatter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

import com.bld.crypto.pubkey.CryptoPublicKeyUtils;
import com.bld.crypto.pubkey.annotations.CryptoPubKey;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A factory for creating CryptoPubKeyAnnotationFormatter objects.
 */
public final class CryptoPubKeyAnnotationFormatterFactory implements AnnotationFormatterFactory<CryptoPubKey> {

	/** The Constant FIELD_TYPES. */
	private final static Set<Class<?>> FIELD_TYPES=new HashSet<>(Arrays.asList(String.class,Number.class));
	
	/** The crypto pub key utils. */
	private CryptoPublicKeyUtils cryptoPubKeyUtils;
	
	/** The obj mapper. */
	private ObjectMapper objMapper;
	

	
	
	/**
	 * Instantiates a new crypto pub key annotation formatter factory.
	 *
	 * @param cryptoPubKeyUtils the crypto pub key utils
	 * @param objMapper the obj mapper
	 */
	public CryptoPubKeyAnnotationFormatterFactory(CryptoPublicKeyUtils cryptoPubKeyUtils, ObjectMapper objMapper) {
		super();
		this.cryptoPubKeyUtils = cryptoPubKeyUtils;
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
	public Printer<?> getPrinter(CryptoPubKey annotation, Class<?> fieldType) {
		return new CryptoPubKeyFormatter<>(objMapper, fieldType, annotation, cryptoPubKeyUtils);
	}

	/**
	 * Gets the parser.
	 *
	 * @param annotation the annotation
	 * @param fieldType the field type
	 * @return the parser
	 */
	@Override
	public Parser<?> getParser(CryptoPubKey annotation, Class<?> fieldType) {
		return new CryptoPubKeyFormatter<>(objMapper, fieldType, annotation, cryptoPubKeyUtils);
	}

	
}
