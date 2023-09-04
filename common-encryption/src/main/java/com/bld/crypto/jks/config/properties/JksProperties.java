package com.bld.crypto.jks.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import io.jsonwebtoken.SignatureAlgorithm;

@Configuration
@ConfigurationProperties(prefix = "com.bld.crypto.jks")
public class JksProperties {

	
	private Resource file;
	
	private String password;

	private String alias;
	
	private String instanceJks;

	private SignatureAlgorithm signatureAlgorithm;

	public Resource getFile() {
		return file;
	}

	public void setFile(Resource file) {
		this.file = file;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getInstanceJks() {
		return instanceJks;
	}

	public void setInstanceJks(String instanceJks) {
		this.instanceJks = instanceJks;
	}

	public SignatureAlgorithm getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public void setSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
	}
	
	
	
}
