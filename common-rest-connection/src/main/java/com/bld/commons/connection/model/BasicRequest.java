package com.bld.commons.connection.model;

import javax.validation.constraints.NotNull;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public class BasicRequest<T> {

	private T body;

	@NotNull
	private String url;

	@NotNull
	private HttpMethod method;

	private HttpHeaders httpHeaders;

	private Object[] uriParams;
	

	public BasicRequest(MediaType mediaType) {
		super();
		this.httpHeaders = new HttpHeaders();
		this.httpHeaders.setContentType(mediaType);
		this.uriParams = new Object[] {};
	}
	
	

	public BasicRequest(@NotNull String url, @NotNull HttpMethod method) {
		super();
		this.url = url;
		this.method = method;
		this.httpHeaders = new HttpHeaders();
		this.uriParams = new Object[] {};
	}



	public BasicRequest(String url, HttpMethod method, MediaType mediaType) {
		super();
		this.httpHeaders = new HttpHeaders();
		this.httpHeaders.setContentType(mediaType);
		this.url = url;
		this.method = method;
		this.uriParams = new Object[] {};
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public HttpHeaders getHttpHeaders() {
		return httpHeaders;
	}

	public T getBody() {
		return body;
	}

	public void setBody(T object) {
		this.body = object;
	}

	public Object[] getUriParams() {
		return uriParams;
	}

	public void setUriParams(Object... uriParams) {
		this.uriParams = uriParams;
	}

}
