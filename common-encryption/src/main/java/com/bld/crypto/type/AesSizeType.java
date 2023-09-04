package com.bld.crypto.type;

public enum AesSizeType {

	sz16(16),
	sz24(24),
	sz32(32);
	
	private int size;

	private AesSizeType(int size) {
		this.size=size;
	}
	
	public int size() {
		return this.size;
	}
}
