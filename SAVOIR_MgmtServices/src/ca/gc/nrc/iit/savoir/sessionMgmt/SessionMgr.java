// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.sessionMgmt;

import java.util.Date;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import ca.gc.iit.nrc.savoir.domain.Session;
import ca.gc.iit.nrc.savoir.domain.Person;
import ca.gc.iit.nrc.savoir.domain.SessionAuthorization;
import ca.gc.iit.nrc.savoir.domain.User;
import ca.gc.nrc.iit.savoir.model.session.Parameter;

/**
 * Manages SAVOIR sessions.
 * There are two sorts of sessions, authored and unauthored. Authored sessions 
 * include a choreographed interaction of resources (a scenario) - managing 
 * this scenario is delegated to the 
 * {@linkplain ca.gc.nrc.iit.savoir.scenarioMgmt.ScenarioMgr Scenario Manager}. 
 * Also managed by the session manager is network reservations (delegated to 
 * the scheduling service), and triggering the loading and unloading of 
 * resources included in a session (delegated to the 
 * {@linkplain ca.gc.nrc.iit.savoir.resourceMgmt.ResourceMgr Resource Manager}).
 */
@WebService
public interface SessionMgr {

	/**
	 * TODO ???
	 */
	public boolean activateSession(@WebParam(name = "sessionID") int sessionID);

	/**
	 * Creates a new session, as a subsession of another session.
	 * 
	 * @param sessionID		The session ID of the parent session
	 * @param subSession	The information for the new subsession
	 * 
	 * @return the session ID of the new session
	 */
	public int createSubSession(@WebParam(name = "sessionID") int sessionID,
			@WebParam(name = "subSession") Session subSession);

	/**
	 * Creates a new default session for a user.
	 * 
	 * @param endTime		The time when this session should end
	 * @param userName		The username of the user to create the session for
	 * 
	 * @return a reference to the new session
	 */
	@WebResult(name = "Session")
	public Session newSessionAgnostic(
			@WebParam(name = "endTime") Date endTime, 
			@WebParam(name = "userName") String userName);
	
	/**
	 * Ends the user's default session
	 * 
	 * @param userName		The username of the user to end the session for
	 */
	public void endSessionAgnostic(
			@WebParam(name = "userName") String userName);
	
//	/*
//	 * added by yyh for demo authored session
//	 */
//	/**
//	 * TODO ??? -aaron: Not sure how this differs from newSessionAgnostic()
//	 * 					besides the inclusion of a session name and scheduling.
//	 * 					{@link #newSessionAuthored} should perhaps be used if 
//	 * 					an authored session is required (it could be modified 
//	 * 					to add the scheduling fields, as it's not currently in 
//	 * 					use) 
//	 */
//	@WebResult(name = "Session")
//	public Session getNewAuthoredSessionAgnostic(
//			@WebParam(name = "userName") String userName,
//			@WebParam(name = "sessionName") String sessionName,
//			@WebParam(name = "startTime") Date startTime,
//			@WebParam(name = "endTime") Date endTime);

	/**
	 * Creates a new authored session from a scenario
	 * 
	 * @param sessionName		The name of the session 
	 * @param description		An (optional) description of the session
	 * @param scenarioId		The ID of the scenario to base this session on
	 * @param startTime			Start time of this session (no start time if 
	 * 							null)
	 * @param endTime			End time of this session (never ends if null)
	 * @param userName			The username of the session creator
	 * 
	 * @return The completed session, null for error
	 */
	@WebResult(name = "Session")
	public Session newSessionAuthored(
			@WebParam(name = "sessionName") String sessionName, 
			@WebParam(name = "description") String description, 
			@WebParam(name = "scenarioId") int scenarioId, 
			@WebParam(name = "startTime") Date startTime,
			@WebParam(name = "endTime") Date endTime,
			@WebParam(name = "userName") String userName);
	
	/**
	 * Processes a request to schedule a session.
	 * 
	 * @param sessionID				ID of the session in the database
	 * @param automaticActivation	true if the session if activated 
	 * 								automatically (network set up automatically)
	 * 
	 * @return boolean true if session scheduled successfully
	 */
	public boolean scheduleSession(@WebParam(name = "sessionID") int sessionID,
			@WebParam(name = "automaticActivation") boolean automaticActivation);
	
	/**
	 * Processes a request to schedule a session.
	 * Lets the user set the APN network instead of using spring configuration 
	 * in scheduling service.
	 * 
	 * @param sessionID				ID of the session in the database
	 * @param automaticActivation	true if the session if activated 
	 * 								automatically (network set up automatically)
	 * @param resType				the {@code resType} property to set on the 
	 * 								reservation
	 * 
	 * @return boolean true if session scheduled successfully
	 */
	public boolean scheduleSessionOnResType(
			@WebParam(name = "sessionID") int sessionID, 
			@WebParam(name = "automaticActivation") boolean automaticActivation,
			@WebParam(name = "resType") String resType);

	/**
	 * Cancels a session reservation.
	 * 
	 * @param sessionID			The ID of the session for the reservation to 
	 * 							cancel.
	 * 
	 * @return boolean true if session cancelled successfully
	 */
	public boolean cancelSession(@WebParam(name = "sessionID") int sessionID);

	/**
	 * Gets the status of the session reservation.
	 * 
	 * @param sessionID		The session ID of the reservation to consider
	 * 
	 * @return The reservation status string from the scheduler
	 */
	public String getSessionStatus(@WebParam(name = "sessionID") int sessionID);

//	/**
//	 * TODO ??? -aaron: don't know what this is supposed to do - if, as I 
//	 * 					suspect, it's a notification to the mySavoir tray, please 
//	 * 					observe deprecation notice
//	 * @deprecated use {@link ca.gc.nrc.iit.savoir.thresholdMgmt.MessageSender} instead
//	 */
//	public void notifyDock(@WebParam(name = "xmlMessage") String xmlMessage,
//			@WebParam(name = "parameters") List<String> parameters,
//			@WebParam(name = "values") List<String> values);

	/**
	 * Gets all sessions the caller has access to.
	 * @param username		The username of the caller
	 * @return A list of all sessions the caller can view.
	 */
	public List<Session> getAllSessions(
			@WebParam(name = "username") String username);
	
	/**
	 * Gets all current sessions the caller has access to.
	 * 
	 * @param username		The username of the caller
	 * 
	 * @return A list of all sessions the caller can view in which the end time 
	 * 		is not yet past.
	 */
	public List<Session> getCurrentSessions(
			@WebParam(name = "username") String username);
	
	/**
	 * Gets sessions the user is authorized to load
	 * 
	 * @param userName	The username of the user to load sessions
	 * 
	 * @return a list of all the sessions the user can load
	 */
	@WebResult(name = "Session")
	public List<Session> getLoadableSessions(
			@WebParam(name = "userName") String userName);

	/**
	 * Gets sessions the user is authorized to join
	 * 
	 * @param userName	The username of the user to join sessions
	 * 
	 * @return a list of all sessions the user can join. The user must possess 
	 * 		{@code SCENARIO_RUN} rights on the session, and the session must 
	 * 		be either loaded or running.
	 */
	@WebResult(name = "Session")
	public List<Session> getJoinableSessions(
			@WebParam(name = "userName") String userName);
	
	/**
	 * Gets sessions the user is authorized to remove
	 * 
	 * @param userName	The username of the user to remove sessions
	 * 
	 * @return a list of all sessions the user can remove. The user must 
	 * 		possess {@code SCENARIO_EDIT} rights on the session
	 */
	@WebResult(name = "Session")
	public List<Session> getRemovableSessions(
			@WebParam(name = "userName") String userName);

	/**
	 * Have a user join the given session
	 * 
	 * @param sessionID		The ID of the session to join
	 * @param userName		The username of the user joining the session
	 * 
	 * @throws Exception on user not authorized to join session
	 */
	public void joinSession(@WebParam(name = "sessionID") int sessionID,
			@WebParam(name = "userName") String userName) 
			throws Exception;
	
	/**
	 * Removes a session from the database
	 * 
	 * @param sessionID		The ID of the session to remove
	 * @param userName		The username of the caller
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for no session with this ID exists
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for {@code sessionID} less than or equal to {@code 0}
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int removeSession(@WebParam(name = "sessionID") int sessionID, 
			@WebParam(name = "userName") String userName);
	
	/**
	 * Load a session. This will load all resources for the session, as well 
	 * as scenario rules (for an authored session), and any required network 
	 * resources.
	 * <p>
	 * Note that though this method is not deprecated, it has been superseded 
	 * by {@link #runSession(int)}.
	 * 
	 * @param sessionID		The ID of the session to load
	 * @param userName		The user requesting session load
	 * 
	 * @throws Exception on user not authorized to load session.
	 */
	public void loadSession(@WebParam(name = "sessionID") int sessionID,
			@WebParam(name = "userName") String userName) 
			throws Exception;
	
	/**
	 * Starts a session.
	 * Triggers rules for authored session start.
	 * <p>
	 * Note that though this method is not deprecated, it has been superseded 
	 * by {@link #runSession(int)}.
	 * 
	 * @param sessionID		The ID of the session to start
	 * @param userName		The user to start the session
	 */
	public void beginSession(@WebParam(name = "sessionID") int sessionID,
			@WebParam(name = "userName") String userName);
	
	/**
	 * Runs a session.
	 * If the session is authored, loads resources and rules, and triggers the 
	 * rule engine. If it is unauthored, simply updates state.
	 * 
	 * @param sessionID		The ID of the session to run
	 * @param userName		The user to run it as
	 */
	public void runSession(@WebParam(name = "sessionID") int sessionID, 
			@WebParam(name = "userName") String userName)
			throws Exception;
	
	/**
	 * Ends a session. Releases resources held for session state, scenario 
	 * rules (if authored), edge devices (these are stopped and shut down via 
	 * the message bus), and network. 
	 * 
	 * @param sessionID		The ID of the session to end
	 * @param userName		The user to end the session as
	 */
	public void endSession(@WebParam(name = "sessionID") int sessionID, 
			@WebParam(name = "userName") String userName);
	
	/**
	 * Private, internal version of {@link #endSession(int, String)}. Behaves 
	 * exactly the same, except does not require a username.  
	 * 
	 * @param sessionID		The ID of the session to end
	 * @param userName		The user to end the session as
	 */
	public void endSessionPriv(@WebParam(name = "sessionID") int sessionID);
	
	/**
	 * Synchronous version of {@link #endSession(int, String)}. Behaves exactly 
	 * the same, except that it does not return until the session is ended (or 
	 * a timeout has expired).
	 * 
	 * @param sessionID		The ID of the session to end
	 * @param userName		The user to end the session as
	 */
	public void endSessionSync(@WebParam(name = "sessionID") int sessionID, 
			@WebParam(name = "userName") String userName);
	
	/**
	 * Notifies the session manager that a session has terminated (all session 
	 * ending cleanup has finished being performed).
	 * 
	 * @param sessionID		The ID of the ended session
	 */
	public void sessionEnded(@WebParam(name = "sessionID") int sessionID);
	
	/**
	 * Flushes all state for a session, without properly ending it. Use only 
	 * if the session is in an inconsistent state.
	 * 
	 * @param sessionID		The ID of the session to abort.
	 * @param userName		The user to flush the session on behalf of
	 */
	public void flushSession(@WebParam(name = "sessionID") int sessionID, 
			@WebParam(name = "userName") String userName);
	
	/**
	 * Updates SAVOIR's record of resource parameters for loaded session
	 * 
	 * @param sessionID		The session loaded
	 * @param resourceID	The resource being updated
	 * @param resourceName	The name of the resource being updated
	 * @param activityID	The activity running on the resource 
	 * @param params		The parameters being updated
	 */
	public void updateResourceParameters(
			@WebParam(name = "sessionID") int sessionID, 
			@WebParam(name = "resourceID") String resourceID, 
			@WebParam(name = "resourceName") String resourceName,
			@WebParam(name = "activityID") String activityID, 
			@WebParam(name = "params") List<Parameter> params);
	
	
	/**
	 * Notification that a session's resources have all been loaded
	 * 
	 * @param sessionId		The session whose resources are loaded
	 */
	public void sessionLoaded(int sessionId);
	
	/**
	 * Gets a list of user default session IDs for users involved in the given 
	 * subsession of the given session
	 * 
	 * @param sessionId			The master session ID
	 * @param subsessionId		The subsession ID
	 * 
	 * @return a list of default session IDs of currently logged in users who 
	 * 		are specially authorized on that subsession, null for no such 
	 * 		authored session, empty for no logged in users fitting criteria
	 */
	public List<Integer> getUserSessionIdsForSubsession(int sessionId, 
			int subsessionId);
	

	/*
//	 * set the scheduler for a session
//	 */
//	public boolean setScheduler(@WebParam(name = "sessionID") String sessionID,
//			@WebParam(name = "sessionName") String sessionName,
//			@WebParam(name = "startTime") long startTime,
//			@WebParam(name = "endTime") long endTime,
//			@WebParam(name = "startCommand") String startCommand,
//			@WebParam(name = "endCommand") String endCommand,
//			@WebParam(name = "hostedby") String hostedby);
//
//	public int updateScheduler(@WebParam(name = "sID") String sID,
//			@WebParam(name = "sName") String sName,
//			@WebParam(name = "time") long time,
//			@WebParam(name = "command") String command,
//			@WebParam(name = "flag") int flag,
//			@WebParam(name = "hostedby") String hostedby);
	
	@WebResult(name = "Session")
	public List<Session> getSubSessionList(@WebParam(name = "parentSessionID") int parentSessionID);

	public void updateSession(@WebParam(name = "session") Session updatedSession);
	
	@WebResult(name = "Session")
	public Session getSessionById(@WebParam(name = "sessionID")int sessionID);
	
	@WebResult(name = "Session")
	public List<Session> getSessionListByName(@WebParam(name = "sessionName")String sessionName);
	
//	public void startSessionBySessionID(@WebParam(name = "sessionID")int sessionID);
//	
//	public void endSessionBySessionID(@WebParam(name = "sessionID") int sessionID);
	
	@WebResult(name = "Person")
	public Person getPersonByUserName(@WebParam(name = "userName") String userName);
	
	public void updateSessionPaticipantStatus(@WebParam(name = "session") Session session,
			@WebParam(name = "user") User user);
	
	
	//--------------------------------------------------------
	// User / Group Authorization View-Create-Delete operations
	//--------------------------------------------------------
	
	/**
	 * Gets the roles users have on a particular session
	 * 
	 * @param sessionId		The session ID
	 * @param userName		The user calling this
	 * 
	 * @return a map of user IDs to roles defined on that session. (empty for 
	 * 		invalid sessionId, or unauthenticated/unauthorized caller)
	 */
	@WebResult(name = "SessionAuthorization")
	public List<SessionAuthorization> getUserAuthorizations(
			@WebParam(name="sessionId") int sessionId, 
			@WebParam(name = "userName") String userName);
	
	/**
	 * Gets the roles groups have on a particular session
	 * 
	 * @param sessionId		The session's ID
	 * @param userName		The user calling this
	 * 
	 * @return a map of group IDs to roles defined on the session (empty for 
	 * 		invalid sessionId, or unauthenticated/unauthorized caller). An 
	 * 		{@code authorizedId} of {@code 0} corresponds to an authorization 
	 * 		to everyone.
	 */
	@WebResult(name = "SessionAuthorization")
	public List<SessionAuthorization> getGroupAuthorizations(
			@WebParam(name="sessionId") int sessionId, 
			@WebParam(name = "userName") String userName);
	
	/**
	 * Authorizes a user on a session with a given role. If the user already 
	 * has a role on the session, will change it to this one.
	 * 
	 * @param userId		The user's ID (cannot be that of the caller) 
	 * 						(caller must possess rights >= both subject's 
	 * 						original and new role)
	 * @param sessionId		The ID of the session to be added to
	 * @param roleId		The role's ID
	 * @param username		The username of the caller
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for user, role, or session does not exist
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for invalid {@code userId}, {@code roleId}, 
	 * 				{@code sessionId}
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUBJECT_UNAUTHORIZED} 
	 * 				for subject not authorized on resource in session
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int setUserAuthorization(@WebParam(name="userId") int userId, 
			@WebParam(name="sessionId") int sessionId, 
			@WebParam(name="roleId") int roleId,
			@WebParam(name = "username") String username);
	
	/**
	 * Authorizes a group on a session with a given role. If the group already 
	 * has a role on the session, will change it to this one.
	 * 
	 * @param groupId		The group's ID (if changing an existing 
	 * 						authorization, cannot be the group which grants
	 * 						management rights to edit authorizations, and 
	 * 						caller must possess rights >= both subject's 
	 * 						original and new role). A group ID of {@code 0} 
	 * 						represents all users.
	 * @param sessionId		The ID of the session
	 * @param roleId		The role's ID
	 * @param username		The username of the caller
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for group, role, or session does not exist
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for invalid {@code groupId}, {@code roleId}, 
	 * 				{@code sessionId}
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED}
	 * 				for unauthorized caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUBJECT_UNAUTHORIZED} 
	 * 				for subject not authorized on resource in session 
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int setGroupAuthorization(@WebParam(name="groupId") int groupId, 
			@WebParam(name="sessionId") int sessionId,
			@WebParam(name="roleId") int roleId,
			@WebParam(name = "username") String username);
	
	/**
	 * Removes a user's authorization on a given session
	 * 
	 * @param userId		The user's ID (cannot be that of the caller, or 
	 * 						user with more rights than caller)
	 * @param sessionId		The session's ID
	 * @param userName		The user calling this
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for user or session does not exist or user had no 
	 * 				authorization on session
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for invalid {@code userId}, {@code sessionId}
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int unsetUserAuthorization(@WebParam(name="userId") int userId, 
			@WebParam(name="sessionId") int sessionId, 
			@WebParam(name = "userName") String userName);
	
	/**
	 * Removes a group's authorization on a session
	 * 
	 * @param groupId		The group's ID (cannot be the group which grants 
	 * 						management rights to remove authorizations, and 
	 * 						caller must possess rights >= subject's). A value 
	 * 						of {@code 0} removes authorizations that are 
	 * 						explicitly assigned to the group of all users (This 
	 * 						is <b>not</b> all authorizations assigned on this 
	 * 						session, but rather authorizations explicitly 
	 * 						assigned to "everyone").
	 * @param sessionId		The session's ID
	 * @param userName		The user calling this
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for group or session does not exist or group had no 
	 * 				authorization on session
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for invalid {@code groupId}, {@code sessionId} 
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int unsetGroupAuthorization(@WebParam(name="groupId") int groupId, 
			@WebParam(name="sessionId") int sessionId, 
			@WebParam(name = "userName") String userName);
}
