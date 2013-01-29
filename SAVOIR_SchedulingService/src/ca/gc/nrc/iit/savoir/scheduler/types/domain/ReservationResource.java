// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types.domain;

public class ReservationResource {
	
	private static final String resource_type = "Resource";
	
	private static final String participant_type = "Participant";
	
	private static final int LIGHTPATH = 1;
	
	private static final int SCENARIO = 2;
	
	private String sourceIP;

	private String sourceType;

	private String sourceID;

	private String destinationIP;

	private String destinationType;

	private String destinationID;	
	
	private int networkResourceID = -1;

	private int bandwidth = 0;
	
	private int resourceType = 0;

	public ReservationResource() {

	}

	public ReservationResource(ReservationResource clone) {
		this.sourceIP = clone.sourceIP;
		this.destinationIP = clone.destinationIP;		
		this.bandwidth = clone.bandwidth;
		this.networkResourceID = clone.networkResourceID;
	}

	public ReservationResource(String sourceTriple, String destinationTriple) {
		this.destinationIP = destinationTriple;
		this.sourceIP = sourceTriple;
	}

	public String getSourceIP() {
		return sourceIP;
	}

	public void setSourceIP(String sourceIP) {
		this.sourceIP = sourceIP;
	}

	public String getDestinationIP() {
		return destinationIP;
	}

	public void setDestinationIP(String destinationIP) {
		this.destinationIP = destinationIP;
	}

	public int getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(int bandwidth) {
		this.bandwidth = bandwidth;
	}

	public String getSourceType() {
		return sourceType;
	}

	public String getSourceID() {
		return sourceID;
	}

	public String getDestinationType() {
		return destinationType;
	}

	public String getDestinationID() {
		return destinationID;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public void setSourceID(String sourceID) {
		this.sourceID = sourceID;
	}

	public void setDestinationType(String destinationType) {
		this.destinationType = destinationType;
	}

	public void setDestinationID(String destinationID) {
		this.destinationID = destinationID;
	}

	public int getNetworkResourceID() {
		return networkResourceID;
	}

	public void setNetworkResourceID(int networkResourceID) {
		this.networkResourceID = networkResourceID;
	}

	public int getResourceType() {
		return resourceType;
	}

	public void setResourceType(int resourceType) {
		this.resourceType = resourceType;
	}

}
