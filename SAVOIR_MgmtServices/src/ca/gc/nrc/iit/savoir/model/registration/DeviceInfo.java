// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.registration;

/**
 * Contains device-management parameters from a registration ticket.
 * 
 * @author Aaron Moss
 */
public class DeviceInfo {

	/** Maximum number of users that can simultaneously use this device;
	 *  {@code -1} for unknown or no limit. */
	private int maxSimultaneousUsers;
	/** Details of how users are authenticated to this device. */
	private AuthenticationInfo authentication;

	
	public DeviceInfo() {}
	
	
	//Java bean API
	public int getMaxSimultaneousUsers() {
		return maxSimultaneousUsers;
	}

	public AuthenticationInfo getAuthentication() {
		return authentication;
	}

	
	public void setMaxSimultaneousUsers(int maxSimultaneousUsers) {
		this.maxSimultaneousUsers = maxSimultaneousUsers;
	}

	public void setAuthentication(AuthenticationInfo authentication) {
		this.authentication = authentication;
	}
	

	//Fluent API
	public DeviceInfo withMaxSimultaneousUsers(int maxSimultaneousUsers) {
		this.maxSimultaneousUsers = maxSimultaneousUsers;
		return this;
	}

	public DeviceInfo withAuthentication(AuthenticationInfo authentication) {
		this.authentication = authentication;
		return this;
	}
}
