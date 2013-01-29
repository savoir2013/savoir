// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import ca.gc.iit.nrc.savoir.domain.types.ParameterType;

@XmlType(name="CredentialSchema")
public class CredentialSchema {
	
	/** Unique ID for this schema */
	private int schemaId;
	/** Parameters in this schema */
	private List<CredentialSchemaParameter> params;
	/** Description of this credential schema */
	private String description;
	
	public CredentialSchema() {}

	public int getSchemaId() {
		return schemaId;
	}

	public void setSchemaId(int schemaId) {
		this.schemaId = schemaId;
	}

	public List<CredentialSchemaParameter> getParams() {
		return params;
	}
	
	public List<ParameterType> getRequiredParams() {
		List<ParameterType> required = new ArrayList<ParameterType>();
		
		for (CredentialSchemaParameter p : params) {
			if (p.isRequired()) {
				required.add(p.getParameterType());
			}
		}
		
		return required;
	}

	public void setParams(List<CredentialSchemaParameter> params) {
		this.params = params;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
