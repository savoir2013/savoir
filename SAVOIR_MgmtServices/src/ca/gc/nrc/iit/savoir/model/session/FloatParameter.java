// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.session;

/**
 * Parameter with floating-point value
 * 
 * @author Aaron Moss
 */
public class FloatParameter extends Parameter {
	
	/** floating-point value of parameter */
	private Double floatValue;
	
	public FloatParameter() {
		super();
		this.dataType = "xs:decimal";
	}
	
	public Double getFloatValue() {
		return this.floatValue;
	}
	
	/**
	 * Overrides behaviour of default setValue()
	 * If value cannot be parsed as floating-point, nulls the floatValue
	 */
	@Override
	public void setValue(String value) {
		super.setValue(value);
		try {
			this.floatValue = Double.parseDouble(value);
		} catch (NumberFormatException e) {
			this.floatValue = null;
		} catch (NullPointerException e) {
			this.floatValue = null;
		}
	}
	
	public void setValue(double floatValue) {
		this.floatValue = floatValue;
		this.value = Double.toString(floatValue);
	}
	
	public void setFloatValue(double floatValue) {
		this.floatValue = floatValue;
		this.value = Double.toString(floatValue);
	}
	
	public Parameter withValue(double floatValue) {
		this.floatValue = floatValue;
		this.value = Double.toString(floatValue);
		
		return this;
	}
	
	public FloatParameter clone() {
		FloatParameter p = new FloatParameter();
		p.setId(getId());
		p.value = value;
		p.floatValue = floatValue;
		p.setName(getName());
		p.dataType = dataType;
		
		return p;
	}
}
