/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.model.MapRequest.java
 */
package com.bld.commons.connection.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * REST request whose body is a {@code Map<String,Object>}.
 * Implements {@link BasicMapRequest} for fluent field management.
 */
public class MapRequest extends RestBasicRequest<Map<String,Object>> implements BasicMapRequest {

	/** HTTP methods that carry a request body: their data goes in the body, not in the query string. */
	private static final List<HttpMethod> BODY_METHODS = List.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH);

	/** Query parameters, used only for methods that also carry a body (POST/PUT/PATCH). */
	private Map<String, Object> queryParams;

	/**
	 * Instantiates a new map request.
	 *
	 * @param url    the url
	 * @param method the method
	 */
	private MapRequest(String url, HttpMethod method) {
		super(url, method);
		super.data = new HashMap<>();
		this.queryParams = new HashMap<>();
	}

	/**
	 * Instantiates a new map request.
	 *
	 * @param url       the url
	 * @param method    the method
	 * @param mediaType the media type
	 */
	private MapRequest(String url, HttpMethod method, MediaType mediaType) {
		super(url, method, mediaType);
		super.data = new HashMap<>();
		this.queryParams = new HashMap<>();
	}
	
	public static MapRequest newInstance(String url, HttpMethod method) {
		return new MapRequest(url, method);
	}

	/**
	 * Creates a new {@link MapRequest} for an HTTP GET call.
	 * Query parameters should be added via {@link #addData(String, Object)};
	 * they are appended as URL query string entries at dispatch time.
	 *
	 * @param url the target URL
	 * @return a new MapRequest configured for GET
	 */
	public static MapRequest newInstanceGet(String url) {
		return new MapRequest(url, HttpMethod.GET);
	}

	/**
	 * Creates a new {@link MapRequest} for an HTTP DELETE call.
	 * Parameters added via {@link #addData(String, Object)} are sent as query string entries.
	 *
	 * @param url the target URL
	 * @return a new MapRequest configured for DELETE
	 */
	public static MapRequest newInstanceDelete(String url) {
		return new MapRequest(url, HttpMethod.DELETE);
	}

	/**
	 * Creates a new {@link MapRequest} for an HTTP POST call.
	 * Parameters added via {@link #addData(String, Object)} are sent as the request body
	 * encoded as a {@link org.springframework.util.MultiValueMap}.
	 *
	 * @param url the target URL
	 * @return a new MapRequest configured for POST
	 */
	public static MapRequest newInstancePost(String url) {
		return new MapRequest(url, HttpMethod.POST);
	}

	/**
	 * Creates a new {@link MapRequest} for an HTTP PUT call.
	 * Parameters added via {@link #addData(String, Object)} are sent as the request body
	 * encoded as a {@link org.springframework.util.MultiValueMap}.
	 *
	 * @param url the target URL
	 * @return a new MapRequest configured for PUT
	 */
	public static MapRequest newInstancePut(String url) {
		return new MapRequest(url, HttpMethod.PUT);
	}

	/**
	 * Creates a new {@link MapRequest} for an HTTP PATCH call.
	 * Parameters added via {@link #addData(String, Object)} are sent as the request body
	 * encoded as a {@link org.springframework.util.MultiValueMap}.
	 *
	 * @param url the target URL
	 * @return a new MapRequest configured for PATCH
	 */
	public static MapRequest newInstancePatch(String url) {
		return new MapRequest(url, HttpMethod.PATCH);
	}

	/**
	 * Adds the data.
	 *
	 * @param key   the field name
	 * @param value the field value
	 */
	@Override
	public void addData(String key, Object value) {
		super.data.put(key, value);
	}

	/**
	 * Clear data.
	 */
	@Override
	public void clearData() {
		super.data = new HashMap<>();
	}

	/**
	 * Removes the data.
	 *
	 * @param key the field name to remove
	 */
	@Override
	public void removeData(String key) {
		super.data.remove(key);
	}

	/**
	 * Adds a query parameter, routing it according to the HTTP method:
	 * <ul>
	 *   <li>methods that carry a body (POST/PUT/PATCH): the parameter is kept in the dedicated
	 *       query string, rebuilt on the URL at dispatch time;</li>
	 *   <li>methods without a body (GET/DELETE, ...): the parameter is added directly to the main
	 *       data map, since for these the data map already becomes the query string.</li>
	 * </ul>
	 * <p>Throws when the {@code Content-Type} is not configured: without it we cannot tell how the
	 * body would be encoded and therefore cannot route the parameter with confidence.
	 *
	 * @param key   the query parameter name
	 * @param value the query parameter value
	 */
	public void addQueryParam(String key, Object value) {
		if (getHttpHeaders().getContentType() == null) {
			throw new IllegalStateException(
					"Content-Type non configurato: impossibile instradare il query param '" + key
							+ "' con sicurezza. Impostare il Content-Type sulla request prima di chiamare addQueryParam.");
		}
		if (BODY_METHODS.contains(getMethod()))
			this.queryParams.put(key, value);
		else
			addData(key, value);
	}

	/**
	 * Gets the query parameters (only populated for methods that carry a body).
	 *
	 * @return the query parameters map
	 */
	public Map<String, Object> getQueryParams() {
		return queryParams;
	}

}
