// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.oauth.consumer.cxf;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.HttpAuthSupplier;

import ca.gc.nrc.iit.oauth.common.OAuthConsumer;
import ca.gc.nrc.iit.oauth.common.OAuthParams;
import ca.gc.nrc.iit.oauth.common.OAuthToken;
import ca.gc.nrc.iit.oauth.common.exception.OAuthException;
import ca.gc.nrc.iit.oauth.common.nonce.OAuthNonceFactory;
import ca.gc.nrc.iit.oauth.consumer.OAuthClient;
import ca.gc.nrc.iit.oauth.consumer.http.HttpClient;


/**
 * A HttpAuthSupplier to provide OAuth authentication.
 * This should be subclassed with a concrete implementation to supply 
 * OAuthAccessTokens for given parameters.
 * 
 * @author Aaron Moss
 */
public abstract class OAuthHttpAuthSupplier extends HttpAuthSupplier {

	protected OAuthHttpAuthSupplier() {
		super();
	}
	
	protected OAuthHttpAuthSupplier(String name) {
		super(name);
	}
	
	@Override
	public final String getAuthorizationForRealm(HTTPConduit conduit, URL url,
			Message msg, String realm, String fullHeader) {
		return authnStr(url, msg);
	}

	@Override
	public final String getPreemptiveAuthorization(HTTPConduit conduit, URL url, 
			Message msg) {
		return authnStr(url, msg);
	}
	
	private String authnStr(URL url, Message msg) {
		OAuthToken token = getOAuthToken(url, msg);
		OAuthConsumer consumer = getOAuthConsumer(url, msg);
		if (token == null || consumer == null) {
			return null;
		}
		
		//get HTTP request message for method
		//NOTE I'm not sure that this is ever set at this point in the 
		// interceptor chain - if you have problems with non-POST requests,
		// here is your culprit.
		String httpMethod = (String)msg.get(Message.HTTP_REQUEST_METHOD);
		
		if (httpMethod == null) {
			//if no HTTP request method is set on the message, assume POST
			httpMethod = HttpClient.POST;
		}
		
		try {
			OAuthParams params = new OAuthParams(httpMethod, url.toString(), 
					consumer.getConsumerKey(), OAuthNonceFactory.newNonce(), 
					getOAuthSignatureMethod(url, msg), token.getToken());
			OAuthClient.ensureSignature(params, token, consumer);
			return OAuthClient.getAuthorizationHeader(params);
		} catch (OAuthException e) {
			return null;
		} catch (IOException e) {
			return null;
		} catch (URISyntaxException e) {
			return null;
		}
	}
	
	/**
	 * Gets the OAuth access token for a given message
	 * @param url	The URL to call
	 * @param msg	The CXF message
	 * @return
	 */
	protected abstract OAuthToken getOAuthToken(URL url, Message msg);
	
	/**
	 * Gets the OAuth consumer for a given message
	 * @param url	The URL to call
	 * @param msg	The CXF message
	 * @return
	 */
	protected abstract OAuthConsumer getOAuthConsumer(URL url, Message msg);
	
	/**
	 * Gets the OAuth signature method for a given message
	 * @param url	The URL to call
	 * @param msg	The CXF message
	 * @return
	 */
	protected abstract String getOAuthSignatureMethod(URL url, Message msg);
}
