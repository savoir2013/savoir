// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.resourceMgmt;

import ca.gc.nrc.iit.savoir.model.session.Message;

/**
 * Represents a message to send to a resource.
 * These messages can be put in insertion order by their "num" property.
 * A reference to the current state of resource they are to be sent to is 
 * stored in the "resource" property. The message to send and address to send it 
 * to are stored in "msg" and "endpoint", respectively.
 * 
 * @author Aaron Moss
 */
public class ResourceMessage {

	/** a per-session unique ordering number for message insertion */
	private int num;
	/** current state of this message's edge device */
	private InstanceState resource;
	/** endpoint to send message go */
	private String endpoint;
	/** Message to send */
	private Message msg;
	
	
	public ResourceMessage() {}
	
	public ResourceMessage(int num, InstanceState resource, String endpoint, 
			Message msg) {
		this.num = num;
		this.resource = resource;
		this.endpoint = endpoint;
		this.msg = msg;
	}

	
	public int getNum() {
		return num;
	}
	
	public InstanceState getResource() {
		return resource;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public Message getMsg() {
		return msg;
	}

	public void setResource(InstanceState resource) {
		this.resource = resource;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public void setMsg(Message msg) {
		this.msg = msg;
	}

}
