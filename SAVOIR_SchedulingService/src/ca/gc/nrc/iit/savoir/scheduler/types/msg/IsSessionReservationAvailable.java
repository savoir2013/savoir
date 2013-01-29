// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types.msg;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

import ca.gc.iit.nrc.savoir.domain.Session;
import ca.gc.nrc.iit.savoir.scheduler.types.SessionReservationType;

@XmlType(name="IsSessionReservationAvailable")
public class IsSessionReservationAvailable implements Serializable{

	private static final long serialVersionUID = 526163452077634335L;

	private Session newSession;
	
	private SessionReservationType reservationType;
	
	private boolean automaticActivation;
	
	//added by yyh 12-05-10
	private String resType;

	public Session getNewSession() {
		return newSession;
	}

	public SessionReservationType getReservationType() {
		return reservationType;
	}

	public void setNewSession(Session newSession) {
		this.newSession = newSession;
	}

	public void setReservationType(SessionReservationType reservationType) {
		this.reservationType = reservationType;
	}

	public void setAutomaticActivation(boolean automaticActivation) {
		this.automaticActivation = automaticActivation;
	}

	public boolean isAutomaticActivation() {
		return automaticActivation;
	}
	
	public String getResType(){
		return resType;
	}
	
	public void setResType(String resT){
		resType =  resT;
	}
	
}
