// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="ResourceInstance")
public class ResourceInstance {

	/** Instanciated resource**/
	private Resource resource;
	/** Instance Parameters and their values**/
	private List<ResourceParameter> parameters;
	
	public ResourceInstance(){
	}
	
	public Resource getResource() {
		return resource;
	}
	public List<ResourceParameter> getParameters() {
		return parameters;
	}
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public void setParameters(List<ResourceParameter> parameters) {
		this.parameters = parameters;
	}
	
}
