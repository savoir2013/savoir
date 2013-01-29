// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.session;

/**
 * Java representation of the available actions for a SAVOIR message.
 * Note that "launch" and "notify" are not in the SAVOIR message spec, but are 
 * used by SAVOIR in communication with the user's system tray client.
 * 
 * @see The SAVOIR message specification for semantic descriptions of these actions.
 */
public enum Action {
	GET_PROFILE("getProfile"),
	LAUNCH("launch"),
	NOTIFY("notify"),
	SUBSCRIBE("subscribe"),
	ACKNOWLEDGE("acknowledge"),
	AUTHENTICATE("authenticate"),
	END_SESSION("endSession"),
	GET_STATUS("getStatus"),
	LOAD("load"),
	PAUSE("pause"),
	REPORT_STATUS("reportStatus"),
	RESUME("resume"),
	SET_PARAMETER("setParameter"),
	START("start"),
	START_RESPONSE("startResponse"),
	STOP("stop");
	
	private String xml;
	
	private Action(String xml) {
		this.xml = xml;
	}
	
	public String toString() {
		return this.xml;
	}
}
