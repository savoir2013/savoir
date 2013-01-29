// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.parser;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Logging error handler for SAX API.
 * Logs all non-fatal errors to warning list, but does not throw an exception.
 * 
 * @author Aaron Moss
 */
public class LenientErrorHandler implements ErrorHandler {

	/** Warnings found by this error handler */
	private List<String> warnings;
	

	/**
	 * Sets up a new LenientErrorHandler with a new warning list.
	 * The list can be accessed by calling {@link #getWarnings()}.
	 */
	public LenientErrorHandler() {
		this.warnings = new ArrayList<String>();
	}
	
	/**
	 * Sets up a new LeninentErrorHandler with the specified warning list
	 * @param warnings		The list to append warnings to. Should be set up 
	 * 						by caller.
	 */
	public LenientErrorHandler(List<String> warnings) {
		this.warnings = warnings;
	}
	
	
	public List<String> getWarnings() {
		return this.warnings;
	}
	
	
	@Override
	public void warning(SAXParseException e) {
		if (warnings != null) warnings.add(e.getMessage());
	}
	
	@Override
	public void error(SAXParseException e) {
		if (warnings != null) warnings.add(e.getMessage());
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		throw e;
	}

}
