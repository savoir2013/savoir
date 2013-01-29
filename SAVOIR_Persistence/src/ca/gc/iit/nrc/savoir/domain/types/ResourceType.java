// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain.types;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="ResourceType")
public class ResourceType {

	public static final String NETWORK_RESOURCE = "NETWORK_RESOURCE";
	public static final String SERVICE_RESOURCE = "SERVICE";
	
	private String id;
	private String name;
	private String description;
	private String resourceClass;

	public ResourceType(){}
	
	public ResourceType(String id, String name, String description, String resourceClass) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.resourceClass = resourceClass;
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getResourceClass() {
		return resourceClass;
	}

	public void setResourceClass(String resourceClass) {
		this.resourceClass = resourceClass;
	}

}
