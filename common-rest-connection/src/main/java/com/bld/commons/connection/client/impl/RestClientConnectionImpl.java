package com.bld.commons.connection.client.impl;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.bld.commons.connection.client.RestClientConnection;
import com.bld.commons.connection.constant.CommonRestConnectionCostant;
import com.bld.commons.connection.model.BasicRequest;
import com.bld.commons.connection.utils.RestConnectionMapper;

/**
 * The Class ClientConnectionImpl.
 */
@Component
public class RestClientConnectionImpl implements RestClientConnection {

	

	/** The rest template. */
	@Autowired
	private RestTemplate restTemplate;

	/** The log. */
	private final static Log logger = LogFactory.getLog(RestClientConnectionImpl.class);

	
	
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
		this.setTimeout(basicRequest);
		ResponseEntity<T> response = this.restTemplate.exchange(url, basicRequest.getMethod(), request, responseClass, basicRequest.getUriParams());
		obj=response.getBody();
		
		return obj;
	}

	/**
	 * Gets the rest template.
	 *
	 * @param verificaProxy the verifica proxy
	 * @return the rest template
	 */

	
	
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
	
	
	
	private <K> void setTimeout(BasicRequest<K> basicRequest) {
	    //Explicitly setting ClientHttpRequestFactory instance to     
	    //SimpleClientHttpRequestFactory instance to leverage 
	    //set*Timeout methods
	    restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());
	    if(basicRequest.getTimeout()!=null) {
	    	SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate
		            .getRequestFactory();
		    rf.setReadTimeout(basicRequest.getTimeout());
		    rf.setConnectTimeout(basicRequest.getTimeout());
	    }
	    
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





