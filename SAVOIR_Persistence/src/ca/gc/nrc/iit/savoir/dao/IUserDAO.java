// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import ca.gc.iit.nrc.savoir.domain.Role;
import ca.gc.iit.nrc.savoir.domain.User;

public interface IUserDAO{
	
	public void addUser(User user);
	
	/**
	 * Updates user information for a SAVOIR user.
	 * @param userId		The user's ID #
	 * @param loginId		The user's new login ID
	 * @param password		The user's new password
	 * @param beginTime		The new time the user becomes valid
	 * @param endTime		The new time the user ceases to be valid
	 * @return	0 for success
	 * 			-1 for no such user
	 * 			-2 for invalid userId parameter, loginId or password null
	 * 			!=0 for other error
	 */
	public int updateUser(int userId, String loginId, String password, Date beginTime, Date endTime);
	
	public void removeUser (int userId);
	public User getUserById(int userId);
	
	/**
	 * Retrieves a User given that user's distinguished name (login ID)
	 * @param distinguishedName		The user's DName
	 * @return The SAVOIR user having that DName, null for none such
	 */
	public User getUserByDN(String distinguishedName);
//added at 09-17-09 to update user table
	public void updateUserSite(String userName, int siteId);
//end
	
	// added 16-09-09 for integration of UserMgr v2
	
	/**
	 * Checks if a given distinguished name is listed in the database
	 * @param name		The distinguished name to check 
	 * @return is the name valid?
	 */
	public boolean isDName(String name);
	
	/**
	 * Gets a user's ID from their DName
	 * @param caller	The DName of the caller
	 * @return	the user ID of a valid caller, 0 for an invalid caller or null
	 */
	public int getUserID(String caller);
	
	/**
	 * Gets all the groups a user is a member of
	 * @param userId	The user's ID
	 * @return a set of the IDs of the groups this user is a member of, null on error
	 */
	public Set<Integer> getMemberships(int userId);
	
	/**
	 * Gets the authorizations the given user has on all groups
	 * @param userId	The user's ID
	 * @return a map mapping group IDs to roles the user has on the session
	 */
	public Map<Integer, Role> getGroupAuthorizations(int userId);
	
	/**
	 * Gets the authorizations the given user has on all sessions
	 * @param userId	The user's ID
	 * @return a map mapping session IDs to roles the user has on the session
	 */
	public Map<Integer, Role> getSessionAuthorizations(int userId);
	
	/**
	 * Gets the authorizations the given user has on all authored sessions
	 * 
	 * @param userId	The user's ID
	 * 
	 * @return a map mapping session IDs to roles the user has on the session
	 */
	public Map<Integer, Role> getAuthoredSessionAuthorizations(int userId);
	
	/**
	 * Adds a new user authorization
	 * @param userId	The user to authorize
	 * @param roleId	The role to give the user
	 * @param groupId	The group to set the authorization on
	 * @param isMember	Is the user to be a member of the group?
	 * @param beginTime	Time authorization becomes valid (optional)
	 * @param endTime	Time authorization ceases to be valid (optional)
	 * @return 0 for success
	 * 			-1 for user, role, or group does not exist
	 * 			-2 for invalid userId, roleId, groupId
	 * 			!=0 for other error
	 */
	public int addAuthorization(int userId, int roleId, int groupId, boolean isMember, 
			Date beginTime, Date endTime);
	
	/**
	 * Updates a user authorization
	 * @param userId	The user to authorize
	 * @param roleId	The role to give the user
	 * @param groupId	The group to change the authorization on
	 * @param isMember	Is the user to be a member of the group?
	 * @param beginTime	Time authorization becomes valid (optional)
	 * @param endTime	Time authorization ceases to be valid (optional)
	 * @return 0 for success
	 * 			-1 for user, role, or group does not exist
	 * 			-2 for invalid userId, roleId, groupId
	 * 			!=0 for other error
	 */
	public int updateAuthorization(int userId, int roleId, int groupId, boolean isMember, 
			Date beginTime, Date endTime);
	
	/**
	 * Removes a user authorization
	 * @param userId	The user to deauthorize
	 * @param groupId	The group to remove the authorization on
	 * @return 0 for success
	 * 			-1 for user or group does not exist
	 * 			-2 for invalid userId or groupId
	 * 			!=0 for other error
	 */
	public int removeAuthorization(int userId, int groupId);
	
	/**
	 * @return the next user ID in the series (the current max + 1) (0 for error)	
	 */
	public int getNextUserId();
	
	// end added
	//added at 10-03-2009
	public User getUserByPersonID(int personID);
	//end
	
	public void updateSystemRole(int userId, int roleId);
}
