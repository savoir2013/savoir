// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types;

import java.util.Calendar;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ScenarioConflict")
public class ScenarioConflict extends SchedulingConflict {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6762877270680916351L;

	private int scenarioID;
	
	private String reason;

	private Calendar startTime;
	
	private Calendar endTime;
	
	public ScenarioConflict(){		
	}
	
	public ScenarioConflict(int scenarioID, String reason, Calendar startTime, Calendar endTime){		
		this.scenarioID = scenarioID;
		this.reason = reason;		
		this.startTime = startTime;
		this.endTime = endTime;
	}	
	
	public String getReason() {
		return reason;
	}
	
	public void setReason(String reason) {
		this.reason = reason;
	}

	public int getScenarioID() {
		return scenarioID;
	}

	public void setScenarioID(int scenarioID) {
		this.scenarioID = scenarioID;
	}

	public Calendar getStartTime() {
		return startTime;
	}

	public Calendar getEndTime() {
		return endTime;
	}

	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(Calendar endTime) {
		this.endTime = endTime;
	}
	
	public String toString(){
		return reason + " Scenario ID: " + scenarioID + " between "
				+ startTime.getTime().toString() + " and "
				+ endTime.getTime().toString();
	}

	
}
