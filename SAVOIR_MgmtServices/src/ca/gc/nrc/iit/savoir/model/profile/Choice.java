// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a choice element of a SAVOIR profile message.
 * 
 * @author Aaron Moss
 */
public class Choice {

	/**
	 * Java representation of available values for the choice type attribute of 
	 * a SAVOIR profile message.
	 */ 
	public static enum ChoiceType {
		ACTIVITY("activity"),
		SINGLE("single"),
		MULTIPLE("multiple"),
		USER_ENTERED("userEntered");
		
		private String xml;
		
		private ChoiceType(String xml) {
			this.xml = xml;
		}
		
		public String toString() {
			return this.xml;
		}
	}
	
	/** Unique ID of this choice element */
	private String id;
	/** Label for this choice */
	private String label;
	/** Type of this choice */
	private ChoiceType type;
	/** Parameter ID for the options of this choice */
	private String paramId;
	/** Available options for this choice */
	private List<Option> options;
	
	
	public Choice() {}


	//Java bean API for Choice
	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}
	
	public ChoiceType getType() {
		return type;
	}

	public String getParamId() {
		return paramId;
	}
	
	public List<Option> getOptions() { 
		return options;
	}

	
	public void setId(String id) {
		this.id = id;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public void setType(ChoiceType type) {
		this.type = type;
	}

	public void setParamId(String paramId) {
		this.paramId = paramId;
	}
	
	public void setOptions(Collection<? extends Option> options) {
		if (options == null) this.options = null;
		else this.options = new ArrayList<Option>(options);
	}
	
	
	//Fluent API for Choice
	public Choice withId(String id) {
		this.id = id;
		return this;
	}

	public Choice withLabel(String label) {
		this.label = label;
		return this;
	}
	
	public Choice withType(ChoiceType type) {
		this.type = type;
		return this;
	}

	public Choice withParamId(String paramId) {
		this.paramId = paramId;
		return this;
	}
	
	public Choice addOption(Option option) {
		if (this.options == null) {
			this.options = new ArrayList<Option>();
		}
		
		this.options.add(option);
		
		return this;
	}
}
