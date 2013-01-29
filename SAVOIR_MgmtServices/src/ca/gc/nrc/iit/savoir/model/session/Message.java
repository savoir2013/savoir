// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.session;

import java.io.Serializable;

import ca.gc.nrc.iit.savoir.model.SavoirXml;

/**
 * Java representation of a SAVOIR message,
 * with all required data for construction and routing.
 * 
 * @author Aaron Moss
 */
public class Message extends SavoirXml implements Serializable {

	private static final long serialVersionUID = 1L;

	/** message ID attribute */
	private String id;
	/** action attribute of message */
	private Action action;
	/** sessionID attribute of message */
	private String sessionId;
	/** all service elements of message */
	private Service service;
	
	public Message() {
	}
	
	//Java bean API for Message construction
	public String getId() {
		return id;
	}
	
	public Action getAction() {
		return action;
	}

	public String getSessionId() {
		return sessionId;
	}

	public Service getService() {
		return service;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void setId(int id) {
		this.id = Integer.toString(id);
	}
	
	public void setAction(Action action) {
		this.action = action;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setService(Service service) {
		this.service = service;
	}
	
	//Fluent API for Message construction
	public Message withId(String id) {
		this.id = id;
		return this;
	}
	
	public Message withId(int id) {
		this.id = Integer.toString(id);
		return this;
	}
	
	public Message withAction(Action action) {
		this.action = action;
		return this;
	}
	
	public Message withSessionId(String sessionId) {
		this.sessionId = sessionId;
		return this;
	}
	
	public Message withService(Service service) {
		this.service = service;
		return this;
	}
}
