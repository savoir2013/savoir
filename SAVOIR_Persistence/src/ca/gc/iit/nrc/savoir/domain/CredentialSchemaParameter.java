// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain;

import javax.xml.bind.annotation.XmlType;

import ca.gc.iit.nrc.savoir.domain.types.ParameterType;

@XmlType(name="CredentialSchemaParameter")
public class CredentialSchemaParameter {
	
	/** Parameter definition **/
	private ParameterType parameterType;
	/** Is this parameter required, or optional? */
	private boolean required;
	/** May there be multiple of this parameter, or only a single instance? */
	private boolean allowMultiple;
	
	public CredentialSchemaParameter() {}

	public ParameterType getParameterType() {
		return parameterType;
	}

	public void setParameterType(ParameterType parameterType) {
		this.parameterType = parameterType;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isAllowMultiple() {
		return allowMultiple;
	}

	public void setAllowMultiple(boolean allowMultiple) {
		this.allowMultiple = allowMultiple;
	}
}
