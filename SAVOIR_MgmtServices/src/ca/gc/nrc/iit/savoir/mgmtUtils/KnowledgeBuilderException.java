// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.mgmtUtils;

import java.util.ArrayList;
import java.util.List;

import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;

/**
 * A wrapper around the error output of the Drools compiler.
 * 
 * @author Aaron Moss
 */
public class KnowledgeBuilderException extends Exception {
	private static final long serialVersionUID = 1L;

	private KnowledgeBuilderErrors errors;
	
	/**
	 * Wraps the error output of the Drools compiler in an exception
	 * @param errors	The errors returned from 
	 * 					{@code KnowledgeBuilder.getErrors()}
	 */
	public KnowledgeBuilderException(KnowledgeBuilderErrors errors) {
		this.errors = errors;
	}

	/**
	 * Returns the error messages
	 */
	@Override
	public String getMessage() {
		return errors.toString();
	}
	
	/**
	 * Returns the error messages
	 */
	@Override
	public String toString() {
		return errors.toString();
	}
	
	/**
	 * Gets the KnowledgeBuilderErrors this exception wraps
	 * @return a list of KnowledgeBuilderError wrapped by this exception.
	 */
	public List<KnowledgeBuilderError> getErrors() {
		return new ArrayList<KnowledgeBuilderError>(errors);
	}
}
