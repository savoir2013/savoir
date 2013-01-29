// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import ca.gc.iit.nrc.savoir.domain.Group;
import ca.gc.iit.nrc.savoir.domain.GroupNode;
import ca.gc.iit.nrc.savoir.domain.Role;

public interface IGroupDAO {

	/**
	 * Creates a new group
	 * @param group		The group to create
	 */
	public void addGroup(Group group);
	
	/**
	 * Deletes a group
	 * @param groupId	The group to delete
	 */
	public void removeGroup(int groupId);
	
	/**
	 * Updates a group
	 * @param groupId	The ID of the group to update
	 * @param name		The new name of the group (null for no change)
	 * @param desc		The new description of the group (null for no change)
	 */
	public int updateGroup(int groupId, String name, String desc);
	
	/**
	 * Gets a group by its ID
	 *  
	 * @param groupId	The ID of the group
	 * 
	 * @return the group with that ID (null for none such or error)
	 */
	public Group getGroupById(int groupId);
	
	/**
	 * Gets group information by group ID
	 * @param groups	The IDs of the groups to get
	 * @return a set of group objects representing that group
	 */
	public Set<Group> getGroupsById(Set<Integer> groups);
	
	/**
	 * Adds a user to a given role on a given group.
	 * @param userId	The user's ID
	 * @param roleId	The role's ID
	 * @param groupId	The group to add the user to
	 * @param isMember	Is this user a member of the group?
	 * @param beginTime	The time the authorization becomes valid
	 * @param endTime	The time the authorization ceases to be valid
	 */
	public void addUserAuthorization(int userId, int roleId, int groupId, 
			boolean isMember, Date beginTime, Date endTime);
	
	/**
	 * Adds a group to a given role on another group.
	 * @param subgroupId	The added group's ID
	 * @param roleId		The role's ID
	 * @param groupId		The ID of the group added to
	 * @param isSubgroup	Is added group a subgroup of the other group?
	 */
	public void addGroupAuthorization(int subgroupId, int roleId, int groupId, 
			boolean isSubgroup);
	
	/**
	 * Given a set of groups, get the direct supergroups of every group in the set
	 * @param groups		The group IDs of a set of groups 
	 * @return a set of the IDs of all direct supergroups of the given groups, null on error
	 */
	public Set<Integer> getDirectSupergroups(Set<Integer> groups);
	
	/**
	 * Given a set of groups, get the direct subgroups of every group in the set
	 * @param groups		The group IDs of a set of groups 
	 * @return a set of the IDs of all direct subgroups of the given groups, null on error
	 */
	public Set<Integer> getDirectSubgroups(Set<Integer> groups);
	
	/**
	 * Given a set of groups, get the direct members of every group in the set
	 * 
	 * @param groups		The group IDs of a set of groups
	 * 
	 * @return a set of the user IDs of all direct members of the given groups, null on error
	 */
	public Set<Integer> getMembers(Set<Integer> groups);
	
	/**
	 * Given a set of roles on a group, get the users authorized on that group having any of those roles.
	 * @param groupId	The ID of the group
	 * @param roles		The IDs of the roles
	 * @return The IDs of all users having any of those roles on the given group
	 */
	public Set<Integer> getAuthorizedUsersByRole(int groupId,  Set<Integer> roles);
	
	/**
	 * Given a set of roles on a supergroup, get the groups directly authorized on that supergroup 
	 * with any of those roles.
	 * @param groupId	The ID of the group
	 * @param roles		The IDs of the roles
	 * @return The IDs of all subgroups having any of those roles on the given group
	 */
	public Set<Integer> getAuthorizedSubgroupsByRole(int groupId,  Set<Integer> roles);
	
	/**
	 * Gets the graph of group memberships and subgroups for the given group
	 * @param groupId	The ID of the group to root the graph at
	 * @return a GroupNode containing the user graph rooted at that group
	 */
	public GroupNode getUserGraphByGroupId(int groupId);
	
	/**
	 * Gets the users authorized on a given group
	 * @param group	The group to check authorizations on
	 * @return a map of User IDs to rights
	 */
	public Map<Integer, Role> getAuthorizedUsers(int group);
	
	/**
	 * Gets the groups authorized on a given group
	 * @param group	The group to check authorizations on
	 * @return a map of Group IDs to rights
	 */
	public Map<Integer, Role> getAuthorizedGroups(int group);
	
	/**
	 * Gets the authorizations the given group has on all groups
	 * @param groups	The group's ID
	 * @return a map mapping group IDs to authorizations
	 */
	public Map<Integer, Role> getAuthorizations(int group);
	
	/**
	 * Gets the authorizations the given groups have on all groups
	 * @param groups	The groups' IDs
	 * @return a map mapping group IDs to roles on those groups
	 */
	public Map<Integer, Set<Role>> getGroupAuthorizations(Set<Integer> groups);
	
	/**
	 * Gets the authorizations the given groups have on all sessions
	 * @param groups	The groups' IDs
	 * @return a map mapping session IDs to roles on those sessions
	 */
	public Map<Integer, Set<Role>> getSessionAuthorizations(Set<Integer> groups);
	
	/**
	 * Gets the authorizations the given groups have on all authored sessions
	 * @param groups	The groups' IDs
	 * @return a map mapping session IDs to roles on those sessions
	 */
	public Map<Integer, Set<Role>> getAuthoredSessionAuthorizations(Set<Integer> groups);
	
	/**
	 * Adds a new group authorization
	 * @param subgroupId	The group to authorize
	 * @param roleId		The role to give the group
	 * @param groupId		The group to set the authorization on
	 * @param isSubgroup	Is the user to be a member of the group?
	 * @return 0 for success
	 * 			-1 for subgroup, role, or group does not exist
	 * 			-2 for invalid subgroupId, roleId, groupId
	 * 			!=0 for other error
	 */
	public int addAuthorization(int subgroupId, int roleId, int groupId, boolean isSubgroup);
	
	/**
	 * Updates a group authorization
	 * @param subgroupId	The group to authorize
	 * @param roleId		The role to give the group
	 * @param groupId		The group to change the authorization on
	 * @param isSubgroup	Is the user to be a member of the group?
	 * @return 0 for success
	 * 			-1 for subgroup, role, or group does not exist
	 * 			-2 for invalid subgroupId, roleId, groupId
	 * 			!=0 for other error
	 */
	public int updateAuthorization(int subgroupId, int roleId, int groupId, boolean isSubgroup);
	
	/**
	 * Removes a group authorization
	 * @param subgroupId	The group to deauthorize
	 * @param groupId		The group to remove the authorization on
	 * @return 0 for success
	 * 			-1 for subgroup or group does not exist
	 * 			-2 for invalid subgroupId or groupId
	 * 			!=0 for other error
	 */
	public int removeAuthorization(int subgroupId, int groupId);
	
	/**
	 * @return The user IDs of all users who are not members of any group
	 */
	public Set<Integer> getGrouplessUsers();
	
	/**
	 * @return The group IDs of all groups which are not subgroups of any other group
	 */
	public Set<Integer> getRootGroups();
	
	/**
	 * @return The user IDs of all users
	 */
	public Set<Integer> getAllUsers();
	
	/**
	 * @return The group IDs of all groups
	 */
	public Set<Integer> getAllGroups();
	
	/**
	 * @return the next group ID in the series (the current max + 1) (0 for error)	
	 */
	public int getNextGroupId();
}
