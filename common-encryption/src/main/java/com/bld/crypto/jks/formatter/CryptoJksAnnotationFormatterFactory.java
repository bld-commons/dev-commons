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

public final class CryptoJksAnnotationFormatterFactory implements AnnotationFormatterFactory<CryptoJks> {

	private final static Set<Class<?>> FIELD_TYPES=new HashSet<>(Arrays.asList(String.class,Number.class));
	
	/** The crypto jks utils. */
	private CryptoJksUtils cryptoJksUtils;
	
	private ObjectMapper objMapper;
	

	
	
	public CryptoJksAnnotationFormatterFactory(CryptoJksUtils cryptoJksUtils, ObjectMapper objMapper) {
		super();
		this.cryptoJksUtils = cryptoJksUtils;
		this.objMapper = objMapper;
	}

	@Override
	public Set<Class<?>> getFieldTypes() {
		return FIELD_TYPES;
	}

	@Override
	public Printer<?> getPrinter(CryptoJks annotation, Class<?> fieldType) {
		return new CryptoJksFormatter<>(annotation, this.cryptoJksUtils, this.objMapper, fieldType);
	}

	@Override
	public Parser<?> getParser(CryptoJks annotation, Class<?> fieldType) {
		return new CryptoJksFormatter<>(annotation, this.cryptoJksUtils, this.objMapper, fieldType);
	}

	
}
