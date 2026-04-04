/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.model.RestBasicRequest.java
 */
package com.bld.commons.connection.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Base class for REST requests. Extends {@link BasicRequest} with URI template
 * parameters, Content-Type and Accept management — features that are not
 * applicable to SOAP calls.
 *
 * @param <T> the type of the request body
 */
public abstract class RestBasicRequest<T> extends BasicRequest<T> {

	/** The uri params. */
	private List<Object> uriParams;

	/**
	 * Instantiates a new rest basic request.
	 *
	 * @param url    the url
	 * @param method the method
	 */
	protected RestBasicRequest(String url, HttpMethod method) {
		super(url, method);
		this.uriParams = new ArrayList<>();
	}

	/**
	 * Instantiates a new rest basic request.
	 *
	 * @param url       the url
	 * @param method    the method
	 * @param mediaType the media type
	 */
	protected RestBasicRequest(String url, HttpMethod method, MediaType mediaType) {
		super(url, method);
		this.uriParams = new ArrayList<>();
		this.setContentType(mediaType);
	}

	/**
	 * Gets the uri params.
	 *
	 * @return the uri params
	 */
	public Object[] getUriParams() {
		return uriParams.toArray();
	}

	/**
	 * Adds the uri params.
	 *
	 * @param uriParams the uri params
	 */
	public void addUriParams(Object... uriParams) {
		if (ArrayUtils.isNotEmpty(uriParams))
			this.uriParams.addAll(Arrays.asList(uriParams));
	}

	/**
	 * Clear uri params.
	 */
	public void clearUriParams() {
		this.uriParams.clear();
	}

	/**
	 * Sets the content type.
	 *
	 * @param mediaType the new content type
	 */
	public void setContentType(MediaType mediaType) {
		this.getHttpHeaders().setContentType(mediaType);
	}

	public void setContentType(String mediaType) {
		if (StringUtils.isNotBlank(mediaType))
			this.getHttpHeaders().setContentType(MediaType.parseMediaType(mediaType));
	}

	/**
	 * Sets the accept.
	 *
	 * @param mediaTypes the accepted media types
	 */
	public void setAccept(MediaType... mediaTypes) {
		if (ArrayUtils.isNotEmpty(mediaTypes))
			this.getHttpHeaders().setAccept(Arrays.asList(mediaTypes));
	}

	public void setAccept(String... mediaTypes) {
		if (ArrayUtils.isNotEmpty(mediaTypes))
			this.getHttpHeaders().setAccept(MediaType.parseMediaTypes(Arrays.asList(mediaTypes)));
	}

}
