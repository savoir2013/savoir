// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="Connection")
public class Connection {

	private int connectionID;
	
	private EndPoint sourceEndPoint;
	
	private EndPoint targetEndPoint;
	
	private String directionality;
	
	private double bwRequirement;
	
	private double minBwRequirement;
	
	private boolean lpNeeded;
	
	private Resource networkResource;

	public Connection(){
		
	}
	
	public int getConnectionID() {
		return connectionID;
	}

	public void setConnectionID(int connectionID) {
		this.connectionID = connectionID;
	}

	public EndPoint getSourceEndPoint() {
		return sourceEndPoint;
	}

	public EndPoint getTargetEndPoint() {
		return targetEndPoint;
	}

	public String getDirectionality() {
		return directionality;
	}

	public double getBwRequirement() {
		return bwRequirement;
	}

	public double getMinBwRequirement() {
		return minBwRequirement;
	}

	public boolean isLpNeeded() {
		return lpNeeded;
	}

	public Resource getNetworkResource() {
		return networkResource;
	}

	public void setSourceEndPoint(EndPoint sourceEndPoint) {
		this.sourceEndPoint = sourceEndPoint;
	}

	public void setTargetEndPoint(EndPoint targetEndPoint) {
		this.targetEndPoint = targetEndPoint;
	}

	public void setDirectionality(String directionality) {
		this.directionality = directionality;
	}

	public void setBwRequirement(double bwRequirement) {
		this.bwRequirement = bwRequirement;
	}

	public void setMinBwRequirement(double minBwRequirement) {
		this.minBwRequirement = minBwRequirement;
	}

	public void setLpNeeded(boolean lpNeeded) {
		this.lpNeeded = lpNeeded;
	}

	public void setNetworkResource(Resource networkResource) {
		this.networkResource = networkResource;
	}
	
}
