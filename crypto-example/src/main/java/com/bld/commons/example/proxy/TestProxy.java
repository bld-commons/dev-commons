package com.bld.commons.example.proxy;

import java.util.Map;

import com.bld.commons.example.model.Auth;

//@ApiController
public interface TestProxy {

	public Map<String,String> api(Auth auth);
}
