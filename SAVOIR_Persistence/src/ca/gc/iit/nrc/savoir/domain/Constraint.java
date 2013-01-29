// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="Constraint")
public class Constraint {

	private String resourceId;

	private String configArgs;
	
	private String id;
	
	private String parameterId;
	
	public Constraint(){
		
	}

	public String getResourceId() {
		return resourceId;
	}

	public String getConfigArgs() {
		return configArgs;
	}

	public String getId() {
		return id;
	}

	public String getParameterId() {
		return parameterId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public void setConfigArgs(String configArgs) {
		this.configArgs = configArgs;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setParameterId(String parameterId) {
		this.parameterId = parameterId;
	}	

	
}
