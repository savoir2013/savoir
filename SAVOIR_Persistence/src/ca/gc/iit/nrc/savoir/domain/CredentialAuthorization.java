// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="CredentialAuthorization")
public class CredentialAuthorization {
	
	@XmlType(name="CredentialAuthorizationRight")
	public static enum CredentialAuthorizationRight {
		VIEW, UPDATE, DELETE, GRANT_VIEW, GRANT_UPDATE, GRANT_DELETE;
	}
	
	private int resourceId;
	private int userId;
	private int groupId;
	private Date beginTime;
	private Date endTime;
	private List<CredentialAuthorizationRight> rights;
	
	public CredentialAuthorization() {}

	public int getResourceId() {
		return resourceId;
	}

	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public List<CredentialAuthorizationRight> getRights() {
		return rights;
	}

	public void setRights(List<CredentialAuthorizationRight> rights) {
		this.rights = rights;
	}
}
