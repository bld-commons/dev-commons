/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pkcs12.CryptoPkcs12Utils.java
 */
package com.bld.crypto.pkcs12;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.NamedParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bld.crypto.exception.CryptoException;
import com.bld.crypto.key.JksKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Utility class that provides hybrid asymmetric/symmetric encryption and decryption
 * using X25519 (XDH) Elliptic-Curve Diffie-Hellman key agreement combined with
 * AES-256-GCM authenticated encryption.
 *
 * <p>Encryption scheme per operation:
 * <ol>
 *   <li>Generate a fresh ephemeral X25519 key pair.</li>
 *   <li>Perform ECDH between the ephemeral private key and the recipient's static
 *       X25519 public key to obtain a shared secret.</li>
 *   <li>Derive a 256-bit AES key via HKDF-SHA256 using a random 12-byte GCM nonce
 *       as the salt.</li>
 *   <li>Encrypt the payload with AES-256-GCM (128-bit authentication tag).</li>
 *   <li>Output the three components as dot-separated Base64 strings:
 *       {@code ephemeralPublicKey.gcmNonce.ciphertext+tag}.</li>
 * </ol>
 *
 * <p>The PKCS12 keystore must contain an <em>XDH / X25519</em> key pair.
 * It can be created with OpenSSL:
 * <pre>
 * openssl genpkey -algorithm X25519 -out private.key
 * openssl pkcs12 -export -nocerts \
 *     -inkey private.key \
 *     -out mase-x25519.p12 \
 *     -name "mase-x25519"
 * </pre>
 * When no certificate is present the public key is automatically derived from
 * the private key by {@link com.bld.crypto.pkcs12.config.CryptoPkcs12Configuration}.
 */
public final class CryptoPkcs12Utils {

	private static final Logger logger = LoggerFactory.getLogger(CryptoPkcs12Utils.class);

	/** GCM authentication tag length in bits. */
	private static final int GCM_TAG_LENGTH_BITS = 128;

	/** GCM nonce length in bytes. */
	private static final int GCM_IV_LENGTH_BYTES = 12;

	/** AES key length in bytes (AES-256). */
	private static final int AES_KEY_LENGTH_BYTES = 32;

	/** Separator between the three Base64-encoded output components. */
	private static final String DELIMITER = ".";

	/** HKDF context / info label. */
	private static final byte[] HKDF_INFO = "CryptoPkcs12".getBytes(StandardCharsets.UTF_8);

	/** JCA algorithm name for X25519 key pair generation and key agreement. */
	private static final String XDH_ALGORITHM = "XDH";

	/** JCA transformation for AES-GCM. */
	private static final String AES_GCM_CIPHER = "AES/GCM/NoPadding";

	/** The X25519 key pair loaded from the PKCS12 keystore. */
	private final JksKey jksKey;

	/** Jackson {@link ObjectMapper} injected by Spring for JSON serialization/deserialization. */
	@Autowired
	private ObjectMapper objMapper;


	/**
	 * Constructs a new {@code CryptoPkcs12Utils} wrapping the given key pair.
	 *
	 * @param jksKey the X25519 key pair loaded from a PKCS12 keystore
	 */
	public CryptoPkcs12Utils(JksKey jksKey) {
		super();
		this.jksKey = jksKey;
	}


	/**
	 * Encrypts a plain-text string using the recipient's X25519 public key.
	 *
	 * <p>Output format:
	 * {@code Base64(ephemeralPublicKey).Base64(gcmNonce).Base64(ciphertext+authTag)}
	 *
	 * @param value the plain-text value to encrypt; returns {@code null} if blank
	 * @return the encrypted, dot-delimited Base64 string
	 * @throws CryptoException if any cryptographic error occurs
	 */
	public String encryptValue(String value) {
		if (StringUtils.isBlank(value))
			return null;
		try {
			// 1. Generate ephemeral X25519 key pair
			KeyPairGenerator kpg = KeyPairGenerator.getInstance(XDH_ALGORITHM);
			kpg.initialize(NamedParameterSpec.X25519);
			java.security.KeyPair ephemeral = kpg.generateKeyPair();

			// 2. ECDH: ephemeral private key + recipient static X25519 public key
			KeyAgreement ka = KeyAgreement.getInstance(XDH_ALGORITHM);
			ka.init(ephemeral.getPrivate());
			ka.doPhase(jksKey.getPublicKey(), true);
			byte[] sharedSecret = ka.generateSecret();

			// 3. Random GCM nonce (12 bytes)
			byte[] gcmIV = new byte[GCM_IV_LENGTH_BYTES];
			new SecureRandom().nextBytes(gcmIV);

			// 4. HKDF-SHA256: derive 256-bit AES key using the nonce as salt
			byte[] aesKey = hkdf(sharedSecret, gcmIV, AES_KEY_LENGTH_BYTES);

			// 5. AES-256-GCM encrypt (ciphertext includes 128-bit auth tag)
			Cipher cipher = Cipher.getInstance(AES_GCM_CIPHER);
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, gcmIV));
			byte[] ciphertext = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

			// 6. Assemble: ephemeralPub.nonce.ciphertext
			String ephPubB64 = Base64.getEncoder().encodeToString(ephemeral.getPublic().getEncoded());
			String ivB64     = Base64.getEncoder().encodeToString(gcmIV);
			String ctB64     = Base64.getEncoder().encodeToString(ciphertext);
			return ephPubB64 + DELIMITER + ivB64 + DELIMITER + ctB64;
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new CryptoException(e);
		}
	}


	/**
	 * Decrypts a previously encrypted value using the recipient's X25519 private key.
	 *
	 * @param value the dot-delimited Base64 encrypted string produced by
	 *              {@link #encryptValue(String)}; returns {@code null} if blank
	 * @return the plain-text string
	 * @throws CryptoException if decryption fails (wrong key, tampered ciphertext, bad format)
	 */
	public String decryptValue(String value) {
		if (StringUtils.isBlank(value))
			return null;
		try {
			String[] parts = value.split("\\.");
			if (parts.length != 3)
				throw new CryptoException("Invalid PKCS12 encrypted value: expected 3 dot-separated Base64 components");

			byte[] ephPubDER  = Base64.getDecoder().decode(parts[0]);
			byte[] gcmIV      = Base64.getDecoder().decode(parts[1]);
			byte[] ciphertext = Base64.getDecoder().decode(parts[2]);

			// Reconstruct ephemeral X25519 public key from its DER encoding
			KeyFactory kf = KeyFactory.getInstance(XDH_ALGORITHM);
			PublicKey ephPub = kf.generatePublic(new X509EncodedKeySpec(ephPubDER));

			// ECDH: recipient static X25519 private key + ephemeral public key
			KeyAgreement ka = KeyAgreement.getInstance(XDH_ALGORITHM);
			ka.init(jksKey.getPrivateKey());
			ka.doPhase(ephPub, true);
			byte[] sharedSecret = ka.generateSecret();

			// HKDF-SHA256 (same parameters as encryption)
			byte[] aesKey = hkdf(sharedSecret, gcmIV, AES_KEY_LENGTH_BYTES);

			// AES-256-GCM decrypt (auth tag verification is implicit)
			Cipher cipher = Cipher.getInstance(AES_GCM_CIPHER);
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, gcmIV));
			return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
		} catch (CryptoException e) {
			throw e;
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new CryptoException(e);
		}
	}


	/**
	 * Serialises {@code value} to JSON and encrypts it.
	 *
	 * @param value the object to encrypt
	 * @return the encrypted string
	 * @throws JsonProcessingException if JSON serialisation fails
	 */
	public String encryptObject(Object value) throws JsonProcessingException {
		return encryptValue(objMapper.writeValueAsString(value));
	}


	/**
	 * Decrypts an encrypted string and deserialises the JSON result into the target type.
	 *
	 * @param <T>      the target type
	 * @param value    the encrypted string
	 * @param response the target class
	 * @return the deserialised object
	 * @throws JsonProcessingException if JSON deserialisation fails
	 */
	public <T> T decryptObject(String value, Class<T> response) throws JsonProcessingException {
		return objMapper.readValue(decryptValue(value), response);
	}


	/**
	 * Encrypts a value and applies an additional Base64 layer, making the result safe
	 * for embedding in URL path segments or query parameters.
	 *
	 * @param value the plain-text value to encrypt
	 * @return the URL-safe double-encoded encrypted string, or {@code null} if blank
	 */
	public String encryptUri(String value) {
		String encrypted = encryptValue(value);
		if (StringUtils.isEmpty(encrypted))
			return null;
		return Base64.getEncoder().encodeToString(encrypted.getBytes(StandardCharsets.UTF_8));
	}


	/**
	 * Removes the outer URL-safe Base64 layer and decrypts the inner cipher text.
	 *
	 * @param value the URL-safe double-encoded encrypted string
	 * @return the plain-text string, or {@code null} if blank
	 */
	public String decryptUri(String value) {
		if (StringUtils.isBlank(value))
			return null;
		String decoded = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
		return decryptValue(decoded);
	}


	/**
	 * Serialises {@code value} to JSON, encrypts it, and URL-encodes the result.
	 *
	 * @param value the object to encrypt
	 * @return the URL-safe encrypted string
	 * @throws JsonProcessingException if JSON serialisation fails
	 */
	public String encryptObjectUri(Object value) throws JsonProcessingException {
		return encryptUri(objMapper.writeValueAsString(value));
	}


	/**
	 * URL-decodes, decrypts, and deserialises an encrypted object string.
	 *
	 * @param <T>      the target type
	 * @param value    the URL-safe encrypted string
	 * @param response the target class
	 * @return the deserialised object
	 * @throws JsonMappingException    if JSON mapping fails
	 * @throws JsonProcessingException if JSON deserialisation fails
	 */
	public <T> T decryptObjectUri(String value, Class<T> response) throws JsonMappingException, JsonProcessingException {
		return objMapper.readValue(decryptUri(value), response);
	}


	/**
	 * Returns the X25519 public key from the loaded key pair.
	 *
	 * @return the recipient's {@link PublicKey}
	 */
	public PublicKey getPublicKey() {
		return jksKey.getPublicKey();
	}


	/**
	 * Returns the Base64-encoded X.509 DER representation of the X25519 public key.
	 *
	 * @return the Base64-encoded public key string
	 */
	public String publicKey() {
		return jksKey.publicKey();
	}


	/**
	 * Derives a key of the requested length using HKDF-SHA256.
	 *
	 * @param inputKeyMaterial the shared secret produced by ECDH
	 * @param salt             the GCM nonce, used as the HKDF salt
	 * @param outputLength     the desired output length in bytes
	 * @return the derived key bytes
	 */
	private static byte[] hkdf(byte[] inputKeyMaterial, byte[] salt, int outputLength) {
		HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
		hkdf.init(new HKDFParameters(inputKeyMaterial, salt, HKDF_INFO));
		byte[] output = new byte[outputLength];
		hkdf.generateBytes(output, 0, outputLength);
		return output;
	}

}
