// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain;

import javax.xml.bind.annotation.XmlType;

import ca.gc.iit.nrc.savoir.domain.types.ParameterType;

@XmlType(name="CredentialParameter")
public class CredentialParameter {
	
	/** Parameter definition **/
	private ParameterType parameter;
	/** Parameter Value **/
	private String value;

	public CredentialParameter() {}

	
	public ParameterType getParameter() {
		return parameter;
	}

	public void setParameter(ParameterType parameter) {
		this.parameter = parameter;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
