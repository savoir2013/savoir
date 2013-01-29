// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.resourceMgmt;

/**
 * Represents the current state of an activity on an Edge Device, within a 
 * given session.
 * 
 * @author Aaron Moss
 */
public enum ResourceState {
	/** No active communication between activity on ED and SAVOIR */
	INACTIVE,
	/** SAVOIR user successfully authenticted, ED not yet loaded */
	AUTHENTICATED,
	/** ED loaded, but not yet started */
	LOADED,
	/** ED started, and not paused */
	RUNNING,
	/** ED started, but paused */
	PAUSED,
	/** Stop message sent to ED, but not yet acknowledged */
	SENT_STOP,
	/** ED stopped, but session not yet ended */
	STOPPED;
}
