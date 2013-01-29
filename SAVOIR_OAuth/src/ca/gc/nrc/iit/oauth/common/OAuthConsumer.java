// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.oauth.common;

/**
 * Represents an OAuth consumer, with a consumer key and secret
 * 
 * @author Aaron Moss, NRC-IIT
 */
public class OAuthConsumer {
	/** The ID of the consumer */
	private String consumerKey;
	/** The consumer's secret.
	 *  In shared secret implementations, this will be the consumer's password.
	 *  In PKI implementations, this will be the consumer's private key client-side,
	 *  and the consumer's public key server-side. */
	private Object consumerSecret;
	
	public OAuthConsumer() {
		this(null, null);
	}
	
	public OAuthConsumer(String consumerKey, Object consumerSecret) {
		this.setConsumerKey(consumerKey);
		this.setConsumerSecret(consumerSecret);
	}
	
	public String getConsumerKey() {
		return this.consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey == null ? "" : consumerKey;
	}

	public Object getConsumerSecret() {
		return this.consumerSecret;
	}

	public void setConsumerSecret(Object consumerSecret) {
		this.consumerSecret = consumerSecret == null ? "" : consumerSecret;
	}
}
