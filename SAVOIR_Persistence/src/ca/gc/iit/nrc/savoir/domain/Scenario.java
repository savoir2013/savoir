// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain;

import java.util.Date;

import javax.xml.bind.annotation.XmlType;

/**
 * Represents a SAVOIR Scenario.
 * 
 * Includes methods to access the scenario's XML definition and rules (both in 
 * source and compiled format)
 * 
 * @author Aaron Moss
 */

@XmlType(name = "Scenario")
public class Scenario {

	/** Unique ID for this scenario */
	private int scenarioId;
	/** Human-readable name for this scenario */
	private String scenarioName;
	/** Time this scenario was last modified */
	private Date lastModified;
	/** Username of scenario author */
	private String authorId;
	/** Name of scenario author (human readable) */
	private String authorName;
	/** Description of scenario */
	private String description;
	
	/** URI for Scenario XML */
	private String xmlUri;
	/** path for rule files */
	private String ruleUri;
	
	/** 
	 * String representing APN parameters.
	 * 
	 * The format for this string is a a set of pipe-delimited strings, where 
	 * the first is the reservation method, and all subsequent are connections, 
	 * a connection being defined by the semicolon-delimited fields of 
	 * min_bandwidth, max_bandwidth, source, and destination.
	 */
	private String apnParameters;
	/**
	 * String listing device names.
	 * 
	 * The device names shall be derived from the resource names, and be 
	 * separated by commas and spaces
	 */
	private String deviceNames;
	
	
	public Scenario() {}
	
	
	public int getScenarioId() {
		return scenarioId;
	}
	
	public String getScenarioName() {
		return scenarioName;
	}
	
	public Date getLastModified() {
		return lastModified;
	}
	
	public String getAuthorId() {
		return authorId;
	}
	
	public String getAuthorName() {
		return authorName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getXmlUri() {
		return xmlUri;
	}
	
	public String getRuleUri() {
		return ruleUri;
	}
	
	/**
	 * @return The URI of the compiled binary of the scenario rules.
	 * 		Will be the URI of the rulefile with ".bin" appended.
	 */
	public String genRuleBinaryUri() {
		return ruleUri + ".bin";
	}
	
	public String getApnParameters() {
		return apnParameters;
	}
	
	public String getDeviceNames() {
		return deviceNames;
	}
	
	
	public void setScenarioId(int scenarioId) {
		this.scenarioId = scenarioId;
	}
	
	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}
	
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}
	
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setXmlUri(String xmlUri) {
		this.xmlUri = xmlUri;
	}
	
	public void setRuleUri(String ruleUri) {
		this.ruleUri = ruleUri;
	}
	
	public void setApnParameters(String apnParameters) {
		this.apnParameters = apnParameters;
	}
	
	public void setDeviceNames(String deviceNames) {
		this.deviceNames = deviceNames;
	}
}
