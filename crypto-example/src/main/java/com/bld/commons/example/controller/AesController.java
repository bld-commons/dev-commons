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

import com.bld.commons.example.model.EmployeeAes;
import com.bld.commons.example.model.EmployeePubKey;
import com.bld.commons.example.model.Registry;

@RestController
@RequestMapping("/aes")
public class AesController {
	
	private final static Logger logger=LoggerFactory.getLogger(AesController.class);

	@GetMapping(path="/encrypt",produces = "application/json")
	@ResponseBody
	public EmployeeAes encryptEmployee() {
		Registry registry=new Registry("Mario", "Rossi", new Date());
		List<Integer>idProfile=Arrays.asList(1,2,3);
		Integer[] idAuthorities=new Integer[] {1,2,3};
		EmployeeAes employee=new EmployeeAes(registry, idProfile, idAuthorities);
		return employee;
	}
	
	
	@PostMapping(path="/decrypt",consumes = "application/json")
	public void decryptEmploy(@RequestBody EmployeeAes employee) {
		logger.info(employee.toString());
	}
}
