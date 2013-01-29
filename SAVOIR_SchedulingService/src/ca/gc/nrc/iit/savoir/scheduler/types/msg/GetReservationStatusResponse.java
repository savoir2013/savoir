// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types.msg;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "GetReservationStatusResponse")
public class GetReservationStatusResponse implements Serializable {

	private static final long serialVersionUID = 3722304595607221365L;

	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
