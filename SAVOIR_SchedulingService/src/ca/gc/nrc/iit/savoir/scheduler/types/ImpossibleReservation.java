// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="ImpossibleReservation")
public class ImpossibleReservation extends SchedulingConflict {

	private static final long serialVersionUID = 6148826935385684898L;
	
	private String text;	
	
	public ImpossibleReservation(){
		
	}
	
	public ImpossibleReservation(String message) {
		this.text = message;
	}

	public String toString(){
		return text;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
}
