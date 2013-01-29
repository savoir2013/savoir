// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler;

import java.util.Map;

public abstract class IOnlineProblem {
	private IEDManager edgeDevicesManager;

	private INRManager networkResourcesManager;

	private Map<String,INRManager> networkManagers;
	
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
	
	public Map<String, INRManager> getNetworkManagers() {
		return networkManagers;
	}

	public void setNetworkManagers(Map<String, INRManager> networkManagers) {
		this.networkManagers = networkManagers;
	}

}
