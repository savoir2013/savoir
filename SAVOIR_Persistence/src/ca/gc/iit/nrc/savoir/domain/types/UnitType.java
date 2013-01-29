// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain.types;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="UnitType")
public class UnitType {

	private String id;
	private String name;
	private String description;

	public UnitType(){}
	
	public UnitType(String id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
