/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.hmac.config.data.HmacProperties.java
 */
package com.bld.crypto.hmac.config.data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the HMAC module.
 *
 * <p>Example {@code application.properties}:
 * <pre>
 * com.bld.crypto.hmac.secret=my-shared-secret-at-least-32-bytes
 * com.bld.crypto.hmac.salt=optional-salt          # enables PBKDF2 key derivation
 * com.bld.crypto.hmac.key-length=256              # bits, used with salt
 * com.bld.crypto.hmac.algorithm=HmacSHA256        # default
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "com.bld.crypto.hmac", ignoreUnknownFields = true)
public final class HmacProperties {

	/** The raw secret (or password when salt + keyLength are provided). */
	private String secret;

	/** Optional PBKDF2 salt. When set together with {@link #keyLength}, key derivation is used. */
	private String salt;

	/** PBKDF2 derived key length in bits (e.g. 256). Required when {@link #salt} is set. */
	private Integer keyLength;

	/** HMAC algorithm. Defaults to {@code HmacSHA256}. */
	private String algorithm = "HmacSHA256";

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public Integer getKeyLength() {
		return keyLength;
	}

	public void setKeyLength(Integer keyLength) {
		this.keyLength = keyLength;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
}
