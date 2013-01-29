// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types.msg;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

import ca.gc.iit.nrc.savoir.domain.Session;

@XmlType(name="ActivateSessionReservation")
public class ActivateSessionReservation implements Serializable{	
	
	private static final long serialVersionUID = -3365873420137036738L;
	
	private Session savoirSession;

	public Session getSession() {
		return savoirSession;
	}

	public void setSession(Session savoirSession) {
		this.savoirSession = savoirSession;
	}
	
}
