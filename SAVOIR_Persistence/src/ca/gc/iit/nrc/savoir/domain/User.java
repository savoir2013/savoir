// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain;

import java.util.Date;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="User")
public class User {	
	
	/** The unique identifier of the user **/
	private int userID;
	/** The Distinguished Name (login ID) of the user **/
	private String dName;
	/** The user's password XXX remove this from the bean? **/
	private String password;
	/** The time the user first becomes valid **/
	private Date beginTime;
	/** The time that this user's validity expires **/
	private Date endTime;
	/** A use is a person too **/
	private Person person;
	/** The user's site ID **/
	private int siteId;
	/** The user's system role */
	private Role role;
	
	
	public User(){}
	
	public User(int userIDIn, String dNameIn, String passwordIn, 
			Date beginTimeIn, Date endTimeIn, int siteIdIn, Role roleIn) {
		this.userID = userIDIn;
		this.dName = dNameIn;
		this.password = passwordIn;
		this.beginTime = beginTimeIn;
		this.endTime = endTimeIn;
		this.siteId = siteIdIn;
		this.role = roleIn;
	}
	
	public int getUserID() {
		return userID;
	}
	public String getDName() {
		return dName;
	}
	public String getPassword() {
		return password;
	}
	public Date getBeginTime() {
		return beginTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public int getSiteId() {
		return siteId;
	}
	public Role getRole() {
		return role;
	}
	public void setUserID(int userID) {
		this.userID = userID;
	}
	public void setDName(String name) {
		dName = name;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public Person getPerson() {
		return person;
	}
	public void setPerson(Person person) {
		this.person = person;
	}
	public void setSiteId(int siteId) {
		this.siteId = siteId;
	}
	public void setRole(Role role) {
		this.role = role;
	}
}
