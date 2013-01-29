// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;

/**
 * Represents a merge of a branched control flow.
 * Though other semantics may later be supported, at present this 
 * implementation waits for all incoming nodes to pass control to it, and then 
 * advances control to the outgoing node ("AND" semantics).
 * 
 * @author Aaron Moss
 */
public class JoinNode extends Node {

	/** Number of incoming connections to node at present */
	private int index = 0;
	/** the node to transfer control to */
	private String nextNode;
	
	
	public JoinNode() {}
	
	
	public int getIndex() {
		return index;
	}
	
	public String getNextNode() {
		return nextNode;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public void setNextNode(String nextNode) {
		this.nextNode = nextNode;
	}
	
	/**
	 * Gets the name of this node, for linking, uniquely.
	 * Call EXACTLY ONCE per incoming link
	 * @return the name of this node to use for an incoming link
	 */
	public String getNewName() {
		return name + "-" + (index++);
	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		//rule "<name>"
		//	salience 10
		//
		//	when
		//		{cn_<i> : ControlNode(name == \"<name>-<i>\")}+
		//	then
		//		{drools.retract(cn_<i>);}+
		//		drools.insert(new ControlNode("<nextNode>"));
		//end
		
		sb.append("rule \"").append(name).append("\"\n")
			.append("\tsalience 10\n\n")
			.append("\twhen\n");
			for (int i = 0; i < index; i++) {
				sb.append("\t\tcn_").append(i)
						.append(" : ControlNode(name == ").append("\"")
						.append(name).append("-").append(i).append("\")\n");
			}
			sb.append("\tthen\n");
			for (int i = 0; i < index; i++) {
				sb.append("\t\tdrools.retract(cn_").append(i).append(");\n");
			}
				sb.append("\t\tdrools.insert(new ControlNode(\"")
						.append(nextNode).append("\"));\n");
		sb.append("end\n");
		
		return sb.toString();
	}
}
