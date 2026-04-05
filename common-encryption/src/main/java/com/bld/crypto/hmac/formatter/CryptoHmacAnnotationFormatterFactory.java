/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.hmac.formatter.CryptoHmacAnnotationFormatterFactory.java
 */
package com.bld.crypto.hmac.formatter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

import com.bld.crypto.hmac.CryptoHmacUtils;
import com.bld.crypto.hmac.annotation.CryptoHmac;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link AnnotationFormatterFactory} that creates {@link CryptoHmacFormatter} instances
 * for fields annotated with {@link CryptoHmac}.
 */
public final class CryptoHmacAnnotationFormatterFactory implements AnnotationFormatterFactory<CryptoHmac> {

	private static final Set<Class<?>> FIELD_TYPES = new HashSet<>(Arrays.asList(String.class, Number.class));

	private final CryptoHmacUtils cryptoHmacUtils;

	private final ObjectMapper objMapper;

	public CryptoHmacAnnotationFormatterFactory(CryptoHmacUtils cryptoHmacUtils, ObjectMapper objMapper) {
		this.cryptoHmacUtils = cryptoHmacUtils;
		this.objMapper = objMapper;
	}

	@Override
	public Set<Class<?>> getFieldTypes() {
		return FIELD_TYPES;
	}

	@Override
	public Printer<?> getPrinter(CryptoHmac annotation, Class<?> fieldType) {
		return new CryptoHmacFormatter<>(objMapper, fieldType, annotation, cryptoHmacUtils);
	}

	@Override
	public Parser<?> getParser(CryptoHmac annotation, Class<?> fieldType) {
		return new CryptoHmacFormatter<>(objMapper, fieldType, annotation, cryptoHmacUtils);
	}
}
