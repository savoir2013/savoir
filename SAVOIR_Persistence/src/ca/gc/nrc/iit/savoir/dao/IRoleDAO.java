// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao;

import java.util.List;
import java.util.Set;

import ca.gc.iit.nrc.savoir.domain.GroupAuthorization;
import ca.gc.iit.nrc.savoir.domain.Role;

public interface IRoleDAO {

	/**
	 * Creates a new role
	 * @param group		The role to create
	 */
	public void addRole(Role role);
	
	/**
	 * Deletes a role
	 * @param roleId	The ID of the role to delete
	 */
	public void removeRole(int roleId);
	
	/**
	 * Updates a role
	 * @param roleId	The ID of the role to update
	 * @param roleInfo	The information to update (all null fields will be left as is)
	 * @return	0 for success
	 * 			-1 for no such role
	 * 			-2 for invalid roleId parameter, null roleInfo parameter,
	 * 				or all roleInfo fields null
	 * 			!=0 for other error
	 */
	public int updateRole(int roleId, Role roleInfo);
	
	/**
	 * Gets a role, given its ID
	 * @param roleId	The ID of the role to search for
	 * @return the role for success, null for failure
	 */
	public Role getRoleById(int roleId);
	
	/**
	 * Gets users having a given role on a given group
	 * @param roleId	The role ID to search by
	 * @param groupId	The ID of the group  
	 * @return the IDs of all users having that role on that group
	 */
	public Set<Integer> getUsersByRole(int roleId, int groupId);
	
	/**
	 * Gets groups having a given role on another group
	 * @param roleId	The role ID to search by
	 * @param groupId	The ID of the group  
	 * @return the IDs of all groups having that role on the other group
	 */
	public Set<Integer> getGroupsByRole(int roleId, int groupId);
	
	/**
	 * @return a list of all the roles defined in the system
	 */
	public List<Role> getRoles();
	
	/**
	 * Gets roles users have on a given group
	 * @param groupId	The group ID
	 * @return a map of user IDs to roles defined on that group
	 */
	public List<GroupAuthorization> getUserAuthorizationsByGroup(int groupId);
	
	/**
	 * Gets the roles subgroups have on a particular group
	 * @param groupId		The group ID of the supergroup
	 * @return a map of group IDs to roles defined on the supergroup
	 */
	public List<GroupAuthorization> getGroupAuthorizationsBySupergroup(int groupId);
	
	/**
	 * @return the next role ID in the series (the current max + 1) (0 for error)	
	 */
	public int getNextRoleId();
}
