// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.session;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Parameter with temporal value. Parses as XML format.
 * 
 * @author Aaron Moss
 */
public class DateParameter extends Parameter {

	/**
	 * Subclass of {@code Date} to allow overriding of {@code toString()}
	 */
	private static class XmlDate extends Date {
		private static final long serialVersionUID = -9049176772602518545L;
		
		private String stringValue = null;
		
		public XmlDate(Date date) {
			super(date.getTime());
		}
		
		@Override
		public String toString() {
			if (stringValue == null) {
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTime(this);
				try {
					stringValue = DatatypeFactory.newInstance()
						.newXMLGregorianCalendar(cal).toXMLFormat();
				} catch (DatatypeConfigurationException ignored) {}				
			}
			return stringValue;
		}
	}
	
	/** temporal value of parameter */
	private Date dateValue;
	
	public DateParameter() {
		super();
		this.dataType = "xs:dateTime";
	}
	
	public Date getDateValue() {
		return this.dateValue;
	}
	
	/**
	 * Overrides behaviour of default setValue()
	 * If value cannot be parsed as a temporal value, nulls the dateValue.
	 * Parses dates according to the rules of 
	 * javax.xml.datatype.XMLGregorianCalendar
	 */
	@Override
	public void setValue(String value) {
		super.setValue(value);
		
		if (value == null) {
			this.dateValue = null;
			return;
		}
		
		XMLGregorianCalendar cal = null;
		try {
			cal = 
				DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(value.trim());
		} catch (Exception ignored) {
		}
		if (cal == null) {
			this.dateValue = null;
		} else {
			this.dateValue = new XmlDate(cal.toGregorianCalendar().getTime());
		}
	}
	
	public void setValue(Date dateValue) {
		this.dateValue = new XmlDate(dateValue);
		this.value = this.dateValue.toString();
	}
	
	public void setDateValue(Date dateValue) {
		this.dateValue = new XmlDate(dateValue);
		this.value = this.dateValue.toString();
	}
	
	public Parameter withValue(Date dateValue) {
		this.dateValue = new XmlDate(dateValue);
		this.value = this.dateValue.toString();
		
		return this;
	}
	
	public DateParameter clone() {
		DateParameter p = new DateParameter();
		p.setId(getId());
		p.value = value;
		p.dateValue = new XmlDate(dateValue);
		p.setName(getName());
		p.dataType = dataType;
		
		return p;
	}
}
