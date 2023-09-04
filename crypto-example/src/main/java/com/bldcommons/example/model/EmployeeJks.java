package com.bldcommons.example.model;

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
public class EmployeeJks {

	@CryptoJks
	private Registry registry;
	
	@CryptoJks
	private List<Integer> idProfiles;
	
	@CryptoJks
	private Integer[] idAuthorities; 
	
}
