// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;

/**
 * Represents an activity rule that does not transfer control to a new activity.
 * 
 * @author Aaron Moss
 */
public class DataflowRule extends ActivityRule {

	public DataflowRule() {}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		//rule "<activity>_<index>"
		//
		//	when
		//		#Control on <activity>
		//		ControlNode(name == "<activity>")
		//		#bindings
		//		{<serviceBinding>}
		//		{<varBinding>}
		//		#conditions
		//		{<condition>}
		//	then
		//		#consequences
		//		{<consequence>}+
		//end
		
		sb.append("rule \"").append(activity).append("_").append(index).append("\"\n\n")
				.append("\twhen\n")
					.append("\t\tControlNode(name == \"").append(activity)
							.append("\")\n");
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
		if (null != consequences) 
			for (Consequence consequence : consequences) {
				sb.append("\t\t").append(consequence.toString());
			}
		sb.append("end\n");
		
		return sb.toString();
	}
}
