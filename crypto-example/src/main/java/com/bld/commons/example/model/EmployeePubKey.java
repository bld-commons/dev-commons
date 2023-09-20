package com.bld.commons.example.model;

import java.util.List;

import com.bld.crypto.pubkey.annotations.CryptoPubKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EmployeePubKey {

	@CryptoPubKey("test")
	private Registry registry;
	
	@CryptoPubKey("test")
	private List<Integer> idProfiles;
	
	@CryptoPubKey("test")
	private Integer[] idAuthorities; 
	
}
