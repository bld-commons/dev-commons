package com.bld.commons.example.model;

import java.util.Date;

import com.bld.commons.utils.json.annotations.DateTimeZone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Registry {

	private String firstName;
	
	private String lastName;
	
	@DateTimeZone(format = "yyyy-MM-dd")
	private Date dtBirth;
	
	
}
