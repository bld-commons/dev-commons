package com.bldcommons.example.model;

import java.util.Date;

import com.bld.commons.utils.json.annotations.JsonDateTimeZone;

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
	
	@JsonDateTimeZone(format = "yyyy-MM-dd")
	private Date dtBirth;
	
	
}
