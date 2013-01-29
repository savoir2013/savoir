// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ExceptionCaught")
public class ExceptionCaught extends SchedulingConflict {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2046432196092865728L;

	private String exceptionMessage;
	
	private String message;

	public ExceptionCaught(){
		
	}
	
	public ExceptionCaught(String exceptionMessage, String message) {		
		this.exceptionMessage = exceptionMessage;
		this.message = message;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}

	public String getMessage() {
		return message;
	}

	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String toString(){
		return message + " " + exceptionMessage;
	}
	
}
