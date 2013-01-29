// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain;

import java.util.Set;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="Role")
public class Role {

	@XmlType(name="Right")
	public static enum Right {
		/* User Management rights */
		VIEW_MEMBERS, UPDATE_MEMBERS, MANAGE_GROUP,
		/* Credential Management rights */
		UPDATE_CREDENTIALS,
		/* Session Management rights */
		SCENARIO_RUN, AUTHORED_RUN, SCENARIO_EDIT, SESSION_EDIT, AUDIT;
	}
	
	/** The unique identifier of the role **/
	private int roleId;
	/** The name of the role **/
	private String roleName;
	/** The rights granted by this group **/
	private Set<Right> rights;
	/** A short description of the group **/
	private String description;
	
	public Role() {}
	
	public Role(int roleId, String roleName, Set<Right> rights,	String description) {
		this.roleId = roleId;
		this.roleName = roleName;
		this.rights = rights;
		this.description = description;
	}

	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public Set<Right> getRights() {
		return rights;
	}

	public void setRights(Set<Right> rights) {
		this.rights = rights;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return this.roleName;
	}
}
