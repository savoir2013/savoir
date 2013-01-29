// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.registration;

/**
 * Information about device user authentication 
 * 
 * @author Aaron Moss
 */
public class AuthenticationInfo {

	/**
	 * Types of user authentication a device can perform.
	 */
	public static enum AuthenticationType {
		USERNAME_PASSWORD("username_password"),
		BLANK("");
		
		private String xml;
		
		private AuthenticationType(String xml) {
			this.xml = xml;
		}
		
		public String toString() {
			return this.xml;
		}
	}
	
	/** Is user authentication required for this device? */
	private boolean required;
	/** What type of user authentication does this device perform? */
	private AuthenticationType type;

	
	public AuthenticationInfo() {}
	
	
	//Java bean API
	public boolean isRequired() {
		return required;
	}

	public AuthenticationType getType() {
		return type;
	}

	
	public void setRequired(boolean required) {
		this.required = required;
	}

	public void setType(AuthenticationType type) {
		this.type = type;
	}
	
	
	//Fluent API
	public AuthenticationInfo withRequired(boolean required) {
		this.required = required;
		return this;
	}

	public AuthenticationInfo withType(AuthenticationType type) {
		this.type = type;
		return this;
	}
	
}
