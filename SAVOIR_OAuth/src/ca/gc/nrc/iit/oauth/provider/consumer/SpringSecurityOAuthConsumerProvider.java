// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.oauth.provider.consumer;

import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

import ca.gc.nrc.iit.oauth.common.OAuth;
import ca.gc.nrc.iit.oauth.common.OAuthConsumer;
import ca.gc.nrc.iit.oauth.common.exception.OAuthProblemException;


/**
 * An OAuth provider drawing users from a Spring Security UserDetailsService.
 * 
 * This OAuth provider assumes two-legged OAuth, used to authenticate a web-based application's
 * users. To wire this provider, one of the bean definition files referenced in the webapp's 
 * web.xml contextConfigLocation context-param must contain a bean with id "oAuthProvider", 
 * this class, and a "userDetails" property referencing the UserDetailsService to use.
 * 
 * @author Aaron Moss, NRC-IIT
 */
public class SpringSecurityOAuthConsumerProvider implements OAuthConsumerProvider {

	/** The UserDetailsService to draw this provider's users from */
	protected UserDetailsService userDetails = null;
	
	@Override
	public synchronized OAuthConsumer getOAuthConsumer(String consumerKey) 
			throws OAuthProblemException {
		UserDetails user = null;
		
		// try to load from local cache if not throw exception
		try {
			user = userDetails.loadUserByUsername(consumerKey);
		} catch (UsernameNotFoundException e) {
			OAuthProblemException problem = 
				new OAuthProblemException(OAuth.Problems.CONSUMER_KEY_REJECTED);
			problem.setParameter(OAuth.Problems.CONSUMER_KEY_REJECTED, consumerKey);
			throw problem;
		}
		
		if(user == null) {
		  throw new OAuthProblemException(OAuth.Problems.TOKEN_REJECTED);
		}
		
		return new OAuthConsumer(user.getUsername(), user.getPassword());
	}

	public void setUserDetails(UserDetailsService userDetailsIn) {
		this.userDetails = userDetailsIn;
	}
}
