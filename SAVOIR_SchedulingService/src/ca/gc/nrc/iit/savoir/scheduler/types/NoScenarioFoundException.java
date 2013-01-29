// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types;

import javax.xml.bind.annotation.XmlRootElement;



@XmlRootElement(name="NoScenarioFoundException")
public class NoScenarioFoundException extends SchedulingConflict {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1877322198297792564L;
	private int sessionID;

	public NoScenarioFoundException() {

	}

	public NoScenarioFoundException(int sID) {
		this.sessionID = sID;
	}

	public int getSessionID() {
		return sessionID;
	}

	public void setSessionID(int sessionID) {
		this.sessionID = sessionID;
	}

	@Override
	public String toString() {
		return "Session " + sessionID
				+ " could not be scheduled. No matching APN scenario found.";
	}

}
