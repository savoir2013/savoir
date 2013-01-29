// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.registration;

import ca.gc.iit.nrc.savoir.domain.PersonInfo;

/**
 * Represents the general service information contained in a registration 
 * ticket.
 *  
 * @author Aaron Moss
 */
public class ServiceInfo {

	/** Unique service ID */
	private String id;
	/** Service name */
	private String name;
	/** ID of resource type */
	private String type;
	/** Human-readable service description */
	private String description;
	/** Username of SAVOIR user who is contact for service */
	private String contactUser;
	/** Contact data of person who is contact for service */
	private PersonInfo contactInfo;
	
	
	public ServiceInfo() {}


	//Java bean API
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public String getDescription() {
		return description;
	}

	public String getContactUser() {
		return contactUser;
	}

	public PersonInfo getContactInfo() {
		return contactInfo;
	}


	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public void setContactUser(String contactUser) {
		this.contactUser = contactUser;
	}

	public void setContactInfo(PersonInfo contactInfo) {
		this.contactInfo = contactInfo;
	}
	
	
	//Fluent API
	public ServiceInfo withId(String id) {
		this.id = id;
		return this;
	}

	public ServiceInfo withName(String name) {
		this.name = name;
		return this;
	}
	
	public ServiceInfo withType(String type) {
		this.type = type;
		return this;
	}
	
	public ServiceInfo withDescription(String description) {
		this.description = description;
		return this;
	}

	public ServiceInfo withContactUser(String contactUser) {
		this.contactUser = contactUser;
		return this;
	}

	public ServiceInfo withContactInfo(PersonInfo contactInfo) {
		this.contactInfo = contactInfo;
		return this;
	}
}
