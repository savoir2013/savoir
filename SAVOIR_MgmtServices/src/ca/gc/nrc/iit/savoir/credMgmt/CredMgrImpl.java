// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.credMgmt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.jws.WebService;

import org.apache.log4j.Logger;

import ca.gc.iit.nrc.savoir.domain.Credential;
import ca.gc.iit.nrc.savoir.domain.CredentialAuthorization;
import ca.gc.iit.nrc.savoir.domain.CredentialParameter;
import ca.gc.iit.nrc.savoir.domain.CredentialSchema;
import ca.gc.iit.nrc.savoir.domain.Group;
import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.iit.nrc.savoir.domain.User;
import ca.gc.iit.nrc.savoir.domain.CredentialAuthorization.CredentialAuthorizationRight;
import ca.gc.nrc.iit.savoir.dao.impl.DAOFactory;
import ca.gc.nrc.iit.savoir.userMgmt.GraphUtils;
import ca.gc.nrc.iit.savoir.userMgmt.UserMgmtAuthorizer;

import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.*;

/**
 * Implements {@link CredMgr} interface by managing authentication, 
 * authorization, and database access.
 *  
 * @author Aaron Moss
 */
@WebService(endpointInterface = "ca.gc.nrc.iit.savoir.credMgmt.CredMgr")
public class CredMgrImpl implements CredMgr {

	private static UserMgmtAuthorizer authorizer = UserMgmtAuthorizer.getAuthorizer();
	
	private static final Logger logger = Logger.getLogger(CredMgrImpl.class);
	
	/** full admin rights for a credential */
	private static final List<CredentialAuthorizationRight> ADMIN_RIGHTS = 
		Arrays.asList(CredentialAuthorizationRight.VIEW, 
				CredentialAuthorizationRight.UPDATE,
				CredentialAuthorizationRight.DELETE,
				CredentialAuthorizationRight.GRANT_VIEW, 
				CredentialAuthorizationRight.GRANT_UPDATE,
				CredentialAuthorizationRight.GRANT_DELETE);
	
	@Override
	public List<Resource> getAuthorizedResources(String userName) {
		//authorize user
		if (userName == null 
				|| !DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(userName)) {
			logger.debug(
					"get authorized resources called by invalid caller \"" + 
					userName + "\"");
			return Collections.emptyList();
		}
		
		//get resources for calling user and groups
		int userId = DAOFactory.getDAOFactoryInstance().getUserDAO()
			.getUserID(userName);
		Set<Integer> groups = 
			GraphUtils.allSupergroups(DAOFactory.getDAOFactoryInstance()
					.getUserDAO().getMemberships(userId));
		
		logger.info("userId = " + userId + "\nGroupId = " + groups);
		return DAOFactory.getDAOFactoryInstance().getCredentialDAO().
			getAuthorizedResources(userId, groups);
	}
	
	@Override
	public List<Credential> retrieveCredentials(int resource, String userName) {
		//validate input
		if (resource <= 0 || 
				DAOFactory.getDAOFactoryInstance().getResourceDAO().
				getResourceById(resource) == null) {
			return new ArrayList<Credential>();
		}
		
		//authorize
		if (userName == null 
				|| !DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(userName)) {
			logger.debug("retrieve credentials called by invalid caller \"" + 
					userName + "\"");
			return Collections.emptyList();
		}
		
		//get user ID and groups
		int userId = DAOFactory.getDAOFactoryInstance().getUserDAO()
			.getUserID(userName);
//		Set<Integer> groups = 
//			GraphUtils.allSupergroups(DAOFactory.getDAOFactoryInstance()
//					.getUserDAO().getMemberships(userId));
//		
//		//authorization to view credentials is implicit in having authorization 
//		// to the credentials
//		return DAOFactory.getDAOFactoryInstance().getCredentialDAO().
//			retrieveCredentials(userId, groups, resource);
		
		//start list of credentials with the user's credentials
		LinkedHashSet<Integer> credIds = new LinkedHashSet<Integer>();
		
		credIds.addAll(DAOFactory.getDAOFactoryInstance().getCredentialDAO().getUserCredentials(userId, resource));
		
		//go through groups user belongs to and add credentials for them
		
		//set of groups that we've already checked (this is an optimization to ensure that we never look up the same group twice
		// for instance, a user may directly belong to the SAVOIR group and the SAVOIR_ADMIN group, a subgroup of SAVOIR
		// this set prevents the SAVOIR group from being looked up as a parent of the SAVOIR_ADMIN group when it has already been 
		// looked up as a membership of the user)
		Set<Integer> alreadySeen = new HashSet<Integer>();
		//set of groups newly seen (initialized to the user's direct group memberships)
		Set<Integer> newGroups = DAOFactory.getDAOFactoryInstance().getUserDAO().getMemberships(userId);
		
		while (!newGroups.isEmpty()) {
			//add credentials for newly seen groups to list (being a set, will not allow duplicates)
			credIds.addAll(DAOFactory.getDAOFactoryInstance().getCredentialDAO().getGroupCredentials(newGroups, resource));
			//add newly seen groups to set of groups already seen
			alreadySeen.addAll(newGroups);
			//get parents of current crop of groups
			newGroups = DAOFactory.getDAOFactoryInstance().getGroupDAO().getDirectSupergroups(newGroups);
			//trim all groups in parents that have already been seen
			newGroups.removeAll(alreadySeen);
		}
		
		//add credentials assigned to everyone
		Set<Integer> everyoneGroup = new HashSet<Integer>(Collections.singleton(0));
		credIds.addAll(DAOFactory.getDAOFactoryInstance().getCredentialDAO().getGroupCredentials(everyoneGroup, resource));
		
		//convert credential IDs to credentials
		List<Credential> creds = new ArrayList<Credential>();
		for (Integer credId : credIds) {
			Credential cred = DAOFactory.getDAOFactoryInstance().getCredentialDAO().getCredentialById(credId);
			if (cred != null) creds.add(cred);
		}
		
		return creds;
	}

	@Override
	public List<CredentialAuthorization> getCredentialAuthorizations(
			int credId, String userName) {
		logger.info("Credential Info request initiated");
		
		//validate input
		if (credId <= 0) {
			return Collections.emptyList();
		}
		
		//authorize
		if (userName == null 
				|| !DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(userName)) {
			logger.debug(
					"get credential authorizations called by invalid caller \"" 
					+ userName + "\"");
			return Collections.emptyList();
		}
		
		//check authorization on credential
		int callerId = DAOFactory.getDAOFactoryInstance().getUserDAO()
			.getUserID(userName);
		
		if (!authorizer.isAuthorizedOnCred(
				callerId, CredentialAuthorizationRight.VIEW, credId)) {
			logger.debug("caller \"" + userName + 
					"\" unauthorized to view credential " + credId);
			return Collections.emptyList();
		}
		
		if (authorizer.isSysadmin(callerId)) {
			return DAOFactory.getDAOFactoryInstance().getCredentialDAO()
				.getAuthorizations(credId);
		} else {
			//if not sysadmin, filter out users the caller isn't authorized to 
			// see
			
			//get groups, users the caller is authorized to see
			Set<Integer> groups = authorizer.allAuthorizedGroups(
					callerId, UserMgmtAction.VIEW_MEMBERS);
			//get users the caller is authorized to see
			Set<Integer> users = authorizer.allAuthorizedUsersInGroups(
					callerId, UserMgmtAction.VIEW_MEMBERS, groups);
			
			//filter list by what the caller is authorized to see
			List<CredentialAuthorization> credAuthz = 
				new ArrayList<CredentialAuthorization>();
			for (CredentialAuthorization ca : 
				DAOFactory.getDAOFactoryInstance().getCredentialDAO()
					.getAuthorizations(credId)) {
				
				if (ca.getUserId() == EVERYONE && ca.getGroupId() == EVERYONE) {
					credAuthz.add(ca);
				}
				if (users.contains(ca.getUserId())) {
					credAuthz.add(ca);
				}
				if (groups.contains(ca.getGroupId())) {
					credAuthz.add(ca);
				}
			}
			
			return credAuthz;
		}
	}

	@Override
	public int storeCredentials(int resource, String desc, 
			List<CredentialParameter> creds, Date beginTime, Date endTime, 
			String userName) {
		logger.info("Credential storage request initiated");
		
		//validate input
		if (creds == null || resource <= 0) {
			return INVALID_PARAMETERS;
		}
		
		if (!timesSane(beginTime, endTime) || 
				DAOFactory.getDAOFactoryInstance().getResourceDAO().
					getResourceById(resource) == null) {
			return PRECONDITION_ERROR;
		}
		
		//authorize
		User user;
		if (userName == null || 
				(user = DAOFactory.getDAOFactoryInstance().getUserDAO()
						.getUserByDN(userName)) == null) {
			logger.debug("store credential called by invalid caller \"" + 
					userName + "\"");
			return INVALID_CALLER;
		}
		
		//no authorization needed to do non-admin credential storage
		
		//validate schema, if neccessary
		if (!CredSchemaValidator.isValidForResource(creds, resource)) {
			return SCHEMA_VALIDATION_FAILS;
		}
		
		//if no description, set to 
		// "<resource.description> : <caller.first_name> <caller.last_name>"
		if (desc == null) {
			String fName = user.getPerson().getPersonInfo().getFName(),
				lName = user.getPerson().getPersonInfo().getLName();
			Resource res = 
				DAOFactory.getDAOFactoryInstance().getResourceDAO()
					.getResourceById(resource);
			String rName = res == null ? "" : res.getDescription();
			
			
			String uName = ((fName == null ? "" : fName) + " " + 
					(lName == null ? "" : lName)).trim();
			desc = rName + " : " + uName;
		}
		
		//store credential
		Credential cred = new Credential();
		cred.setCredentialId(
				DAOFactory.getDAOFactoryInstance().getCredentialDAO()
					.getNextCredentialId());
		cred.setParameters(creds);
		cred.setDescription(desc);
		DAOFactory.getDAOFactoryInstance().getCredentialDAO()
			.addCredential(cred);
		
		//store authorization on credential for user
		DAOFactory.getDAOFactoryInstance().getCredentialDAO().addAuthorization(
				resource, user.getUserID(), NO_GROUP, cred.getCredentialId(), 
				ADMIN_RIGHTS, beginTime, endTime);
		
		return SUCCESS;
	}
	
	@Override
	public int adminStoreCredentials(int group, int resource, String desc, 
			List<CredentialParameter> creds, Date beginTime, Date endTime, 
			String userName) {
		logger.info("Administrative credential storage request initiated");
		
		//validate input
		if (creds == null || resource <= 0 || group < 0) {
			return INVALID_PARAMETERS;
		}
		
		if (!timesSane(beginTime, endTime) || 
				DAOFactory.getDAOFactoryInstance().getResourceDAO().
					getResourceById(resource) == null) {
			return PRECONDITION_ERROR;
		}
		
		//authorize
		if (userName == null 
				|| !DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(userName)) {
			logger.debug("admin store credential called by invalid caller \"" + 
					userName + "\"");
			return INVALID_CALLER;
		}
		
		//check authorization
		if (!authorizer.isAuthorizedOnGroup(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(userName), 
				UserMgmtAction.UPDATE_CREDENTIALS, group)) {
			logger.debug("caller \"" + userName + 
					"\" unauthorized to store credential on group" + group);
			return UNAUTHORIZED;
		}
		
		//validate schema, if neccessary
		if (!CredSchemaValidator.isValidForResource(creds, resource)) {
			return SCHEMA_VALIDATION_FAILS;
		}
		
		//if no description, set to 
		// "<resource.description> : <group.group_name>"
		if (desc == null) {
			String gName;
			if (group == 0) {
				gName = "Everyone";
			} else {
				Set<Group> groups = 
					DAOFactory.getDAOFactoryInstance().getGroupDAO()
						.getGroupsById(new HashSet<Integer>(Collections.singleton(group)));
				gName = groups.isEmpty() ? 
						"" 
						: groups.iterator().next().getGroupName();
			}
			
			Resource res = 
				DAOFactory.getDAOFactoryInstance().getResourceDAO()
					.getResourceById(resource);
			String rName = res == null ? "" : res.getDescription();
			
			desc = rName + " : " + gName;
		}
		
		//store credential
		Credential cred = new Credential();
		cred.setCredentialId(
				DAOFactory.getDAOFactoryInstance().getCredentialDAO()
					.getNextCredentialId());
		cred.setParameters(creds);
		cred.setDescription(desc);
		DAOFactory.getDAOFactoryInstance().getCredentialDAO()
			.addCredential(cred);
		
		//store authorization on credential for group
		DAOFactory.getDAOFactoryInstance().getCredentialDAO().addAuthorization(
				resource, NO_USER, group, cred.getCredentialId(), ADMIN_RIGHTS,
				beginTime, endTime);
		
		return SUCCESS;
	}

	@Override
	public int updateCredentials(int credId, String desc, 
			List<CredentialParameter> creds, String userName) {
		logger.info("Update request initiated");
		
		//validate input
		if (creds == null || credId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		//authorize
		if (userName == null 
				|| !DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(userName)) {
			logger.debug("update credential called by invalid caller \"" + 
					userName + "\"");
			return INVALID_CALLER;
		}
		
		//check authorization
		if (!authorizer.isAuthorizedOnCred(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(userName), 
				CredentialAuthorizationRight.UPDATE, credId)) {
			logger.debug("caller \"" + userName + 
					"\" unauthorized to update credential " + credId);
			return UNAUTHORIZED;
		}
		
		//validate schema, if neccessary
		//for every resource this credential is assigned to
		for (int resource : 
				DAOFactory.getDAOFactoryInstance().getCredentialDAO().
					getResourceForCredential(credId)) {
			//check the credential against the resource schema
			if (!CredSchemaValidator.isValidForResource(creds, resource)) {
				return SCHEMA_VALIDATION_FAILS;
			}
		}
		
		//update credential
		return DAOFactory.getDAOFactoryInstance().getCredentialDAO().
			updateCredential(credId, desc, creds);
	}
	
	@Override
	public int removeCredentials(int credId, String caller) {
		logger.info("Remove request initiated");
		
		//validate input
		if (credId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		//authorize
		if (caller == null 
				|| !DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			logger.debug("remove credential called by invalid caller \"" + 
					caller + "\"");
			return INVALID_CALLER;
		}
		
		//check authorization
		if (!authorizer.isAuthorizedOnCred(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(caller), 
				CredentialAuthorizationRight.DELETE, credId)) {
			logger.debug("caller \"" + caller + 
					"\" unauthorized to delete credential " + credId);
			return UNAUTHORIZED;
		}
		
		//remove credential
		return DAOFactory.getDAOFactoryInstance().getCredentialDAO()
			.removeCredential(credId);
	}
	
	@Override
	public int grantCredentials(int userTo, int groupTo, int resourceOn, 
			int credOn, List<CredentialAuthorizationRight> granting, 
			Date beginTime, Date endTime, String userName) {
		
		logger.info("Grant request initiated");
		
		//validate input
		if (granting == null || resourceOn <= 0 || credOn <= 0 || userTo < 0 
				|| groupTo < 0) {
			return INVALID_PARAMETERS;
		}
		
		if ((userTo != NO_USER && groupTo != NO_GROUP)
				|| !timesSane(beginTime, endTime)
				|| !grantSane(granting)
				|| DAOFactory.getDAOFactoryInstance().getResourceDAO().
					getResourceById(resourceOn) == null) {
			return PRECONDITION_ERROR;
		}
		
		//authorize
		if (userName == null 
				|| !DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(userName)) {
			logger.debug("grant credential called by invalid caller \"" + 
					userName + "\"");
			return INVALID_CALLER;
		}
		
		//check authorization
		if (!authorizer.isGrantAuthorizedOnCred(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(userName),
				granting, credOn, resourceOn, userTo, groupTo)) {
			logger.debug("caller \"" + userName + 
					"\" unauthorized to grant credential");
			return UNAUTHORIZED;
		}
		
		//perform credential grant
		return DAOFactory.getDAOFactoryInstance().getCredentialDAO()
			.addAuthorization(resourceOn, userTo, groupTo, credOn, granting, 
					beginTime, endTime);
	}
	
	@Override
	public int revokeCredentials(int userFrom, int groupFrom, int resourceOn, 
			int credOn, String userName) {

		logger.info("Revoke request initiated");
		
		//validate input
		if (resourceOn <= 0 || credOn <= 0 || userFrom < 0 || groupFrom < 0) {
			return INVALID_PARAMETERS;
		}
		
		if ((userFrom != NO_USER && groupFrom != NO_GROUP)
				|| DAOFactory.getDAOFactoryInstance().getResourceDAO().
					getResourceById(resourceOn) == null
			) {
			return PRECONDITION_ERROR;
		}
		
		//authenticate
		if (userName == null 
				|| !DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(userName)) {
			logger.debug("grant credential called by invalid caller \"" + 
					userName + "\"");
			return INVALID_CALLER;
		}
		
		//check authorization
		if (!authorizer.isGrantAuthorizedOnCred(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(userName),
				new ArrayList<CredentialAuthorizationRight>(), credOn, 
					resourceOn, userFrom, groupFrom)) {
			logger.debug("caller \"" + userName + 
					"\" unauthorized to revoke credential");
			return UNAUTHORIZED;
		}
		
		//revoke credential
		return DAOFactory.getDAOFactoryInstance().getCredentialDAO()
			.removeAuthorization(resourceOn, userFrom, groupFrom, credOn);
	}

	
	@Override
	public CredentialSchema getCredentialSchemaByResource(int resourceId) {
		//check parameter validity
		if (resourceId <= 0) {
			return null;
		}
		
		//no authentication, authorization, this should be public information
		
		//return schema
		return DAOFactory.getDAOFactoryInstance().getCredentialDAO()
			.getSchemaByResource(resourceId);
	}

	/**
	 * Checks if a set of granting rights respects the following rules:
	 * <ul>
	 * <li> {@code UPDATE} and {@code DELETE} are not granted without 
	 * 		{@code VIEW}
	 * <li> <code>GRANT_<i>X</i></code> is not granted without <i>{@code X}</i> 
	 * 		(where <i>{@code X}</i> is one of {@code VIEW}, {@code UPDATE}, or 
	 * 		{@code DELETE})
	 * </ul>
	 * 
	 * @param granting	The rights to check
	 * 
	 * @return	{@code true} for follows the rules, {@code false} for not
	 */
	private boolean grantSane(List<CredentialAuthorizationRight> granting) {
		
		if(!granting.contains(CredentialAuthorizationRight.VIEW)) {
			//all rights depend on VIEW,
			// the empty set is valid, all others invalid if VIEW is not present
			return granting.isEmpty();
		}
		
		if(//VIEW is checked by the previous check
			(granting.contains(CredentialAuthorizationRight.GRANT_UPDATE)
				&& !granting.contains(CredentialAuthorizationRight.UPDATE))
			|| (granting.contains(CredentialAuthorizationRight.GRANT_DELETE)
				&& !granting.contains(CredentialAuthorizationRight.DELETE))) {
			return false;
		}
		
		return true;
	}
	
}
