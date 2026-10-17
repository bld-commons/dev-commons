package com.bld.commons.example.model;

import java.util.List;

import com.bld.crypto.pkcs12.annotation.CryptoPkcs12;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Outbound twin of {@link EmployeeDecryptPkcs12}: the same three fields, so the payload
 * produced here is the one the Python service reads back with {@code from_json}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EmployeeEncryptPkcs12 {

	@CryptoPkcs12("registry_table")
	private Registry registry;

	@CryptoPkcs12("profile_table")
	private List<Integer> idProfiles;

	@CryptoPkcs12("role_table")
	private Integer[] idAuthorities;

}
