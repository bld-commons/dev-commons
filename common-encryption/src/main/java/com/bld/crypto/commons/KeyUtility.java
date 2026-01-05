package com.bld.crypto.commons;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;

import com.bld.crypto.exception.CryptoException;
import com.bld.crypto.type.InstanceType;

public class KeyUtility {

	public static PublicKey getPublicKey(String publicKey,InstanceType instanceType) {
		try {
			if (StringUtils.isNotBlank(publicKey)) {
				byte[] keyBytes = Base64.getDecoder().decode(publicKey);
				X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
				KeyFactory keyFactory = KeyFactory.getInstance(instanceType.name());
				return keyFactory.generatePublic(spec);
			}
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new CryptoException(e);
		}
		return null;
	}
	
}
