// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.oauth.provider.token;

import ca.gc.nrc.iit.oauth.common.OAuthConsumer;
import ca.gc.nrc.iit.oauth.common.OAuthToken;

/**
 * A factory for generating tokens for OAuth.
 * These tokens should be unique, and, for maximum security, not follow an easily 
 * predictable pattern.
 * 
 * @author Aaron Moss
 */
public abstract class OAuthTokenFactory {
	
	private static OAuthTokenFactory tokenFactory = null;
	
	/** Number of characters in the resulting token.
	 *  This is a suggestion, rather than a hard rule. */
	protected static final int DEFAULT_NUM_BYTES = 32;
	
	protected final int numBytes;
	
	protected OAuthTokenFactory() {
		this(DEFAULT_NUM_BYTES);
	}
	
	/**
	 * @param numBytesIn	A suggestion for the number of characters in the resulting token
	 */
	protected OAuthTokenFactory(int numBytesIn) {
		numBytes = numBytesIn;
	}
	
	/**
	 * @return a new token
	 */
	public static OAuthToken newToken(OAuthConsumer consumer) {
		if (tokenFactory == null) {
			//set default token factory if none set
			tokenFactory = new TimestampTokenFactory();
		}
		return tokenFactory.generateToken(consumer);
	}
	
	/**
	 * @param consumer	Consumer data that may be used in generating the token
	 */
	protected abstract OAuthToken generateToken(OAuthConsumer consumer);
	
	public static void setOAuthTokenFactory(OAuthTokenFactory factory) {
		tokenFactory = factory;
	}
}
