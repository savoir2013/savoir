// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types.msg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="DeleteSessionReservationResponse")
public class DeleteSessionReservationResponse implements Serializable{

	private static final long serialVersionUID = -5924874104182496979L;

	private boolean successful;
	
	private String failureCause;
	
	private List<Integer> failingSessions;

	public boolean isSuccessful() {
		return successful;
	}

	public String getFailureCause() {
		return failureCause;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public void setFailureCause(String failureCause) {
		this.failureCause = failureCause;
	}
	
	public final List<Integer> getFailingSessions() {
		if (failingSessions == null)
			failingSessions = new ArrayList<Integer>();
		return failingSessions;

	}

	public final void setFailingSessions(List<Integer> failingSessions) {
		this.failingSessions = failingSessions;
	}
	
}
