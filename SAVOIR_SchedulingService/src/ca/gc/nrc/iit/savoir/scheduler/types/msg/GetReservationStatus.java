// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types.msg;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

import ca.gc.iit.nrc.savoir.domain.Session;


@XmlType(name = "GetReservationStatus")
public class GetReservationStatus implements Serializable {

	private static final long serialVersionUID = 4788069144834081035L;

	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

}
