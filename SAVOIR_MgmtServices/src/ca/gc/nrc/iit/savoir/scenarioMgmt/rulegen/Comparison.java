// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;

/**
 * Operators for rule when-condition comparisons.
 * 
 * @author Aaron Moss
 */
public enum Comparison {
	/** Equal */ EQ("=="),
	/** Non-equal */ NE("!="),
	/** Strictly lesser */ LT("<"),
	/** Lesser or equal*/ LE("<="),
	/** Strictly greater */ GT(">"),
	/** Greater or equal */ GE(">=");
	
	private String symbol;
	
	private Comparison(String symbol) {
		this.symbol = symbol;
	}
	
	public String toString() {
		return this.symbol;
	}
}
