// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="Credential")
public class Credential {
	
	/** Credential ID */
	private int credentialId;
	/** Set of Name-Value pairs this credential is composed of */
	private Map<String, CredentialParameter> parameters;
	/** Human-readable description of this credential (optional) */
	private String description;
	
	/** Parameter-type dependent "username" */
	private String username;
	/** Parameter-type dependent "password" */
	private String password;
	
	public Credential() {
		parameters = new HashMap<String, CredentialParameter>();
	}

	public int getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(int credentialId) {
		this.credentialId = credentialId;
	}

	public List<CredentialParameter> getParameters() {
		return new ArrayList<CredentialParameter>(parameters.values());
	}

	public void setParameters(List<CredentialParameter> parameters) {
		this.parameters = new HashMap<String, CredentialParameter>();
		if (parameters != null) for (CredentialParameter p : parameters) {
			this.parameters.put(p.getParameter().getId(), p);
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets the "username" of this credential
	 * @return	the value of the "USERNAME" parameter of this credential, if
	 * 				such exists, the empty string otherwise (Note that this 
	 * 				may later be expanded to other parameters representing a 
	 * 				"username")
	 */
	public String username() {
		if (username == null) {
			CredentialParameter p = parameters.get("USERNAME");
			if (p != null) {
				username = p.getValue();
			} else {
				username = "";
			}
		}
		
		return username;
	}
	
	/**
	 * Gets the "password" of this credential
	 * @return	the value of the "PASSWORD" parameter of this credential, if 
	 * 				such exists, the empty string otherwise (Note that this may 
	 * 				later be expanded to other parameters representing a 
	 * 				"password")
	 */
	public String password() {
		if (password == null) {
			CredentialParameter p = parameters.get("PASSWORD");
			if (p != null) {
				password = p.getValue();
			} else {
				password = "";
			}
		}
		
		return password;
	}
}
