package com.bld.commons.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.bld.commons.utils.json.annotations.CleanExcessSpaces;
import com.bld.commons.utils.types.UpperLowerType;

@RestController
@RequestMapping("/clean")
public class CleanExcessSpacesController {

	@GetMapping(path = "/spaces", produces = "text/plain")
	@ResponseBody
	public String cleanSpaces(@RequestParam("text") @CleanExcessSpaces String text) {
		return text;
	}

	@GetMapping(path = "/upper", produces = "text/plain")
	@ResponseBody
	public String cleanUpper(@RequestParam("text") @CleanExcessSpaces(upperLowerType = UpperLowerType.UPPER) String text) {
		return text;
	}

}
