package com.bldcommons.example.model;

import java.util.List;

import com.bld.crypto.jks.annotation.CryptoJks;
import com.bld.crypto.type.CryptoType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EmployeeInvertedJks {

	@CryptoJks(decrypt = CryptoType.privateKey,encrypt = CryptoType.publicKey)
	private Registry registry;
	
	@CryptoJks(decrypt = CryptoType.privateKey,encrypt = CryptoType.publicKey)
	private List<Integer> idProfiles;
	
	@CryptoJks(decrypt = CryptoType.privateKey,encrypt = CryptoType.publicKey)
	private Integer[] idAuthorities; 
	
}
