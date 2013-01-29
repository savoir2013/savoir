// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.userMgmt;

import java.util.HashSet;
import java.util.Set;

import ca.gc.nrc.iit.savoir.dao.impl.DAOFactory;

/**
 * A utility class for algorithms to traverse the User/Group graph.
 * 
 * @author Aaron Moss
 */
public class GraphUtils {

	/**
	 * Check if a user is in a group (applies recursively to subgroups)
	 * 
	 * @param user		The user to check
	 * @param group		The group to check
	 * 
	 * @return is the user in the group?
	 */
	public static boolean userIsInGroup(int user, int group) {
		//shortcut escape for implicit "EVERYONE" group
		if (group == 0) return true;
		
		//get groups user is in
		Set<Integer> newGroups = 
			DAOFactory.getDAOFactoryInstance().getUserDAO()
				.getMemberships(user);
		//list of groups already checked
		Set<Integer> checkedGroups = new HashSet<Integer>();
		
		while (!newGroups.isEmpty()) {	// while we have more groups to check
			if (newGroups.contains(group)) {	//check the new groups
				return true;
			}
			
			//add these groups to the checked list
			checkedGroups.addAll(newGroups);
			//get their supergroups
			newGroups = 
				DAOFactory.getDAOFactoryInstance().getGroupDAO()
					.getDirectSupergroups(newGroups);
			//cull the groups already checked
			newGroups.removeAll(checkedGroups);
		}
		
		return false;	//exhausted graph, did not locate group
	}
	
	/**
	 * Check if a user is in any of a set of groups
	 * 
	 * @param user		The user's ID
	 * @param groups	The IDs of the groups to check
	 * 
	 * @return is the user in any of the groups?
	 */
	public static boolean userIsInAnyGroup(int user, Set<Integer> groups) {
		//shortcut escape for implicit "EVERYONE" group
		if (groups.contains(0)) return true;
		
		//get groups user is in
		Set<Integer> newGroups = 
			DAOFactory.getDAOFactoryInstance().getUserDAO()
				.getMemberships(user);
		//list of groups already checked
		Set<Integer> checkedGroups = new HashSet<Integer>();
		
		while (!newGroups.isEmpty()) {	// while we have more groups to check
			//check the new groups
			for (int group : groups) {
				if (newGroups.contains(group)) {
					return true;
				}
			}
			
			//add these groups to the checked list
			checkedGroups.addAll(newGroups);
			//get their supergroups
			newGroups = 
				DAOFactory.getDAOFactoryInstance().getGroupDAO()
					.getDirectSupergroups(newGroups);
			//cull the groups already checked
			newGroups.removeAll(checkedGroups);
		}
		
		return false;	//exhausted graph, did not find any groups
	}
	
	/**
	 * Gets all the subgroups (applied recursively) of the given groups
	 * 
	 * @param groups	The set of groups to check (must not be null)
	 * 					(note that as an internal method, there are no validity 
	 * 					checks)
	 * 
	 * @return the union of the original set of groups and all their subgroups, 
	 * 	direct or indirect
	 */
	public static Set<Integer> allSubgroups(Set<Integer> groups) {
		//shortcut escape for implicit "EVERYONE" group
		if (groups.contains(0)) {
			return DAOFactory.getDAOFactoryInstance().getGroupDAO()
				.getAllGroups();
		}
		
		Set<Integer> resultSet = new HashSet<Integer>();
		Set<Integer> newGroups;
		
		while (!groups.isEmpty()) {
			//get new groups
			newGroups = 
				DAOFactory.getDAOFactoryInstance().getGroupDAO()
					.getDirectSubgroups(groups);
			//add previous groups to result set
			resultSet.addAll(groups);
			//trim previously searched groups from new groups
			newGroups.removeAll(resultSet);
			//reset pass
			groups = newGroups;
		}
		
		return resultSet;
	}
	
	/**
	 * Gets all the supergroups (applied recursively) of the given groups
	 * 
	 * @param groups	The set of groups to check (must not be null)
	 * 					(note that as an internal method, there are no validity 
	 * 					checks)
	 * 
	 * @return the union of the original set of groups and all their 
	 * 	supergroups, direct or indirect
	 */
	public static Set<Integer> allSupergroups(Set<Integer> groups) {
		Set<Integer> resultSet = new HashSet<Integer>();
		Set<Integer> newGroups;
		
		while (!groups.isEmpty()) {
			//get new groups
			newGroups = 
				DAOFactory.getDAOFactoryInstance().getGroupDAO()
					.getDirectSupergroups(groups);
			//add previous groups to result set
			resultSet.addAll(groups);
			//trim previously searched groups from new groups
			newGroups.removeAll(resultSet);
			//reset pass
			groups = newGroups;
		}
		//add implicit "EVERYONE" group
		resultSet.add(0);
		
		return resultSet;
	}
	
	/**
	 * Gets all users in the given groups
	 * @param groups		the groups to search (must not be null)
	 * 						(note that as an internal method, there are no 
	 * 						validity checks)
	 * @return every user that is a member (direct or indirect) of any of the 
	 * 	groups
	 */
	public static Set<Integer> allUsersInGroups(Set<Integer> groups) {
		//shortcut escape for implicit "EVERYONE" group
		if (groups.contains(0)) {
			return DAOFactory.getDAOFactoryInstance().getGroupDAO()
				.getAllUsers();
		}
		
		//recursively add all members of the authorized groups, and their 
		// subgroups to the users set
		
		//the groups that we've already added the members of
		Set<Integer> alreadyAdded = new HashSet<Integer>();
		
		//The set of users to return
		Set<Integer> users = new HashSet<Integer>();
		
		//while we are still searching groups
		while (!groups.isEmpty()) {
			//the IDs of the group members
			users.addAll(
					DAOFactory.getDAOFactoryInstance().getGroupDAO()
						.getMembers(groups));
			
			//reset for next round
			alreadyAdded.addAll(groups);
			groups = 
				DAOFactory.getDAOFactoryInstance().getGroupDAO()
					.getDirectSubgroups(groups);
			groups.removeAll(alreadyAdded);
		}
		
		return users;
	}

}
