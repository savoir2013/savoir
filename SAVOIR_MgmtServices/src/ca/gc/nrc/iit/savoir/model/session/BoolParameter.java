// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.session;

/**
 * Paramter with boolean value
 * 
 * @author Aaron Moss
 */
public class BoolParameter extends Parameter {

	/** Boolean value of parameter */
	private Boolean boolValue;
	
	public BoolParameter() {
		super();
		this.dataType = "xs:boolean";
	}
	
	public Boolean getBoolValue() {
		return this.boolValue;
	}
	
	/**
	 * Overrides behaviour of default setValue()
	 * If value cannot be parsed as a boolean, nulls the booleanValue.
	 * Parses "true" and "1" as true, "false" and "0" as false.
	 */
	@Override
	public void setValue(String value) {
		super.setValue(value);
		if ("true".equals(value) || "1".equals(value)) 
			this.boolValue = true;
		else if ("false".equals(value) || "0".equals(value)) 
			this.boolValue = false;
		else 
			this.boolValue = null;
	}
	
	public void setValue(boolean boolValue) {
		this.boolValue = boolValue;
		this.value = boolValue ? "true" : "false";
	}
	
	public void setBoolValue(boolean boolValue) {
		this.boolValue = boolValue;
		this.value = boolValue ? "true" : "false";
	}
	
	public Parameter withValue(boolean boolValue) {
		this.boolValue = boolValue;
		this.value = boolValue ? "true" : "false";
		
		return this;
	}
	
	public BoolParameter clone() {
		BoolParameter p = new BoolParameter();
		p.setId(getId());
		p.value = value;
		p.boolValue = boolValue;
		p.setName(getName());
		p.dataType = dataType;
		
		return p;
	}
}
