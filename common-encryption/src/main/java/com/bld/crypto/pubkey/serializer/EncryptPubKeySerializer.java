package com.bld.crypto.pubkey.serializer;

import org.springframework.beans.factory.annotation.Autowired;

import com.bld.crypto.pubkey.CryptoPubKeyData;
import com.bld.crypto.pubkey.CryptoPublicKeyUtils;
import com.bld.crypto.pubkey.annotations.CryptoPubKey;
import com.bld.crypto.pubkey.annotations.EncryptPubKey;
import com.bld.crypto.serializer.EncryptCertificateSerializer;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

@SuppressWarnings("serial")
@JacksonStdImpl
public class EncryptPubKeySerializer<T> extends EncryptCertificateSerializer<T> implements ContextualSerializer {

	private CryptoPubKeyData cryptoPubKey;

	@Autowired
	private CryptoPublicKeyUtils cryptoPublicKeyUtils;

	public EncryptPubKeySerializer() {
		this(null,null);
	}
	
	private EncryptPubKeySerializer(Class<T> t, CryptoPubKeyData cryptoPubKey) {
		super(t);
		this.cryptoPubKey = cryptoPubKey;
	}

	private EncryptPubKeySerializer(Class<T> t, CryptoPubKeyData cryptoPubKey,CryptoPublicKeyUtils cryptoPublicKeyUtils,ObjectMapper objMapper) {
		super(t,objMapper);
		this.cryptoPubKey = cryptoPubKey;
		this.cryptoPublicKeyUtils=cryptoPublicKeyUtils;
	}

	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
		CryptoPubKeyData cryptoPubKeyData=null;
		if(property.getAnnotation(CryptoPubKey.class)!=null) {
			CryptoPubKey cryptoPubKey=property.getAnnotation(CryptoPubKey.class);
			cryptoPubKeyData=new CryptoPubKeyData(cryptoPubKey.value(), cryptoPubKey.url());
		}else if(property.getAnnotation(EncryptPubKey.class)!=null) {
			EncryptPubKey encryptPubKey=property.getAnnotation(EncryptPubKey.class);
			cryptoPubKeyData=new CryptoPubKeyData(encryptPubKey.value(), encryptPubKey.url());
		}
		if (property.getType() != null && property.getType().getRawClass() != null)
			return new EncryptPubKeySerializer<>(property.getType().getRawClass(), cryptoPubKeyData,this.cryptoPublicKeyUtils,this.objMapper);
		else
			return this;
	}


	
	@Override
	protected String encryptValue(String word) {
		return this.cryptoPubKey.isUrl() ? this.cryptoPublicKeyUtils.encryptUri(word, this.cryptoPubKey.getName()) : this.cryptoPublicKeyUtils.encryptValue(word, this.cryptoPubKey.getName());
	}

}
