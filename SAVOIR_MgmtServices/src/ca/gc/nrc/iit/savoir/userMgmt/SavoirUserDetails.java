// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.userMgmt;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;

import ca.gc.iit.nrc.savoir.domain.User;

/**
 * Spring Security wrapper for SAVOIR User object.
 * Used in authentication system to map between SAVOIR User and Spring Security 
 * principal.
 * 
 * @author Aaron Moss
 *
 * @see <a href="http://static.springsource.org/spring-security/site/docs/2.0.x/apidocs/org/springframework/security/userdetails/UserDetails.html">Spring Security API</a>
 * @see <a href="http://static.springsource.org/spring-security/site/docs/2.0.x/reference/springsecurity.html">Spring Security Manual</a>
 */
public class SavoirUserDetails implements UserDetails {

	private static final long serialVersionUID = 1L;

	private User user;
	
	public SavoirUserDetails(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return this.user;
	}
	
	@Override
	public GrantedAuthority[] getAuthorities() {
		return new GrantedAuthority[] {new GrantedAuthorityImpl("ROLE_USER")};
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getDName();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
