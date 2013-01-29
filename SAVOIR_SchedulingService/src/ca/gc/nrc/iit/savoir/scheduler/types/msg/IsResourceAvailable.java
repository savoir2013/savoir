// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types.msg;

import ca.gc.iit.nrc.savoir.domain.Session;

public class IsResourceAvailable {
	
	private Session sessions;
	
	private long offset = 0;
	
	public IsResourceAvailable() {		
	}

	public Session getSession() {
		return sessions;
	}

	public void setSession(Session session) {
		this.sessions = session;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

}
