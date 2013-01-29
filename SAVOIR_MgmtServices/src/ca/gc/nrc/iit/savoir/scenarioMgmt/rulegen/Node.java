// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;

/**
 * A workflow node.
 * 
 * @author Aaron Moss
 *
 */
public abstract class Node {

	/** The rulebase-unique name of this node */
	protected String name;

	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
}
