// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;

import java.util.List;

/**
 * Represents a logical OR.
 * Will be true precisely when any of its subconditions are true.
 * 
 * @author Aaron Moss
 */
public class OrCondition extends CompoundCondition {

	public OrCondition() {}
	
	public OrCondition(List<Condition> conditions) {
		this.conditions = conditions;
	}
	
	public String toString() {
		if (null == conditions || conditions.isEmpty()) {
			//don't print anything for no conditions
			return "";
		} else if (1 == conditions.size()) {
			//simply print the condition if there is only one
			return conditions.get(0).toString();
		} else {
			StringBuilder sb = new StringBuilder();
			
			//(or
			//{<condition>}+
			//)
			sb.append("\t\t(or \n");
			for (Condition condition : conditions) {
				sb.append(condition.toString());
			}
			sb.append("\t\t)\n");
			
			return sb.toString();
		}
	}
}
