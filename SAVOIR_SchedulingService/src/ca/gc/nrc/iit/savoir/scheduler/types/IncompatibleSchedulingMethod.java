// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="IncompatibleSchedulingMethod")
public class IncompatibleSchedulingMethod extends SchedulingConflict {

	private static final long serialVersionUID = 5711607884194470821L;
	
	private String message;
	
	public IncompatibleSchedulingMethod(){
		
	}
	
	public IncompatibleSchedulingMethod(String message) {
		this.message = message;
	}

	public String toString(){
		return message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
