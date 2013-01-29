// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types;

public enum SessionReservationType {

	immediate_reservation("Immediate Reservation"),

	advance_online_reservation("Advance Online Reservation"),

	advance_offline_reservation("Advance Offline Reservation");

	private final String type;

	SessionReservationType(String type) {
		this.type = type;
	}

	public String type() {
		return type;
	}

}
