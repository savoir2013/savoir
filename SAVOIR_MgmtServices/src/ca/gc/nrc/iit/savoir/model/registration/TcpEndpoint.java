// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.registration;

/**
 * Parameters for a TCP endpoint
 * 
 * @author Aaron Moss
 */
public class TcpEndpoint extends ProtocolEndpoint {

	/** IP address */
	private String ipAddress;
	/** TCP port number */
	private int portNumber;
	
	
	public TcpEndpoint() {
		super();
	}


	//Java bean API
	public String getIpAddress() {
		return ipAddress;
	}

	public int getPortNumber() {
		return portNumber;
	}


	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	
	//Fluent API
	public TcpEndpoint withIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
		return this;
	}

	public TcpEndpoint withPortNumber(int portNumber) {
		this.portNumber = portNumber;
		return this;
	}

	@Override
	public TcpEndpoint withProtocol(ProtocolType protocol) {
		this.protocol = protocol;
		return this;
	}
	
}
