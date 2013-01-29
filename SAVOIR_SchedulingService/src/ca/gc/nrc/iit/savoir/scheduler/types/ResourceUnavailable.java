// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types;

import javax.xml.bind.annotation.XmlRootElement;

import ca.gc.iit.nrc.savoir.domain.Resource;

@XmlRootElement(name="ResourceUnavailable")
public class ResourceUnavailable extends SchedulingConflict {

	private static final long serialVersionUID = -3068566700614047050L;

	private Resource resource;

	public ResourceUnavailable() {
	}

	public ResourceUnavailable(Resource resource) {
		this.resource = resource;		
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	@Override
	public String toString() {
		return "Resource " + resource.getResourceID() + " ("
				+ resource.getDescription() + ") is unavailable.";
	}

}
