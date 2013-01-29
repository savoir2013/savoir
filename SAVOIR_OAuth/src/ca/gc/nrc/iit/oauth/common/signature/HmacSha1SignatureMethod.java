/*
 * Copyright 2007 Netflix, Inc.
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

package ca.gc.nrc.iit.oauth.common.signature;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import ca.gc.nrc.iit.oauth.common.OAuth;
import ca.gc.nrc.iit.oauth.common.OAuthConsumer;
import ca.gc.nrc.iit.oauth.common.OAuthToken;
import ca.gc.nrc.iit.oauth.common.exception.OAuthException;


/**
 * The HMAC-SHA1 signature method.
 * 
 * @author John Kristian, Netflix
 * @author Aaron Moss, NRC-IIT
 */
class HmacSha1SignatureMethod extends OAuthSignatureMethod {

    public static final String NAME = HMAC_SHA1;
    
    /** ISO-8859-1 or US-ASCII would work, too. */
    private static final String ENCODING = OAuth.ENCODING;

    private static final String MAC_NAME = "HmacSHA1";

    private SecretKey key = null;
    private OAuthToken token = null;
	
	@Override
	protected String getSignature(String baseString, OAuthToken token, OAuthConsumer consumer)
			throws OAuthException {
		try {
            String signature = base64Encode(computeSignature(baseString, token, consumer));
            return signature;
        } catch (GeneralSecurityException e) {
            throw new OAuthException(e);
        } catch (UnsupportedEncodingException e) {
            throw new OAuthException(e);
        }
	}

	@Override
	protected boolean isValid(String signature, String baseString, OAuthToken token, 
			OAuthConsumer consumer) throws OAuthException {
		try {
            byte[] expected = computeSignature(baseString, token, consumer);
            byte[] actual = decodeBase64(signature);
            return equals(expected, actual);
        } catch (GeneralSecurityException e) {
            throw new OAuthException(e);
        } catch (UnsupportedEncodingException e) {
            throw new OAuthException(e);
        }
	}
    
    @Override
    public String getMethodName() {
    	return NAME;
    }

    private byte[] computeSignature(String baseString, OAuthToken token, OAuthConsumer consumer)
            throws GeneralSecurityException, UnsupportedEncodingException {
        SecretKey key = null;
        synchronized (this) {
            if (this.key == null || !token.equals(this.token)) {
                String keyString = OAuth.percentEncode(consumer.getConsumerSecret().toString())
                        + '&' + OAuth.percentEncode(token.getTokenSecret());
                byte[] keyBytes = keyString.getBytes(ENCODING);
                this.key = new SecretKeySpec(keyBytes, MAC_NAME);
                this.token = token;
            }
            key = this.key;
        }
        Mac mac = Mac.getInstance(MAC_NAME);
        mac.init(key);
        byte[] text = baseString.getBytes(ENCODING);
        return mac.doFinal(text);
    }

}
