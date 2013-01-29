// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt;

import java.util.List;

import ca.gc.iit.nrc.savoir.domain.Scenario;

/**
 * Response object for scenario submission, containing the completed scenario 
 * object (null if a fatal error occurred and no such object was created) and 
 * a list of errors and warnings (empty for none such).
 * 
 * @author Aaron Moss
 */
public class ScenarioCompilerOutput {

	/** The finished scneario (null for errors prevented completion) */
	private Scenario scenario;
	/** Any warnings (empty for none such) */
	private List<String> warnings;
	
	
	public ScenarioCompilerOutput() {}
	
	public ScenarioCompilerOutput(Scenario scenario, List<String> warnings) {
		this.scenario = scenario;
		this.warnings = warnings;
	}

	
	public Scenario getScenario() {
		return scenario;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	
	public void setScenario(Scenario scenario) {
		this.scenario = scenario;
	}

	public void setWarnings(List<String> warnings) {
		this.warnings = warnings;
	}
	
}
