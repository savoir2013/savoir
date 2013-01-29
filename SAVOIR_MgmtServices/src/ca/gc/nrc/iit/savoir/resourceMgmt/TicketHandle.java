// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.resourceMgmt;

/**
 * A handle to allow a web-based client to access a registration ticket.
 * 
 * @author Aaron Moss
 */
public class TicketHandle {

	/** Resource ID */
	private String id;
	/** Resource Name */
	private String name;
	/** URI of ticket */
	private String uri;
	
	
	public TicketHandle() {}
	
	public TicketHandle(String id, String name, String uri) {
		this.id = id;
		this.name = name;
		this.uri = uri;
	}

	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getUri() {
		return uri;
	}

	
	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	
}
