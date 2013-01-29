// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types.msg;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

import ca.gc.nrc.iit.savoir.scheduler.types.SchedulingConflict;

@XmlType(name="IsSessionReservationAvailableResponse")
public class IsSessionReservationAvailableResponse implements Serializable {

	private static final long serialVersionUID = 6554648727955025778L;

	private boolean successful;
	
	private long suggestedTimeOffset;
	
	private SchedulingConflict[] conflicts;
	
	public boolean isSuccessful() {
		return successful;
	}

	public long getSuggestedTimeOffset() {
		return suggestedTimeOffset;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public void setSuggestedTimeOffset(long suggestedTimeOffset) {
		this.suggestedTimeOffset = suggestedTimeOffset;
	}

	public SchedulingConflict[] getConflicts() {
		return conflicts;
	}

	public void setConflicts(SchedulingConflict[] conflicts) {
		this.conflicts = conflicts;
	}
	
}
