// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.oauth.provider.consumer;

import java.io.IOException;

import ca.gc.nrc.iit.oauth.common.OAuthConsumer;
import ca.gc.nrc.iit.oauth.common.exception.OAuthProblemException;

/**
 * Provides OAuth Consumers to an OAuth provider
 * 
 * @author Aaron Moss
 */
public interface OAuthConsumerProvider {
	
	/**
	 * Gets the OAuth consumer token for the user who formed the request message
	 * @param consumerKey	The consumer key (username)
	 * @return	the OAuth consumer for to this user
	 * @throws IOException
	 * @throws OAuthProblemException CONSUMER_KEY_REJECTED on no such user, 
	 * 			with parameter of the rejected consumer key
	 */
    public OAuthConsumer getOAuthConsumer(String consumerKey) throws OAuthProblemException;
}
