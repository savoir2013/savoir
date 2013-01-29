// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package userAdmin;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import OAuth.Validator;

import ca.gc.nrc.iit.savoir.userMgmt.UserMgr;
import ca.gc.iit.nrc.savoir.domain.*;

import static ca.gc.nrc.iit.savoir.utils.XmlUtils.*;

/**
 * Servlet implementation class SavoirUserAdmin
 */
public class SavoirUserAdmin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SavoirUserAdmin.class);
	static String[] CONFIG_FILES = new String[] { "classpath:cxf.xml" };

	/** Action to get list of all users */
	public static final String GET_ALL_USERS = 		"get_all_users";
	/** Action to get list of all groups */
	public static final String GET_GROUPS =		 	"get_groups";
	/** Action to get list of all roles */
	public static final String GET_ROLES = 			"get_roles";
	/** Action to create a user */
	public static final String CREATE_USER = 		"create_user";
	/** Action to delete a user */
	public static final String DELETE_USER = 		"delete_user";
	
	private ApplicationContext ac = new ClassPathXmlApplicationContext(
			CONFIG_FILES);
    private UserMgr userMgr;
    private Validator validator;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SavoirUserAdmin() {
        super();
        userMgr = (UserMgr) this.ac.getBean("userMgrClient");
        validator = (Validator) ac.getBean("userValidator");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		commonDo(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		commonDo(request, response);
	}
	
	/**
	 * Handles incoming messages to this servlet.
	 * 
	 * @param request		The incoming HTTP request
	 * @param response		The HTTP response to make to the request
	 * 
	 * @throws ServletException
	 * @throws IOException
	 */
	private void commonDo(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String msg = request.getParameter("savoirmsg");
		logger.info("Received MSG Req:"  + msg);
		
		//validate caller
		String caller = validator.validateCaller(request);
		
		if (caller == null) {
			//caller not properly authenticated. Return unauthorized error.
			logger.info("Caller unauthorized");
			
			response.setStatus(401);
			response.getWriter().write(
					"Invalid or non-existant authorization token.");
			return;
		}
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(msg)));
			doc.getDocumentElement().normalize();
			XPath xpath = XPathFactory.newInstance().newXPath();
			
			String actionStr = safeEvalString(xpath, "//message/@action", doc);
			
			if (GET_ALL_USERS.equalsIgnoreCase(actionStr)) {
				handleGetAllUsers(doc, caller, response, xpath);
			} else if (GET_GROUPS.equalsIgnoreCase(actionStr)) {
				handleGetGroups(doc, caller, response, xpath);
			} else if (GET_ROLES.equalsIgnoreCase(actionStr)) {
				handleGetRoles(doc, caller, response, xpath);
			} else if (CREATE_USER.equalsIgnoreCase(actionStr)) {
				handleCreateUser(doc, caller, response, xpath);
			} else if (DELETE_USER.equalsIgnoreCase(actionStr)) {
				handleDeleteUser(doc, caller, response, xpath);
			} else {
				response.setStatus(400);
				response.getWriter().write("Messsage is wrong!");
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			response.setStatus(400);
			//response.getWriter().write(ex.printStackTrace());
		}
	}
	
	/**
	 * Handles "{@value #GET_ALL_USERS}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleGetAllUsers(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		List<GroupNode> groups = userMgr.getGroups(caller);
		
		response.setStatus(200);
		String allUsersStr = generateGetAllUsersResp(groups);
		response.getWriter().write(allUsersStr);
		logger.info("Get_All_Users_Resp:" + allUsersStr);
	}
	
	/**
	 * Formats response message for "{@value #GET_ALL_USERS}" actions.
	 * 
	 * @param groups		The roots of the user graph to return the users 
	 * 						from
	 * 
	 * @return The XML response message
	 */
	private String generateGetAllUsersResp(List<GroupNode> groups) {
		
		//parse group nodes into user list
		TreeMap<UserIDName, Set<Group>> users = 
			new TreeMap<UserIDName, Set<Group>>();
		for (GroupNode group : groups) {
			addUsers(group, users);
		}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<message action=\"get_all_users_resp\">\n");
		
//		String userXmlStr = "";
		
		for (Map.Entry<UserIDName, Set<Group>> entry : users.entrySet()) {
			UserIDName user = entry.getKey();
						
			sb.append("<user>\n");
			
			sb.append("<id>").append(user.userID).append("</id>\n");
			sb.append("<dgname>").append(user.userName).append("</dgname>\n");
			sb.append("<givenname>").append(user.givenName)
				.append("</givenname>\n");
			sb.append("<surname>").append(user.surname).append("</surname>\n");
			
			//Role handling below is broken, this should be the user's system 
			// role anyway. As there is a 1:1 correspondance between roles and 
			// groups in SAVOIR, decided to omit role information. If we wished 
			// to include it, would likely add new adminGetGroups() method that 
			// would return the full user graph, populated with User objects 
			// instead of UserIDName
			
			for (Group group : entry.getValue()) {
				sb.append(groupResponseXml(group));
			}
			
			//append blank role
			sb.append("<role>\n" +
					"<id></id><name></name><description></description>\n"
		           + "</role>\n");
			
			sb.append("</user>\n");
			
//			userXmlStr += "<user>\n";
//			userXmlStr += "<id>" + user.getUserID() + "</id>\n"
//			            + "<dgname>" + user.getDName() + "</dgname>"
//			            + "<givenname>" + user.getPerson().getPersonInfo().getFName() + "</givenname>\n"
//			            + "<surname>" + user.getPerson().getPersonInfo().getLName() + "</surname>\n";
//			Group theUserGroup = findoutUserGroup(userMgr.getGroups(userName), user.getUserID());
//			if(theUserGroup != null){
//				userXmlStr +=  "<group>\n" 
//				           + "<id>" + theUserGroup.getGroupId() + "</id>\n"
//				           + "<name>" + theUserGroup.getGroupName() + "</name>\n"
//				           + "<description>" + theUserGroup.getDescription() + "</description>\n"
//				           + "</group>";
//				Role theRole = findoutGroupRole(
//						theUserGroup.getGroupId(), user.getUserID(), userName);
//				if(theRole != null){
//				userXmlStr += "<role>\n" 
//		           + "<id>" + theRole.getRoleId() + "</id>\n"
//		           + "<name>" + theRole.getRoleName() + "</name>\n"
//		           + "<description>" + theRole.getDescription() + "</description>"
//		           + "</role>\n";
//				}else{
//					userXmlStr += "<role>\n" 
//			           + "<id>" + "</id>\n"
//			           + "<name>" + "</name>\n"
//			           + "<description>" + "</description>"
//			           + "</role>\n";
//				}
//			}else{
//				userXmlStr += "<group>\n" 
//		           + "<id>" + "</id>\n"
//		           + "<name>" + "</name>\n"
//		           + "<description>" + "</description>\n"
//		           + "</group>";
//				userXmlStr += "<role>\n" 
//		           + "<id>" + "</id>\n"
//		           + "<name>" + "</name>\n"
//		           + "<description>" + "</description>"
//		           + "</role>\n";
//			}
//			userXmlStr += "</user>\n";
			
		}
		
//		sb.append(usrXmlStr);
		sb.append("</message>\n");
		
		return sb.toString();
	}
	
	/**
	 * Handles "{@value #GET_GROUPS}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleGetGroups(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		List<GroupNode> groups = userMgr.getGroups(caller);
		
		response.setStatus(200);
		String getGroupsResp = generateGetGroupsResp(groups);
		response.getWriter().write(getGroupsResp);
		logger.info("Get_Groups_Resp:" + getGroupsResp);
	}
	
	/**
	 * Formats response message for "{@value #GET_GROUPS}" actions.
	 * 
	 * @param groups		The roots of the group graph to return
	 * 
	 * @return The XML response message
	 */
	private String generateGetGroupsResp(List<GroupNode> groups) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<message action=\"get_groups_resp\">\n");
		for (Group group : flattenTree(groups)) {
			sb.append(groupResponseXml(group));
		}
		sb.append("</message>\n");
		
		return sb.toString();
	}
	
	/**
	 * Handles "{@value #GET_ROLES}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleGetRoles(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		List<Role> roles = userMgr.getRoles();
		
		response.setStatus(200);
		String getRolesResp =  generateGetRolesResp(roles);
		response.getWriter().write(getRolesResp);
		logger.info("Get_Roles_Resp:" + getRolesResp);
	}
	
	/**
	 * Formats response message for "{@value #GET_ROLES}" actions.
	 * 
	 * @param roles		The list of roles to return
	 * 
	 * @return The XML response message
	 */
	private String generateGetRolesResp(List<Role> roles){
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<message action=\"get_roles_resp\">\n");
		if (roles != null) for (Role role : roles) {
			sb.append(roleResponseXml(role));
		}
		sb.append("</message>\n");
		
		return sb.toString();
	}
	
	/**
	 * Handles "{@value #CREATE_USER}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleCreateUser(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		String uName;
		String password;
		String firstName;
		String lastName;
		String email;
		int siteId;
		int groupId;
		int roleId;
		Role role;
		
		//parse parameters
		uName = evalString(xpath, 
				"//service/user/username", doc);
		
		password = evalString(xpath, 
				"//service/user/password", doc);
		
		firstName = evalString(xpath, 
				"//service/user/givenname", doc);

		lastName = evalString(xpath, 
				"//service/user/surname", doc);
		
		Integer parsedSiteId = evalInt(xpath, 
				"//service/user/siteid", doc);
		siteId = (parsedSiteId == null) ? 0 : parsedSiteId;
		
		Integer parsedGroupId = evalInt(xpath, 
				"//service/user/groupid", doc);
		groupId = (parsedGroupId == null) ? 0 : parsedGroupId;
		
		Integer parsedRoleId = evalInt(xpath, 
				"//service/user/roleid", doc);
		if (parsedRoleId != null) {
			roleId = parsedRoleId;
			role = new Role();
			role.setRoleId(roleId);
		} else {
			roleId = 0;
			role = null;
		}
		
		
		email = evalString(xpath, 
				"//service/user/email", doc);
		
		//changed by Aaron 27-Jul-2010 to use unbounded start and end times
		Date beginDate = null;
		Date endDate = null;
//		String beginDateStr = "1900-1-1";
//		String endDateStr = "2019-1-1";
//		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//		Date beginDate = formatter.parse(beginDateStr);
//		Date enddate = formatter.parse(endDateStr);
		
		User user = new User(
				0, uName, password, beginDate, endDate, siteId, role);
		PersonInfo personInfo = new PersonInfo();
		personInfo.setFName(firstName);
		personInfo.setLName(lastName);
		personInfo.setEmail1(email);
		
//		String addResultStr = 
//			addUser(theUser, personInfo, groupId, roleId, userName);
		
		String createUserResp;
		
		//create user
		int retCode = userMgr.createUser1(user, personInfo, caller);
		if (retCode != 0 /* = SUCCESS */) {
			response.setStatus(400);
			createUserResp = generateCreateUserResp(0, false);
			response.getWriter().write(createUserResp);
			logger.info("Create_User_Resp:" + createUserResp);
			return;
		}
		
		//add to group
		int userId = userMgr.getUserByName(uName).getUserID();
		userMgr.setUserAuthorization(
				userId, groupId, roleId, true, beginDate, endDate, caller);
		
		response.setStatus(200);
		createUserResp = generateCreateUserResp(userId, true);
		response.getWriter().write(createUserResp);
		logger.info("Create_User_Resp:" + createUserResp);
	}
	
	/**
	 * Formats response message for "{@value #CREATE_USER}" actions.
	 * 
	 * @param userId		The ID of the newly created user (ignored if 
	 * 						{@code success == false})
	 * @param success		Was the user created successfullly?
	 * 
	 * @return The XML response message
	 */
	private String generateCreateUserResp(int userId, boolean success) {
		if (success) {
			return 
			"<message action=\"create_user_resp\">\n"
			+ "<result>success</result>\n" 
			+ "<userid>" + userId + "</userid>\n"
			+ "</message>";
		} else {
			return 
				"<message action=\"create_user_resp\">\n"
				+ "<result>failure</result>\n" + "</message>";
		}
	}
	
	/**
	 * Handles "{@value #DELETE_USER}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleDeleteUser(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		Integer parsedUserId = evalInt(xpath, 
				"//service/user/userid", doc);
		int userId = (parsedUserId == null) ? 0 : parsedUserId;
		
		int deleteResult = userMgr.deleteUser(userId, caller);
		
		String deleteUserResp;
		if( deleteResult == 0){
			response.setStatus(200);
			deleteUserResp = generateDeleteUserResp(true);
		}else{
			//delete fail
			response.setStatus(400);
			deleteUserResp = generateDeleteUserResp(false);
		}
		response.getWriter().write(deleteUserResp);
		logger.info("Delete_User_Resp:" + deleteUserResp);
	}
	
	/**
	 * Formats response message for "{@value #DELETE_USER}" actions.
	 * 
	 * @param success		Was the action successful?
	 * 
	 * @return The XML response message
	 */
	private String generateDeleteUserResp(boolean success) {
		return 
			"<message action=\"delete_user_resp\">\n" 
			+ "<result>" + (success ? "success" : "failure") + "</result>\n" 
			+ "</message>\n";
	}
	
	/**
	 * Formats a group object as XML
	 * 
	 * @param group			The group object to format
	 * 
	 * @return an XML representation of the group object
	 */
	public static String groupResponseXml(Group group) {
		return 
			"<group>\n" 
	        + "<id>" + group.getGroupId() + "</id>\n"
	        + "<name>" + group.getGroupName() + "</name>\n"
	        + "<description>" + group.getDescription() + "</description>\n"
	        + "</group>\n";
	}
	
	/**
	 * Formats a role object as XML
	 * 
	 * @param role			The role object to format
	 * 
	 * @return an XML representation of the role object
	 */
	private String roleResponseXml(Role role) {
		return 
			"<role>\n"
	        + "<id>" + role.getRoleId() + "</id>\n"
	        + "<name>" +  role.getRoleName() + "</name>\n"
	        + "<rights>" + role.getRights() + "</rights>\n"
	        + "<description>" + role.getDescription() + "</description>\n"
	        + "</role>\n";
	}
	
	/**
	 * Adds the users in a user graph to a tree mapping users to groups
	 *  
	 * @param group		The root of the user graph to consider
	 * @param users		The exisiting map of users to groups (will be modified)
	 */
	private void addUsers(GroupNode group, 
			TreeMap<UserIDName, Set<Group>> users) {
		
		if (group == null || users == null) return;
		
		Group newGroup = group.getGroup();
		List<UserIDName> members = group.getMembers();
		if (members != null) for (UserIDName member : members) {
			//get set of groups for this member
			Set<Group> groupSet = users.get(member);
			//intialize if unset
			if (groupSet == null) {
				groupSet = new TreeSet<Group>();
				users.put(member, groupSet);
			}
			//add new group to groups
			groupSet.add(newGroup);
		}
		
		List<GroupNode> subgroups = group.getSubgroups();
		if (subgroups != null) for (GroupNode subgroup : subgroups) {
			addUsers(subgroup, users);
		}
	}
	
	/**
	 * Flattens a group graph to flat list with no duplicates
	 * 
	 * @param groups	The group graph to flatten
	 * 
	 * @return	a list of all the groups in the graph, flattened using a 
	 * 		depth-first search, keeping only the first occurence of duplicates.
	 */
	private List<Group> flattenTree(List<GroupNode> groups) {
		if (groups == null) return null;
		
		Set<Group> flat = new LinkedHashSet<Group>();
		for (GroupNode group : groups) {
			flattenTree(group, flat);
		}
		return new ArrayList<Group>(flat);
	}
	
	/**
	 * Flattens a group graph to a collection, adding groups to the collection 
	 * in depth-first order.
	 * 
	 * @param group		The group graph
	 * @param flat		The collection to add groups too
	 */
	private void flattenTree(GroupNode group, Collection<Group> flat) {
		flat.add(group.getGroup());
		
		List<GroupNode> subgroups = group.getSubgroups();
		if (subgroups != null) for (GroupNode subgroup : subgroups) {
			flattenTree(subgroup, flat);
		}
	}
	
//	private Group findoutUserGroup(List<GroupNode> groups, int userId){
//		for (GroupNode group : groups) {
//			for (UserIDName user : group.getMembers()) {
//				if (user.userID == userId) {
//					return group.getGroup();
//				}
//			}
//			if (group.getSubgroups() != null) {
//				findoutUserGroup(group.getSubgroups(), userId);
//			}
//		}
//		return null;
//	}
//	
//	private Role findoutGroupRole(int groupId, int userId, String userName){
//		List<GroupAuthorization> groupAuth = 
//			userMgr.getUserAuthorizations(groupId, userName);
//		if (groupAuth != null) {
//			for (GroupAuthorization ga : groupAuth) {
//				if (ga.getAuthorizedId() == userId) {
//					return ga.getRole();
//				}
//			}
////			WRONG
////			if (groupAuth.size() > 0) {
////				return groupAuth.get(0).getRole();
////			}
//		}
//		return null; 
//	}

}
