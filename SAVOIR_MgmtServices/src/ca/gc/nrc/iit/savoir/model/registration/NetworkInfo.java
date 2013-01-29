// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.registration;

/**
 * Represents the network connection information contained in a registration 
 * ticket.
 * 
 * @author Aaron Moss
 */
public class NetworkInfo {

	/** The endpoint to use to pass messages to the service */
	private ProtocolEndpoint toService;
	/** The endpoint to use to pass messages to the SAVOIR server */
	private ProtocolEndpoint toSavoir;
	
	
	public NetworkInfo() {}


	//Java bean API
	public ProtocolEndpoint getToService() {
		return toService;
	}

	public ProtocolEndpoint getToSavoir() {
		return toSavoir;
	}

	
	public void setToService(ProtocolEndpoint toService) {
		this.toService = toService;
	}

	public void setToSavoir(ProtocolEndpoint toSavoir) {
		this.toSavoir = toSavoir;
	}
	
	
	//Fluent API
	public NetworkInfo withToService(ProtocolEndpoint toService) {
		this.toService = toService;
		return this;
	}

	public NetworkInfo withToSavoir(ProtocolEndpoint toSavoir) {
		this.toSavoir = toSavoir;
		return this;
	}
}
