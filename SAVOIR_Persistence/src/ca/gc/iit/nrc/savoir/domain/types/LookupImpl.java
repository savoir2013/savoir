// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University


package ca.gc.iit.nrc.savoir.domain.types;

public class LookupImpl implements Lookupable {

	private String id;
	private String name;
	private String description;

	public LookupImpl(String id, String name, String description) {
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

}
