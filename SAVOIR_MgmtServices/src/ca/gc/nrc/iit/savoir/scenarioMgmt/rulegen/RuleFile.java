// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Generator for scenario rule file.
 * Wraps a list of nodes in the neccessary imports and globals.
 * 
 * @author Aaron Moss
 */
public class RuleFile {

	/** The nodes in this rulefile */
	private List<Node> nodes;
	
	public RuleFile() {
		nodes = new ArrayList<Node>();
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
	
	public void addNode(Node node) {
		nodes.add(node);
	}
	
	public void addNodes(Collection<? extends Node> nodes) {
		this.nodes.addAll(nodes);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		//package ca.gc.nrc.iit.savoir.scenarioMgmt.runtime
		//
		//import java.util.Arrays
		//import java.util.List
		//import ca.gc.nrc.iit.savoir.scenarioMgmt.ControlNode
		//import ca.gc.nrc.iit.savoir.scenarioMgmt.ScenarioMgrImpl.MgmtProxy
		//import ca.gc.nrc.iit.savoir.model.session.Parameter
		//import ca.gc.nrc.iit.savoir.model.session.IntParameter
		//import ca.gc.nrc.iit.savoir.model.session.FloatParameter
		//import ca.gc.nrc.iit.savoir.model.session.BoolParameter
		//import ca.gc.nrc.iit.savoir.model.session.DateParameter
		//import ca.gc.nrc.iit.savoir.model.Service
		//
		//global MgmtProxy mgmtProxy
		//global Integer sessionId
		//global String userId
		//{
		//<node>}+
		
		sb.append("package ca.gc.nrc.iit.savoir.scenarioMgmt.runtime\n\n" +
				"import java.util.Arrays\n" +
				"import java.util.List\n" +
				"import ca.gc.nrc.iit.savoir.scenarioMgmt.ControlNode\n" +
				"import ca.gc.nrc.iit.savoir.scenarioMgmt.ScenarioMgrImpl.MgmtProxy\n" +
				"import ca.gc.nrc.iit.savoir.model.session.Parameter\n" +
				"import ca.gc.nrc.iit.savoir.model.session.IntParameter\n" +
				"import ca.gc.nrc.iit.savoir.model.session.FloatParameter\n" +
				"import ca.gc.nrc.iit.savoir.model.session.BoolParameter\n" +
				"import ca.gc.nrc.iit.savoir.model.session.DateParameter\n" +
				"import ca.gc.nrc.iit.savoir.model.session.Service\n\n" +
				"global MgmtProxy mgmtProxy\n" +
				"global Integer sessionId\n" +
				"global String userId\n");
		
		for (Node node : nodes) {
			sb.append("\n").append(node.toString());
		}
		
		return sb.toString();
	}
}
