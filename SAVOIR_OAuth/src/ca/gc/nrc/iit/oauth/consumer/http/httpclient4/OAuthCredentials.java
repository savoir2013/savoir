/*
 * Copyright 2009 Paul Austin
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

package ca.gc.nrc.iit.oauth.consumer.http.httpclient4;

import java.security.Principal;

import org.apache.http.auth.BasicUserPrincipal;
import org.apache.http.auth.Credentials;

import ca.gc.nrc.iit.oauth.common.OAuthConsumer;
import ca.gc.nrc.iit.oauth.common.OAuthToken;


/**
 * An OAuthConsumer and optional OAuthToken to be used as credentials for an 
 * AuthScheme based on OAuth.
 * 
 * @author John Kristian
 * @author Aaron Moss, NRC-IIT
 */
public class OAuthCredentials implements Credentials {

    private final OAuthConsumer consumer;
    private final OAuthToken token;

    public OAuthCredentials(OAuthConsumer consumer) {
    	this(consumer, null);
    }
    
    public OAuthCredentials(OAuthConsumer consumer, OAuthToken token) {
        this.consumer = consumer;
        this.token = token == null ? new OAuthToken() : token;
    }

    /**
     * Constructs a simple accessor, containing only a consumer key and secret.
     * This is useful for two-legged OAuth; that is interaction between a
     * Consumer and Service Provider with no User involvement.
     * @param consumerKey		username
     * @param consumerSecret	password
     */
    public OAuthCredentials(String consumerKey, String consumerSecret) {
        this(new OAuthConsumer(consumerKey, consumerSecret));
    }
    
    /**
     * Convenience method that constructs the OAuth classes for you
     * @param consumerKey		The consumer key
     * @param consumerSecret	The consumer secret
     * @param token				The token
     * @param tokenSecret		The token secret
     */
    public OAuthCredentials(String consumerKey, String consumerSecret, String token, 
    		String tokenSecret) {
    	this(new OAuthConsumer(consumerKey, consumerSecret), new OAuthToken(token, tokenSecret));
    }

    public OAuthConsumer getConsumer() {
        return consumer;
    }
    
    public OAuthToken getToken() {
    	return token;
    }

    /** Get the current consumer secret, to be used as a password. */
    @Override
    public String getPassword() {
        if (consumer == null || consumer.getConsumerSecret() == null) {
        	return null;
        }    	
    	return consumer.getConsumerSecret().toString();
    }

	@Override
	public Principal getUserPrincipal() {
		return consumer == null ? null : new BasicUserPrincipal(getConsumer().getConsumerKey());
	}

}
