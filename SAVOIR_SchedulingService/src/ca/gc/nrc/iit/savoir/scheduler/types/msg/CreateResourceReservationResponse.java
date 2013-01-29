// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types.msg;

import ca.gc.nrc.iit.savoir.scheduler.types.SchedulingConflict;

public class CreateResourceReservationResponse {

	private boolean successful;
	
	private long reservationID;

	private SchedulingConflict[] schedulingConflicts;
	
	private long timeOffset;
	
	public boolean isSuccessful() {
		return successful;
	}

	public long getReservationID() {
		return reservationID;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public void setReservationID(long reservationID) {
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
	
	
	
}
