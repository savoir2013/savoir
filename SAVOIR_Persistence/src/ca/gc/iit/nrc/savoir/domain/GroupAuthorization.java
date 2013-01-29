// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="GroupAuthorization")
public class GroupAuthorization extends Authorization {
	
	/** does this authorization belong to a member or subgroup? */
	private boolean isContained;
	
	public GroupAuthorization() {}

	public boolean isContained() {
		return isContained;
	}

	public void setContained(boolean isContained) {
		this.isContained = isContained;
	}
}
