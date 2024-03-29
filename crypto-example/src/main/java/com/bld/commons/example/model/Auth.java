package com.bld.commons.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//@Data
////@ToString
//@AllArgsConstructor
//@NoArgsConstructor
public class Auth {

	private String username;
	
	private String password;
	
	

	public Auth(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	
	
	public Auth() {
		super();
	}



	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
