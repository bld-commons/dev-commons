package com.bld.commons.example.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.bld.commons.example.model.EmployeePubKey;
import com.bld.commons.example.model.Registry;

@RestController
@RequestMapping("/pub-key")
public class PubKeyController {
	
	private final static Logger logger=LoggerFactory.getLogger(PubKeyController.class);

	@GetMapping(path="/encrypt",produces = "application/json")
	@ResponseBody
	public EmployeePubKey encryptEmployee() {
		Registry registry=new Registry("Mario", "Rossi", new Date());
		List<Integer>idProfile=Arrays.asList(1,2,3);
		Integer[] idAuthorities=new Integer[] {1,2,3};
		EmployeePubKey employee=new EmployeePubKey(registry, idProfile, idAuthorities);
		return employee;
	}
	
	
	@PostMapping(path="/decrypt",consumes = "application/json")
	public void decryptEmploy(@RequestBody EmployeePubKey employee) {
		logger.info(employee.toString());
	}
}
