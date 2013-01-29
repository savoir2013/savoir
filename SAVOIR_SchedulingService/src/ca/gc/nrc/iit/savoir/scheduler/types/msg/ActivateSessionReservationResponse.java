// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types.msg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

/**
 * @author harzallahy
 *
 */
@XmlType(name="ActivateSessionReservationResponse")
public class ActivateSessionReservationResponse implements Serializable {

	private static final long serialVersionUID = 109412101573273267L;
	/** true if everything has been activated, false if not**/
	private boolean successful;
	/** sessionIDs of the subsessions that couldn't be activated**/
	private List<Integer> failingSessions;

	private String status;

	public boolean isSuccessful() {
		return successful;
	}

	public String getStatus() {
		return status;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public final List<Integer> getFailingSessions() {
		if (failingSessions == null)
			failingSessions = new ArrayList<Integer>();
		return failingSessions;

	}

	public final void setFailingSessions(List<Integer> failingSessions) {
		this.failingSessions = failingSessions;
	}

}
