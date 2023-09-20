package com.bld.commons.example.model;

import java.util.List;

import com.bld.crypto.aes.annotation.CryptoAes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EmployeeAes {

	@CryptoAes("test1")
	private Registry registry;
	
	@CryptoAes("test1")
	private List<Integer> idProfiles;
	
	@CryptoAes("test1")
	private Integer[] idAuthorities; 
	
}
