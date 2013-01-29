// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a branch in control flow.
 * Though other semantics may later be supported, at present this 
 * implementation passes control to all outgoing nodes in parallel ("AND" 
 * semantics). 
 * 
 * @author Aaron Moss
 */
public class SplitNode extends Node {

	/** the node to transfer control to */
	private List<String> nextNodes;
	
	
	public SplitNode() {}
	
	
	public List<String> getNextNodes() {
		return nextNodes;
	}
	
	public void setNextNodes(List<String> nextNodes) {
		this.nextNodes = nextNodes;
	}
	
	public void addNextNode(String nextNode) {
		if (null == this.nextNodes) this.nextNodes = new ArrayList<String>();
		this.nextNodes.add(nextNode);
	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		//rule "<name>"
		//	salience 10
		//
		//	when
		//		#Control on <name>
		//		cn_0 : ControlNode(name == "<name>")
		//	then
		//		#transfer control to next nodes
		//		drools.retract(cn_0);
		//		{drools.insert(new ControlNode("<nextNode"));}+
		//end
		
		sb.append("rule \"").append(name).append("\"\n")
			.append("\tsalience 10\n\n")
			.append("\twhen\n")
				.append("\t\tcn_0 : ControlNode(name == \"").append(name)
						.append("\")\n")
			.append("\tthen\n")
				.append("\t\tdrools.retract(cn_0);\n");
				if (null != nextNodes) 
					for (String nextNode : nextNodes) {
						sb.append("\t\tdrools.insert(new ControlNode(\"")
								.append(nextNode).append("\"));\n");
					}
		sb.append("end\n");
		
		return sb.toString();
	}
}
