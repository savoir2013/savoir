// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types.msg;

public class CompleteNetworkReservationResponse {

	private boolean successful;

	public CompleteNetworkReservationResponse() {
	}

	public CompleteNetworkReservationResponse(boolean successful) {
		this.successful = successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public boolean isSuccessful() {
		return successful;
	}

}
