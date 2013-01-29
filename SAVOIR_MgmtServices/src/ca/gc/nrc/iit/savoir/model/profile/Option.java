// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.profile;

/**
 * Represents the option element of an SAVOIR profile message. Contains a single 
 * user-selectable option.
 * 
 * @author Aaron Moss
 */
public class Option {

	/** The name displayed to the user for this option */
	private String name;
	/** The parameter ID of this option (optional) */
	private String paramId;
	/** The parameter value associated with this option */
	private String paramValue;
	
	
	public Option() {}


	//Java bean API for Option
	public String getName() {
		return name;
	}

	public String getParamId() {
		return paramId;
	}

	public String getParamValue() {
		return paramValue;
	}


	public void setName(String name) {
		this.name = name;
	}

	public void setParamId(String paramId) {
		this.paramId = paramId;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}
	
	
	//Fluent API for Option
	public Option withName(String name) {
		this.name = name;
		return this;
	}

	public Option withParamId(String paramId) {
		this.paramId = paramId;
		return this;
	}

	public Option withParamValue(String paramValue) {
		this.paramValue = paramValue;
		return this;
	}
	
}
