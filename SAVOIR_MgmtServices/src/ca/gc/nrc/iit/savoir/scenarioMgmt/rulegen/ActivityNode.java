// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an activity instance in a scenario. 
 * Contains all rules active when that instance has control of the scenario.
 * 
 * @author Aaron Moss
 */
public class ActivityNode extends Node {

	/** Rules triggered when this activity has focus */
	private List<ActivityRule> rules; 
	

	public ActivityNode() {
		rules = new ArrayList<ActivityRule>();
	}

	
	public List<ActivityRule> getRules() {
		return rules;
	}
	
	public void setRules(List<ActivityRule> rules) {
		this.rules.clear();
		for (ActivityRule r : rules) {
			addRule(r);
		}
	}
	
	private int index = 1;
	public void addRule(ActivityRule rule) {
		
		rule.setActivity(name);
		rule.setIndex(index++);
		
		rules.add(rule);
	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (ActivityRule rule : rules) {
			sb.append("\n").append(rule.toString());
		}
		
		return sb.toString();
	}
}
