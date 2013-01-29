// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.session;

/**
 * Parameter with integer value
 * 
 * @author Aaron Moss
 */
public class IntParameter extends Parameter {
	
	/** integer value of parameter */
	private Long intValue;
	
	public IntParameter() {
		super();
		this.dataType = "xs:integer";
	}
	
	public Long getIntValue() {
		return this.intValue;
	}
	
	/**
	 * Overrides behaviour of default setValue()
	 * If value cannot be parsed as an integer, nulls the intValue
	 */
	@Override
	public void setValue(String value) {
		super.setValue(value);
		try {
			this.intValue = Long.parseLong(value);
		} catch(NumberFormatException e) {
			this.intValue = null;
		}
	}
	
	public void setValue(long intValue) {
		this.intValue = intValue;
		this.value = Long.toString(intValue);
	}
	
	public void setIntValue(long intValue) {
		this.intValue = intValue;
		this.value = Long.toString(intValue);
	}
	
	public Parameter withValue(long intValue) {
		this.intValue = intValue;
		this.value = Long.toString(intValue);
		
		return this;
	}
	
	public IntParameter clone() {
		IntParameter p = new IntParameter();
		p.setId(getId());
		p.value = value;
		p.intValue = intValue;
		p.setName(getName());
		p.dataType = dataType;
		
		return p;
	}
}
