/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.model.MapRequest.java
 */
package com.bld.commons.connection.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * The Class MapRequest.
 */
public class MapRequest extends BasicRequest<Map<String,Object>> {

	
	/**
	 * Instantiates a new map request.
	 *
	 * @param url the url
	 * @param method the method
	 */
	private MapRequest(String url, HttpMethod method) {
		super(url, method);
		super.data=new HashMap<>();
	}
	
	

	/**
	 * Instantiates a new map request.
	 *
	 * @param url the url
	 * @param method the method
	 * @param mediaType the media type
	 */
	private MapRequest(String url, HttpMethod method, MediaType mediaType) {
		super(url, method, mediaType);
		super.data=new HashMap<>();
	}



	/**
	 * New instance get.
	 *
	 * @param url the url
	 * @return the map request
	 */
	public static MapRequest newInstanceGet(String url) {
		return new MapRequest(url, HttpMethod.GET);
	}
	
	/**
	 * New instance delete.
	 *
	 * @param url the url
	 * @return the map request
	 */
	public static MapRequest newInstanceDelete(String url) {
		return new MapRequest(url, HttpMethod.DELETE);
	}
	
	/**
	 * New instance post.
	 *
	 * @param url the url
	 * @return the map request
	 */
	public static MapRequest newInstancePost(String url) {
		return new MapRequest(url, HttpMethod.POST);
	}
	
	/**
	 * New instance put.
	 *
	 * @param url the url
	 * @return the map request
	 */
	public static MapRequest newInstancePut(String url) {
		return new MapRequest(url, HttpMethod.PUT);
	}
	
	/**
	 * New instance patch.
	 *
	 * @param url the url
	 * @return the map request
	 */
	public static MapRequest newInstancePatch(String url) {
		return new MapRequest(url, HttpMethod.PATCH);
	}
	
	/**
	 * Adds the data.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void addData(String key,Object value) {
		super.data.put(key, value);
	}

	/**
	 * Clear data.
	 */
	public void clearData() {
		super.data=new HashMap<>();
	}
	
	/**
	 * Removes the data.
	 *
	 * @param props the props
	 */
	public void removeData(String props) {
		super.data.remove(props);
	}

}
