package com.bld.commons.connection.client.impl;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.bld.commons.connection.client.ClientConnection;
import com.bld.commons.connection.constant.CommonRestConnectionCostant;
import com.bld.commons.connection.model.BasicRequest;
import com.bld.commons.connection.utils.RestConnectionMapper;

/**
 * The Class ClientConnectionImpl.
 */
@Component
public class ClientConnectionImpl implements ClientConnection {

	

	/** The rest template. */
	private RestTemplate restTemplate;

	/** The port proxy. */
	@Value("${com.dxc.connection.proxy.port:}")
	private Integer portProxy;

	/** The proxy. */
	@Value("${connection.proxy.proxy.ip:}")
	private String proxy;

	/** The log. */
	private final static Log logger = LogFactory.getLog(ClientConnectionImpl.class);

	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see it.csm.csmapp.rest.v1_0.component.
	 * ClientConnection#getRestTemplate(java. lang.String, java.util.Map,
	 * java.lang.Class)
	 */
	@Override
	public <T, K> T getRestTemplate(BasicRequest<K> basicRequest, Class<T> responseClass) throws Exception {
		String url = basicRequest.getUrl();
		logger.debug("Url: " + url);
		this.restTemplate = getRestTemplate();
		
		// ResponseEntity<T> obj = this.restTemplate..exchange(basicRequest.getUrl(),
		// basicRequest.getMethod(), request, responseClass);
		T obj = null;
		HttpEntity<?> request = null;
		RestBuilder restBuilder=null;
		switch (basicRequest.getMethod()) {
		case DELETE:
		case GET:
			restBuilder=urlBuilder(basicRequest);
			url=restBuilder.getUrl();
			request=restBuilder.getRequest();
			//obj=restTemplate.getForObject(builder.toUriString(), responseClass,basicRequest.getUriParams());
			break;
		case HEAD:
			break;
		case OPTIONS:
			break;
		case PATCH:
		case POST:
		case PUT:
			restBuilder=bodyBuilder(basicRequest);
			url=restBuilder.getUrl();
			request=restBuilder.getRequest();			
			break;
		case TRACE:
		
			break;
		default:
			break;

		}

		ResponseEntity<T> response = restTemplate.exchange(url, basicRequest.getMethod(), request, responseClass, basicRequest.getUriParams());
		obj=response.getBody();
		
		return obj;
	}

	/**
	 * Gets the rest template.
	 *
	 * @param verificaProxy the verifica proxy
	 * @return the rest template
	 */
	private RestTemplate getRestTemplate() {
		RestTemplate restTemplate = null;
		if (StringUtils.isNotEmpty(this.proxy) && this.portProxy != null) {
			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(this.proxy, this.portProxy));
			requestFactory.setProxy(proxy);
			restTemplate = new RestTemplate(requestFactory);
		} else {
			restTemplate = new RestTemplate();
			restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
		}
		return restTemplate;
	}
	
	
	private <K>RestBuilder bodyBuilder(BasicRequest<K> basicRequest) throws Exception{
		Object body=basicRequest.getBody();
		if(MediaType.APPLICATION_FORM_URLENCODED.equals(basicRequest.getHttpHeaders().getContentType())) {
			basicRequest.getHttpHeaders().add(CommonRestConnectionCostant.COMMON_REST_CONNECTION, "true");
			MapUtils.unmodifiableMap(basicRequest.getHttpHeaders());
			Map<String, Object> params=RestConnectionMapper.fromObjectToMap(basicRequest.getBody());
			body=RestConnectionMapper.mapToMultiValueMap(new LinkedMultiValueMap<>(),params,"");
		}
		HttpEntity<?> request = new HttpEntity<>(body, basicRequest.getHttpHeaders());
		return new RestBuilder(basicRequest.getUrl(), request);
	}
	
	
	private <K>RestBuilder urlBuilder(BasicRequest<K> basicRequest) throws Exception{
		basicRequest.getHttpHeaders().add(CommonRestConnectionCostant.COMMON_REST_CONNECTION, "true");
		MapUtils.unmodifiableMap(basicRequest.getHttpHeaders());
		HttpEntity<?> request = new HttpEntity<>(basicRequest.getHttpHeaders());
		Map<String, Object> params = RestConnectionMapper.fromObjectToMap(basicRequest.getBody());
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(basicRequest.getUrl());
		RestConnectionMapper.builderQuery(builder, params, "");
		String url=builder.toUriString();
		return new RestBuilder(url, request);
	}
	
	
	private class RestBuilder{
		
		private String url;
		
		private HttpEntity<?> request;
		
		

		private RestBuilder(String url, HttpEntity<?> request) {
			super();
			this.url = url;
			this.request = request;
		}

		private String getUrl() {
			return url;
		}

		
		private HttpEntity<?> getRequest() {
			return request;
		}

		
		
		
	}

}





