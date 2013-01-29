// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.oauth.provider.spring;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.AbstractAuthenticationToken;

public class OAuthAuthenticationToken extends AbstractAuthenticationToken {
	
	private static final long serialVersionUID = 1L;
	
	private String consumerKey;
	
	public OAuthAuthenticationToken(String consumerKey) {
		super(new GrantedAuthority[] {new GrantedAuthorityImpl("ROLE_USER")});
		this.consumerKey = consumerKey;
	}
	
	public OAuthAuthenticationToken(String consumerKey, GrantedAuthority[] authorities) {
		super(authorities);
		this.consumerKey = consumerKey;
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return consumerKey;
	}
}
