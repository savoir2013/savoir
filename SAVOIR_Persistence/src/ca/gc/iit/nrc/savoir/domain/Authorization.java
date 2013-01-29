// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain;

/**
 * Generic authorization object.
 * 
 * @author Aaron Moss
 */
public abstract class Authorization {

	/** ID of the entity this authorization is provided on */
	protected int authorizedOn;
	/** ID of the entity (user or group) this authorization is provided to */
	protected int authorizedTo;
	/** Role that {@code authorizedTo} has on {@code authorizedOn} */
	protected Role role;
	
	public Authorization() {
		super();
	}

	public int getAuthorizedOn() {
		return authorizedOn;
	}

	public int getAuthorizedTo() {
		return authorizedTo;
	}

	public Role getRole() {
		return role;
	}

	public void setAuthorizedOn(int authorizedOn) {
		this.authorizedOn = authorizedOn;
	}

	public void setAuthorizedTo(int authorizedTo) {
		this.authorizedTo = authorizedTo;
	}

	public void setRole(Role role) {
		this.role = role;
	}

}
