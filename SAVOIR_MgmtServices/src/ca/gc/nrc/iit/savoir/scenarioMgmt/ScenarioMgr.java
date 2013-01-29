// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;

import ca.gc.iit.nrc.savoir.domain.Scenario;
import ca.gc.nrc.iit.savoir.model.session.Parameter;

/**
 * SAVOIR Management Service responsible for storing, creating, and running 
 * scenarios.
 * A scenario is the template for an authored session - it includes a set of 
 * resources, and rules to orchestrate their interactions.
 * 
 * @author Aaron Moss
 */
@WebService
public interface ScenarioMgr {

	/**
	 * Load into memory a knowledge base corresponding to this session
	 * 
	 * @param sessionId		The session ID for which to load the scenario
	 * @param userName		The user loading this scenario
	 * 
	 * @return	true for scenario exists for this session, 
	 * 			false for session unauthored
	 */
	public boolean loadScenario(@WebParam(name = "sessionId") int sessionId,
			@WebParam(name = "userName") String userName);
	
	/**
	 * Starts execution of a scenario
	 * 
	 * @param sessionId		The session ID corresponding to the scenario to run
	 */
	public void startScenario(@WebParam(name = "sessionId") int sessionId);
	
	/**
	 * Stops execution of a scenario
	 * 
	 * @param sessionId		The session ID corresponding to the scenario to stop
	 */
	public void endScenario(@WebParam(name = "sessionId") int sessionId);
	
	/**
	 * Call to set/update the state of a resource instance in the rule engine
	 * 
	 * @param sessionId		The session ID
	 * @param serviceId		The service ID
	 * @param activityId	The ID of the activity active on the service
	 * @param parameters	The new parameter values
	 */
	public void enterResource(@WebParam(name = "sessionId") int sessionId, 
			@WebParam(name = "serviceId") String serviceId, 
			@WebParam(name = "serviceName") String serviceName,
			@WebParam(name = "activityId") String activityId,
			@WebParam(name = "parameters") List<Parameter> parameters);
	
	
	/**
	 * @return a list of all available scenarios
	 */
	public List<Scenario> getScenarios();
	
	/**
	 * Gets scenarios by scenario ID
	 * 
	 * @param scenarioIds	The IDs of the scenarios to get
	 * 
	 * @return a list of scenarios, in the same order as the provided IDs
	 */
	public List<Scenario> getScenariosByIds(List<Integer> scenarioIds);
	
	/**
	 * Gets the scenarios that the given user can remove. The scenarios must 
	 * not have any authored sessions based off them, and the caller must be 
	 * either the scenario author or a sysadmin.
	 * 
	 * @param userName		The username of the calling user
	 * 
	 * @return a list of scenarios the calling user can remove, empty for none 
	 * 			such, null for error
	 */
	public List<Scenario> getRemovableScenarios(String userName);
	
	/**
	 * Submit a new scenario.
	 * 
	 * @param xml	The XML file representing the scenario
	 * 
	 * @return the completed scenario (null if uncompleted) and a list of 
	 * 			warnings, in human-readable format. The list will be empty on 
	 * 			successful completion, though a non-empty warning list does not 
	 * 			imply an unsuccessfully submitted scenario.
	 */
	public ScenarioCompilerOutput submitScenario(String xml);

	/**
	 * Removes a scenario. Will also delete scenario and rule files associated 
	 * with the scenario. If any authored sessions exist that use this 
	 * scenario, scenario removal will fail.
	 * 
	 * @param scenarioId	The ID of the scenario
	 * @param userName		The caller of this method - must be a sysadmin or 
	 * 						the author of this scenario to remove this scenario.
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for no scenario with this ID exists
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for {@code scenarioId} less than or equal to {@code 0}
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#PRECONDITION_ERROR}
	 * 				for sessions exist using this scenario
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#FILE_IO_ERROR}
	 * 				for some scenario file could not be deleted from the 
	 * 				repositories
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int removeScenario(int scenarioId, String userName);
}
