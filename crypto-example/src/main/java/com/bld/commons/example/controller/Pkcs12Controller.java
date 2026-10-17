package com.bld.commons.example.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.bld.commons.example.client.Pkcs12Client;
import com.bld.commons.example.model.EmployeeDecryptPkcs12;
import com.bld.commons.example.model.EmployeeEncryptPkcs12;
import com.bld.commons.example.model.Registry;

/**
 * The Java half of the cross-language exchange with {@code crypto-example-py}.
 *
 * <p>Both services expose the same four routes, so neither has to be the caller: the
 * encrypted side is whichever one the request started from.
 */
@RestController
@RequestMapping("/pkcs12")
public class Pkcs12Controller {

	@Autowired
	private Pkcs12Client peer;

	@GetMapping(path = "/encrypt", produces = "application/json")
	@ResponseBody
	public EmployeeEncryptPkcs12 encryptEmployee() {
		return sample();
	}

	@PostMapping(path = "/decrypt", consumes = "application/json", produces = "application/json")
	public Map<String, Object> decryptEmploy(@RequestBody EmployeeDecryptPkcs12 employee) {
		return plain(employee.getRegistry(), employee.getIdProfiles(), employee.getIdAuthorities());
	}

	@PostMapping(path = "/send", produces = "application/json")
	public String sendToPeer() {
		return this.peer.decrypt(sample());
	}

	@GetMapping(path = "/peer-encrypt", produces = "application/json")
	public Map<String, Object> peerEncrypt() {
		EmployeeDecryptPkcs12 employee = this.peer.encrypt();
		return plain(employee.getRegistry(), employee.getIdProfiles(), employee.getIdAuthorities());
	}

	/** The payload {@code JksController} builds, rebuilt for the PKCS12 annotation. */
	private EmployeeEncryptPkcs12 sample() {
		return new EmployeeEncryptPkcs12(new Registry("Mario", "Rossi", new Date()), Arrays.asList(1, 2, 3),
				new Integer[] { 1, 2, 3 });
	}

	/** Plain, unannotated view: a returned model would be encrypted again on the way out. */
	private Map<String, Object> plain(Registry registry, List<Integer> profiles, Integer[] authorities) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("registry", registry);
		body.put("idProfiles", profiles);
		body.put("idAuthorities", authorities);
		return body;
	}
}
