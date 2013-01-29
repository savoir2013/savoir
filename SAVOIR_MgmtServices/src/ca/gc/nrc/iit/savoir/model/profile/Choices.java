// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents the choices element of a SAVOIR service profile message. This 
 * element is used to present the user options in the UI. 
 * 
 * @author Aaron Moss
 */
public class Choices implements Iterable<Choice> {

	/** base URI of this choices element */
	private String baseUri;
	/** list of choices, in the order given */
	private List<Choice> choices;
	/** Map of choices, indexed by id (should be null exactly when 
	 *  {@code choices} is) */
	private Map<String, Choice> choiceIds;
	
	
	public Choices() {}


	//Java bean API for Choices
	public String getBaseUri() {
		return baseUri;
	}

	public List<Choice> getChoices() {
		return choices;
	}
	
	public Choice getChoice(String id) {
		if (choiceIds == null) return null;
		return choiceIds.get(id);
	}
	
	public Choice getChoice(int index) {
		if (choices == null) return null;
		try {
			return choices.get(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}


	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public void setChoices(List<Choice> choices) {
		this.choices = choices;
		if (choices != null) {
			choiceIds = new HashMap<String, Choice>();
			
			for (Choice choice : choices) {
				String id = choice.getId();
				if (id != null) choiceIds.put(id, choice);
			}
		} else {
			choiceIds = null;
		}
	}
	
	
	//Fluent API for Choices
	public Choices withBaseUri(String baseUri) {
		this.baseUri = baseUri;
		return this;
	}
	
	public Choices addChoice(Choice choice) {
		if (this.choices == null) {
			this.choices = new ArrayList<Choice>();
			this.choiceIds = new HashMap<String, Choice>();
		}
		
		this.choices.add(choice);
		String id = choice.getId();
		if (id != null) this.choiceIds.put(id, choice);
		
		return this;
	}


	@Override
	public Iterator<Choice> iterator() {
		if (choices == null) return null;
		else return choices.iterator();
	}
}
