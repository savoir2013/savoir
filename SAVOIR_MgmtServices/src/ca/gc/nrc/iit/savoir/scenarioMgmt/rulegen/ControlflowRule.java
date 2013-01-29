// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;

/**
 * Represents an activity rule that transfers control to a new activity.
 * 
 * @author Aaron Moss
 */
public class ControlflowRule extends ActivityRule {

	/** the node to transfer control to */
	private String nextNode;
	
	
	public ControlflowRule() {}

	
	public String getNextNode() {
		return nextNode;
	}

	public void setNextNode(String nextNode) {
		this.nextNode = nextNode;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		//rule "<activity>_<index>"
		//	salience 10
		//
		//	when
		//		#Control on <activity>
		//		cn_0 : ControlNode(name == "<activity>")
		//		#bindings
		//		{<serviceBinding>}
		//		{<varBinding>}
		//		#conditions
		//		{<condition>}
		//	then
		//		#consequences
		//		{<consequence>}
		//		#transfer control to next node
		//		drools.retract(cn_0);
		//		drools.insert(new ControlNode("<nextNode>"));
		//end
		
		sb.append("rule \"").append(activity).append("_").append(index)
				.append("\"\n")
			.append("\tsalience 10\n\n")	
			.append("\twhen\n")
					.append("\t\tcn_0 : ControlNode(name == \"")
							.append(activity).append("\")\n");
					if (null != services) 
						for (ServiceBinding sBind : services) {
							sb.append(sBind.toString());
						}
					if (null != vars) 
						for (VarBinding vBind : vars) {
							sb.append(vBind.toString());
						}
					if (null != conditions) 
						for (Condition condition : conditions) {
							sb.append(condition.toString());
						}
				sb.append("\tthen\n");
		for (Consequence consequence : consequences) {
			sb.append("\t\t").append(consequence.toString());
		}
		sb.append("\t\tdrools.retract(cn_0);\n");
		sb.append("\t\tdrools.insert(new ControlNode(\"")
			.append(nextNode).append("\"));\n");
		sb.append("end\n");
		
		return sb.toString();
	}
}
