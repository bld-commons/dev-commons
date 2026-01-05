package com.bld.commons.example.model;

import java.util.List;

import com.bld.crypto.jks.annotation.CryptoJks;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EmployeeDecryptJks {

	@CryptoJks("registry_table")
	private Registry registry;
	
	@CryptoJks("profile_table")
	private List<Integer> idProfiles;
	
	@CryptoJks("role_table")
	private Integer[] idAuthorities; 
	
}
