package com.bld.crypto.pubkey;

public class CryptoPubKeyData {

	private String name;
	
	private boolean url;

	public CryptoPubKeyData(String name, boolean url) {
		super();
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public boolean isUrl() {
		return url;
	}
	
	
	
}
