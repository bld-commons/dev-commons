package com.bld.commons.example.model;

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
public class EmployeeInvertedDecryptJks {

	@CryptoJks(decrypt = CryptoType.publicKey,encrypt = CryptoType.privateKey,value="registry_table")
	private Registry registry;
	
	@CryptoJks(decrypt = CryptoType.publicKey,encrypt = CryptoType.privateKey,value="profile_table")
	private List<Integer> idProfiles;
	
	@CryptoJks(decrypt = CryptoType.publicKey,encrypt = CryptoType.privateKey,value="role_table")
	private Integer[] idAuthorities; 
	
}
