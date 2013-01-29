// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="Group")
public class Group implements Comparable<Group> {
	
	/** The unique identifier of the group **/
	private int groupId;
	/** The name of the group **/
	private String groupName;
	/** A short description of the group **/
	private String description;
	
	public Group() {}
	
	public Group(int groupIdIn, String groupNameIn, String descriptionIn) {
		this.groupId = groupIdIn;
		this.groupName = groupNameIn;
		this.description = descriptionIn;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Implements Comparable
	 * @return	the string comparison of the two objects, first by group name, then group ID
	 * 			(all of which should be equal for the same group)
	 */
	@Override
	public int compareTo(Group o) {
		int compare = this.groupName.compareTo(o.groupName);
		if (compare == 0) {
			compare = this.groupId - o.groupId;
		}
		return compare;
	}
	
	@Override
	public String toString() {
		return this.groupName;
	}
}
