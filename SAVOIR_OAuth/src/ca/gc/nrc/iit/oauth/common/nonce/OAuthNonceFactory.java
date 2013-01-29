// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.oauth.common.nonce;

/**
 * A factory for generating nonces for OAuth.
 * These nonces should be non-repeating, and not follow an easily predictable pattern
 * for maximum security.
 * 
 * @author Aaron Moss
 */
public abstract class OAuthNonceFactory {
	
	private static OAuthNonceFactory nonceFactory = null;
	
	protected static final int DEFAULT_NUM_BYTES = 16;
	
	/** Number of characters in the resulting nonce.
	 *  This is a suggestion, rather than a hard rule. */
	protected final int numBytes;
	
	protected OAuthNonceFactory() {
		this(DEFAULT_NUM_BYTES);
	}
	
	/**
	 * @param numBytesIn	A suggestion for the number of characters in the resulting token
	 */
	protected OAuthNonceFactory(int numBytesIn) {
		numBytes = numBytesIn > 0 ? numBytesIn : DEFAULT_NUM_BYTES;
	}
	
	/**
	 * @return a new nonce
	 */
	public static String newNonce() {
		if (nonceFactory == null) {
			//set default nonce factory if none set
			nonceFactory = new RandomNonceFactory();
		}
		return nonceFactory.generateNonce();
	}
	
	/**
	 * @return a new nonce. This is the backing implementation of newNonce()
	 */
	protected abstract String generateNonce();
	
	public static void setOAuthNonceFactory(OAuthNonceFactory factory) {
		nonceFactory = factory;
	}
}
