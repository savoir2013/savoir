// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.servletClient;

import java.util.List;
import java.util.Vector;

public class Scenario {

	private String id = "";

	private List<String> connections = new Vector<String>();

	public Scenario(String id, List<String> meta){
		this.id = id;
		this.connections = meta;
	}
	
	public Scenario(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public List<String> getConnections() {
		return connections;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setConnections(List<String> connections) {
		this.connections = connections;
	}
	
}
