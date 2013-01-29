// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package OAuth;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import ca.gc.nrc.iit.oauth.common.OAuthParams;
import ca.gc.nrc.iit.oauth.common.exception.OAuthException;
import ca.gc.nrc.iit.oauth.provider.servlet.OAuthServletUtils;
import ca.gc.nrc.iit.savoir.oAuthProvider.OAuthProviderService;
import ca.gc.nrc.iit.savoir.oAuthProvider.WebServiceOAuthParams;

/**
 * Gets authentication information off a servlet request, and validates it.
 * 
 * @author Aaron Moss
 */
public class Validator {

	private OAuthProviderService oaService;
	
	public Validator(){
		super();
	}
	
	public void setOaService(OAuthProviderService oaService) {
		this.oaService = oaService;
	}
	
	/**
	 * Gets and validates the authentication information off a HTTP Servlet 
	 * request.
	 * 
	 * @param request		The request to authenticate.
	 * 
	 * @return the username of the authenticated caller, null for no valid 
	 * 		authentication information
	 */
	public String validateCaller(HttpServletRequest request) {
		try {
			
			OAuthParams requestParams = 
				OAuthServletUtils.getTokenParams(request);
			return validate(requestParams);
			
		} catch (IOException e) {
			return null;
		} catch (URISyntaxException e) {
			return null;
		}
	}
	
	public String validateCaller(HttpServletRequest request, String url) {
		try {
			
			OAuthParams requestParams = 
				OAuthServletUtils.getTokenParams(request, url);
			return validate(requestParams);
			
		} catch (IOException e) {
			return null;
		} catch (URISyntaxException e) {
			return null;
		}
	}
	
	private String validate(OAuthParams requestParams) {
		try {
			
			WebServiceOAuthParams wsRequestParams = 
				new WebServiceOAuthParams(requestParams);
			oaService.validateMessage(wsRequestParams);
			
			//will only reach here if request params validate successfully
			return requestParams.getConsumerKey();
		
		} catch (OAuthException e) {
			return null;
		} 
	}
}
