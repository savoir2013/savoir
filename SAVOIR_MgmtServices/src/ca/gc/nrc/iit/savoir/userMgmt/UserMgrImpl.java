// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.userMgmt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.jws.WebService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ca.gc.iit.nrc.savoir.domain.Group;
import ca.gc.iit.nrc.savoir.domain.GroupAuthorization;
import ca.gc.iit.nrc.savoir.domain.GroupNode;
import ca.gc.iit.nrc.savoir.domain.Person;
import ca.gc.iit.nrc.savoir.domain.PersonInfo;
import ca.gc.iit.nrc.savoir.domain.Role;
import ca.gc.iit.nrc.savoir.domain.Site;
import ca.gc.iit.nrc.savoir.domain.User;
import ca.gc.iit.nrc.savoir.domain.UserIDName;
import ca.gc.iit.nrc.savoir.domain.Role.Right;

import ca.gc.nrc.iit.savoir.dao.impl.DAOFactory;

import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.*;

/**
 * Implements {@link UserMgr} interface by handling authentication, 
 * authorization, and database access.
 * 
 * @author Aaron Moss
 */
@WebService(endpointInterface = "ca.gc.nrc.iit.savoir.userMgmt.UserMgr")
public class UserMgrImpl implements UserMgr{
	private static UserMgmtAuthorizer authorizer = UserMgmtAuthorizer.getAuthorizer();
	
	//private static final Logger logger = Logger.getLogger(UserMgrImpl.class);
	protected final Log logger = LogFactory.getLog(getClass());
	
	/*
	 * Data for group creation
	 */
	/** Name of admin role for new group creation */
	private static final int ADMIN_ROLE_ID = 1;
	private static final Group EVERYONE_GROUP = 
		new Group(0, "EVERYONE", "All users");
	
	
	//--------------------------------------------------------
	// User Create-Read-Update-Delete operations
	//--------------------------------------------------------
	
	@Override
	public int createUser(String lName, String fName, String id, String pword, 
			Date begin, Date end, int siteId, List<String> info, int roleId, 
			String caller) {
		//populate User object
		User user = new User(0, id, pword, begin, end, siteId, 
				DAOFactory.getDAOFactoryInstance().getRoleDAO()
					.getRoleById(roleId));
		//populate UserInfo object
		PersonInfo userInfo = loadPersonInfo(lName, fName, info);
		
		//create user
		return createUser1(user, userInfo, caller);
	}
	
	@Override
	public int createUser1(User user, PersonInfo userInfo, String caller) {
		//check for non-null parameters, also non-empty essential parameters
		if (user == null || userInfo == null || user.getPassword() == null 
				|| ((userInfo.getLName() == null 
							|| userInfo.getLName().isEmpty())
						&& (userInfo.getFName() == null 
								|| userInfo.getFName().isEmpty()))) {
			logger.debug("add user called with invalid parameters");
			return INVALID_PARAMETERS;
		}
		
		if (!timesSane(user.getBeginTime(), user.getEndTime())) {
			return PRECONDITION_ERROR;
		}
		
		//check caller authenticity
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			logger.debug(
					"create user called by invalid caller \"" + caller + "\"");
			//return invalid caller if cannot be authenticated, or does not 
			// match any record in the database
			return INVALID_CALLER;
		}
		
		//check caller authorization
		if (!authorizer.isAuthorized(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(caller), 
				UserMgmtAction.CREATE_USER)) {
			logger.debug("caller \"" + caller + "\" unauthorized to add user");
			return UNAUTHORIZED;
		}
		
		//handle login ID
		String loginID = user.getDName();
		if (loginID == null || loginID.length() <= 0) {
			logger.debug("add user called without login ID");
			return INVALID_PARAMETERS;
		}
		
		logger.trace("create user called by \"" + caller + "\" for \"" + 
				loginID + "\"");
		
		//ensure non-null first and last name parameters
		if (userInfo.getFName() == null) {
			userInfo.setFName("");
		}
		if (userInfo.getLName() == null) {
			userInfo.setLName("");
		}
		
		//check that there is no existing user with this login ID
		if (DAOFactory.getDAOFactoryInstance().getUserDAO().isDName(loginID)) {
			return ALREADY_EXISTS;
		}
		
//		/* Uncomment if using PKI-based authentication */
//		//generate and store certificate
//		String dNameReq = user.getDName();	//the DName to request
//		if (dNameReq != null) {
//			//trim out everything before and including CommonName tag, if 
//			// applicable
//			String[] split = dNameReq.split("CN=");
//			dNameReq = split[split.length - 1];
//		} else {
//			//if there is no distinguished name request, set it to the user's 
//			// name
//			StringBuilder sb = new StringBuilder();
//			String s = userInfo.getFName();
//			if (s.length() > 0) {
//				sb.append(s);
//				sb.append(' ');
//			}
//			sb.append(userInfo.getLName());
//			dNameReq = sb.toString();
//		}
//		
//		String dName = 
//			myproxyMgr.generateCert(user.getUserID(), user.getPassword(), 
//				dNameReq);
//		//check certificate correctly generated
//		if (dName == null) {
//			return MYPROXY_ERROR;
//		}
//		user.setDName(dName);
		
		//creates user in SAVOIR database
		Person person = new Person();
		person.setPersonId(
				DAOFactory.getDAOFactoryInstance().getPersonDAO()
					.getNextPersonId());
		person.setPersonInfo(userInfo);
		DAOFactory.getDAOFactoryInstance().getPersonDAO().addPerson(person);
		user.setUserID(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getNextUserId());
		user.setPerson(person);
		DAOFactory.getDAOFactoryInstance().getUserDAO().addUser(user);
		
		return SUCCESS;
	}
	
	@Override
	public int updateUser(int userId, PersonInfo info, String caller) {
		//validate input
		if (userId <= 0 || info == null) {
			return INVALID_PARAMETERS;
		}
		
		//authenticate caller
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			logger.debug("update user called by invalid caller \"" + caller + 
					"\"");
			//return invalid caller if cannot be authenticated, or does not 
			// match any record in the database
			return INVALID_CALLER;
		}
		
		//check caller authorization
		int callerID = 
			DAOFactory.getDAOFactoryInstance().getUserDAO().getUserID(caller);
		if (!authorizer.isAuthorizedOnUser(callerID, 
				UserMgmtAction.UPDATE_USER, userId)) {
			logger.debug("caller \"" + callerID + 
					"\" unauthorized to update user \"" + userId + "\"");
			return UNAUTHORIZED;
		}
		logger.trace("updateUser called by \"" + caller + "\" on \"" + userId + 
				"\"");
		
		//check that we are not blanking both LAST_NAME and FIRST_NAME
		User oldUser = 
			DAOFactory.getDAOFactoryInstance().getUserDAO().getUserById(userId);
		if (oldUser == null) {
			return NO_SUCH_ENTITY;
		}
		PersonInfo old = oldUser.getPerson().getPersonInfo();
		//if both first and last name are being set to empty
		// if there is no update to the name (info.getXName() == null)
		//  this means the existing value is null or empty
		// if there is an update to the name (info.getXName() != null)
		//  this means the new value is empty
		if ((info.getFName() == null ? 
				(old.getFName() == null || old.getFName().isEmpty()) :
				info.getFName().isEmpty())
			&& (info.getLName() == null ? 
				(old.getLName() == null || old.getLName().isEmpty()) :
				info.getLName().isEmpty())) {
			return PRECONDITION_ERROR;
		}
		
		//updates user in database
		return DAOFactory.getDAOFactoryInstance().getPersonDAO().updatePerson(
				oldUser.getPerson().getPersonId(), info);
	}
	
	@Override
	public int adminUpdateUser(int userId, String loginId, String password, 
			Date beginTime, Date endTime, String caller) {
		//validate input
		if (userId <= 0) {
			return INVALID_PARAMETERS;
		}
		if (!timesSane(beginTime, endTime)) {
			return PRECONDITION_ERROR;
		}
		
		//authenticate caller
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			logger.debug("admin update user called by invalid caller \"" + 
					caller + "\"");
			//return invalid caller if cannot be authenticated, or does not 
			// match any record in the database
			return INVALID_CALLER;
		}
		
		//check caller authorization
		int callerID = 
			DAOFactory.getDAOFactoryInstance().getUserDAO().getUserID(caller);
		if (!authorizer.isAuthorizedOnUser(callerID, 
				UserMgmtAction.UPDATE_USER, userId)) {
			logger.debug("caller \"" + callerID + 
					"\" unauthorized for admin update of user \"" + userId + 
					"\"");
			return UNAUTHORIZED;
		}
		logger.trace("adminUpdateUser called by \"" + caller + "\" on \"" + 
				userId + "\"");
		
		//check that this user already exists
		User oldUser = 
			DAOFactory.getDAOFactoryInstance().getUserDAO().getUserById(userId);
		if (oldUser == null) {
			return NO_SUCH_ENTITY;
		}
		
		//check that there is no existing user with this login ID, if updating
		if (oldUser.getDName().equals(loginId)) {
			loginId = null;
		}
		if (loginId != null && 
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(loginId)) {
			return ALREADY_EXISTS;
		}
		
		loginId = oldUser.getDName();
		if (password == null) {
			password = oldUser.getPassword();
		}
		
		//update user
		return DAOFactory.getDAOFactoryInstance().getUserDAO()
			.updateUser(userId, loginId, password, beginTime, endTime);
	}
	
	@Override
	public int deleteUser(int id, String caller) {
		//validate input
		if (id <= 0) {
			logger.debug("delete user called without parameter");
			return INVALID_PARAMETERS;
		}
		
		//check caller authenticity
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			//return invalid caller if cannot be authenticated, or does not 
			// match any record in the database
			logger.debug("delete user called by invalid caller \"" + caller + 
					"\"");
			return INVALID_CALLER;
		}
		
		//check authorization
		if (!authorizer.isAuthorizedOnUser(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(caller),
				UserMgmtAction.DELETE_USER, id)) {
			logger.debug("user \"" + caller + "\" unauthorized to delete user");
			return UNAUTHORIZED;
		}
		logger.trace("deleteUser called by \"" + caller + "\" on \"" + id + 
				"\"");
		
//		/* Uncomment if PKI is used for authentication */
//		//get user information
//		User user = DAOFactory.getDAOFactoryInstance().getUserDAO().getUserById(id);
//		
//		//remove user from MyProxy repository
//		if (myproxyMgr.removeUser(id, user.getPassword()) != SUCCESS) {
//			return MYPROXY_ERROR;
//		}
		
		//deletes user from SAVOIR database
		User u = DAOFactory.getDAOFactoryInstance().getUserDAO()
			.getUserById(id);
		if (u == null) {
			return NO_SUCH_ENTITY;
		}
		DAOFactory.getDAOFactoryInstance().getUserDAO().removeUser(id);
		DAOFactory.getDAOFactoryInstance().getPersonDAO().removePerson(
				u.getPerson().getPersonId());
		return SUCCESS;
	}
	
	@Override
	public List<PersonInfo> getUserData(int[] userIDs, String caller) {
		//validate data
		if (userIDs == null) {
			logger.debug("Null parameter passed to getUserData");
			return new ArrayList<PersonInfo>();
		}
		
		//check caller authenticity
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			logger.debug("Invalid caller \"" + caller + 
					"\" called getUserData");
			//return no data if cannot be authenticated, or does not match any 
			// record in the database
			return new ArrayList<PersonInfo>();
		}
		logger.trace("getUserData called by \"" + caller + "\"");
		
		//authorize
		Set<Integer> authorizedUsers = authorizer.allAuthorizedUsers(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(caller), 
				UserMgmtAction.VIEW_MEMBERS);
		
		//fill values
		List<PersonInfo> info = new ArrayList<PersonInfo>(userIDs.length);
		for (int i = 0; i < userIDs.length; i++) {
			//insert the appropriate user info value if authorized, null 
			// otherwise
			info.add(authorizedUsers.contains(userIDs[i]) ? 
					DAOFactory.getDAOFactoryInstance().getUserDAO()
							.getUserById(userIDs[i]).getPerson().getPersonInfo() 
					: null);
		}
		
		//return
		return info;
	}
	
	@Override
	public List<UserIDName> getUsers(String caller) {
		//get users caller is authorized to view
		Set<UserIDName> resultset = getUserInfoSet(null, caller);
		//return as list
		return new ArrayList<UserIDName>(resultset);
	}
	
	@Override
	public List<UserIDName> getUsersByID(int[] userID, String caller) {
		//validate input
		if (userID == null || userID.length == 0) {
			return new ArrayList<UserIDName>();
		}
		
		//build restriction set
		ArrayList<Integer> restrictSet = new ArrayList<Integer>(userID.length);
		for (int uid : userID) {
			restrictSet.add(uid);
		}
		
		//get users in restrict set caller is authorized to view
		Set<UserIDName> resultset = getUserInfoSet(restrictSet, caller);
		// and return as list
		return new ArrayList<UserIDName>(resultset);
	}
	
	/**
	 * Gets the set of UserIDName restricted by the restrictSet 
	 * 
	 * @param restrictSet	The set of ids to restrict the output to - no 
	 * 						restriction if null
	 * @param caller		The caller of this method
	 * 
	 * @return	The intersection of the userIDNames the caller is authorized to 
	 * 		view with the restriction set (if applicable)
	 */
	private Set<UserIDName> getUserInfoSet(Collection<Integer> restrictSet, 
			String caller) {		
		//check caller authenticity
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			//return no data if cannot be authenticated, or does not match any 
			// record in the database
			return new HashSet<UserIDName>();
		}
		
		//get all users the caller is authorized to view
		Set<Integer> authorizedUsers = authorizer.allAuthorizedUsers(
				DAOFactory.getDAOFactoryInstance().getUserDAO().getUserID(caller), 
				UserMgmtAction.VIEW_MEMBERS);
		
		//the set of all the users the caller is authorized to view
		Set<UserIDName> users = new TreeSet<UserIDName>();
		
		PersonInfo ui;
		//for every user the caller is authorized to view
		for (Integer id : authorizedUsers) {
			//if there is no restriction set, or the restriction set contains 
			// this user
			if (restrictSet == null || restrictSet.contains(id)) {
				//get user information
				User u = DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserById(id); 
				ui = u.getPerson().getPersonInfo();
				//and add to result set
				if (ui != null) {
					users.add(new UserIDName(
							id, u.getDName(), ui.getFName(), ui.getLName()));
				}
			}
		}
		
		return users;
	}
	
	@Override
	public List<User> adminGetUsers(String caller) {
		//get users caller is authorized to view
		Set<User> resultset = getUserSet(null, caller);
		// and return as a list
		return new ArrayList<User>(resultset);
	}
	
	@Override
	public List<User> adminGetUsersByID(int[] userID, String caller) {
		//validate input
		if (userID == null || userID.length == 0) {
			return new ArrayList<User>();
		}
		
		//build restriction set
		ArrayList<Integer> restrictSet = new ArrayList<Integer>(userID.length);
		for (int uid : userID) {
			restrictSet.add(uid);
		}
		
		//get users (in restriction set) that caller is authorized to view
		Set<User> resultset = getUserSet(restrictSet, caller);
		// and return as a list
		return new ArrayList<User>(resultset);
	}
	
	/**
	 * Gets the set of User restricted by the restrictSet 
	 * 
	 * @param restrictSet	The set of ids to restrict the output to - no 
	 * 						restriction if null
	 * @param caller		The calling user
	 * 
	 * @return	The intersection of the users the caller is authorized to 
	 * 			update with the restriction set (if applicable). Note that all 
	 * 			user objects will have their password stripped.
	 */
	private Set<User> getUserSet(Collection<Integer> restrictSet, 
			String caller) {		
		//check caller authenticity
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			//return no data if cannot be authenticated, or does not match any 
			// record in the database
			return new HashSet<User>();
		}
		
		//get all users the caller is authorized to administer
		Set<Integer> authorizedUsers = authorizer.allAuthorizedUsers(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(caller), 
				UserMgmtAction.UPDATE_USER);
		
		//the set of all the users the caller is authorized to view
		Set<User> users = new LinkedHashSet<User>();
		
		User ui;
		//for every user the caller is authorized to view
		for (Integer id : authorizedUsers) {
			//if there is no restriction set, or the user is in the set
			if (restrictSet == null || restrictSet.contains(id)) {
				//add the user to the return list, stripping the password
				ui = DAOFactory.getDAOFactoryInstance().getUserDAO().getUserById(id);
				if (ui != null) {
					ui.setPassword(null);
					users.add(ui);
				}
			}
		}
		
		return users;
	}
	
	//--------------------------------------------------------
	// Group Create-Read-Update-Delete operations
	//--------------------------------------------------------
	
	@Override
	public int createGroup(String name, String desc, String caller) {
		//populate Group and pass to other method
		return createGroup1(new Group(0, name, desc), caller);
	}

	@Override
	public int createGroup1(Group group, String caller) {
		//validate input parameters
		if (group.getGroupName() == null || group.getGroupName().isEmpty()) {
			return INVALID_PARAMETERS;
		}
		
		//check caller authenticity
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			logger.debug("create group called by invalid caller \"" + caller + 
					"\"");
			//return invalid caller if cannot be authenticated, or does not 
			// match any record in the database
			return INVALID_CALLER;
		}
		
		//check caller authorization
		int callerId = 
			DAOFactory.getDAOFactoryInstance().getUserDAO().getUserID(caller);
		if (!authorizer.isAuthorized(callerId, UserMgmtAction.CREATE_GROUP)) {
			logger.debug("caller \"" + caller + "\" unauthorized to add group");
			return UNAUTHORIZED;
		}
		
		//get new group ID
		group.setGroupId(
				DAOFactory.getDAOFactoryInstance().getGroupDAO()
					.getNextGroupId());
		// and save group to DB
		DAOFactory.getDAOFactoryInstance().getGroupDAO().addGroup(group);
		
		//add user to admin role
		DAOFactory.getDAOFactoryInstance().getGroupDAO().addUserAuthorization(
				callerId, ADMIN_ROLE_ID, group.getGroupId(), false, null, null);
		
		//return appropriately
		return SUCCESS;
	}

	@Override
	public int deleteGroup(int groupId, String caller) {
		//validate input
		if (groupId <= 0) {
			logger.debug("delete group called with invalid parameter");
			return INVALID_PARAMETERS;
		}
		
		//check caller authenticity
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			//return invalid caller if cannot be authenticated, 
			// or does not match any record in the database
			logger.debug("delete group called by invalid caller \"" + caller + 
					"\"");
			return INVALID_CALLER;
		}
		
		//check authorization
		if (!authorizer.isAuthorizedOnGroup(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(caller),
				UserMgmtAction.MANAGE_GROUP, groupId)) {
			logger.debug("user \"" + caller + 
					"\" unauthorized to delete group");
			return UNAUTHORIZED;
		}
		logger.trace("deleteUser called by \"" + caller + "\" on \"" + 
				groupId + "\"");
		
		//delete group from SAVOIR database
		DAOFactory.getDAOFactoryInstance().getGroupDAO().removeGroup(groupId);
		
		return SUCCESS;
	}

	@Override
	public int updateGroup(int groupId, String name, String desc, 
			String caller) {
		//validate input
		if (groupId <= 0 || 
				//name and desc are both null, or name is empty
				(name == null ? desc == null : name.isEmpty())) {
			return INVALID_PARAMETERS;
		}
		
		//authenticate caller
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			logger.debug("update group called by invalid caller \"" + caller + 
					"\"");
			//return invalid caller if cannot be authenticated, 
			// or does not match any record in the database
			return INVALID_CALLER;
		}
		
		//check caller authorization
		if (!authorizer.isAuthorizedOnGroup(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(caller), 
				UserMgmtAction.MANAGE_GROUP, groupId)) {
			logger.debug("caller \"" + caller + 
					"\" unauthorized to update group \"" + groupId + "\"");
			return UNAUTHORIZED;
		}
		logger.trace("updateGroup called by \"" + caller + "\" on \"" + 
				groupId + "\"");
		
		//updates group in database
		DAOFactory.getDAOFactoryInstance().getGroupDAO()
			.updateGroup(groupId, name, desc);
		
		return SUCCESS;
	}
	
	@Override
	public List<GroupNode> getGroups(String caller) {
		//check caller authenticity
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			//return no data if cannot be authenticated, or does not match any 
			// record in the database
			return Collections.emptyList();
		}
		
		GroupNode everyone = new GroupNode();
		everyone.setGroup(EVERYONE_GROUP);
		
		//get all groups the caller is authorized to view
		User callerUser = 
			DAOFactory.getDAOFactoryInstance().getUserDAO().getUserByDN(caller);
		int callerId = callerUser.getUserID();
		
		if (callerUser.getRole().getRoleId() == ADMIN_ROLE_ID) {
			//authorized to see entire graph if sysadmin 
			// can optimise algorithm significantly in this case
			Set<Integer> authorizedGroups = 
				DAOFactory.getDAOFactoryInstance().getGroupDAO()
					.getRootGroups();
			Set<GroupNode> rootGroups = new TreeSet<GroupNode>();
			
			//get graph from IDs
			for (int id : authorizedGroups) {
				rootGroups.add(
						DAOFactory.getDAOFactoryInstance().getGroupDAO()
							.getUserGraphByGroupId(id));
			}
			
			//add groups to tree root
			everyone.setSubgroups(new ArrayList<GroupNode>(rootGroups));
			
			//add groupless users
			Set<Integer> grouplessUsers = 
				DAOFactory.getDAOFactoryInstance().getGroupDAO()
					.getGrouplessUsers();
			Set<UserIDName> users = new TreeSet<UserIDName>();
			User u;
			PersonInfo ui;
			//for all the users in no group, add to the root of the user graph
			for (int id : grouplessUsers) {
				u = DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserById(id);
				ui = u.getPerson().getPersonInfo();
				if (ui != null) {
					users.add(new UserIDName(
							id, u.getDName(), ui.getFName(), ui.getLName()));
				}
			}
			everyone.setMembers(new ArrayList<UserIDName>(users));
			
			//convert to list and return
			return new ArrayList<GroupNode>(Arrays.asList(everyone));
			
		} else {
			//caller not sysadmin, must resolve roots of viewing graphs
			Set<Integer> authorizedGroups = authorizer.allAuthorizedGroups(
					DAOFactory.getDAOFactoryInstance().getUserDAO()
						.getUserID(caller), 
					UserMgmtAction.VIEW_MEMBERS);
			
			//find the graph for these groups
			Set<GroupNode> rootGroups = new TreeSet<GroupNode>(); 
			Set<Integer> foundGroups = 
				new HashSet<Integer>(authorizedGroups.size());
			
			//for each authorized group
			for (int id : authorizedGroups) {
				//if we've not already found it in the graph
				if (!foundGroups.contains(id)) {
					GroupNode newNode = 
						DAOFactory.getDAOFactoryInstance().getGroupDAO()
							.getUserGraphByGroupId(id);
					Set<Integer> newfoundGroups = getGroupIds(newNode);
					
					//remove previous "root" nodes that are included in this 
					// group's tree
					Iterator<GroupNode> rootIter = rootGroups.iterator();
					while (rootIter.hasNext()) {
						if (newfoundGroups.contains(
								rootIter.next().getGroup().getGroupId())) {
							rootIter.remove();
						}
					}
					//add this tree to the root
					rootGroups.add(newNode);
					
					//mark this group's nodes as "found"
					foundGroups.addAll(newfoundGroups);
				}
			}
			
			//convert to list
			List<GroupNode> retGroups = new ArrayList<GroupNode>(rootGroups);
			everyone.setSubgroups(retGroups);
			//check that the caller can see themself
			if (!containsUser(retGroups, callerId)) {
				//if the caller can't seem themself, add the caller to the root 
				// group
				
				PersonInfo ui = 
					DAOFactory.getDAOFactoryInstance().getUserDAO().
						getUserById(callerId).getPerson().getPersonInfo();
				UserIDName self = new UserIDName(
						callerId, caller, ui.getFName(), ui.getLName());
				
				everyone.setMembers(
						new ArrayList<UserIDName>(Arrays.asList(self)));
			} else {
				everyone.setMembers(new ArrayList<UserIDName>());
			}

			return new ArrayList<GroupNode>(Arrays.asList(everyone));
		}
	}
	
	/**
	 * Gets all the group IDs in a user graph
	 * 
	 * @param node	The root of the user graph
	 * 
	 * @return the set of all group IDs in any group in this graph
	 */
	private Set<Integer> getGroupIds(GroupNode node) {
		//set of groups
		HashSet<Integer> ids = new HashSet<Integer>();
		//add initial group
		ids.add(node.getGroup().getGroupId());
		//add all subgroups, recursively
		for (GroupNode n : node.getSubgroups()) {
			ids.addAll(getGroupIds(n));
		}
		return ids;
	}
	
	/**
	 * Checks if a user graph contains a given user
	 * 
	 * @param groups	The user graph (not null)
	 * @param userId	The user to search for
	 * 
	 * @return is the user contained in the user graph?
	 */
	private boolean containsUser(List<GroupNode> groups, int userId) {
		//for each of the groups
		for (GroupNode group : groups) {
			//for every member of that group
			for (UserIDName user : group.getMembers()) {
				//look for user in group directly
				if (user.userID == userId) {
					return true;
				}
			}
			//look for user in subgroups
			if (containsUser(group.getSubgroups(), userId)) {
				return true;
			}
		}
		//user not found anywhere in graph
		return false;
	}
	
	@Override
	public List<UserIDName> getMembers(int groupId, String caller) {
		//validate input
		if (groupId < 0 ) {
			return Collections.emptyList();
		}
		
		//authenticate caller
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			logger.debug("get group members called by invalid caller \"" + 
					caller + "\"");
			//return invalid caller if cannot be authenticated, 
			// or does not match any record in the database
			return Collections.emptyList();
		}
		
		//check caller authorization
		if (!authorizer.isAuthorizedOnGroup(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(caller), 
				UserMgmtAction.VIEW_MEMBERS, groupId)) {
			logger.debug("caller \"" + caller + 
					"\" unauthorized to view members of group \"" + groupId + 
					"\"");
			return Collections.emptyList();
		}
		logger.trace("getMembers called by \"" + caller + "\" on \"" + 
				groupId + "\"");
		
		//get members
		Set<Integer> members;
		if (groupId == EVERYONE) {
			//users with no group (thus direct members of the "everyone" group
			members = DAOFactory.getDAOFactoryInstance().getGroupDAO()
				.getGrouplessUsers();
		} else {
			//members of this group
			members = DAOFactory.getDAOFactoryInstance()
				.getGroupDAO().getMembers(new HashSet<Integer>(Collections.singleton(groupId)));
		}
		Set<UserIDName> users = new TreeSet<UserIDName>();
		
		User u;
		PersonInfo ui;
		//for all the members of the group
		for (int id : members) {
			//get user information and add to return list
			u = DAOFactory.getDAOFactoryInstance().getUserDAO().getUserById(id);
			ui = u.getPerson().getPersonInfo();
			if (ui != null) {
				users.add(new UserIDName(
						id, u.getDName(), ui.getFName(), ui.getLName()));
			}
		}
	
		return new ArrayList<UserIDName>(users);
	}

	@Override
	public List<Group> getSubgroups(int groupId, String caller) {
		//validate input
		if (groupId <= 0 ) {
			return Collections.emptyList();
		}
		
		//authenticate caller
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			logger.debug("get subgroups called by invalid caller \"" + caller + 
					"\"");
			//return invalid caller if cannot be authenticated, 
			// or does not match any record in the database
			return Collections.emptyList();
		}
		
		//check caller authorization
		if (!authorizer.isAuthorizedOnGroup(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(caller), 
				UserMgmtAction.VIEW_MEMBERS, groupId)) {
			logger.debug("caller \"" + caller + 
					"\" unauthorized to view subgroups of group \"" + groupId + 
					"\"");
			return Collections.emptyList();
		}
		logger.trace("getSubgroups called by \"" + caller + "\" on \"" + 
				groupId + "\"");
		
		//get subgroups, return as list
		Set<Integer> subgroups;
		if (groupId == EVERYONE) {
			//root groups (direct children of the group of all users)
			subgroups = DAOFactory.getDAOFactoryInstance().getGroupDAO()
				.getRootGroups();
		} else {
			//subgroups of the given group
			subgroups = DAOFactory.getDAOFactoryInstance().getGroupDAO()
				.getDirectSubgroups(new HashSet<Integer>(Collections.singleton(groupId)));
		}
		return new ArrayList<Group>(DAOFactory.getDAOFactoryInstance()
				.getGroupDAO().getGroupsById(subgroups));
	}
	
	//--------------------------------------------------------
	// Helper Methods
	//--------------------------------------------------------

	/**
	 * Loads a {@code PersonInfo} object with its values
	 * 
	 * @param lName		surname
	 * @param fName		given name
	 * @param info		a list such that {@code info[i]} is a field name of the 
	 * 					{@link ca.gc.nrc.iit.savoir.mgmtUtils.Constants.UserInfoFields} 
	 * 					enum and {@code info[i+1]} is the appropriate value for 
	 * 					that field. Any unrecognized field will be ignored.
	 * 
	 * @return a fully loaded {@code PersonInfo} object
	 */
	private PersonInfo loadPersonInfo(String lName, String fName, 
			List<String> info) {
		PersonInfo ui = new PersonInfo();
		
		//load first and last names
		ui.setFName(fName);
		ui.setLName(lName);
		
		Iterator<String> iter = info.iterator();
		
		//look for valid information to load
		for (int i = 0; i + 1 < info.size(); i+=2) {
			//get the field name, if valid, and load the accompanying data into 
			// the userInfo object
			String fieldName = iter.next();
			switch(UserInfoFields.valueOf(fieldName)) {
			case MID_NAME:
				ui.setMName(iter.next());
				break;
			case HONORIFIC:
				ui.setHonorific(iter.next());
				break;
			case EMAIL1:
				ui.setEmail1(iter.next());
				break;
			case EMAIL2:
				ui.setEmail2(iter.next());
				break;
			case WORK_PHONE:
				ui.setWorkPhone(iter.next());
				break;
			case CELL_PHONE:
				ui.setCellPhone(iter.next());
				break;
			case HOME_PHONE:
				ui.setHomePhone(iter.next());
				break;
			case ORGANIZATION:
				ui.setOrganization(iter.next());
				break;
			case STREET_ADDRESS:
				ui.setStreetAddress(iter.next());
				break;
			case CITY:
				ui.setCity(iter.next());
				break;
			case REGION:
				ui.setRegion(iter.next());
				break;
			case COUNTRY:
				ui.setCountry(iter.next());
				break;
			case POSTAL:
				ui.setPostal(iter.next());
				break;
			default:
				logger.debug("UserInfo loader called with invalid field " +
						"parameter: \"" + fieldName + "\" with value \"" + 
						iter.next() + "\"");
			}
		}
		
		return ui;
	}
	
	//--------------------------------------------------------
	// User / Group Authorization Create-Delete operations
	//--------------------------------------------------------
	
	@Override
	public int setUserAuthorization(int userId, int groupId, int roleId, 
			boolean isMember, Date beginTime, Date endTime, String caller) {
		
		//validate input parameters
		if (userId <= 0 || groupId <= 0 || roleId <= 0) {
			return INVALID_PARAMETERS;
		}
		if (!timesSane(beginTime, endTime)) {
			return PRECONDITION_ERROR;
		}
		
		//check caller authenticity
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			logger.debug("add user authorization called by invalid caller \"" + 
					caller + "\"");
			//return invalid caller if cannot be authenticated, 
			// or does not match any record in the database
			return INVALID_CALLER;
		}
		
		//check caller authorization
		if (!authorizer.isAuthorizedOnMember(DAOFactory.getDAOFactoryInstance()
				.getUserDAO().getUserID(caller), userId, roleId, groupId)) {
			logger.debug("caller \"" + caller + 
					"\" unauthorized to add user authorization");
			return UNAUTHORIZED;
		}
		
		//change authorization if already set, add new otherwise
		if (DAOFactory.getDAOFactoryInstance().getUserDAO().
				getGroupAuthorizations(userId).containsKey(groupId)) {
	
			return DAOFactory.getDAOFactoryInstance().getUserDAO()
				.updateAuthorization(userId, roleId, groupId, isMember, 
						beginTime, endTime);
		} else {
			
			return DAOFactory.getDAOFactoryInstance().getUserDAO()
				.addAuthorization(userId, roleId, groupId, isMember, beginTime, 
						endTime);
		}
	}
	
	@Override
	public int setGroupAuthorization(int subgroupId, int groupId, int roleId, 
			boolean isSubgroup, String caller) {
		
		//validate input parameters
		if (subgroupId <= 0 || groupId <= 0 || roleId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		//check caller authenticity
		if (caller == null || !DAOFactory.getDAOFactoryInstance().getUserDAO()
				.isDName(caller)) {
			logger.debug("add group authorization called by invalid caller " +
					"\"" + caller + "\"");
			//return invalid caller if cannot be authenticated, 
			// or does not match any record in the database
			return INVALID_CALLER;
		}
		
		//check caller authorization
		if (!authorizer.isAuthorizedOnSubgroup(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(caller), 
				subgroupId, roleId, groupId)) {
			logger.debug("caller \"" + caller + 
					"\" unauthorized to add group authorization");
			return UNAUTHORIZED;
		}
		
		//change authorization if already set, add new otherwise
		if (DAOFactory.getDAOFactoryInstance().getGroupDAO()
				.getAuthorizations(subgroupId).containsKey(groupId)) {
		
			return DAOFactory.getDAOFactoryInstance().getGroupDAO()
				.updateAuthorization(subgroupId, roleId, groupId, isSubgroup);
		} else {
			
			return DAOFactory.getDAOFactoryInstance().getGroupDAO()
				.addAuthorization(subgroupId, roleId, groupId, isSubgroup);
		}
	}
	
	@Override
	public int unsetUserAuthorization(int userId, int groupId, String caller) {
		//validate input parameters
		if (userId <= 0 || groupId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		//check caller authenticity
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			logger.debug("remove user authorization called by invalid caller " +
					"\"" + caller + "\"");
			//return invalid caller if cannot be authenticated, 
			// or does not match any record in the database
			return INVALID_CALLER;
		}
		
		//check caller authorization
		if (!authorizer.isAuthorizedOnMember(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(caller), 
				userId, 0, groupId)) {
			
			logger.debug("caller \"" + caller + 
					"\" unauthorized to remove user authorization");
			return UNAUTHORIZED;
		}
		
		//remove subject authorization
		return DAOFactory.getDAOFactoryInstance().getUserDAO()
			.removeAuthorization(userId, groupId);
	}

	@Override
	public int unsetGroupAuthorization(int subgroupId, int groupId, 
			String caller) {
		//validate input parameters
		if (subgroupId <= 0 || groupId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		//check caller authenticity
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			logger.debug("remove group authorization called by invalid " +
					"caller \"" + caller + "\"");
			//return invalid caller if cannot be authenticated, 
			// or does not match any record in the database
			return INVALID_CALLER;
		}
		
		//check caller authorization
		if (!authorizer.isAuthorizedOnSubgroup(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(caller), 
				subgroupId, 0, groupId)) {
			
			logger.debug("caller \"" + caller + 
					"\" unauthorized to remove group authorization");
			return UNAUTHORIZED;
		}
		
		//remove subject authorization
		return DAOFactory.getDAOFactoryInstance().getUserDAO()
			.removeAuthorization(subgroupId, groupId);
	}
	
	//--------------------------------------------------------
	// Role Create-Read-Update-Delete operations
	//--------------------------------------------------------
	
	@Override
	public int createRole(String roleName, Set<Right> rights, String desc, 
			String caller) {
		//populate role object, and pass to create method
		return createRole1(new Role(0, roleName, rights, desc), caller);
	}
	
	@Override
	public int createRole1(Role role, String caller) {
		//validate input parameters
		if (role == null || role.getRoleName() == null || 
				role.getRights() == null || role.getRoleName().isEmpty() || 
				role.getRights().isEmpty()) {
			return INVALID_PARAMETERS;
		}
		
		//check caller authenticity
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			logger.debug("create role called by invalid caller \"" + caller + 
					"\"");
			//return invalid caller if cannot be authenticated, 
			// or does not match any record in the database
			return INVALID_CALLER;
		}
		
		//check caller authorization
		if (!authorizer.isAuthorizedToCreateRole(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(caller), 
				role.getRights())) {
			logger.debug("caller \"" + caller + "\" unauthorized to add role");
			return UNAUTHORIZED;
		}
		
		//get new role ID, and create new role
		role.setRoleId(
				DAOFactory.getDAOFactoryInstance().getRoleDAO()
					.getNextRoleId());
		DAOFactory.getDAOFactoryInstance().getRoleDAO().addRole(role);
		
		//return appropriately
		return SUCCESS;
	}
	
	@Override
	public int deleteRole(int roleId, String caller) {
		//validate input
		if (roleId <= 0) {
			logger.debug("delete role called with invalid parameter");
			return INVALID_PARAMETERS;
		}
		
		//check caller authenticity
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			//return invalid caller if cannot be authenticated, 
			// or does not match any record in the database
			logger.debug("delete role called by invalid caller \"" + caller + 
					"\"");
			return INVALID_CALLER;
		}
		
		//check authorization
		if (!authorizer.isAuthorizedOnRole(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(caller), 
				roleId)) {
			
			logger.debug("user \"" + caller + "\" unauthorized to delete role");
			return UNAUTHORIZED;
		}
		logger.trace("deleteRole called by \"" + caller + "\" on \"" + roleId + 
				"\"");
		
		//check that no users have this role
		Set<Integer> users = 
			DAOFactory.getDAOFactoryInstance().getRoleDAO().getUsersByRole(
					roleId, /*TODO FIXME*/0);
		if (!users.isEmpty()) {
			return PRECONDITION_ERROR;	//return error if there are still users on this role
		}
		
		//delete role from SAVOIR database
		DAOFactory.getDAOFactoryInstance().getRoleDAO().removeRole(roleId);
		
		return SUCCESS;
	}
	
	@Override
	public int updateRole(int roleId, Role roleInfo, String caller) {
		//validate input
		if (roleId <= 0 || roleInfo == null) {
			return INVALID_PARAMETERS;
		}
		
		//authenticate caller
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			
			logger.debug("update group called by invalid caller \"" + caller + 
					"\"");
			//return invalid caller if cannot be authenticated, 
			// or does not match any record in the database
			return INVALID_CALLER;
		}
		
		//check caller authorization
		if (!authorizer.isAuthorizedOnRole(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(caller), 
				roleId)) {
			
			logger.debug("caller \"" + caller + 
					"\" unauthorized to update role \"" + roleId + "\"");
			return UNAUTHORIZED;
		}
		logger.trace("updateRole called by \"" + caller + "\" on \"" + roleId + 
				"\"");
		
		//updates role in database
		return DAOFactory.getDAOFactoryInstance().getRoleDAO()
			.updateRole(roleId, roleInfo);
	}
	
	@Override
	public List<Role> getRoles() {
		//get roles
		return DAOFactory.getDAOFactoryInstance().getRoleDAO().getRoles();
	}
	
	@Override
	public List<GroupAuthorization> getUserAuthorizations(int groupId, 
			String caller) {
		//validate input
		if(groupId <= 0) {
			return Collections.emptyList();
		}
		
		//authenticate caller
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			
			logger.debug("get user authorizations called by invalid caller " +
					"\"" + caller + "\"");
			//return invalid caller if cannot be authenticated, 
			// or does not match any record in the database
			return Collections.emptyList();
		}
		
		//check authorization
		if (!authorizer.isAuthorizedOnGroup(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(caller),
				UserMgmtAction.VIEW_MEMBERS, groupId)) {
			logger.debug("user \"" + caller + 
					"\" unauthorized to view user authorizations");
			return Collections.emptyList();
		}
		logger.trace("getUserAuthorizations called by \"" + caller + 
				"\" on \"" + groupId + "\"");
		
		//get roles
		return DAOFactory.getDAOFactoryInstance().getRoleDAO()
			.getUserAuthorizationsByGroup(groupId);
	}
	
	@Override
	public List<GroupAuthorization> getGroupAuthorizations(int groupId, 
			String caller) {
		//validate input
		if(groupId <= 0) {
			return Collections.emptyList();
		}
		
		//authenticate caller
		if (caller == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(caller)) {
			
			logger.debug("get group authorizations called by invalid caller " +
					"\"" + caller + "\"");
			//return invalid caller if cannot be authenticated, 
			// or does not match any record in the database
			return Collections.emptyList();
		}
		
		//check authorization
		if (!authorizer.isAuthorizedOnGroup(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(caller),
				UserMgmtAction.VIEW_MEMBERS, groupId)) {
			
			logger.debug("user \"" + caller + 
					"\" unauthorized to view group authorizations");
			return Collections.emptyList();
		}
		logger.trace("getGroupAuthorizations called by \"" + caller + 
				"\" on \"" + groupId + "\"");
		
		//get roles
		return DAOFactory.getDAOFactoryInstance().getRoleDAO().
			getGroupAuthorizationsBySupergroup(groupId);
	}
	
	@Override
	public List<Site> getSites() {
		//get all sites
		List<Site> sites = DAOFactory.getDAOFactoryInstance().getSiteDAO()
				.getSitesByValue("%");
		return sites;
		
	}
	
	@Override
	public void updateUserSite(String userName, String siteUniqDes) {
		//get sites that match the given site
		List<Site> sites = 
			DAOFactory.getDAOFactoryInstance().getSiteDAO()
				.getSitesByValue(siteUniqDes);
		
		//find the exact site that matches the given site
		int siteId = -1;
		if(sites.size() > 0){
			for(int i = 0; i < sites.size(); i++){
				Site theSite = sites.get(i);
				if(theSite.getDescription().compareTo(siteUniqDes) == 0){
					siteId = theSite.getId();
				}
			}
		}
		
		//if found, update user site
		if (siteId != -1) {
			DAOFactory.getDAOFactoryInstance().getUserDAO().updateUserSite(
					userName, siteId);
		}
	}
	
	@Override
	public User getUserByName(String userName){
		//get user from DB
		User u = 
			DAOFactory.getDAOFactoryInstance().getUserDAO()
				.getUserByDN(userName);
		
		//strip password, so it's not sent over the wire
		if (null != u) u.setPassword(null);
		
		return u;
	}
	
	@Override
	public Role getUserRole(String username) {
		if (username != null) {
			//get user for username
			User user = 
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserByDN(username);
			//return user's role
			return user == null ? null : user.getRole();
		} else {
			return null;
		}
	}
}
