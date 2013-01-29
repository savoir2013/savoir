// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.utils;

public class ConfigResponse {

	private Boolean done;

	private String scenarioID;

	public ConfigResponse(boolean done, String scenario) {
		this.done = done;
		this.scenarioID = scenario;
	}

	public Boolean isDone() {
		return done;
	}

	public String getScenarioID() {
		return scenarioID;
	}

	public void setDone(Boolean done) {
		this.done = done;
	}

	public void setScenarioID(String scenarioID) {
		this.scenarioID = scenarioID;
	}

}
