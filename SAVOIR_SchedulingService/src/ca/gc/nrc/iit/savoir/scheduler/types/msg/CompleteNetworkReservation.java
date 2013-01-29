// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types.msg;

public class CompleteNetworkReservation {

	private Long jobID;

	public CompleteNetworkReservation() {
	}

	public CompleteNetworkReservation(Long jobID) {
		this.jobID = jobID;
	}

	public void setJobID(Long jobID) {
		this.jobID = jobID;
	}

	public Long getJobID() {
		return jobID;
	}

}
