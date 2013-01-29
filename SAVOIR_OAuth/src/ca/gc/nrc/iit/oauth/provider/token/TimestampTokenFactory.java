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

package ca.gc.nrc.iit.oauth.provider.token;

import org.apache.commons.codec.digest.DigestUtils;

import ca.gc.nrc.iit.oauth.common.OAuthConsumer;
import ca.gc.nrc.iit.oauth.common.OAuthToken;


/**
 * Generates tokens from the consumer key and the current time.
 * Md5 is used to obfuscate the token and token secret somewhat.
 * This token factory will respect the numBytes parameter, so long
 * as it does not exceed 32. 
 * 
 * @author Praveen Alavilli, AOL
 * @author Aaron Moss, NRC-IIT
 */
public class TimestampTokenFactory extends OAuthTokenFactory {

	@Override
	protected OAuthToken generateToken(OAuthConsumer consumer) {
		// generate oauth_token and oauth_secret
        String consumerKey = consumer.getConsumerKey();
        // generate token and secret based on consumer_key
        
        // for now use md5 of name + current time as token
        String tokenData = consumerKey + System.nanoTime();
        String tokenStr = DigestUtils.md5Hex(tokenData);
        
        String secretData = consumerKey + System.nanoTime() + tokenStr;
        String secret = DigestUtils.md5Hex(secretData);
        if (numBytes < 32) {
        	tokenStr = tokenStr.substring(0, numBytes);
        	secret = secret.substring(0, numBytes);
        }
        
        return new OAuthToken(tokenStr, secret);
	}

}
