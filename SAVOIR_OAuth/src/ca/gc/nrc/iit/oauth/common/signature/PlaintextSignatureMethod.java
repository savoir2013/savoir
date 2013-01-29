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

import ca.gc.nrc.iit.oauth.common.OAuth;
import ca.gc.nrc.iit.oauth.common.OAuthConsumer;
import ca.gc.nrc.iit.oauth.common.OAuthToken;
import ca.gc.nrc.iit.oauth.common.exception.OAuthException;

/**
 * The PLAINTEXT signature method.
 * 
 * @author John Kristian, Netflix
 * @author Aaron Moss, NRC-IIT
 */
class PlaintextSignatureMethod extends OAuthSignatureMethod {

	public static final String NAME = PLAINTEXT;
	
	private String signature = null;
	private OAuthToken token = null;
	
	@Override
	public String getMethodName() {
		return NAME;
	}

	@Override
	protected String getSignature(String baseString, OAuthToken token, OAuthConsumer consumer) {
		return getSignature(token, consumer);
	}

	@Override
	protected boolean isValid(String signature, String baseString, OAuthToken token, 
			OAuthConsumer consumer) throws OAuthException {
		return equals(getSignature(token, consumer), signature);
	}

    private synchronized String getSignature(OAuthToken token, OAuthConsumer consumer) {
        if (signature == null || !token.equals(this.token)) {
        	signature = OAuth.percentEncode(consumer.getConsumerSecret().toString()) + '&'
                    + OAuth.percentEncode(token.getTokenSecret());
            this.token = token;
        }
        return signature;
    }
}
