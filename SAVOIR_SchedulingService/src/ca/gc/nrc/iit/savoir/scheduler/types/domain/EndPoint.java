// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types.domain;

public class EndPoint {

	private int node;
	
	private int resource;
	
	private int user;
	
	public EndPoint(int node, int resource, int user) {	
		this.node = node;
		this.resource = resource;
		this.user = user;
	}

	public int getNode() {
		return node;
	}

	public void setNode(int node) {
		this.node = node;
	}

	public int getResource() {
		return resource;
	}

	public void setResource(int resource) {
		this.resource = resource;
	}

	public int getUser() {
		return user;
	}

	public void setUser(int user) {
		this.user = user;
	}
	
	public String toString(){
		return ""+node+"\t"+resource+"\t"+user;
	}
	
	
}
