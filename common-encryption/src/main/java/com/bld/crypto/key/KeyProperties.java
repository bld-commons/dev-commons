package com.bld.crypto.key;

import org.springframework.core.io.Resource;

public abstract class KeyProperties {

	/** The file. */
	private Resource file;
	/** The password. */
	private String password;
	/** The alias. */
	private String alias;
	/** The instance jks. */
	private String instanceJks;

	public KeyProperties() {
		super();
	}

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

	
}