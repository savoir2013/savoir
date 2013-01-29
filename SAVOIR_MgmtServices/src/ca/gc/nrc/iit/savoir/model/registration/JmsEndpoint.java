// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.registration;

/**
 * Parameters for a JMS network endpoint.
 * 
 * @author Aaron Moss
 */
public class JmsEndpoint extends ProtocolEndpoint {

	/** Method to connect to the JMS server */
	private String connectionMethod;
	/** URI of the JMS broker to contact */
	private String jmsUri;
	/** JMS topic to publish/subcribe to */
	private String jmsTopic;
	
	
	public JmsEndpoint() {
		super();
	}
	
	
	//Java bean API
	public String getConnectionMethod() {
		return connectionMethod;
	}
	
	public String getJmsUri() {
		return jmsUri;
	}

	public String getJmsTopic() {
		return jmsTopic;
	}


	public void setConnectionMethod(String connectionMethod) {
		this.connectionMethod = connectionMethod;
	}
	
	public void setJmsUri(String jmsUri) {
		this.jmsUri = jmsUri;
	}

	public void setJmsTopic(String jmsTopic) {
		this.jmsTopic = jmsTopic;
	}


	//Fluent API
	public JmsEndpoint withConnectionMethod(String connectionMethod) {
		this.connectionMethod = connectionMethod;
		return this;
	}
	
	public JmsEndpoint withJmsUri(String jmsUri) {
		this.jmsUri = jmsUri;
		return this;
	}

	public JmsEndpoint withJmsTopic(String jmsTopic) {
		this.jmsTopic = jmsTopic;
		return this;
	}

	@Override
	public JmsEndpoint withProtocol(ProtocolType protocol) {
		this.protocol = protocol;
		return this;
	}
}
