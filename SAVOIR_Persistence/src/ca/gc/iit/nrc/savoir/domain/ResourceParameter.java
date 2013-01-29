// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : SAVOIR
//  @ File Name : ResourceParameter.java
//  @ Date : 21/10/2008
//  @ Author : Yosri Harzallah
//
//
package ca.gc.iit.nrc.savoir.domain;

import javax.xml.bind.annotation.XmlType;

import ca.gc.iit.nrc.savoir.domain.types.ParameterType;

@XmlType(name="ResourceParameter")
public class ResourceParameter {

	/** ID **/ 
	private String resourceParameterID;
	/** Paramter defintion **/
	private ParameterType parameter;
	/** Paramter Value **/
	private String value;

//	UNIT (null if no value)
    private Unit unit;
    
    
	public ResourceParameter(){
		
	}
	
	public String getResourceParameterID() {
		return resourceParameterID;
	}
	public ParameterType getParameter() {
		return parameter;
	}
	public String getValue() {
		return value;
	}
	public void setResourceParameterID(String resourceParameterID) {
		this.resourceParameterID = resourceParameterID;
	}
	public void setParameter(ParameterType parameter) {
		this.parameter = parameter;
	}
	public void setValue(String value) {
		this.value = value;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public Unit getUnit() {
		return unit;
	}	
	
}