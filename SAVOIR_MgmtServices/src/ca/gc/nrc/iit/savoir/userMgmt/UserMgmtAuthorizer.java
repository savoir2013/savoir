// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.userMgmt;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

import ca.gc.iit.nrc.savoir.domain.CredentialAuthorization;
import ca.gc.iit.nrc.savoir.domain.Role;
import ca.gc.iit.nrc.savoir.domain.Session;
import ca.gc.iit.nrc.savoir.domain.User;
import ca.gc.iit.nrc.savoir.domain.CredentialAuthorization.CredentialAuthorizationRight;
import ca.gc.iit.nrc.savoir.domain.Role.Right;
import ca.gc.nrc.iit.savoir.dao.impl.DAOFactory;
import ca.gc.nrc.iit.savoir.scenarioMgmt.ScenarioMgrImpl;
import ca.gc.nrc.iit.savoir.mgmtUtils.Constants.UserMgmtAction;

import static ca.gc.nrc.iit.savoir.userMgmt.GraphUtils.*;
import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.*;



/**
 * Class to make user authorization decisions for various management services.
 * Most authorizations are decided based on assigning a {@code Role} object to 
 * a user or group. A Role models a fixed set of Rights, which give the user or 
 * group the role is assigned to certain privileges on the entity they have 
 * been authorized on with the role (this may be a group, a session, the SAVOIR 
 * system, etc.)
 * 
 * @author Aaron Moss
 */
public class UserMgmtAuthorizer {
	
	//Role ID of the sysadmin role
	private static final int ADMIN_ROLE_ID = 1;
	
	private static UserMgmtAuthorizer authorizer;
	

	private static final Logger logger = 
		Logger.getLogger(UserMgmtAuthorizer.class);
	
	public UserMgmtAuthorizer() {}
	
	/**
	 * @return singleton UserMgmtAuthorizer instance
	 */
	public static UserMgmtAuthorizer getAuthorizer() {
		if (authorizer == null) {
			authorizer = new UserMgmtAuthorizer();
		}
		return authorizer;
	}
	
	//------------------------------------------------
	// Public methods
	//------------------------------------------------
	
	/**
	 * Checks if a principal is authorised to perform the requested action
	 * 
	 * @param principal		The user performing the action
	 * @param action		The action to be performed
	 * 
	 * @return	<ul>
	 * 			<li>{@code ALLOW} ({@code true})
	 * 				if the principal is allowed to perform the action on the 
	 * 				subject
	 * 			<li>{@code DENY} ({@code false})
	 * 				otherwise (or if the principal or action is invalid)
	 * 			</ul>
	 */
	public boolean isAuthorized(int principal, UserMgmtAction action) {
		//check valid principal and action
		if (principal <= 0 || action == null) {
			return DENY;
		}
		
		switch (action) {
		case CREATE_USER:		//creating a new user
			//ALLOW on principal is sysadmin, 
			//DENY otherwise
			return isSysadmin(principal);
		
		case CREATE_GROUP:		//creating a new group
			//ALLOW for all principals
			return ALLOW;
		
		default:				//unhandled action
			//DENY in all cases
			return DENY;
		}
	}
	
	/**
	 * Checks if a principal is authorised to perform the requested action on 
	 * the given subject.
	 * 
	 * @param principal		The user performing the action
	 * @param action		The action to be performed
	 * @param subject		The user the action is being performed on
	 * 
	 * @return	<ul>
	 * 			<li>{@code ALLOW} ({@code true})
	 * 				if the principal is allowed to perform the action on the 
	 * 				subject
	 * 			<li>{@code DENY} ({@code false})
	 * 				otherwise (or if the principal, subject, or action is 
	 * 				invalid)
	 * 			</ul>
	 */
	public boolean isAuthorizedOnUser(int principal, UserMgmtAction action, 
			int subject) {
		//check valid principal, subject and action
		if (principal <= 0 || subject <= 0 || action == null) {
			return DENY;
		}
		
		switch (action) {

		case UPDATE_USER:		// updating a user's information
			//ALLOW on principal is subject, or principal is sysadmin, 
			//DENY otherwise
			return principal == subject ? ALLOW : isSysadmin(principal);
		
		case DELETE_USER:		// deleting a user from the system
			//DENY on principal is subject, 
			//ALLOW on principal is sysadmin, 
			//DENY otherwise
			return principal == subject ? DENY : isSysadmin(principal);
		
		default:				//unhandled action
			//DENY in all cases
			return DENY;
		}
	}
	
	/**
	 * Checks if a principal is authorised to perform the requested action on 
	 * the given subject.
	 * 
	 * @param principal		The user performing the action
	 * @param action		The action to be performed
	 * @param subject		The group the action is being performed on (0 for 
	 * 						all users)
	 * 
	 * @return	<ul>
	 * 			<li>{@code ALLOW} ({@code true})
	 * 				if the principal is allowed to perform the action on the 
	 * 				subject
	 * 			<li>{@code DENY} ({@code false})
	 * 				otherwise (or if the principal, subject, or action is 
	 * 				invalid)
	 * 			</ul>
	 */
	public boolean isAuthorizedOnGroup(int principal, UserMgmtAction action, 
			int subject) {
		//check valid principal, subject and action
		if (principal <= 0 || subject < 0 || action == null) {
			return DENY;
		}
		
		//sysadmins have full rights on the implicit "everyone" group
		if (subject == EVERYONE && isSysadmin(principal)) {
			return ALLOW;
		}
		
		switch (action) {
		case VIEW_MEMBERS:		// viewing membership information of a group
			//ALLOW on principal has VIEW_MEMBER on any of this group's 
			// supergroups
			
			//check principal has right directly on subject group
			Map<Integer, Role> authz = 
				DAOFactory.getDAOFactoryInstance().getGroupDAO()
					.getAuthorizedUsers(subject);
			
			Role authzRole = authz.get(principal);
			if (authzRole != null 
					&& grantsRight(authzRole.getRights(), 
							UserMgmtAction.VIEW_MEMBERS)) {
				return ALLOW;
			}
			
			//all authorizations on the subject and its supergroups
			//TODO make this nicer (less DAO calls)
			// this would benefit greatly from making VIEW_MEMBERS implicit in 
			// the rights set in a re-working of the set of available Rights
			Set<Integer> supergroups = 
				GraphUtils.allSupergroups(new HashSet<Integer>(Collections.singleton(subject)));
			
			//check direct user authorization
			//for each group to check authorizations on
			for (int supergroup : supergroups) {
				//for each user authorization on that group
				for (Map.Entry<Integer, Role> userAuthz 
						: DAOFactory.getDAOFactoryInstance().getGroupDAO()
							.getAuthorizedUsers(supergroup).entrySet()) {
					//if the authorization is granted to the principal,
					// and contains VIEW_MEMBERS rights
					if (userAuthz.getKey() == principal 
							&& grantsRight(userAuthz.getValue().getRights(), 
									UserMgmtAction.VIEW_MEMBERS)) {
						//authorize the principal
						return ALLOW;
					}
				}
			}
			
			//check group authorization
			Set<Integer> groups = new HashSet<Integer>();
			//for each group to check authorizations on
			for (int supergroup : supergroups) {
				//for each group authorization on that group
				for (Map.Entry<Integer, Role> groupAuthz 
						: DAOFactory.getDAOFactoryInstance().getGroupDAO()
							.getAuthorizedGroups(supergroup).entrySet()) {
					//if the authorization contains VIEW_MEMBERS rights
					if (grantsRight(groupAuthz.getValue().getRights(), 
							UserMgmtAction.VIEW_MEMBERS)) {
						//add this group to the set of groups to check
						groups.add(groupAuthz.getKey());
					}
				}
			}
			
			//check if principal belongs to any of the authorized groups
			return GraphUtils.userIsInAnyGroup(principal, groups);
			
		
		case MANAGE_GROUP:			// managing a group
		case UPDATE_CREDENTIALS:	// adding credentials on this group
			//ALLOW on principal has right on group, 
			//DENY otherwise
			return hasRightOnGroup(principal, action, subject);
		
		default:				//unhandled action
			//DENY in all cases
			return DENY;
		}
	}
	
	/**
	 * Checks if the principal is authorized to manage a user's authorization 
	 * on a group with a role
	 * 
	 * @param principal		The principal
	 * @param subject		The subject user
	 * @param subjectRole	The subject role (0 for none)
	 * @param subjectGroup	The subject group
	 * 
	 * @return	<ul>
	 * 			<li>{@code ALLOW} ({@code true})
	 * 				if the principal:
	 * 				<ol>
	 * 				<li>has {@code UPDATE_MEMBERS} rights on the subject group,
	 * 					</li>
	 * 				<li>and
	 * 					<ol>
	 * 					<li>is not changing the subject's role</li>
	 * 					<li>or
	 * 						<ol>
	 * 						<li>is not the subject,</li>
	 * 						<li>and has rights >= the subject on the subject 
	 * 							group,</li>
	 * 						<li>and has rights >= the subject role on the 
	 * 							subject group</li>
	 * 						</ol></li>
	 * 					</ol></li>
	 * 				<ol>
	 * 			</li>
	 * 			<li>{@code DENY} ({@code false}) otherwise</li>
	 * 			</ul>
	 */
	public boolean isAuthorizedOnMember(int principal, int subject, 
			int subjectRole, int subjectGroup) {
		//check parameter validity
		if (principal <= 0 || subject <= 0 || subjectRole < 0 
				|| subjectGroup <= 0) {
			return DENY;
		}
		
		//ALLOW if role is not changing, merely membership, and principal 
		//	posesses UPDATE_MEMBERS rights on the group.
		//DENY if principal is subject
		//DENY if the subject has rights on the subject group that are a 
		//	superset of the principal's rights
		//DENY if subject role encapsulates rights that are a superset of 
		//	principal's rights
		//ALLOW if principal possesses UPDATE_MEMBERS right on subject group
		//DENY otherwise
		
		//existing authorization of the subject user
		Role subjectAuthz = DAOFactory.getDAOFactoryInstance()
			.getUserDAO().getGroupAuthorizations(subject).get(subjectGroup);
		
		//true for role changing, false otherwise
		boolean roleChanging = 
			(subjectAuthz != null && subjectRole != subjectAuthz.getRoleId());
		
		//deny principal ability to change own role
		if (roleChanging && principal == subject) {
			return DENY;
		}
		
		//Role to check authorization on
		Role checkRole = 
			DAOFactory.getDAOFactoryInstance().getRoleDAO()
				.getRoleById(subjectRole);
		
		//find roles authorized to manage this member
		List<Role> roles = 
			DAOFactory.getDAOFactoryInstance().getRoleDAO().getRoles();
		
		Set<Integer> usableRoles = new HashSet<Integer>();
		for (Role r : roles) {
			//check for grant of UPDATE_MEMBERS, 
			// role not changing
			// or superset of both subject user rights and subject role rights,
			if (grantsRight(r.getRights(), UserMgmtAction.UPDATE_MEMBERS)
				&& (!roleChanging
					|| ((subjectAuthz == null 
							|| isRightsSuperset(r.getRights(), 
									subjectAuthz.getRights()))
						&& (subjectRole == 0 
							|| isRightsSuperset(r.getRights(), 
									checkRole.getRights()))
						)
					)
				) {
				usableRoles.add(r.getRoleId());
			}
		}
		
		//deny if no such role
		if (usableRoles.isEmpty()) {
			return DENY;
		}
		
		//check if the principal holds any of the roles directly on the group
		if (DAOFactory.getDAOFactoryInstance().getGroupDAO()
				.getAuthorizedUsersByRole(subjectGroup, usableRoles)
				.contains(principal)) {
			return ALLOW;
		}
		
		//check if the principal is a member of any groups holding any of the 
		// roles on the group
		Set<Integer> groups = DAOFactory.getDAOFactoryInstance()
			.getGroupDAO().getAuthorizedSubgroupsByRole(subjectGroup, 
					usableRoles);

		return userIsInAnyGroup(principal, groups);
	}
	
	/**
	 * Checks if the principal is authorized to manage a group's authorization 
	 * on another group with a role
	 * 
	 * @param principal		The principal
	 * @param subject		The subject authorized group
	 * @param subjectRole	The subject role (0 for none)
	 * @param subjectGroup	The subject group authorized on
	 * 
	 * @return	<ul>
	 * 			<li>{@code ALLOW} ({@code true})
	 * 				if the principal:
	 * 				<ol>
	 * 				<li>has {@code UPDATE_MEMBERS} rights on the subject 
	 * 					supergroup,</li>
	 * 				<li>and
	 * 					<ol>
	 * 					<li>is not changing the subject's role</li>
	 * 					<li>or
	 * 						<ol>
	 * 						<li>is not a member (directly or indirectly) of the 
	 * 							subject,</li>
	 * 						<li>and has rights >= the subject on the subject 
	 * 							supergroup,</li>
	 * 						<li>and has rights >= the subject role on the 
	 * 							subject supergroup</li>
	 * 						</ol></li>
	 * 					</ol></li>
	 * 				<ol>
	 * 			</li>
	 * 			<li>{@code DENY} ({@code false}) otherwise</li>
	 * 			</ul>
	 */
	public boolean isAuthorizedOnSubgroup(int principal, int subject, 
			int subjectRole, int subjectGroup) {
		//check parameter validity
		if (principal <= 0 || subject <= 0 || subjectRole < 0 
				|| subjectGroup <= 0) {
			return DENY;
		}
		
		//ALLOW if principal posesses UPDATE_MEMBERS rights on the group, and 
		//	role is not changing
		//DENY if principal is member of subject
		//DENY if the subject has rights on the subject group that are a 
		//	superset of the principal's rights
		//DENY if subject role encapsulates rights that are a superset of 
		//	principal's rights
		//ALLOW if principal possesses UPDATE_MEMBERS right on subject group
		//DENY otherwise
		
		//existing authorization of the subject group
		Role subjectAuthz = DAOFactory.getDAOFactoryInstance().getGroupDAO()
			.getAuthorizations(subject).get(subjectGroup);
		
		//true for role changing, false otherwise
		boolean roleChanging = 
			(subjectAuthz != null && subjectRole != subjectAuthz.getRoleId());
		
		//deny if principal is a member of the subject group
		if (roleChanging 
				&& allSubgroups(new HashSet<Integer>(Collections.singleton(subject))).
						contains(principal)) {
			return DENY;
		}
		
		//Role to check authorization on
		Role checkRole = 
			DAOFactory.getDAOFactoryInstance().getRoleDAO()
				.getRoleById(subjectRole);
		
		//find roles authorized to manage this subgroup
		List<Role> roles = 
			DAOFactory.getDAOFactoryInstance().getRoleDAO().getRoles();
		
		Set<Integer> usableRoles = new HashSet<Integer>();
		for (Role r : roles) {
			//check for grant of UPDATE_MEMBERS,
			// role not changing
			// or superset of both subject user rights and subject role rights
			if (grantsRight(r.getRights(), UserMgmtAction.UPDATE_MEMBERS)
				&& (!roleChanging 
					|| ((subjectAuthz == null 
							|| isRightsSuperset(r.getRights(), 
									subjectAuthz.getRights()))
						&& (subjectRole == 0 
							|| isRightsSuperset(r.getRights(), 
									checkRole.getRights()))
						)
					)
				) {
				usableRoles.add(r.getRoleId());
			}
		}
		
		//deny if no such role
		if (usableRoles.isEmpty()) {
			return DENY;
		}
		
		//check if the principal holds any of the roles directly on the group
		if (DAOFactory.getDAOFactoryInstance()
				.getGroupDAO().getAuthorizedUsersByRole(subjectGroup, 
						usableRoles)
				.contains(principal)) {
			return ALLOW;
		}
		
		//check if the principal is a member of any groups holding any of the 
		// roles on the group
		Set<Integer> groups = DAOFactory.getDAOFactoryInstance()
			.getGroupDAO().getAuthorizedSubgroupsByRole(subjectGroup, 
					usableRoles);

		return userIsInAnyGroup(principal, groups);
	}
	
	/**
	 * Checks if a principal is authorized to manage the given role on the 
	 * given group
	 * 
	 * @param principal		The user we check authorization for
	 * @param subjectRole	The role needing management
	 * 
	 * @return	<ul>
	 * 			<li>{@code ALLOW} ({@code true})
	 * 				if the principal is authorized to perform the given role 
	 * 				management action by a different role than that managed
	 * 			<li>{@code DENY} ({@code false})
	 * 				otherwise
	 * 			</ul>
	 */
	public boolean isAuthorizedOnRole(int principal, int subjectRole) {
		//check parameter validity
		if (principal <= 0 || subjectRole <= 0) {
			return DENY;
		}
		
		//DENY if principal possesses subject role,
		//DENY if subject role encapsulates rights that are a superset of 
		//	principal's rights
		//ALLOW if principal possesses UPDATE_MEMBERS right on subject group
		//DENY otherwise
		
		Role subject = 
			DAOFactory.getDAOFactoryInstance().getRoleDAO()
				.getRoleById(subjectRole);
		
		if(subject == null) {
			return DENY;
		}

		//check subject role for principal
		if(DAOFactory.getDAOFactoryInstance()
				.getRoleDAO().getUsersByRole(subjectRole, /*TODO FIXME*/0)
				.contains(principal)) {
			return DENY;
		}
		if(allSubgroups(DAOFactory.getDAOFactoryInstance()
				.getRoleDAO().getGroupsByRole(subjectRole, /*TODO FIXME*/0)
				).contains(principal)) {
			return DENY;
		}
		
		//find roles authorized to manage this role
		List<Role> roles = 
			DAOFactory.getDAOFactoryInstance().getRoleDAO().getRoles();
		
		Set<Integer> usableRoles = new HashSet<Integer>();
		for (Role r : roles) {
			//if this is not the role in question
			if (subjectRole != r.getRoleId() 
					&& grantsRight(r.getRights(), UserMgmtAction.UPDATE_MEMBERS)
					&& isRightsSuperset(r.getRights(), subject.getRights())) {
				usableRoles.add(r.getRoleId());
			}
		}
		
		//deny if no such role
		if (usableRoles.isEmpty()) {
			return DENY;
		}
				
		//check if the principal holds any of the roles directly on the group
		if (DAOFactory.getDAOFactoryInstance()
				.getGroupDAO().getAuthorizedUsersByRole(/*TODO FIXME*/0, 
						usableRoles)
				.contains(principal)) {
			return ALLOW;
		}
		
		//check if the principal is a member of any groups holding any of the 
		// roles on the group
		Set<Integer> groups = DAOFactory.getDAOFactoryInstance()
			.getGroupDAO().getAuthorizedSubgroupsByRole(/*TODO FIXME*/0, 
					usableRoles);

		return userIsInAnyGroup(principal, groups);
	}
	
	/**
	 * Checks authorization to create a role on a given group
	 * 
	 * @param principal		The user requesting the creation
	 * @param rights		The rights to be assigned to the new role
	 * 
	 * @return	<ul>
	 * 			<li>{@code ALLOW} ({@code true})
	 * 				if the principal possesses some role of greater rights on 
	 * 				the group
	 * 			<li>{@code DENY} ({@code false})
	 * 				otherwise
	 * 			</ul>
	 */
	public boolean isAuthorizedToCreateRole(int principal, Set<Right> rights) {
		//ALLOW if the principal possesses some role on the group of greater 
		//	rights
		//DENY otherwise
		
		//find roles authorized to manage this role
		List<Role> roles = 
			DAOFactory.getDAOFactoryInstance().getRoleDAO().getRoles();
		
		Set<Integer> usableRoles = new HashSet<Integer>();
		for (Role r : roles) {
			//if this is not the role in question
			if (grantsRight(r.getRights(), UserMgmtAction.UPDATE_MEMBERS)
					&& isRightsSuperset(r.getRights(),rights)) {
				usableRoles.add(r.getRoleId());
			}
		}
		
		//deny if no such role
		if (usableRoles.isEmpty()) {
			return DENY;
		}
				
		//check if the principal holds any of the roles directly on the group
		if (DAOFactory.getDAOFactoryInstance()
				.getGroupDAO().getAuthorizedUsersByRole(/*TODO FIXME*/0, 
						usableRoles)
				.contains(principal)) {
			return ALLOW;
		}
		
		//check if the principal is a member of any groups holding any of the 
		// roles on the group
		Set<Integer> groups = DAOFactory.getDAOFactoryInstance()
			.getGroupDAO().getAuthorizedSubgroupsByRole(/*TODO FIXME*/0, 
					usableRoles);
		
		return userIsInAnyGroup(principal, groups);
	}
	
	/**
	 * Checks if the caller possesses a right on a credential
	 * 
	 * @param principal		The user to check
	 * @param right			The right in question
	 * @param credential	The credential in question
	 * 
	 * @return	<ul>
	 * 			<li>{@code ALLOW} ({@code true})
	 * 				<ul>
	 * 				<li>if the caller possesses the right on the credential,
	 * 				<li>or if the caller has UPDATE_CREDENTIALS rights on a 
	 * 					group that possesses the right on the credential,
	 * 				<li>or if the right is granted on that credential to all 
	 * 					users
	 * 				</ul></li>
	 * 			<li>{@code DENY} ({@code false}) otherwise</li>
	 * 			</ul>
	 */
	public boolean isAuthorizedOnCred(int principal, 
			CredentialAuthorizationRight right, int credential) {
		//ALLOW if the principal possesses "right" on the credential
		//ALLOW if all users possess "right" on the credential 
		//ALLOW if a group the principal has UPDATE_CREDENTIALS rights on 
		//  possesses "right" on the credential
		//DENY otherwise
		
		if (principal <= 0 || right == null || credential <= 0) {
			return DENY;
		}
		
		List<CredentialAuthorization> authorized = 
			DAOFactory.getDAOFactoryInstance().getCredentialDAO()
				.getAuthorizations(credential);
		
		for (CredentialAuthorization ca : authorized) {
			if (	(//principal is authorized on credential
					ca.getUserId() == principal
					//all users are authorized on credential
					|| (ca.getUserId() == 0 && ca.getGroupId() == 0))
					//and the authorization includes the desired right
					&& ca.getRights().contains(right)
				) {
				return ALLOW;
			}
		}
		
		//having failed in search by user, repeat with groups
		//(getting the groups can be moderately expensive, 
		// so I don't want to do it if I don't have to)
		Set<Integer> groups = 
			allGroupsWithRight(principal, UserMgmtAction.UPDATE_CREDENTIALS);
		
		for (CredentialAuthorization ca : authorized) {
			if (	//group principal has credential mgmt rights on is 
					// authorized on cred
					(ca.getGroupId() != 0 && groups.contains(ca.getGroupId()))
					//and the authorization includes the desired right
					&& ca.getRights().contains(right)
				) {
				return ALLOW;
			}
		}
		
		//no authorization
		return DENY;
	}
	
	/**
	 * Checks if the caller has the right to grant a credential.
	 * If both {@code subjectUser} and {@code subjectGroup} are equal to 0, 
	 * checks if the caller has the right to grant the credential to everyone.
	 * 
	 * @param principal		The user to check
	 * @param granting		The rights attempted to be granted
	 * @param credOn		The credential being granted
	 * @param resourceOn	The resource that credential is being granted on
	 * @param subjectUser	The user being granted to (0 for none)
	 * @param subjectGroup	The group being granted to (0 for none)
	 * 
	 * @return	<ul>
	 * 			<li>{@code ALLOW} ({@code true})
	 * 				<ul>
	 * 				<li>if the caller possesses the sufficient rights to grant 
	 * 					or revoke the credential to the given user or group, 
	 * 					as applicable,
	 * 				<li>or if the caller has UPDATE_CREDENTIALS rights on a 
	 * 					group that possesses sufficient rights on the 
	 * 					credential,
	 * 				<li>or if sufficient rights are granted to all users, and 
	 * 					the principal is a sysadmin 
	 * 				</ul></li>
	 * 			<li>{@code DENY} ({@code false}) otherwise</li>
	 * 			</ul>
	 */
	public boolean isGrantAuthorizedOnCred(int principal, 
			List<CredentialAuthorizationRight> granting, int credOn, 
			int resourceOn, int subjectUser, int subjectGroup) {
		
		//check that principal has right to view subject
		if (subjectUser == EVERYONE && subjectGroup == EVERYONE
				&& !isSysadmin(principal)) {
			return DENY;
		}
		if (subjectUser != NO_USER 
				&& !allAuthorizedUsers(principal, UserMgmtAction.VIEW_MEMBERS)
					.contains(subjectUser)) {
			return DENY;
		}
		if (subjectGroup != NO_GROUP 
				&& !isAuthorizedOnGroup(principal, UserMgmtAction.VIEW_MEMBERS, 
						subjectGroup)) {
			return DENY;
		}
		
		//look for existing grant of this credential
		List<CredentialAuthorization> existingAuthz = 
			DAOFactory.getDAOFactoryInstance().getCredentialDAO().
				getAuthorizationsByResource(credOn, resourceOn);
		
		boolean grantExists = false;
		if (subjectUser != NO_USER) {
			for (CredentialAuthorization ca : existingAuthz) {
				if (ca.getUserId() == subjectUser) {
					grantExists = true;
					break;
				}
			}
		} else if (subjectGroup != NO_GROUP) {
			for (CredentialAuthorization ca : existingAuthz) {
				if (ca.getGroupId() == subjectGroup) {
					grantExists = true;
					break;
				}
			}
		} else if (subjectUser == EVERYONE && subjectGroup == EVERYONE) {
			for (CredentialAuthorization ca : existingAuthz) {
				if (ca.getUserId() == EVERYONE && ca.getGroupId() == EVERYONE) {
					grantExists = true;
					break;
				}
			}
		}
		
		//find rights required to perform this grant
		Set<CredentialAuthorizationRight> rightsRequired;
		//if there is an existing grant, or this is a revokation 
		if (grantExists || granting.isEmpty()) {
			rightsRequired = new HashSet<CredentialAuthorizationRight>(
					Arrays.asList(CredentialAuthorizationRight.GRANT_VIEW, 
							CredentialAuthorizationRight.GRANT_UPDATE, 
							CredentialAuthorizationRight.GRANT_DELETE));
		} else {	
			//new credential grant - just have to have rights to grant what 
			// you're granting
			rightsRequired = new HashSet<CredentialAuthorizationRight>();
			for (CredentialAuthorizationRight right : granting) {
				switch(right) {
				case VIEW:
				case GRANT_VIEW:
					rightsRequired.add(CredentialAuthorizationRight.GRANT_VIEW);
					break;
				case UPDATE:
				case GRANT_UPDATE:
					rightsRequired.add(
							CredentialAuthorizationRight.GRANT_UPDATE);
					break;
				case DELETE:
				case GRANT_DELETE:
					rightsRequired.add(
							CredentialAuthorizationRight.GRANT_DELETE);
					break;
				}
			}
		}
		
		//look for authorization on principal
		for (CredentialAuthorization ca : existingAuthz) {
			if (ca.getUserId() == principal) {
				//user authorization on credential - check for required rights
				boolean passes = true;
				for (CredentialAuthorizationRight right : rightsRequired) {
					if (!ca.getRights().contains(right)) {
						passes = false;
						break;
					}
				}
				if (passes) {
					//if we have a good authorization
					return ALLOW;
				}
			}
		}
		
		//check for authorization to everyone
		if (isSysadmin(principal)) {
			for (CredentialAuthorization ca : existingAuthz) {
				if (ca.getUserId() == EVERYONE && ca.getGroupId() == EVERYONE) {
					//authorization to everyone on credential - check for 
					// required rights
					boolean passes = true;
					for (CredentialAuthorizationRight right : rightsRequired) {
						if (!ca.getRights().contains(right)) {
							passes = false;
							break;
						}
					}
					if (passes) {
						//if we have a good authorization
						return ALLOW;
					}
				}
			}
		}
		
		//check groups for authorization
		Set<Integer> groups = 
			allGroupsWithRight(principal, UserMgmtAction.UPDATE_CREDENTIALS);
		
		for (CredentialAuthorization ca : existingAuthz) {
			if (groups.contains(ca.getGroupId())) {
				//group authorization on credential - check for required rights
				boolean passes = true;
				for (CredentialAuthorizationRight right : rightsRequired) {
					if (!ca.getRights().contains(right)) {
						passes = false;
						break;
					}
				}
				if (passes) {
					//if we have a good authorization
					return ALLOW;
				}
			}
		}
		
		return DENY;	//no authorization
	}
	
	/**
	 * Checks if the principal has a given right on a session
	 * 
	 * @param principal		The user to check the rights of
	 * @param right			The right to check for
	 * @param session		The session to check it on
	 * 
	 * @return	<ul>
	 * 			<li>{@code ALLOW} ({@code true})
	 * 				<ol>
	 * 				<li>if the principal has a role granting that right 
	 * 					directly on the session
	 * 				<li>or, if the principal belongs to a group with such a role
	 * 				</ol></li>
	 * 			<li>{@code DENY} ({@code false}) otherwise</li>
	 * 			</ul>
	 */
	public boolean isAuthorizedOnSession(int principal, Right right, 
			int session) {
		//check valid principal, subject and action
		if (principal <= 0 || session <= 0 || right == null) {
			return DENY;
		}
		
		switch (right) {
		case VIEW_MEMBERS:	/* Get the users/groups authorized on this session*/
		case SCENARIO_RUN:	/* Run a scenario */
		case AUTHORED_RUN:	/* Direct an authored scenario */
		case SCENARIO_EDIT:	/* Edit/author a scenario */
		case SESSION_EDIT:	/* Revise session notes after run */
		case AUDIT:			/* View system logs */
			//check if the principal has the right directly on the session
			Map<Integer, Role> authz = 
				DAOFactory.getDAOFactoryInstance().getSessionDAO()
					.getAuthorizedUsers(session);
			
			Role authzRole = authz.get(principal);
			if (authzRole != null && authzRole.getRights().contains(right)) {
				return ALLOW;
			}
			
			//check if any groups have the right on the group
			authz = 
				DAOFactory.getDAOFactoryInstance().getSessionDAO()
					.getAuthorizedGroups(session);
			
			Set<Integer> groups = new HashSet<Integer>();
			for (Map.Entry<Integer, Role> entry : authz.entrySet()) {
				//every group which has the right we add to a list to search
				if (entry.getValue().getRights().contains(right)) {
					groups.add(entry.getKey());
				}
			}
			
			if (groups.isEmpty()) {
				return DENY;
			}
			
			//check if the principal belongs to any of these groups
			return GraphUtils.userIsInAnyGroup(principal, groups);
		
		default:				//unhandled action
			//DENY in all cases
			return DENY;
		}
	}
	
	/**
	 * Checks if the principal is authorized to manage a user's authorization 
	 * on a session with a role
	 * 
	 * @param principal			The principal
	 * @param subject			The subject user
	 * @param subjectRole		The subject role (0 for none)
	 * @param subjectSession	The subject session
	 * 
	 * @return	<ul>
	 * 			<li>{@code ALLOW} ({@code true})
	 * 				if the principal:
	 * 				<ol>
	 * 				<li>is not the subject,
	 * 				<li>and has {@code UPDATE_MEMBERS} rights on the subject 
	 * 					session,
	 * 				<li>and has rights >= the subject on the subject session,
	 * 				<li>and has rights >= the subject role on the subject 
	 * 					session
	 * 				</ol></li>
	 * 			<li>{@code DENY} ({@code false}) otherwise</li>
	 * 			</ul>
	 */
	public boolean isAuthorizedOnUserSessionAuthorization(
			int principal, int subject, int subjectRole, int subjectSession) {
		//check parameter validity
		if (principal <= 0 || subject <= 0 || subjectRole < 0 
				|| subjectSession <= 0) {
			return DENY;
		}
		
		//DENY if principal is subject
		//DENY if the subject has rights on the subject session 
		//	that are a superset of the principal's rights
		//DENY if subject role encapsulates rights that are a superset of 
		//	principal's rights
		//ALLOW if principal possesses UPDATE_MEMBERS right on subject session
		//DENY otherwise
		
		if (principal == subject) {
			return DENY;
		}
		
		Map<Integer, Role> sessionAuthz = 
			DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.getAuthorizedUsers(subjectSession);
		
		//existing authorization of the subject user
		Role subjectAuthz = sessionAuthz.get(subject);
		
		//Role to check authorization on
		Role checkRole = 
			DAOFactory.getDAOFactoryInstance().getRoleDAO()
				.getRoleById(subjectRole);
		
		//find roles authorized to manage this member
		List<Role> roles = 
			DAOFactory.getDAOFactoryInstance().getRoleDAO().getRoles();
		
		Set<Integer> usableRoles = new HashSet<Integer>();
		for (Role r : roles) {
			//check for grant of UPDATE_MEMBERS, 
			// superset of both subject user rights and subject role rights
			if (grantsRight(r.getRights(), UserMgmtAction.UPDATE_MEMBERS)
					&& (subjectAuthz == null 
							|| isRightsSuperset(r.getRights(), 
									subjectAuthz.getRights()))
					&& (subjectRole == 0 
							|| isRightsSuperset(r.getRights(), 
									checkRole.getRights()))) {
				usableRoles.add(r.getRoleId());
			}
		}
		
		//deny if no such role
		if (usableRoles.isEmpty()) {
			return DENY;
		}
		
		//check if the principal holds any of the roles directly on the session
		Role principalRole = sessionAuthz.get(principal);
		if (usableRoles.contains(principalRole.getRoleId())) {
			return ALLOW;
		}
		
		//check if the principal is a member of any groups holding any of the 
		// roles on the session
		Set<Integer> groups = new HashSet<Integer>();
		Map<Integer, Role> authorizedGroups = 
			DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.getAuthorizedGroups(subjectSession);
		
		for (Map.Entry<Integer, Role> authz : authorizedGroups.entrySet()) {
			if (usableRoles.contains(authz.getValue().getRoleId())) {
				groups.add(authz.getKey());
			}
		}

		return userIsInAnyGroup(principal, groups);
	}
	
	/**
	 * Checks if the principal is authorized to manage a group's authorization 
	 * on a session with a role
	 * 
	 * @param principal			The principal
	 * @param subject			The subject group (0 for all users)
	 * @param subjectRole		The subject role (0 for none)
	 * @param subjectSession	The subject session
	 * 
	 * @return	<ul>
	 * 			<li>{@code ALLOW} ({@code true})
	 * 				<ol>
	 * 				<li>if the principal has {@code UPDATE_MEMBERS} rights on 
	 * 					the subject session</li>
	 * 				<li>and
	 * 					<ul>
	 * 					<li>
	 * 						<ol>
	 * 						<li>the principal is a sysadmin,</li>
	 * 						<li>and the subject group is {@code 0} (all 
	 * 							users)</li>
	 * 						</ol></li>
	 * 					<li>or the principal:
	 * 						<ol>
	 * 						<li>is not a member (directly or indirectly) of the 
	 * 							subject group,</li>
	 * 						<li>and has {@code UPDATE_MEMBERS} rights on the 
	 * 							subject session,</li>
	 * 						<li>and has rights >= the subject on the subject 
	 * 							session,</li>
	 * 						<li>and has rights >= the subject role on the 
	 * 							subject session</li>
	 * 						</ol></li>
	 * 					</ul>
	 * 				</ol></li>
	 * 			<li>{@code DENY} ({@code false}) otherwise</li>
	 * 			</ul>
	 */
	public boolean isAuthorizedOnGroupSessionAuthorization(
			int principal, int subject, int subjectRole, int subjectSession) {
		
		//check parameter validity
		if (principal <= 0 || subject < 0 || subjectRole < 0 
				|| subjectSession <= 0) {
			return DENY;
		}
		
		//DENY if the subject group is all users, and the principal is not a 
		//	sysadmin
		//DENY if principal is member of subject (unless the subject is all 
		//	users)
		//DENY if the subject has rights on the subject session that are a 
		//	superset of the principal's rights
		//DENY if subject role encapsulates rights that are a superset of 
		//	principal's rights
		//ALLOW if principal possesses UPDATE_MEMBERS right on subject session
		//DENY otherwise
		
		//deny if non-sysadmin trying to set rights for all users
		if (subject == 0 && !isSysadmin(principal)) {
			return DENY;
		}
		
		//deny if principal is a member of the subject group (unless the 
		// subject group is all users) 
		if (subject != 0 && 
				allSubgroups(new HashSet<Integer>(Collections.singleton(subject)))
					.contains(principal)) {
			return DENY;
		}
		
		Map<Integer, Role> sessionAuthz = 
			DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.getAuthorizedGroups(subjectSession);
	
		//existing authorization of the subject group
		Role subjectAuthz = sessionAuthz.get(subject);
		
		//Role to check authorization on (ignore in case where subject group is 
		// all users
		Role checkRole = 
//			(subject == 0) ?
//					null :
			(subjectRole == 0) ?
					null :
					DAOFactory.getDAOFactoryInstance().getRoleDAO()
						.getRoleById(subjectRole);
		
		//find roles authorized to manage this subgroup
		List<Role> roles = 
			DAOFactory.getDAOFactoryInstance().getRoleDAO().getRoles();
		
		Set<Integer> usableRoles = new HashSet<Integer>();
		for (Role r : roles) {
			//check for grant of UPDATE_MEMBERS,
			// superset of both subject user rights and subject role rights
			try
			{
				if (grantsRight(r.getRights(), UserMgmtAction.UPDATE_MEMBERS)
					&& (subjectAuthz == null
						|| isRightsSuperset(r.getRights(), subjectAuthz.getRights()))
//					&& (subjectRole == 0 || subject == 0
//						|| isRightsSuperset(r.getRights(), 
//							checkRole.getRights()))) 
					&& (checkRole == null || isRightsSuperset(r.getRights(), checkRole.getRights())))
				{
					usableRoles.add(r.getRoleId());
				}
			}
			catch(Exception e)
			{
				logger.error("Check for grant on UPDATE.MEMBERS failed\n" + e.getMessage());
				//e.printStackTrace();
			}
		}
		
		//deny if no such role
		if (usableRoles.isEmpty()) {
			return DENY;
		}
		
		//check if the principal holds any of the roles directly on the session
		Role principalRole = DAOFactory.getDAOFactoryInstance().getSessionDAO()
			.getAuthorizedUsers(subjectSession).get(principal);
		
		if (principalRole != null && usableRoles.contains(principalRole.getRoleId())) {
			return ALLOW;
		}
		
		//check if the principal is a member of any groups holding any of the 
		// roles on the session
		Set<Integer> groups = new HashSet<Integer>();
		for (Map.Entry<Integer, Role> authz : sessionAuthz.entrySet()) {
			if (usableRoles.contains(authz.getValue().getRoleId())) {
				groups.add(authz.getKey());
			}
		}
		
		boolean allowed = userIsInAnyGroup(principal, groups);
		return allowed;
	}
	
	/**
	 * Checks if the principal, a user, is authorized on a resource
	 * 
	 * @param principal			The principal
	 * @param resource			The subject session
	 * 
	 * @return	<ul>
	 * 			<li>{@code ALLOW} ({@code true}) if the principal, or any 
	 * 				group they belong to, has any credential on the resource
	 * 			<li>{@code DENY} ({@code false}) otherwise
	 * 			</ul>
	 */
	public boolean userIsAuthorizedOnResource(int principal, int resource) {
		//check user credentials
		List<Integer> creds = DAOFactory.getDAOFactoryInstance()
			.getCredentialDAO().getUserCredentials(principal, resource);
		
		if (creds != null && !creds.isEmpty()) return ALLOW;
		
		//get all user groups
		Set<Integer> groups = 
			GraphUtils.allSupergroups(DAOFactory.getDAOFactoryInstance()
					.getUserDAO().getMemberships(principal));
		
		//check group credentials
		creds = DAOFactory.getDAOFactoryInstance().getCredentialDAO().
			getGroupCredentials(groups, resource);
		
		return (creds != null && !creds.isEmpty()) ? ALLOW : DENY;
	}
	
	/**
	 * Checks if the principal, a group, is authorized on a resource
	 * 
	 * @param principal			The principal
	 * @param resource			The subject session
	 * 
	 * @return	<ul>
	 * 			<li>{@code ALLOW} ({@code true}) if the principal, or any 
	 * 				group it is a subgroup of, has any credential on the 
	 * 				resource
	 * 			<li>{@code DENY} ({@code false}) otherwise
	 * 			</ul>
	 */
	public boolean groupIsAuthorizedOnResource(int principal, int resource) {
		//check group credentials
		Set<Integer> groups = new HashSet<Integer>(Collections.singleton(principal));
		
		List<Integer> creds = DAOFactory.getDAOFactoryInstance()
			.getCredentialDAO().getGroupCredentials(groups, resource);
		
		if (creds != null && !creds.isEmpty()) return ALLOW;
		
		//check supergroup credentials
		groups = allSupergroups(groups);
		groups.remove(principal);
		
		creds = DAOFactory.getDAOFactoryInstance().getCredentialDAO()
			.getGroupCredentials(groups, resource);
		
		return (creds != null && !creds.isEmpty()) ? ALLOW : DENY;
	}
	
	
	
	/**
	 * Gets all the users this principal is allowed to perform the given action 
	 * on
	 * 
	 * @param principal		The user performing the action
	 * @param action		The action to be performed
	 * 
	 * 
	 * @return	The set of all users the principal is authorised to perform the 
	 * 			action on, empty set for invalid principal or action
	 */
	public Set<Integer> allAuthorizedUsers(int principal, 
			UserMgmtAction action) {
		
		//check non-null, non-empty principal and action
		if (principal <= 0 || action == null) {
			return Collections.emptySet();
		}
		
		Set<Integer> groups;
		switch (action) {
		case VIEW_MEMBERS:	//if this is a user view action
			//get all the groups the principal is authorised to view
			groups = allGroupsWithRight(principal, UserMgmtAction.VIEW_MEMBERS);
			//get all those groups' subgroups, recursively (VIEW is the only 
			// recursive right)
			groups = allSubgroups(groups);
			return getAuthorizedUsers(principal, action, groups);
		
		case UPDATE_USER:
			if (isSysadmin(principal)) {
				groups = DAOFactory.getDAOFactoryInstance().getGroupDAO()
					.getRootGroups();
				return allUsersInGroups(groups);
			} else {
				return new HashSet<Integer>(Collections.singleton(principal));
			}
		
		default:			//unhandled action
			//no authorised users for an unhandled action
			return Collections.emptySet();
		}
	}
	
	/**
	 * Same as allAuthorizedUsers, except has groups precomputed (should be 
	 * from allAuthorizedGroups)
	 * 
	 * @param principal		The user performing the action
	 * @param action		The action to be performed
	 * @param groups		The groups the principal is authorized to perform 
	 * 						the action on
	 * 
	 * @return The set of all users the principal is authorised to perform the 
	 * 		action on who are in the given groups, or in no group at all (NOTE: 
	 * 		this assumes that the principal is authorized on the groups), empty 
	 * 		set for invalid principal or action
	 */
	public Set<Integer> allAuthorizedUsersInGroups(int principal, 
			UserMgmtAction action, Set<Integer> groups) {
		
		//check non-null, non-empty principal and action
		if (principal <= 0 || action == null) {
			return Collections.emptySet();
		}
		
		return getAuthorizedUsers(principal, action, groups);
	}
	
	/**
	 * Gets all users the principal is authorized to perform the given action 
	 * on
	 * 
	 * @param principal		The user performing the action
	 * @param action		The action to be performed
	 * @param groups		The groups the principal is authorized to perform 
	 * 						the action on
	 * 
	 * @return The set of all users the principal is authorised to perform the 
	 * 		action on who are in the given groups, or in no group at all (NOTE: 
	 * 		this assumes that the principal is authorized on the groups), empty 
	 * 		set for invalid principal or action
	 */
	private Set<Integer> getAuthorizedUsers(int principal, 
			UserMgmtAction action, Set<Integer> groups) {
		switch (action) {
		case VIEW_MEMBERS:	//if this is a user view action
			//get all users in those groups
			return allUsersInGroups(groups);
		
		default:			//unhandled action
			//no authorised users for an unhandled action
			return Collections.emptySet();
		}
	}
	
	/**
	 * Gets all the groups this principal has an authorization for this action 
	 * on
	 * 
	 * @param principal		The user performing the action
	 * @param action		The action to be authorised
	 * 
	 * @return	The set of all groups the principal has an authorization for 
	 * 			this action on, an empty set for invalid principal, or null 
	 * 			action
	 */
	public Set<Integer> allAuthorizedGroups(int principal, UserMgmtAction action) {
		//check non-null, non-empty principal and action
		if (principal <= 0 || action == null) {
			return Collections.emptySet();
		}
		
		switch (action) {
		case VIEW_MEMBERS:	//if this is a user view action
			//get all the groups the principal is authorised to view
			if (isSysadmin(principal)) {
				return DAOFactory.getDAOFactoryInstance().getGroupDAO()
					.getAllGroups();
			} else {
				return allSubgroups(allGroupsWithRight(
						principal, UserMgmtAction.VIEW_MEMBERS));
			}
		
		default:			//unhandled action
			//no authorised users for an unhandled action
			return Collections.emptySet();
		}
	}
	
	/**
	 * Gets all the sessions that this principal has this right on
	 * @param principal		The user performing the action
	 * @param right			The right needed to perform it
	 * @return	The set of all sessions the principal has this right on, 
	 * 			An empty set for invalid principal or null right
	 */
	public Set<Session> allAuthorizedSessions(int principal, Right right) {
		
		if (principal <= 0 || right == null) {
			return Collections.emptySet();
		}
		
		switch (right) {
		case SCENARIO_RUN:	/* Run a scenario */
		case AUTHORED_RUN:	/* Direct an authored scenario */
		case SCENARIO_EDIT:	/* Edit/author a scenario */
		case SESSION_EDIT:	/* Revise session notes after run */
		case AUDIT:			/* View system logs */
			Set<Integer> results = new LinkedHashSet<Integer>();
			
			// get all the groups that this user has rights on
			Map<Integer, Role> authz = 
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getAuthoredSessionAuthorizations(principal);
			
			// for each group
			for (Map.Entry<Integer, Role> a : authz.entrySet()) {
				// if the right in question is granted
				if(a.getValue().getRights().contains(right)) {
					// add the group to the result set
					results.add(a.getKey());
				}
			}
			
			//get all the groups that this user belongs to
			Set<Integer> groups = allSupergroups(
					DAOFactory.getDAOFactoryInstance().getUserDAO()
						.getMemberships(principal));
			//and add the authorizations the user inherits from those group 
			// memberships
			Map<Integer, Set<Role>> groupAuthz = 
				DAOFactory.getDAOFactoryInstance().getGroupDAO()
					.getAuthoredSessionAuthorizations(groups);
			
			//for each group
			for (Map.Entry<Integer, Set<Role>> a : groupAuthz.entrySet()) {
				//for each authorization on that group
				for (Role s : a.getValue()) {
					// if the right in question is granted
					if(s.getRights().contains(right)) {
						// add the group to the result set
						results.add(a.getKey());
					}
				}
			}
			
			//get sessions from session IDs
			Set<Session> sessions = new LinkedHashSet<Session>();
			for (int sessionId : results) {
				sessions.add(DAOFactory.getDAOFactoryInstance().
						getSessionDAO().getSessionById(sessionId));
			}
			return sessions;
			
		default:			/* Unhandled right */
			return Collections.emptySet();
		}
	}
	
	//--------------------------------------------------------
	// helper methods
	//--------------------------------------------------------
	
	/**
	 * Checks if the principal is a system administrator
	 * 
	 * @param userName	The user performing the action
	 * 
	 * @return true for principal is sysadmin, false otherwise
	 */
	public final boolean isSysadmin(String userName) {
		User user = 
			DAOFactory.getDAOFactoryInstance().getUserDAO()
				.getUserByDN(userName);
		return isSysadmin(user);
	}
	
	/**
	 * Checks if the principal is a system administrator
	 * 
	 * @param principal	The user performing the action
	 * 
	 * @return true for principal is sysadmin, false otherwise
	 */
	public final boolean isSysadmin(int principal) {
		User user = 
			DAOFactory.getDAOFactoryInstance().getUserDAO()
				.getUserById(principal);
		return isSysadmin(user);
	}
	
	/**
	 * Checks if the principal is a system administrator
	 * 
	 * @param user		The principal
	 * 
	 * @return true for principal is sysadmin, false otherwise
	 */
	public final boolean isSysadmin(User user) {
		return user != null && user.getRole().getRoleId() == ADMIN_ROLE_ID; 
	}
	
	/**
	 * Gets all groups that the principal has a given right on
	 * @param principal		The user performing the action
	 * @param right			One of the rights stored in roles (should be 
	 * 						limited to {@code VIEW_MEMBER}, 
	 * 						{@code UPDATE_CREDENTIAL}, {@code ADD_MEMBER}, 
	 * 						{@code REMOVE_MEMBER}, {@code MANAGE}})
	 * @return A set of the group IDs of the authorized groups
	 */
	private Set<Integer> allGroupsWithRight(int principal, 
			UserMgmtAction right) {
		
		Set<Integer> results = new HashSet<Integer>();
		
		// get all the groups that this user has rights on
		Map<Integer, Role> authz = 
			DAOFactory.getDAOFactoryInstance().getUserDAO()
				.getGroupAuthorizations(principal);
		
		// for each group
		for (Map.Entry<Integer, Role> a : authz.entrySet()) {
			// if the right in question is granted
			if(grantsRight(a.getValue().getRights(), right)) {
				// add the group to the result set
				results.add(a.getKey());
			}
		}
		
		//get all the groups that this user belongs to
		Set<Integer> groups = allSupergroups(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getMemberships(principal));
		//trim all those we already know have authorization
		groups.removeAll(results);
		//and add the authorizations the user inherits from those group 
		// memberships
		Map<Integer, Set<Role>> groupAuthz = 
			DAOFactory.getDAOFactoryInstance().getGroupDAO()
				.getGroupAuthorizations(groups);
		
		//for each group
		for (Map.Entry<Integer, Set<Role>> a : groupAuthz.entrySet()) {
			//for each authorization on that group
			for (Role s : a.getValue()) {
				// if the right in question is granted
				if(grantsRight(s.getRights(), right)) {
					// add the group to the result set
					results.add(a.getKey());
				}
			}
		}
		
		return results;
	}
	
	/**
	 * Checks if the principal has a right on a group
	 * @param principal		The user performing the action
	 * @param right			One of the rights stored in roles (should be 
	 * 						limited to {@code VIEW_MEMBER}, 
	 * 						{@code UPDATE_CREDENTIAL}, {@code ADD_MEMBER}, 
	 * 						{@code REMOVE_MEMBER}, {@code MANAGE}})
	 * @return does the user (or one of the user's groups) have the right on 
	 * 		the group  
	 */
	private boolean hasRightOnGroup(int principal, UserMgmtAction right, 
			int subject) {

		//check if the principal has the right directly on the group
		Map<Integer, Role> authz = 
			DAOFactory.getDAOFactoryInstance().getGroupDAO()
				.getAuthorizedUsers(subject);
		
		Role authzRole = authz.get(principal);
		if (authzRole != null && grantsRight(authzRole.getRights(), right)) {
			return true;
		}
		
		//check if any groups have the right on the group
		authz = 
			DAOFactory.getDAOFactoryInstance().getGroupDAO()
				.getAuthorizedGroups(subject);
		
		Set<Integer> groups = new HashSet<Integer>();
		for (Map.Entry<Integer, Role> entry : authz.entrySet()) {
			//every group which has the right we add to a list to search
			if (grantsRight(entry.getValue().getRights(), right)) {
				groups.add(entry.getKey());
			}
		}
		
		if (groups.isEmpty()) {
			return false;
		}
		
		//check if the principal belongs to any of these groups
		return GraphUtils.userIsInAnyGroup(principal, groups);
	}
	
	/**
	 * Checks if a role grants a user management right
	 * @param s		The roles rights set
	 * @param r		The right
	 * @return is the right granted by that string?
	 */
	private boolean grantsRight(Set<Right> s, UserMgmtAction r) {
		switch(r) {
		case VIEW_MEMBERS:
			return s.contains(Right.VIEW_MEMBERS);
		case UPDATE_CREDENTIALS:
			return s.contains(Right.UPDATE_CREDENTIALS);
		case UPDATE_MEMBERS:
			return s.contains(Right.UPDATE_MEMBERS);
		case MANAGE_GROUP:
			return s.contains(Right.MANAGE_GROUP);
		default:
			return false;
		}
	}
	
	/**
	 * Checks if one set of rights grants a superset of the rights of another 
	 * set of rights
	 * 
	 * @param superset		The set of rights to check for being a superset
	 * @param subset		The set of rights to check superset against
	 * 
	 * @return does superset grant rights that are a superset of the rights 
	 * 		that subset grants?
	 */
	private boolean isRightsSuperset(Set<Right> superset, Set<Right> subset) {
		return superset.containsAll(subset);
	}
}
