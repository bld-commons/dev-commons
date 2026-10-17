package com.bld.commons.example.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.bld.commons.example.model.EmployeeDecryptPkcs12;
import com.bld.commons.example.model.EmployeeEncryptPkcs12;

/**
 * Calls the Python counterpart. The URL is a property so the two services can be started
 * in either order without editing code.
 *
 * <p>Feign serialises the request body through Jackson, so the {@code @CryptoPkcs12}
 * fields of {@link EmployeeEncryptPkcs12} go out as tokens and are decrypted on the other
 * side. The response comes back as {@link EmployeeDecryptPkcs12}, so Jackson decrypts it on
 * the way in for the same reason: the peer emits one token per field, which is the
 * inbound shape, not a single whole-object token for {@code decryptObject}.
 */
@FeignClient(name = "PKCS12-CLIENT", url = "${crypto-example.peer-url}")
public interface Pkcs12Client {

	@PostMapping(path = "/pkcs12/decrypt", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	String decrypt(@RequestBody EmployeeEncryptPkcs12 employee);

	@GetMapping(path = "/pkcs12/encrypt", produces = MediaType.APPLICATION_JSON_VALUE)
	EmployeeDecryptPkcs12 encrypt();
}
