/*
 * Copyright 2007 AOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.gc.nrc.iit.oauth.provider;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import ca.gc.nrc.iit.oauth.common.OAuth;
import ca.gc.nrc.iit.oauth.common.OAuthConsumer;
import ca.gc.nrc.iit.oauth.common.OAuthParams;
import ca.gc.nrc.iit.oauth.common.OAuthToken;
import ca.gc.nrc.iit.oauth.common.exception.OAuthException;
import ca.gc.nrc.iit.oauth.common.exception.OAuthProblemException;
import ca.gc.nrc.iit.oauth.common.exception.SanitizedOAuthException;
import ca.gc.nrc.iit.oauth.common.signature.OAuthSignatureMethod;
import ca.gc.nrc.iit.oauth.provider.consumer.OAuthConsumerProvider;
import ca.gc.nrc.iit.oauth.provider.spring.BeanManager;
import ca.gc.nrc.iit.oauth.provider.token.OAuthTokenFactory;
import ca.gc.nrc.iit.oauth.provider.validator.OAuthValidator;
import ca.gc.nrc.iit.oauth.provider.validator.SimpleOAuthValidator;


/**
 * Implements OAuth provider logic, to be used by OAuth provider servlets.
 * 
 * This OAuth provider assumes two-legged OAuth, used to authenticate a web-based application's
 * users. The users are drawn from an OAuthConsumerProvider.
 * 
 * This provider can be wired with Spring, or manually configured. To wire with Spring, one of 
 * the bean definition files referenced in the webapp's web.xml contextConfigLocation context-param 
 * must contain a bean with id "oAuthProvider", this class, with property "consumerProvider", 
 * an OAuthConsumerProvider.
 * 
 * Some optional bean parameters include "sendExceptionBody", "acceptPlaintext",
 * "acceptHmacSha1", and "acceptRsaSha1". Details of these are provided in the javadoc
 * for the respective parameters
 * 
 * Based on net.oauth.example.provider.core.SampleOAuthProvider,
 * distributed with the Java OAuth implementation at http://oauth.googlecode.com/svn/code/java/
 * 
 * @author Praveen Alavilli, AOL
 * @author Aaron Moss, NRC-IIT
 */
public class OAuthProvider {
	
	private static OAuthProvider provider;
	private OAuthValidator validator;
	private OAuthConsumerProvider consumerProvider, requestTokenProvider, accessTokenProvider;
	
	protected Logger log = Logger.getLogger(OAuthProvider.class);
	
	/* Spring parameters */
	/** Controls whether or not the body of OAuth exceptions are sent with the denial message 
	 * (This defaults to false, as setting it to true provides extra information to clients 
	 * with invalid logins, and is therefore a security liability). */
	protected boolean sendExceptionBody = false;
	
	/* Signature methods accepted */
	/** Will this provider accept the PLAINTEXT signature method?
	 *  Defaults to false, as PLAINTEXT does not provide credential 
	 *  security on non-transport-layer-secured communications. */
	private static boolean acceptPlaintext = false;
	/** Will this provider accept the HMAC-SHA1 signature method?
	 *  Defaults to true. */
	private static boolean acceptHmacSha1 = true;
	/** Will this provider accept the RSA-SHA1 signature method?
	 *  Defaults to true. */
	private static boolean acceptRsaSha1 = true;
	
	//enable defaults for signature method
	static {
		OAuthSignatureMethod.unregisterMethod(OAuthSignatureMethod.PLAINTEXT);
	}
	
	/**
	 *	Entry type to store OAuthConsumer with the OAuthToken for that consumer's login
	 */
	protected static class OAuthConsumerToken {
		public OAuthConsumer consumer;
		public OAuthToken token;
		
		public OAuthConsumerToken(OAuthConsumer consumer, OAuthToken token) {
			this.consumer = consumer;
			this.token = token;
		}
	}
	
	/**
	 * Gets an OAuthConsumer from a cached map 
	 */
	protected static class ConsumerTokenMapConsumerProvider implements OAuthConsumerProvider {
		private Map<String, OAuthConsumerToken> backingMap;
		
		public ConsumerTokenMapConsumerProvider(Map<String, OAuthConsumerToken> backingMap) {
			this.backingMap = backingMap;
		}
		
		@Override
		public OAuthConsumer getOAuthConsumer(String consumerKey) throws OAuthProblemException {
			OAuthConsumerToken consumerToken = backingMap.get(consumerKey);
			
			if (consumerToken == null) {
				OAuthProblemException p = 
					new OAuthProblemException(OAuth.Problems.CONSUMER_KEY_REJECTED);
				p.setParameter(OAuth.Problems.CONSUMER_KEY_REJECTED, consumerKey);
				throw p;
			}
			
			return consumerToken.consumer;
		}
	}
	
	/** Currently valid request tokens */
	protected Map<String, OAuthConsumerToken> requestTokens;
	/** Currently valid access tokens */
	protected Map<String, OAuthConsumerToken> accessTokens;
	
	public static OAuthProvider getOAuthProvider() {
		if (provider == null) {
    		ApplicationContext ctx = BeanManager.getBeanManager().getContext();
			if (ctx != null) {
				provider = (OAuthProvider) ctx.getBean("oAuthProvider");
			}
    	}
		if (provider == null) {
			provider = new OAuthProvider();
		}
		
    	return provider;
	}
	
	public OAuthProvider() {
		this(null, null);
	}
	
	public OAuthProvider(OAuthConsumerProvider provider, OAuthValidator validator) {
		this.requestTokens = new HashMap<String, OAuthConsumerToken>();
		this.accessTokens = new HashMap<String, OAuthConsumerToken>();
		this.consumerProvider = provider;
		this.requestTokenProvider = new ConsumerTokenMapConsumerProvider(this.requestTokens);
		this.accessTokenProvider = new ConsumerTokenMapConsumerProvider(this.accessTokens);
		this.validator = validator;
	}
	
	protected OAuthConsumerProvider getConsumerProvider() throws OAuthProblemException {
		if (consumerProvider == null) {
			throw new OAuthProblemException(OAuth.Problems.CONSUMER_KEY_REFUSED);
		}
		return consumerProvider;
	}
	
	public void setConsumerProvider(OAuthConsumerProvider provider) {
		consumerProvider = provider;
	}
	
	protected OAuthValidator getValidator() throws OAuthProblemException {
		if (validator == null) {
			validator = new SimpleOAuthValidator();
		}
		return validator;
	}
	
	public void setValidator(OAuthValidator validator) {
		this.validator = validator;
	}
	
	/**
	 * Implements the first phase of the OAuth handshake, getting a request token.
	 * @param params	The OAuth parameters
	 * @return a new request token for those parameters
	 * @throws OAuthException 
	 */
	public OAuthToken newRequestToken(OAuthParams params) throws OAuthException {
		try {
			String consumerKey = params.getConsumerKey();
		
			//get username/password
			OAuthConsumer consumer = getConsumerProvider().getOAuthConsumer(consumerKey);
			
			//validate message
			getValidator().validateMessage(params, new OAuthToken(), consumer);
			
	        //generate request token and secret
	        return generateRequestToken(consumer);
		} catch (OAuthProblemException e) {
			log.info("New Request Token fails.", e);
	    	if (sendExceptionBody) {
	    		throw e;
	    	} else {
	    		throw new SanitizedOAuthException(e);
	    	}
		}
	}
	
	/**
	 * Implements the second phase of the OAuth handshake, authorizing the request token
	 * @param params	The OAuth parameters including the request token
	 * @throws OAuthException 
	 */
	public void authorizeRequestToken(OAuthParams params) throws OAuthException {
		try {
			OAuthToken token = getRequestToken(params);
			
			if (!token.isAuthorized()) {
				markAsAuthorized(token);
			}
		} catch (OAuthProblemException e) {
			log.info("Authorize Request Token fails.", e);
	    	if (sendExceptionBody) {
	    		throw e;
	    	} else {
	    		throw new SanitizedOAuthException(e);
	    	}
		}
	}
	
	/**
	 * Implements the final phase of the OAuth handshake, getting an access token 
	 * @param params	The OAuth parameters
	 * @throws OAuthException 
	 */
	public OAuthToken newAccessToken(OAuthParams params) throws OAuthException {
		try {
			String consumerKey = params.getConsumerKey();
			
			OAuthToken token = getRequestToken(params);
			OAuthConsumer consumer = requestTokenProvider.getOAuthConsumer(consumerKey);
			getValidator().validateMessage(params, token, consumer);
			
			//ensure token is authorized
			if (!token.isAuthorized()) {
				throw new OAuthProblemException(OAuth.Problems.PERMISSION_DENIED);
			}
			
			//generate access token and secret
			return generateAccessToken(consumer, token);
		} catch (OAuthProblemException e) {
			log.info("New Access Token fails.", e);
	    	if (sendExceptionBody) {
	    		throw e;
	    	} else {
	    		throw new SanitizedOAuthException(e);
	    	}
		}
	}
	
	/**
	 * Validates an OAuth message - throws exception if invalid
	 * @param params	The OAuth parameters of the message
	 * @throws OAuthException 
	 */
	public void validateMessage(OAuthParams params) throws OAuthException {
		try {
			OAuthToken token = getAccessToken(params);
			OAuthConsumer consumer = accessTokenProvider.getOAuthConsumer(params.getConsumerKey());
			getValidator().validateMessage(params, token, consumer);
		} catch (OAuthProblemException e) {
			log.info("Validate Message fails.", e);
	    	if (sendExceptionBody) {
	    		throw e;
	    	} else {
	    		throw new SanitizedOAuthException(e);
	    	}
		}
	}
	
	/**
	 * Logs out an OAuth client
	 * @param params	The request parameters
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws OAuthException 
	 */
	public void logout(OAuthParams params) throws OAuthException {
		try {
			OAuthToken token = getAccessToken(params);
			OAuthConsumer consumer = accessTokenProvider.getOAuthConsumer(params.getConsumerKey());
			getValidator().validateMessage(params, token, consumer);
			accessTokens.remove(consumer.getConsumerKey());
		} catch (OAuthProblemException e) {
			log.info("Logout fails.", e);
	    	if (sendExceptionBody) {
	    		throw e;
	    	} else {
	    		throw new SanitizedOAuthException(e);
	    	}
		}
	}
	
	/**
     * Generate a fresh access token and secret for a consumer.
     * @return the new access token
     * @throws OAuthProblemException
     */
    protected synchronized OAuthToken generateAccessToken(OAuthConsumer consumer, 
    		OAuthToken requestToken) throws OAuthProblemException {
    	// update token in cache
    	String consumerKey = consumer.getConsumerKey();
    	requestTokens.remove(consumerKey);
    	OAuthToken accessToken = OAuthTokenFactory.newToken(consumer);
    	accessTokens.put(consumerKey, new OAuthConsumerToken(consumer, accessToken));
    	return accessToken;
    }
	
	/**
     * Generate a fresh request token and secret for a consumer.
	 * @throws OAuthProblemException 
     * 
     * @throws OAuthProblemException
     */
    protected synchronized OAuthToken generateRequestToken(OAuthConsumer consumer) 
    		throws OAuthProblemException {       
        // add to the local cache
    	String consumerKey = consumer.getConsumerKey();
    	OAuthToken token = OAuthTokenFactory.newToken(consumer);
    	OAuthConsumerToken consumerToken = 
    		new OAuthConsumerToken(consumer, token);
    	requestTokens.put(consumerKey, consumerToken);
    	return token;
    }
	
	/**
	 * Gets OAuth request token
	 * @param request	The parameters of the request
	 * @return the OAuthToken corresponding to those parameters, if any
	 * @throws OAuthProblemException
	 */
	protected synchronized OAuthToken getRequestToken(OAuthParams request) 
			throws OAuthProblemException {
		String consumerToken = request.getToken();
		
		if (consumerToken == null) {
			//invalid request
			OAuthProblemException e = 
				new OAuthProblemException(OAuth.Problems.PARAMETER_ABSENT);
			e.setParameter(OAuth.Problems.PARAMETER_ABSENT, 
        			OAuth.percentEncode(OAuthParams.OAUTH_TOKEN));
			throw e;
		}
		
		OAuthConsumerToken token = requestTokens.get(request.getConsumerKey());
		
		if (token == null) {
			//assume the token expired
			throw new OAuthProblemException(OAuth.Problems.TOKEN_EXPIRED);
		}
		
		if (!consumerToken.equals(token.token.getToken())) {
			//invalid token
			throw new OAuthProblemException(OAuth.Problems.TOKEN_REJECTED);
		}
		
		return token.token;
	}
	
	/**
	 * Gets OAuth access token
	 * @param request	The parameters of the request
	 * @return the OAuthToken corresponding to those parameters, if any
	 * @throws IOException
	 * @throws OAuthProblemException
	 */
	protected synchronized OAuthToken getAccessToken(OAuthParams request) 
			throws OAuthProblemException {
		String consumerToken = request.getToken();
		OAuthConsumerToken token = accessTokens.get(request.getConsumerKey());
					
		if (token == null) {
			//assume the token expired
			throw new OAuthProblemException(OAuth.Problems.TOKEN_EXPIRED);
		}
		
		if (!consumerToken.equals(token.token.getToken())) {
			//invalid token
			throw new OAuthProblemException(OAuth.Problems.TOKEN_REJECTED);
		}
		
		return token.token;
	}
    
    /**
     * Set the access token 
     */
    protected synchronized void markAsAuthorized(OAuthToken token) {
    	//mark the token as authorized
    	token.setAuthorized(true);
    }
    
	public static void setAcceptPlaintext(boolean acceptPlaintext) {
		if (OAuthProvider.acceptPlaintext != acceptPlaintext) {
			if (acceptPlaintext == true) {
				OAuthSignatureMethod.registerMethod(OAuthSignatureMethod.PLAINTEXT);
			} else {
				OAuthSignatureMethod.unregisterMethod(OAuthSignatureMethod.PLAINTEXT);
			}
			OAuthProvider.acceptPlaintext = acceptPlaintext;
		}
		//do nothing on unchanged value
	}

	public static void setAcceptHmacSha1(boolean acceptHmacSha1) {
		if (OAuthProvider.acceptHmacSha1 != acceptHmacSha1) {
			if (acceptHmacSha1 == true) {
				OAuthSignatureMethod.registerMethod(OAuthSignatureMethod.HMAC_SHA1);
			} else {
				OAuthSignatureMethod.unregisterMethod(OAuthSignatureMethod.HMAC_SHA1);
			}
			OAuthProvider.acceptHmacSha1 = acceptHmacSha1;
		}
		//do nothing on unchanged value
	}

	public static void setAcceptRsaSha1(boolean acceptRsaSha1) {
		if (OAuthProvider.acceptRsaSha1 != acceptRsaSha1) {
			if (acceptRsaSha1 == true) {
				OAuthSignatureMethod.registerMethod(OAuthSignatureMethod.RSA_SHA1);
			} else {
				OAuthSignatureMethod.unregisterMethod(OAuthSignatureMethod.RSA_SHA1);
			}
			OAuthProvider.acceptRsaSha1 = acceptRsaSha1;
		}
		//do nothing on unchanged value
	}
	
	public void setSendExceptionBody(boolean doSend) {
		sendExceptionBody = doSend;
	}
}