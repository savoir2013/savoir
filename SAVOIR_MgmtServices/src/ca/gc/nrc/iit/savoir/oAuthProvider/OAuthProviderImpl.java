// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.oAuthProvider;

import javax.jws.WebService;
import ca.gc.nrc.iit.oauth.common.OAuthToken;
import ca.gc.nrc.iit.oauth.common.exception.OAuthException;
import ca.gc.nrc.iit.oauth.provider.OAuthProvider;
import ca.gc.nrc.iit.oauth.provider.consumer.OAuthConsumerProvider;
import ca.gc.nrc.iit.oauth.provider.validator.OAuthValidator;

/**
 * Thin wrapper around OAuth Provider to implement Web Service.
 * 
 * @author Aaron Moss
 */
@WebService(endpointInterface = "ca.gc.nrc.iit.savoir.oAuthProvider.OAuthProviderService")
public class OAuthProviderImpl implements OAuthProviderService {

	private OAuthProvider provider;
	
	public OAuthProviderImpl() {
		provider = new OAuthProvider();
	}
	
	//wrappers for bean property setters on provider
	public void setConsumerProvider(OAuthConsumerProvider oacp) {
		this.provider.setConsumerProvider(oacp);
	}
	
	public void setValidator(OAuthValidator validator) {
		this.provider.setValidator(validator);
	}
	
	public void setSendExceptionBody(boolean doSend) {
		this.provider.setSendExceptionBody(doSend);
	}
	
	public void setAcceptPlaintext(boolean acceptPlaintext) {
		OAuthProvider.setAcceptPlaintext(acceptPlaintext);
	}
	
	public void setAcceptHmacSha1(boolean acceptHmac) {
		OAuthProvider.setAcceptPlaintext(acceptHmac);
	}
	
	public void setAcceptRsaSha1(boolean acceptRsa) {
		OAuthProvider.setAcceptPlaintext(acceptRsa);
	}
	
	@Override
	public void authorizeRequestToken(WebServiceOAuthParams params)
			throws OAuthException {
		provider.authorizeRequestToken(params.toOAuthParams());
	}

	@Override
	public void logout(WebServiceOAuthParams params) throws OAuthException {
		provider.logout(params.toOAuthParams());
	}

	@Override
	public OAuthToken newAccessToken(WebServiceOAuthParams params)
			throws OAuthException {
		return provider.newAccessToken(params.toOAuthParams());
	}

	@Override
	public OAuthToken newRequestToken(WebServiceOAuthParams params)
			throws OAuthException {
		return provider.newRequestToken(params.toOAuthParams());
	}

	@Override
	public void validateMessage(WebServiceOAuthParams params)
			throws OAuthException {
		provider.validateMessage(params.toOAuthParams());
	}
	
}
