// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types.msg;

import ca.gc.nrc.iit.savoir.scheduler.types.domain.Reservation;

public class IsNetworkAvailable {
	
	private Reservation reservation;

	private boolean automaticActivation;
	
	public IsNetworkAvailable(CreateNetworkReservation req) {
		this.setReservation(new Reservation(req.getReservation()));
		this.setAutomaticActivation(req.isAutomaticActivation());
	}

	public Reservation getReservation() {
		return reservation;
	}

	public void setReservation(Reservation reservation) {
		this.reservation = reservation;
	}

	public boolean isAutomaticActivation() {
		return automaticActivation;
	}

	public void setAutomaticActivation(boolean automaticActivation) {
		this.automaticActivation = automaticActivation;
	}

}
