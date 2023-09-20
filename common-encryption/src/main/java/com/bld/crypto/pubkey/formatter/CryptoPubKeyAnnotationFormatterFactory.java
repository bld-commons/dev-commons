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

public final class CryptoPubKeyAnnotationFormatterFactory implements AnnotationFormatterFactory<CryptoPubKey> {

	private final static Set<Class<?>> FIELD_TYPES=new HashSet<>(Arrays.asList(String.class,Number.class));
	
	private CryptoPublicKeyUtils cryptoPubKeyUtils;
	
	private ObjectMapper objMapper;
	

	
	
	public CryptoPubKeyAnnotationFormatterFactory(CryptoPublicKeyUtils cryptoPubKeyUtils, ObjectMapper objMapper) {
		super();
		this.cryptoPubKeyUtils = cryptoPubKeyUtils;
		this.objMapper = objMapper;
	}

	@Override
	public Set<Class<?>> getFieldTypes() {
		return FIELD_TYPES;
	}

	@Override
	public Printer<?> getPrinter(CryptoPubKey annotation, Class<?> fieldType) {
		return new CryptoPubKeyFormatter<>(objMapper, fieldType, annotation, cryptoPubKeyUtils);
	}

	@Override
	public Parser<?> getParser(CryptoPubKey annotation, Class<?> fieldType) {
		return new CryptoPubKeyFormatter<>(objMapper, fieldType, annotation, cryptoPubKeyUtils);
	}

	
}
