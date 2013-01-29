// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types.domain;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;

public class ReservationService {

	private List<ReservationResource> resourcesList = new Vector<ReservationResource>();

	private int sessionID = 0;
	
	private Calendar startTime = Calendar.getInstance();

	private Calendar endTime = Calendar.getInstance();
	
	private boolean accepted = false;

	public ReservationService(){
		
	}
	
	public ReservationService(ReservationService clone){
		this.startTime = clone.startTime;
		this.endTime = clone.endTime;
		this.accepted = clone.accepted;
		this.sessionID = clone.sessionID;
		this.resourcesList = new Vector<ReservationResource>();
		for(ReservationResource co : clone.getResourcesList()){
			this.resourcesList.add(new ReservationResource(co));
		}
	}
	
	public List<ReservationResource> getResourcesList() {
		return resourcesList;
	}

	public void setResourcesList(List<ReservationResource> resourcesList) {
		this.resourcesList = resourcesList;
	}

	public Calendar getStartTime() {
		return startTime;
	}

	public void setStartTime(Calendar startTime) {
		this.startTime.setTimeInMillis(startTime.getTimeInMillis());
	}
	
	public void setStartTime(long startTime) {
		this.startTime.setTimeInMillis(startTime);
	}

	public Calendar getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime.setTimeInMillis(endTime);
	}
	
	public void setEndTime(Calendar endTime) {
		this.endTime.setTimeInMillis(endTime.getTimeInMillis());
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	public int getSessionID() {
		return sessionID;
	}

	public void setSessionID(int sessionID) {
		this.sessionID = sessionID;
	}
}
