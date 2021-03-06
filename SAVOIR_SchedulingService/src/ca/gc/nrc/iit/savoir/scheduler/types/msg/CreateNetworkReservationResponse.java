// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types.msg;

import ca.gc.nrc.iit.savoir.scheduler.types.SchedulingConflict;

public class CreateNetworkReservationResponse {

	private boolean successful;
	
	private String reservationID;
	
	private Long jobID;

	private SchedulingConflict[] schedulingConflicts;
	
	private long timeOffset;
	
	public boolean isSuccessful() {
		return successful;
	}

	public String getReservationID() {
		return reservationID;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public void setReservationID(String reservationID) {
		this.reservationID = reservationID;
	}

	public SchedulingConflict[] getSchedulingConflicts() {
		return schedulingConflicts;
	}

	public void setSchedulingConflicts(SchedulingConflict[] schedulingConflicts) {
		this.schedulingConflicts = schedulingConflicts;
	}

	public long getTimeOffset() {
		return timeOffset;
	}

	public void setTimeOffset(long timeOffset) {
		this.timeOffset = timeOffset;
	}

	public void setJobID(Long jobID) {
		this.jobID = jobID;
	}

	public Long getJobID() {
		return jobID;
	}
	
	
	
}
