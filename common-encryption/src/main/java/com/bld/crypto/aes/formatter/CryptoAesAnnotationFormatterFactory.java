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

public final class CryptoAesAnnotationFormatterFactory implements AnnotationFormatterFactory<CryptoAes> {

	private final static Set<Class<?>> FIELD_TYPES=new HashSet<>(Arrays.asList(String.class,Number.class));
	
	private CryptoAesUtils cryptoAesUtils;
	
	private ObjectMapper objMapper;
	

	
	
	public CryptoAesAnnotationFormatterFactory(CryptoAesUtils cryptoAesUtils, ObjectMapper objMapper) {
		super();
		this.cryptoAesUtils = cryptoAesUtils;
		this.objMapper = objMapper;
	}

	@Override
	public Set<Class<?>> getFieldTypes() {
		return FIELD_TYPES;
	}

	@Override
	public Printer<?> getPrinter(CryptoAes annotation, Class<?> fieldType) {
		return new CryptoAesFormatter<>(objMapper, fieldType, annotation, cryptoAesUtils);
	}

	@Override
	public Parser<?> getParser(CryptoAes annotation, Class<?> fieldType) {
		return new CryptoAesFormatter<>(objMapper, fieldType, annotation, cryptoAesUtils);
	}

	
}
