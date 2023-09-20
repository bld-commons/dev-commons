package com.bld.commons.example.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.bld.commons.example.client.JksClient;
import com.bld.commons.example.client.TestInterceptorClient;
import com.bld.commons.example.model.Auth;
import com.bld.commons.example.model.EmployeeInvertedJks;
import com.bld.commons.example.model.EmployeeJks;
import com.bld.commons.example.model.Registry;
import com.bld.crypto.jks.annotation.CryptoJks;

@RestController
@RequestMapping("/jks")
public class JksController {
	
	private final static Logger logger=LoggerFactory.getLogger(JksController.class);
	
	@Autowired
	private JksClient jksClient;
	
	@Autowired
	private TestInterceptorClient testInterceptorClient;

	@GetMapping(path="/encrypt",produces = "application/json")
	@ResponseBody
	public EmployeeJks encryptEmployee() {
		Registry registry=new Registry("Mario", "Rossi", new Date());
		List<Integer>idProfile=Arrays.asList(1,2,3);
		Integer[] idAuthorities=new Integer[] {1,2,3};
		EmployeeJks employee=new EmployeeJks(registry, idProfile, idAuthorities);
		return employee;
	}
	
	@GetMapping(path="/inverted/encrypt",produces = "application/json")
	@ResponseBody
	public EmployeeInvertedJks invertedEncryptEmployee() {
		Registry registry=new Registry("Mario", "Rossi", new Date());
		List<Integer>idProfile=Arrays.asList(1,2,3);
		Integer[] idAuthorities=new Integer[] {1,2,3};
		EmployeeInvertedJks employee=new EmployeeInvertedJks(registry, idProfile, idAuthorities);
		return employee;
	}
	
	@PostMapping(path="/decrypt",consumes = "application/json")
	public void decryptEmploy(@RequestBody EmployeeJks employee) {
		logger.info(employee.toString());
	}
	
	@PostMapping(path="/inverted/decrypt",consumes = "application/json")
	public void invertedDecryptEmploy(@RequestBody EmployeeInvertedJks employee) {
		logger.info(employee.toString());
	}
	
	@PostMapping(path="/auth-connection")
	public void authConnection() {
		logger.info("jks client");
		this.jksClient.testConnection("test");
		logger.info("test interceptor client");
		this.testInterceptorClient.testConnection("test");
	}
	
	@PostMapping(path="/test-connection",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public void testConnection(@RequestParam @CryptoJks String test) {
		logger.info("Connection Success: "+test);
	}
}
