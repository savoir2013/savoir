// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.oauth.common.nonce;

import java.util.Random;

import ca.gc.nrc.iit.oauth.common.signature.Base64;


/**
 * A nonce factory that is not generally cryptographically secure,
 * but should be sufficient for most purposes.
 * 
 * The backing implementation of this nonce is the Base64 encoding
 * of bytes provided by java.util.Random.nextBytes(). It will always 
 * return a nonce of length l = 4n (due to the encoding scheme), 
 * such that l is no shorter than numBytes, and no longer than 
 * numBytes + 3 
 * 
 * @author Aaron Moss
 */
public class RandomNonceFactory extends OAuthNonceFactory {

	private static Random rand = new Random();
	private byte[] bytes;
	
	public RandomNonceFactory() {
		this(DEFAULT_NUM_BYTES);
	}
	
	public RandomNonceFactory(int numBytesIn) {
		super(numBytesIn);
		//shrink byte array to account for Base64 expansion
		bytes = new byte[((numBytes / 4) + (numBytes % 4 == 0 ? 0 : 1)) * 3];
	}
	
	@Override
	protected String generateNonce() {
		rand.nextBytes(bytes);
		return new String(Base64.encodeBase64(bytes));
	}

}
