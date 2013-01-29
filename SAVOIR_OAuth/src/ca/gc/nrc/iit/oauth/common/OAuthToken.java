// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.oauth.common;

/**
 * Encapsulates shared secret/PKI information for OAuth tokens
 * 
 * @author Aaron Moss, NRC-IIT
 */
public class OAuthToken {
	
	/** The token */
	private String token;
	/** The token secret.
	 * In shared secret implementations, this will be the shared token secret.
	 * In PKI implementations, this may not be used. */
	private String tokenSecret;
	/** has this token been authorized? */
	private boolean authorized;
	
	public OAuthToken() {
		this(null, null);
	}
	
	public OAuthToken(String token, String tokenSecret) {
		this.setToken(token);
		this.setTokenSecret(tokenSecret);
		this.authorized = false;
	}

	public String getToken() {
		return this.token;
	}

	public void setToken(String token) {
		this.token = token == null ? "" : token;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret == null ? "" : tokenSecret;
	}
	
	public boolean isAuthorized() {
		return this.authorized;
	}
	
	public void setAuthorized(boolean authorized) {
		this.authorized = authorized;
	}
	
	@Override
	public boolean equals(Object o) {
		if (! (o instanceof OAuthToken)) {
			return false;
		}
		OAuthToken t = (OAuthToken)o;
		return (this.token.equals(t.token))
			&& (this.tokenSecret.equals(t.tokenSecret));
	}
	
	@Override
	public int hashCode() {
		return token.hashCode() + tokenSecret.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		if (!token.equals("")) 
			sb.append("Token:\t").append(token).append("\n");
		if (!tokenSecret.equals("")) 
			sb.append("Token Secret:\t").append(tokenSecret).append("\n");
		
		return sb.toString();
	}
}
