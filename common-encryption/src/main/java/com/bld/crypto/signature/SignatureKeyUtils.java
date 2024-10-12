package com.bld.crypto.signature;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.bld.crypto.key.JksKey;
import com.bld.crypto.signature.config.SignatureConfiguration;
import com.bld.crypto.signature.config.properties.SignatureKeyProperties;

@Component
public class SignatureKeyUtils {

	@Autowired
	@Qualifier(SignatureConfiguration.SIGNATURE_JKS_KEY)
	private JksKey jksKey;

	@Autowired
	private SignatureKeyProperties signatureKeyProperties;

	public static String sign(String text, PrivateKey privateKey,String instance) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature signature = Signature.getInstance(instance);
		signature.initSign(privateKey);
		signature.update(text.getBytes());
		return Base64.getEncoder().encodeToString(signature.sign());
	}

	public String sign(String text) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		return SignatureKeyUtils.sign(text, this.jksKey.getPrivateKey(),this.signatureKeyProperties.getInstanceSignature());
	}

	public static boolean verify(PublicKey publicKey, String signed, String checkText,String instance) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature signature = Signature.getInstance(instance);
		signature.initVerify(publicKey);

		signature.update(checkText.getBytes());

		return signature.verify(Base64.getDecoder().decode(signed));
	}

	public boolean verify(String signed, String checkText) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		return SignatureKeyUtils.verify(this.jksKey.getPublicKey(), signed, checkText,this.signatureKeyProperties.getInstanceSignature());
	}
}
