package com.bld.commons.example.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bld.crypto.jks.annotation.CryptoJks;

@FeignClient(name = "JKS-CLIENT",url = "http://localhost:8080")
public interface JksClient {

	
	@PostMapping(path="/jks/test-connection",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public void testConnection(@RequestParam("test") @CryptoJks String test);
}
