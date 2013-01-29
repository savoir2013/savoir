// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="GroupNode")
public class GroupNode implements Comparable<GroupNode> {
	/** Information pertaining to this group **/
	private Group group;
	/** Subgroups of this group **/
	private List<GroupNode> subgroups;
	/** Members of this group **/
	private List<UserIDName> members;
	
	public GroupNode() {
		subgroups = new ArrayList<GroupNode>();
		members = new ArrayList<UserIDName>();
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public List<GroupNode> getSubgroups() {
		return subgroups;
	}

	public void setSubgroups(List<GroupNode> subgroups) {
		this.subgroups = subgroups == null ? new ArrayList<GroupNode>() : subgroups;
	}

	public List<UserIDName> getMembers() {
		return members;
	}

	public void setMembers(List<UserIDName> members) {
		this.members = members == null ? new ArrayList<UserIDName>() : members;
	}

	@Override
	public int compareTo(GroupNode o) {
		return this.group.compareTo(o.group);
	}
}
