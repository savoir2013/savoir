// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.oAuthProvider;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.WebFault;

import ca.gc.nrc.iit.oauth.common.OAuthToken;
import ca.gc.nrc.iit.oauth.common.exception.OAuthException;

/**
 * Exposes an OAuth Provider via a Web Service interface.
 * This passes the same data that the handshake described in the OAuth 1.0a 
 * specification uses, but passed as Web Service calls, rather than HTTP 
 * requests. It is therefore based on OAuth rather than an OAuth 
 * implementation. It also adds a {@code logout()} method, used to invalidate 
 * a client's access token.
 * 
 * @author Aaron Moss
 *
 * @see <a href="http://oauth.net/core/1.0a/">OAuth Spec 1.0a</a>
 */
@WebService
@WebFault(name = "OAuthException", 
		faultBean = "ca.gc.nrc.iit.oauth.common.exception.OAuthException")
public interface OAuthProviderService {
	
	/**
	 * Implements the first phase of the OAuth handshake, getting a request 
	 * token.
	 * 
	 * @param params	The OAuth parameters
	 * 
	 * @return a new request token for those parameters
	 * 
	 * @throws OAuthException 
	 */
	@WebResult(name = "OAuthToken")
	public OAuthToken newRequestToken(
			@WebParam(name="params") WebServiceOAuthParams params) 
			throws OAuthException;
	
	/**
	 * Implements the second phase of the OAuth handshake, authorizing the 
	 * request token.
	 * 
	 * @param params	The OAuth parameters including the request token
	 * 
	 * @throws OAuthException 
	 */
	public void authorizeRequestToken(
			@WebParam(name="params") WebServiceOAuthParams params) 
			throws OAuthException;
	
	/**
	 * Implements the final phase of the OAuth handshake, getting an access 
	 * token.
	 *  
	 * @param params	The OAuth parameters
	 * 
	 * @throws OAuthException 
	 */
	@WebResult(name = "OAuthToken")
	public OAuthToken newAccessToken(
			@WebParam(name="params") WebServiceOAuthParams params) 
			throws OAuthException;
	
	/**
	 * Validates an OAuth message - throws exception if invalid
	 * 
	 * @param params	The OAuth parameters of the message
	 * 
	 * @throws OAuthException on invalid message
	 */
	public void validateMessage(
			@WebParam(name="params") WebServiceOAuthParams params)
			throws OAuthException;
	
	/**
	 * Logs out an OAuth client, invalidating their access token
	 * 
	 * @param params	The request parameters
	 * 
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws OAuthException on OAuth error
	 */
	public void logout(
			@WebParam(name="params") WebServiceOAuthParams params)
			throws OAuthException;
}
