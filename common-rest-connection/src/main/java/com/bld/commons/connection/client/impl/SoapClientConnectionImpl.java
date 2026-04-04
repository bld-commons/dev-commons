/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.client.impl.SoapClientConnectionImpl.java
 */
package com.bld.commons.connection.client.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.bld.commons.connection.client.AbstractClientConnection;
import com.bld.commons.connection.client.SoapClientConnection;
import com.bld.commons.connection.model.SoapRequest;
import com.bld.commons.connection.utils.SoapXmlBuilder;

/**
 * Default implementation of {@link SoapClientConnection}.
 * Uses Spring {@link org.springframework.web.client.RestTemplate} to execute SOAP 1.1 calls.
 * A new {@link org.springframework.web.client.RestTemplate} instance is created for each call
 * so that per-request settings (e.g. timeout) never leak across concurrent calls.
 */
@Component
public class SoapClientConnectionImpl extends AbstractClientConnection implements SoapClientConnection {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(SoapClientConnectionImpl.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T entitySoapTemplate(SoapRequest<?, ?> soapRequest, Class<T> responseClass) throws Exception {
		String envelope = buildAndLogRequest(soapRequest);
		HttpEntity<String> request = new HttpEntity<>(envelope, soapRequest.getHttpHeaders());
		ResponseEntity<String> response = this.buildRestTemplate(soapRequest.getTimeout()).exchange(
				soapRequest.getUrl(),
				soapRequest.getMethod(),
				request,
				String.class);
		logger.info("[SOAP] POST {} -> {}", soapRequest.getUrl(), response.getStatusCode());
		logger.debug("[SOAP] Response headers: {}", response.getHeaders());
		logger.debug("[SOAP] Response body:\n{}", response.getBody());
		return SoapXmlBuilder.unmarshalBody(response.getBody(), responseClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> listSoapTemplate(SoapRequest<?, ?> soapRequest, Class<T[]> responseClass) throws Exception {
		String envelope = buildAndLogRequest(soapRequest);
		HttpEntity<String> request = new HttpEntity<>(envelope, soapRequest.getHttpHeaders());
		ResponseEntity<String> response = this.buildRestTemplate(soapRequest.getTimeout()).exchange(
				soapRequest.getUrl(),
				soapRequest.getMethod(),
				request,
				String.class);
		logger.info("[SOAP] POST {} -> {}", soapRequest.getUrl(), response.getStatusCode());
		logger.debug("[SOAP] Response headers: {}", response.getHeaders());
		logger.debug("[SOAP] Response body:\n{}", response.getBody());
		T[] result = SoapXmlBuilder.unmarshalBody(response.getBody(), responseClass);
		return Arrays.asList(result);
	}

	/**
	 * Sets the SOAPAction header, builds the envelope and logs the outgoing request.
	 *
	 * @param soapRequest the SOAP request
	 * @return the serialised SOAP envelope
	 * @throws Exception if the envelope cannot be built
	 */
	private String buildAndLogRequest(SoapRequest<?, ?> soapRequest) throws Exception {
		String soapActionValue = StringUtils.isNotEmpty(soapRequest.getSoapAction())
				? "\"" + soapRequest.getSoapAction() + "\""
				: "\"\"";
		soapRequest.getHttpHeaders().set("SOAPAction", soapActionValue);
		String envelope = SoapXmlBuilder.buildEnvelope(soapRequest);
		logger.info("[SOAP] POST {} - SOAPAction: {}", soapRequest.getUrl(), soapActionValue);
		logger.debug("[SOAP] Request headers: {}", soapRequest.getHttpHeaders());
		logger.debug("[SOAP] Request envelope:\n{}", envelope);
		return envelope;
	}

}
