package com.bld.crypto.pubkey.config.data;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;

public class CipherPublicKeys {

	private Map<String,PublicKey> map;
	

	public CipherPublicKeys() {
		super();
		this.map=new HashMap<>();
	}


	public void addPublicKey(String name,final PublicKey publicKey) {
		this.map.put(name, publicKey);
	}
	
	public PublicKey getPublicKey(String name) {
		return this.map.get(name);
	}
	
	
	public boolean isEmpty() {
		return MapUtils.isEmpty(this.map);
	}
	
	
}
