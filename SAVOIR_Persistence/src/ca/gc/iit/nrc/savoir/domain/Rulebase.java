// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="Rulebase")
public class Rulebase {

	/** The unique identifier of the Rulebase **/
	private int rulebaseId;
	/** The URL (physical location) of the Rulebase **/
	private String rulebaseURL;
	/** The unique identified of the Author (original User which created) of this Rulebase **/
	private int rulebaseUserId;	
	/** The unique identifier of the Session this Rulebase is defined on **/
	private int rulebaseSessionId;
	/** The Rule Engine upon which this Rulebase is designed to run **/
	private String ruleEngine;
	
	public Rulebase() {}
	
	public Rulebase(int rulebaseId, String rulebaseURL, int sessionId, String engine) {
		this.rulebaseId = rulebaseId;
		this.rulebaseURL = rulebaseURL;
		this.rulebaseSessionId = sessionId;
		if (engine != null && engine.equals("")!=true) { this.ruleEngine = engine; } else { this.ruleEngine = "Drools"; } //failsafe for default Rule Engine 	
	}

	public int getRulebaseId() {
		return rulebaseId;
	}

	public void setRulebaseId(int rulebaseId) {
		this.rulebaseId = rulebaseId;
	}

	public String getRulebaseURL() {
		return rulebaseURL;
	}

	public void setRulebaseURL(String rulebaseURL) {
		this.rulebaseURL = rulebaseURL;
	}

	public int getRulebaseUserId() {
		return rulebaseUserId;
	}

	public void setRulebaseUserId(int rulebaseUserId) {
		this.rulebaseUserId = rulebaseUserId;
	}
	
	public int getRulebaseSessionId() {
		return rulebaseSessionId;
	}

	public void setRulebaseSessionId(int rulebaseSessionId) {
		this.rulebaseSessionId = rulebaseSessionId;
	}

	public String getRulebaseRuleEngineId() {
		return ruleEngine;
	}

	public void setRulebaseRuleEngineId(String engine) {
		this.ruleEngine = engine;
	}
	
}
