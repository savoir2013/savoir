// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types.msg;

import ca.gc.nrc.iit.savoir.scheduler.types.SchedulingConflict;

public class IsNetworkAvailableResponse {

	private boolean successful;
	
	private int timeOffset;
	
	private SchedulingConflict[] schedulingConflicts;
	
	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public int getTimeOffset() {
		return timeOffset;
	}

	public void setTimeOffset(int timeOffset) {
		this.timeOffset = timeOffset;
	}

	public SchedulingConflict[] getSchedulingConflicts() {
		return schedulingConflicts;
	}

	public void setSchedulingConflicts(SchedulingConflict[] schedulingConflicts) {
		this.schedulingConflicts = schedulingConflicts;
	}
	
}
