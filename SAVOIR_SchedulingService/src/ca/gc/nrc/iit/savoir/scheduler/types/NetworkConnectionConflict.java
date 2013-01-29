// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types;

import javax.xml.bind.annotation.XmlRootElement;



@XmlRootElement(name="NetworkConnectionConflict")
public class NetworkConnectionConflict extends SchedulingConflict {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5955834027469924066L;

	private int serviceID;
	
	private int connectionID;
	
	private String reason;
	
	private int maxBW;

	public NetworkConnectionConflict(){		
	}
	
	public NetworkConnectionConflict(int service, int connection, String reason, int maxBW){
		this.serviceID = service;
		this.connectionID = connection;
		this.reason = reason;
		this.maxBW = maxBW;
	}
	
	public int getServiceID() {
		return serviceID;
	}

	public void setServiceID(int serviceID) {
		this.serviceID = serviceID;
	}

	public int getConnectionID() {
		return connectionID;
	}

	public String getReason() {
		return reason;
	}

	public int getMaxBW() {
		return maxBW;
	}

	public void setConnectionID(int connectionID) {
		this.connectionID = connectionID;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public void setMaxBW(int maxBW) {
		this.maxBW = maxBW;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}	
	
}
