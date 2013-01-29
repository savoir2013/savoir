// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="UnscheduledSubSession")
public class UnscheduledSubSession extends SchedulingConflict {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7974239406011846776L;
	private int sessionID;
	
	public UnscheduledSubSession(){		
	}
	
	public UnscheduledSubSession(int sessionID){		
		this.sessionID = sessionID;		
	}	
	
	public int getSessionID() {
		return sessionID;
	}

	public void setSessionID(int sessionID) {
		this.sessionID = sessionID;
	}
		
	public String toString(){
		return "Subsession " + sessionID + " could not be scheduled ";
				
	}

	
}
