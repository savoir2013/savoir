// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types.msg;

import ca.gc.iit.nrc.savoir.domain.Resource;

public class ActivateNetworkReservation {

	private Resource networkResource;

	public final Resource getNetworkResource() {
		return networkResource;
	}

	public final void setNetworkResource(Resource networkResource) {
		this.networkResource = networkResource;
	}
	
}
