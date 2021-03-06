// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : SAVOIR
//  @ File Name : Person.java
//  @ Date : 21/10/2008
//  @ Author : Yosri Harzallah
//
//
package ca.gc.iit.nrc.savoir.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="Person")
public class Person {

	/** personID **/
	private int personId;
	/** details on the person **/
	private PersonInfo personInfo;
	/** list of time slots where the person is unavailable **/
	private List<TimeSlot> busyTimeSlots;

	public Person(){
		
	}
	
	public int getPersonId() {
		return personId;
	}

	public PersonInfo getPersonInfo() {
		return personInfo;
	}

	public List<TimeSlot> getBusyTimeSlots() {
		return busyTimeSlots;
	}

	public void setPersonId(int personId) {
		this.personId = personId;
	}

	public void setPersonInfo(PersonInfo personInfo) {
		this.personInfo = personInfo;
	}

	public void setBusyTimeSlots(List<TimeSlot> busyTimeSlots) {
		this.busyTimeSlots = busyTimeSlots;
	}

	public String toString(){
		return this.getPersonInfo().getFName() + ", " + this.getPersonInfo().getLName();
	}
	
}
