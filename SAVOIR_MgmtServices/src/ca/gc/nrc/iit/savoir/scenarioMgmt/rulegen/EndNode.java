// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;

/**
 * Represents a scenario end rule.
 * 
 * @author Aaron Moss
 */
public class EndNode extends Node {

	public EndNode() {
		this.name = "End";
	}
	
	/**
	 * Overrides default behaviour so that name cannot be changed
	 */
	public void setName(String name) {}
	
	public String toString() {
		//rule "End"
		//	salience 15
		//	
		//	when
		//		ControlNode(name == "End")
		//	then 
		//		mgmtProxy.endSession(sessionId, userId);
		//end 
		
		return
			"rule \"End\"\n" +
				"\tsalience 15\n\n" +
				"\twhen\n" +
					"\t\tControlNode(name == \"End\")\n" +
				"\tthen\n" +
					"\t\tmgmtProxy.endSession(sessionId, userId);\n" +
			"end\n"
		;
	}
}
