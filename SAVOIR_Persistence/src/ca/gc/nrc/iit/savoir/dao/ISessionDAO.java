// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import ca.gc.iit.nrc.savoir.domain.Group;
import ca.gc.iit.nrc.savoir.domain.Participant;
import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.iit.nrc.savoir.domain.Role;
import ca.gc.iit.nrc.savoir.domain.Session;
import ca.gc.iit.nrc.savoir.domain.TimeSlot;

public interface ISessionDAO {

	// returns new session ID
	public int addSession(Session s);

	public int addSession(Session s, int masterSessionID);

	/**
	 * Removes the session with the given session ID, and all its subsessions
	 * 
	 * @param sessionId		The ID of the session to remove
	 */
	public void removeSession(int sessionId);

	public Session getSessionById(int sessionId);

	public List<Session> getSubsessions(int sessionID);

	public List<Session> getRelevantSessionsInTimeInterval(Calendar startTime,
			Calendar endTime);
	
	public List<Session> getRelevantSessionsInTimeInterval(Resource networkResource, Calendar startTime,
			Calendar endTime);

	public TimeSlot getScheduledTime(int sessionID);

	public boolean isScheduled(int sessionID);

	public List<Participant> getSessionParticipants(int sessionID);
	
	public List<Group> getSessionAuthorizedGroups(int sessionID);
	
	public int updateSession(Session s);
	
	public List<Session> getSessionsByStatus(String... status);
	
	public Map<Integer, Integer> getNetworkReservationPerSession();
	
	//added 10-01-09
	public List<Session> getSessionListByName(String sessionName);
	public int updateSessionParticipant(int sessionID, Participant participant);
	public List<Participant> getSessionParticipantsByStatus(int sessionID, String participantStatus);
	public Session getSessionByStatusAndUserID(String status, int userID);
	//end add
	
	/**
	 * Gets the list of sessions that use a given scenario.
	 * 
	 * @param scenarioId		The ID of the scenario
	 * 
	 * @return a list of IDs of sessions using that scenario (empty for none 
	 * 			such, null for error)
	 */
	public List<Integer> getSessionIdsByScenarioId(int scenarioId);
	
	/**
	 * Updates the session status to the given value.
	 * 
	 * @param sessionID		the ID of the session
	 * @param status		the new session status
	 */
	public void updateSessionStatus(int sessionID, String status);
	
	/**
	 * Gets the IDs of the subsessions of a given session
	 * 
	 * @param sessionID		The ID of the master session
	 * 
	 * @return a list of subsession IDs, empty for none such, null for error
	 */
	public List<Integer> getSubsessionIds(int sessionID);
	
	/**
	 * Gets the users authorized on a given session
	 * 
	 * @param sessionId		The ID of the session to check authorizations on
	 * 
	 * @return a map of User IDs to rights
	 */
	public Map<Integer, Role> getAuthorizedUsers(int sessionId);
	
	/**
	 * Gets the groups authorized on a given session
	 * 
	 * @param sessionId		The ID of the session to check authorizations on
	 * 
	 * @return a map of Group IDs to rights (group ID 0 represents all users)
	 */
	public Map<Integer, Role> getAuthorizedGroups(int sessionId);
	
	/**
	 * Adds a new user authorization
	 * 
	 * @param userId		The user to authorize
	 * @param roleId		The role to give the user
	 * @param sessionId		The session to set the authorization on
	 * 
	 * @return 0 for success
	 * 			-1 for user, role, or session does not exist
	 * 			-2 for invalid userId, roleId, sessionId
	 * 			!=0 for other error
	 */
	public int addUserAuthorization(int userId, int roleId, int sessionId);
	
	/**
	 * Updates a user authorization
	 * 
	 * @param userId		The user to authorize
	 * @param roleId		The role to give the user
	 * @param sessionId		The session to change the authorization on
	 * 
	 * @return 0 for success
	 * 			-1 for user, role, or session does not exist
	 * 			-2 for invalid userId, roleId, sessionId
	 * 			!=0 for other error
	 */
	public int updateUserAuthorization(int userId, int roleId, int sessionId);
	
	/**
	 * Removes a user authorization
	 * 
	 * @param userId		The user to deauthorize
	 * @param sessionId		The session to remove the authorization on
	 * 
	 * @return 0 for success
	 * 			-1 for user or session does not exist
	 * 			-2 for invalid userId or sessionId
	 * 			!=0 for other error
	 */
	public int removeUserAuthorization(int userId, int sessionId);
	
	/**
	 * Adds a new group authorization
	 * 
	 * @param groupId		The group to authorize (0 for all users)
	 * @param roleId		The role to give the group
	 * @param sessionId		The session to set the authorization on
	 * 
	 * @return 0 for success
	 * 			-1 for group, role, or session does not exist
	 * 			-2 for invalid groupId, roleId, sessionId
	 * 			!=0 for other error
	 */
	public int addGroupAuthorization(int groupId, int roleId, int sessionId);
	
	/**
	 * Updates a group authorization
	 * 
	 * @param groupId		The group to authorize (0 for all users)
	 * @param roleId		The role to give the group
	 * @param sessionId		The session to change the authorization on
	 * 
	 * @return 0 for success
	 * 			-1 for group, role, or session does not exist
	 * 			-2 for invalid groupId, roleId, sessionId
	 * 			!=0 for other error
	 */
	public int updateGroupAuthorization(int groupId, int roleId, int sessionId);
	
	/**
	 * Removes a group authorization
	 * 
	 * @param groupId		The group to deauthorize (0 for the implicit group 
	 * 						of all users - note that this does <b>not</b> 
	 * 						remove all authorizations on this session, just 
	 * 						those given to the implicit group of all users)
	 * @param sessionId		The session to remove the authorization on
	 * 
	 * @return 0 for success
	 * 			-1 for group or session does not exist
	 * 			-2 for invalid groupId or sessionId
	 * 			!=0 for other error
	 */
	public int removeGroupAuthorization(int groupId, int sessionId);
}
