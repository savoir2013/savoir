// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.oauth.consumer.cxf;

import java.net.URL;

import org.apache.cxf.message.Message;

import ca.gc.nrc.iit.oauth.common.OAuthConsumer;
import ca.gc.nrc.iit.oauth.common.OAuthToken;
import ca.gc.nrc.iit.oauth.common.signature.OAuthSignatureMethod;


/**
 * OAuthHttpSupplier that always provides the same token.
 * The signature method defaults to HMAC-SHA1, but this can be overridden 
 * through the oauthSignatureMethod bean.
 * 
 * @author Aaron Moss
 */
public class SimpleOAuthHttpAuthSupplier extends OAuthHttpAuthSupplier {

	private OAuthToken token = null;
	private OAuthConsumer consumer = null;
	private String sigMethod = null;
	
	public SimpleOAuthHttpAuthSupplier() {}
	
	public SimpleOAuthHttpAuthSupplier(OAuthToken token) {
		this.token = token;
	}
	
	public SimpleOAuthHttpAuthSupplier(OAuthToken token, String sigMethod) {
		this.token = token;
		this.sigMethod = sigMethod;
	}
	
	@Override
	protected OAuthToken getOAuthToken(URL url, Message msg) {
		return this.token;
	}
	
	@Override
	protected OAuthConsumer getOAuthConsumer(URL url, Message msg) {
		return this.consumer;
	}
	
	@Override
	protected String getOAuthSignatureMethod(URL url, Message msg) {
		if (this.sigMethod == null) {
			this.sigMethod = OAuthSignatureMethod.HMAC_SHA1;
		}
		return this.sigMethod;
	}
	
	/**
	 * Sets the OAuth credentials together
	 * @param token		The token to set
	 * @param consumer	The consumer to set
	 */
	public void setCredentials(OAuthToken token, OAuthConsumer consumer) {
		this.token = token;
		this.consumer = consumer;
	}
	
	public void setToken(OAuthToken token) {
		this.token = token;
	}
	
	public OAuthToken getToken() {
		return this.token;
	}
	
	public void setConsumer(OAuthConsumer consumer) {
		this.consumer = consumer;
	}
	
	public OAuthConsumer getConsumer() {
		return this.consumer;
	}
	
	public String getOauthSignatureMethod() {
		return this.sigMethod;
	}
	
	public void setOauthSignatureMethod(String method) {
		this.sigMethod = method;
	}
}
