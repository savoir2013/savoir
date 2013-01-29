// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.registration;

/**
 * Protocol-specific parameters for a network endpoint.
 * 
 * @author Aaron Moss
 */
public abstract class ProtocolEndpoint {
	
	/**
	 * Types of network endpoint.
	 */
	public static enum ProtocolType {
		TCP_SOCKET("tcpSocket"),
		JMS("jms");
		
		private String xml;
		
		private ProtocolType(String xml) {
			this.xml = xml;
		}
		
		public String toString() {
			return this.xml;
		}
	}
	
	
	/** The transport protocol used to communicate with this ED */
	protected ProtocolType protocol;
	
	
	protected ProtocolEndpoint() {}
	
	
	public ProtocolType getProtocol() {
		return protocol;
	}

	
	public void setProtocol(ProtocolType protocol) {
		this.protocol = protocol;
	}
	
	public abstract ProtocolEndpoint withProtocol(ProtocolType protocol);
}
