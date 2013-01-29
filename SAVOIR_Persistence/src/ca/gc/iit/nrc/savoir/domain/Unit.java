// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/*
 * UNIT
 *A unit of measure describing a parameter of a specific Edge Device
 *  @ Project : SAVOIR
 *  @ File Name : Unit.java
 *  @ Date : 21/09/2009
 *  @ Author : Bryan Copeland
 */
package ca.gc.iit.nrc.savoir.domain;

import java.util.Calendar;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="Unit")
public class Unit {	
	
	/** The unique identifier of the unit **/
	private String unitID;
	/** The Name of the unit of measure **/
	private String unitName;
	/** The unit's symbol (scientific abbreviation) **/
	private String unitSymbol;
	/** The unit to convert FROM **/
	private String unitFrom;
	/** The unit to convert TO **/
	private String unitTo;
	
	
	public Unit(){}
	
	public String getUnitID() {
		return unitID;
	}
	public String getUnitName() {
		return unitName;
	}
	public String getUnitSymbol() {
		return unitSymbol;
	}
	public String getUnitFrom() {
		return unitFrom;
	}
	public String getUnitTo() {
		return unitTo;
	}
	public void setUnitID(String unitID) {
		this.unitID = unitID;
	}
	public void setUnitName(String name) {
		unitName = name;
	}
	public void setUnitSymbol(String symbol) {
		this.unitSymbol = symbol;
	}
	public void setUnitFrom(String unitFrom) {
		this.unitFrom = unitFrom;
	}
	public void setUnitTo(String unitTo) {
		this.unitTo = unitTo;
	}
}
