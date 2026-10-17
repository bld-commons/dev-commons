package com.bld.commons.example.model;

import java.util.List;

import com.bld.crypto.pkcs12.annotation.CryptoPkcs12;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Inbound twin of {@link EmployeeEncryptPkcs12}. Jackson decrypts the annotated fields on
 * the way in, so a request body produced by the Python service arrives as plain values.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EmployeeDecryptPkcs12 {

	@CryptoPkcs12("registry_table")
	private Registry registry;

	@CryptoPkcs12("profile_table")
	private List<Integer> idProfiles;

	@CryptoPkcs12("role_table")
	private Integer[] idAuthorities;

}
