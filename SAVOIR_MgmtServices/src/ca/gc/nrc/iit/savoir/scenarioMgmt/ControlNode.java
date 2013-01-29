// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt;

/**
 * Represents a controlling node in an active scenario.
 * 
 * @author Aaron Moss
 */
public class ControlNode {
	
	/** The name of the node - unique within the scenario */
	private String name;
	
	public ControlNode(String name) {
		this.name = name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
}
