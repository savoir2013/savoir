// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.impl;

import ca.gc.nrc.iit.savoir.scheduler.IEDManager;
import ca.gc.nrc.iit.savoir.scheduler.INRManager;
import ca.gc.nrc.iit.savoir.scheduler.IOfflineProblem;

public class OfflineProblem implements IOfflineProblem {
	
	private IEDManager edgeDevicesManager;
	
	private INRManager networkResourcesManager;

	public IEDManager getEdgeDevicesManager() {
		return edgeDevicesManager;
	}

	public INRManager getNetworkResourcesManager() {
		return networkResourcesManager;
	}

	public void setEdgeDevicesManager(IEDManager edgeDevicesManager) {
		this.edgeDevicesManager = edgeDevicesManager;
	}

	public void setNetworkResourcesManager(
			INRManager networkResourcesManager) {
		this.networkResourcesManager = networkResourcesManager;
	}
}
