// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao;

import java.util.List;

import ca.gc.iit.nrc.savoir.domain.Scenario;

public interface IScenarioDAO {
	/**
	 * Retrieves a Scenario by it's scenario ID
	 * 
	 * @param scenarioId	the ID of the scenario to retrieve
	 * 
	 * @return the scenario corresponding to that ID, null for none such
	 */
	public Scenario getScenarioById(int scenarioId);
	
	/**
	 * Gets the scenario for a given session
	 * @param sessionId		The session's ID
	 * @return the scenario for that session, null for none such
	 */
	public Scenario getScenarioBySessionId(int sessionId);
	
	/**
	 * @return	A list of all the scenario records in the database
	 */
	public List<Scenario> getAllScenarios();
	
	/**
	 * Retrieves a list of scenarios by their scenario IDs
	 * 
	 * @param scenarioIds	The IDs of the scenarios in question
	 * 
	 * @return a list of those scenarios
	 */
	public List<Scenario> getScenariosByIds(List<Integer> scenarioIds);
	
	/**
	 * Gets a list of scenarios that could possibly be removed. A scenario can 
	 * be removed if it is not used in any session.
	 * 
	 * @return	a list of scenarios which no session references
	 */
	public List<Scenario> getRemovableScenarios();
	
	/**
	 * Creates a new Scenario.
	 * Note that the task of writing the XML file, rule file, and compiled rule 
	 * binary is left to the caller, and will not be performed by this method.
	 * 
	 * @param scn		The scenario to store
	 */
	public void newScenario(Scenario scn);
	
	/**
	 * Updates an existing Scenario.
	 * 
	 * @param newScenario	The scenario object containing the fields to 
	 * 						change. Any field left null will be unchanged. 
	 * 						{@code scenarioId} must not be null (and cannot be 
	 * 						changed), while changes to {@code authorName} will 
	 * 						be ignored, though the author can be changed by 
	 * 						setting {@code authorId} to a new value (the empty 
	 * 						string will null the author reference)
	 */
	public void updateScenario(Scenario newScenario);
	
	/**
	 * Removes an existing Scenario.
	 * @param scenarioId	The ID of the scenario to remove
	 */
	public void removeScenario(int scenarioId);
	
	/**
	 * @return the greatest scenario ID currently in the database
	 */
	public int maxScenarioId();
}
