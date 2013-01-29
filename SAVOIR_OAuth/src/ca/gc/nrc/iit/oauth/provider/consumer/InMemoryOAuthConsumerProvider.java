// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.oauth.provider.consumer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ca.gc.nrc.iit.oauth.common.OAuth;
import ca.gc.nrc.iit.oauth.common.OAuthConsumer;
import ca.gc.nrc.iit.oauth.common.exception.OAuthProblemException;


/**
 * Example OAuthConsumerProvider based on HashMap.
 * For a more industrial-grade OAuthConsumerProvider, look at SpringSecurityOAuthConsumerProvider.
 * 
 * @author Aaron Moss, NRC-IIT
 */
public class InMemoryOAuthConsumerProvider implements OAuthConsumerProvider {

	/** The map of usernames to Consumers */
	protected Map<String, OAuthConsumer> consumers;
	
	public InMemoryOAuthConsumerProvider() {
		consumers = new HashMap<String, OAuthConsumer>();
	}
	
	public void setConsumers(Collection<? extends OAuthConsumer> consumers) {
		this.consumers = new HashMap<String, OAuthConsumer>(consumers.size());
		
		for (OAuthConsumer c : consumers) {
			this.consumers.put(c.getConsumerKey(), c);
		}
	}
	
	public void addConsumer(OAuthConsumer consumer) {
		this.consumers.put(consumer.getConsumerKey(), consumer);
	}
	
	public void addConsumers(Collection<? extends OAuthConsumer> consumers) {
		for (OAuthConsumer c : consumers) {
			this.consumers.put(c.getConsumerKey(), c);
		}
	}
	
	public OAuthConsumer getConsumer(String username) {
		return this.consumers.get(username);
	}
	
	@Override
	public OAuthConsumer getOAuthConsumer(String consumerKey)
			throws OAuthProblemException {
		OAuthConsumer consumer = this.consumers.get(consumerKey);
		
		if (consumer == null) {
			OAuthProblemException p = new OAuthProblemException(OAuth.Problems.CONSUMER_KEY_REJECTED);
			p.setParameter(OAuth.Problems.CONSUMER_KEY_REJECTED, consumerKey);
			throw p;
		}
		
		return consumer;
	}

}
