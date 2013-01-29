// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package broker;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

import ca.gc.nrc.iit.savoir.userMgmt.UserMgr;
import ca.gc.nrc.iit.savoir.scenarioMgmt.ScenarioMgr;
import ca.gc.nrc.iit.savoir.sessionMgmt.SessionMgr;
import ca.gc.nrc.iit.savoir.model.MessageTransformer;
import ca.gc.nrc.iit.savoir.model.profile.ResourceWidget;
import ca.gc.nrc.iit.savoir.resourceMgmt.HostConnectedToMultipleEndPointsException;
import ca.gc.nrc.iit.savoir.resourceMgmt.ResourceMgr;
import ca.gc.iit.nrc.savoir.domain.*;

import OAuth.Validator;

import static ca.gc.nrc.iit.savoir.utils.XmlUtils.*;

/**
 * Servlet implementation class SavoirWebBroker
 */
public class SavoirWebBroker extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/*
	 * Actions of WebBroker messages
	 */
	/** Action to start unauthored session */
	public static final String START_UNAUTHORED_SESSION = 	"start";
	/** Action to get user profile */
	public static final String GET_USER_PROFILE = 			"get_user_profile";
	/** Action to get full user profile */
	public static final String GET_FULL_USER_PROFILE = 		"get_full_user_profile";
	/** Action to save user profile */
	public static final String SAVE_USER_PROFILE = 			"save_user_profile";
	/** Action to get a list of scenarios */
	public static final String GET_SCENARIOS = 				"get_scenarios";
	/** Action to initialize a new default session */
	public static final String INIT_DEFAULT_SESSION = 		"initialize_default_session";
	/** Action to get the status of a specific session */
	public static final String GET_SESSION_STATUS = 		"get_session_status";
	/** Action to start an authored session */
	public static final String START_AUTHORED_SESSION = 	"start_authored_session";
	/** Action to end a session */
	public static final String END_SESSION = 				"end_session";
	
	private static final Logger logger = Logger.getLogger(SavoirWebBroker.class);
	static String[] CONFIG_FILES = new String[] { "classpath:cxf.xml" };

	static ApplicationContext ac = new ClassPathXmlApplicationContext(
			CONFIG_FILES);
	private ResourceMgr resMgr;
    private SessionMgr sesMgr;
    private ScenarioMgr scnMgr;
    private UserMgr userMgr;
    private Validator validator;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SavoirWebBroker() {
        super();
        resMgr = (ResourceMgr) ac.getBean("resourceMgrClient");
        sesMgr = (SessionMgr) ac.getBean("sessionMgrClient");
        scnMgr = (ScenarioMgr) ac.getBean("scenarioMgrClient");
        userMgr = (UserMgr) ac.getBean("userMgrClient");
        validator = (Validator) ac.getBean("userValidator");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {
		commonDo(request, response);
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {
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
		/*
		 * For each message:
		 * 		log the incoming message
		 * 		validate the caller's authentication
		 * 		parse message as XML
		 * 		get action string
		 * 		call action-specific handleXxx(doc, caller, response, xpath) 
		 * 			method
		 * 		in each of the handleXxx():
		 * 			get session ID (if needed)
		 * 			check the given user ID against the authenticated (if 
		 * 				neeeded) 
		 * 			parse out any other required parameters
		 * 			call the appropriate MgmtService
		 * 			build a response message using an action-specific 
		 * 				generateXxxResp() method (NOTE that the 
		 * 				generateXxxResp() method is strictly for formatting - 
		 * 				it should not make any calls to the MgmtServices, but 
		 * 				instead have all its neccessary data passed in by the 
		 * 				handleXxx() method)
		 * 			log the response message
		 * 			(if in any case there is an error preventing standard 
		 * 				response, log it instead)
		 * 			send the response message to the response writer
		 */
		
		String msg = request.getParameter("savoirmsg");
		logger.info("Authorization header: " + request.getHeader("Authorization"));
		logger.info("Received MSG Req:"  + msg);
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
			
			if (START_UNAUTHORED_SESSION.equalsIgnoreCase(actionStr)) {
				handleStartUnauthoredSession(doc, caller, response, xpath);
			} else if (GET_USER_PROFILE.equalsIgnoreCase(actionStr)
					|| GET_FULL_USER_PROFILE.equalsIgnoreCase(actionStr)) {
				handleGetUserProfile(doc, caller, response, xpath);
			} else if (SAVE_USER_PROFILE.equalsIgnoreCase(actionStr)) {
				handleSaveUserProfile(doc, caller, response, xpath);
			} else if (GET_SCENARIOS.equalsIgnoreCase(actionStr)) {
				handleGetScenarios(doc, caller, response, xpath);
			} else if (INIT_DEFAULT_SESSION.equalsIgnoreCase(actionStr)) {
				handleInitDefaultSession(doc, caller, response, xpath);
			} else if (GET_SESSION_STATUS.equalsIgnoreCase(actionStr)) {
				handleGetSessionStatus(doc, caller, response, xpath);
			} else if (START_AUTHORED_SESSION.equalsIgnoreCase(actionStr)) {
				handleStartAuthoredSession(doc, caller, response, xpath);
			} else if (END_SESSION.equalsIgnoreCase(actionStr)) {
				handleEndSession(doc, caller, response, xpath);
			} else {
				response.setStatus(400);
				response.getWriter().write("Unrecognized message action \"" + 
						actionStr + "\"!");
			}
		}catch (Exception ex) {
			ex.printStackTrace();
			response.setStatus(400);
			response.getWriter().write(ex.getMessage());
		}
	}
			
/*
 * Removed actions
 */
//		else if (actionStr.equalsIgnoreCase("join_session")) {
//			XPathExpression exprUserDefaultSessionID = xpath.compile("//message/@sessionID");
//			result = exprUserDefaultSessionID.evaluate(doc, XPathConstants.NODESET);
//			nodes = (NodeList) result;
//			int defaultSessionID = -1;
//			if(nodes.getLength() == 1){
//				defaultSessionID = Integer.valueOf(nodes.item(0).getNodeValue());
//				logger.info("defaultSessionID:" + defaultSessionID);
//			}else{
//				response.setStatus(400);
//				response.getWriter().write("The default session ID is wrong");
//				return;
//			}
//			XPathExpression exprJoinSessionID = xpath.compile("//message/service/parameter[@id='join_sessionID']/@value");
//			result = exprJoinSessionID.evaluate(doc, XPathConstants.NODESET);
//			nodes = (NodeList) result;
//			int joinSessionID = -1;
//			if(nodes.getLength() == 1){
//				joinSessionID = Integer.valueOf(nodes.item(0).getNodeValue());
//				logger.info("JoinSessionID" + joinSessionID);
//			}else{
//				response.setStatus(400);
//				response.getWriter().write("The join session ID is wrong");
//				return;
//			}
//			XPathExpression exprJoinUserName = xpath.compile("//message/service/parameter[@id='username']/@value");
//			result = exprJoinUserName.evaluate(doc, XPathConstants.NODESET);
//			nodes = (NodeList) result;
//			String userName = "";
//			if(nodes.getLength() == 1){
//				userName = nodes.item(0).getNodeValue();
//				
//			}else{
//				response.setStatus(400);
//				response.getWriter().write("The username is not there");
//				return;
//			}
//			//sesMgr.joinSession(joinSessionID, userName);
////			Session joinedSession = sesMgr.getSessionById(joinSessionID);
////			Person thePerson = sesMgr.getPersonByUserName(userName);
////			sesMgr.updateSessionPaticipantStatus(joinedSession, thePerson);
//			//added for demo
//			startSession(joinSessionID);
//			String joinResp = generateJoinResp(joinSessionID);
//			response.setStatus(200);
//			response.getWriter().write(joinResp);
//			logger.info("Join_Resp:" + joinResp);
//			//end
//	}

	/**
	 * Handles "{@value #START_UNAUTHORED_SESSION}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleStartUnauthoredSession(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		//get session ID
		Integer sessionId = evalInt(xpath, "//message/@sessionID", doc);
		if (sessionId == null) {
			response.setStatus(400);
			response.getWriter().write("The start session ID is wrong");
			return;
		}
		
		//modified by yyh for unauthored session
		String sessionType = 
			evalString(xpath, "//message/service/sessiontype", doc);
		if (sessionType == null || sessionType.isEmpty()){
			response.setStatus(400);
			response.getWriter().write("The sessionType is not there");
			return;
		}
		sessionType = sessionType.trim();					
		
		if(sessionType.equalsIgnoreCase("unauthored")) {
			String resourceName = 
				safeEvalString(xpath, "//message/service/resource/@id", doc);
			if (resourceName == null) {
				logger.info("Cannot find resource name for start session");
				response.setStatus(400);
				response.getWriter().write("The resource name is not there");
				return;
			}
			resourceName = resourceName.trim();
			
			boolean startResult = false;
			int subSessionId = -1;
				subSessionId = startUnAuthoredSession(sessionId, resourceName);
				startResult = (subSessionId == -1) ? false : true;
			
			String startUnauthoredSessionResp = 
				generateStartUnauthoredSessionResp(startResult, subSessionId);
			response.setStatus(200);
			response.getWriter().write(startUnauthoredSessionResp);
			logger.info("Start_Resp:" + startUnauthoredSessionResp);
		}else{
		    sesMgr.beginSession(sessionId, caller);
		}
	}
	
	/**
	 * Formats response message for "{@value #START_UNAUTHORED_SESSION}" 
	 * actions.
	 * 
	 * @param success		Was the action successful?
	 * @param sessionId		The session ID of the started session
	 * 
	 * @return The XML response message
	 */
	private String generateStartUnauthoredSessionResp(boolean success, 
			int sessionId) {
		
		if(success == true){
			return 
				"<message action=\"start_session_resp\">\n"
				+ "<result>success</result>\n" 
				+ "<sessionID>" + sessionId + "</sessionID>\n"
				+ "</message>";
		}else{
			return 
				"<message action=\"start_session_resp\">\n"
				+ "<result>fail</result>\n" 
				+ "</message>";
		}
	}
	
	/**
	 * Handles "{@value #GET_USER_PROFILE}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleGetUserProfile(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		User user = userMgr.getUserByName(caller);
		
//		checkAndUpdateSelectableResources(user);
		List<ResourceWidget> resources = resMgr.getWidgetsForUser(caller);
		
		String getUserProfileResp = 
			generateUserProfileResp(user, resources);
		response.setStatus(200);
		response.getWriter().write(getUserProfileResp);
		logger.info("Get_User_Profile_Resp:" + getUserProfileResp);
	}
	
	/**
	 * Formats response message for "{@value #GET_USER_PROFILE}" and 
	 * "{@value #GET_FULL_USER_PROFILE}" actions.
	 * 
	 * TODO needs cleanup, generifying to general resources
	 *
	 * @param user			The user this profile was acquired for
	 * @param resources		The list of resource widgets to format for return
	 * @param isFull		{@code true} for "{@value #GET_FULL_USER_PROFILE}", 
	 * 						{@code false} for "{@value #GET_USER_PROFILE}".
	 * 
	 * @return The XML response message
	 */
	private String generateUserProfileResp(User user, 
			List<ResourceWidget> resources){
		
		boolean isAdmin = false;
		if (user.getRole().getRoleId() == 1) {
			//if user is sysadmin (role 1)
			isAdmin = true;
		}
		
		//account for empty list being nulled by WS:
		if (resources == null) resources = new ArrayList<ResourceWidget>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("<message action=\"get_user_profile_resp\">\n");
		for (ResourceWidget resource : resources) {
			sb.append(resourceWidgetResponseXml(resource));
		}
		sb.append("<isAdmin>").append(isAdmin).append("</isAdmin>\n");
		sb.append("</message>\n");
		
		return sb.toString();
	}
	
	/**
	 * Formats a resource widget object as XML
	 * 
	 * @param widget		The resource widget object to format
	 * 
	 * @return an XML representation of the resource widget object
	 */
	private String resourceWidgetResponseXml(ResourceWidget widget) {
		StringBuilder sb = new StringBuilder();
		
		widget.getWidget();
		sb.append("<resource ");
			addAttr(sb, "id", 
					Integer.toString(widget.getResourceId()), REQUIRED);
			addAttr(sb, "name", widget.getResourceName(), REQUIRED);
			addAttr(sb, "preference", widget.getState().toString(), REQUIRED);
		sb.append(">\n");
		
// The following line is commented out since we have issues returning a complete widget
// to the web broker. See comments in the ResourceMgrImpl.java file at the top of the
// getWidgetsForUser() function for more details
//			sb.append(MessageTransformer.toXml(widget.getWidget())).append('\n');
		sb.append("</resource>\n");
		
		return sb.toString();
	}
	
	/**
	 * Handles "{@value #SAVE_USER_PROFILE}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleSaveUserProfile(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		//get widget states
		NodeList resources = 
			evalNodeList(xpath, "//message/service/resource", doc);
		if (resources == null || resources.getLength() <= 0) {
			logger.info("Could not find widget string for save user profile");
			response.setStatus(400);
			response.getWriter().write("The widgets are not there");
			return;
		}
		
		for (int i = 0; i < resources.getLength(); i++) {
			Node resource = resources.item(i);
			
			Integer resourceId = evalInt(xpath, "@resourceId", resource);
			if (resourceId == null) continue;

			ResourcePreference pref = evalEnum(
					xpath, "@preference", ResourcePreference.class, resource);
			if (pref == null) continue;
			
			resMgr.updateWidgetStateForUser(resourceId, pref, caller);
		}

//		//get widget string
//		String resStr = safeEvalString(xpath, 
//				"//message/service/parameter[@id='widgets']/@value", doc);
//		if (resStr == null) {
//			logger.info("Could not find widget string for save user profile");
//			response.setStatus(400);
//			response.getWriter().write("The widgets string are not there");
//			return;
//		}
//		
//		String[] resArray = resStr.split(",");
//		Set<Integer> selectedResIds = new HashSet<Integer>();
//		for (String resId : resArray) {
//			try {
//				selectedResIds.add(Integer.valueOf(resId));
//			} catch (NumberFormatException ignored) {}
//		}
//		
//		User user = userMgr.getUserByName(userName);
//		int userId = user.getUserID();
//		
//		checkAndUpdateSelectableResources(user);
//		
//		List<Resource> resources = resMgr.getResourcesByUserID(userId);
//		
//		
//		if (resources != null) {
//			logger.info("There are " + resources.size() + " resources " +
//					"belonging to userID " + user.getUserID());
//			
//			for (Resource resource : resources) {
//				int resId = resource.getResourceID();
//				boolean isSelected = selectedResIds.contains(resId);
//				
//				resMgr.updateSelectedResourceByuserIDAndResID(
//						userId, resId, isSelected);
//				logger.info((isSelected ? "Selected" : "Unselected") + 
//						" resourceID" + resId + " for userID" + userId);				
//			}
//		} else {
//			logger.info("There are no resources belonging to userID " + 
//					user.getUserID());
//		}
		
		String saveUserProfileRespStr = generateSaveUserProfileResp();
		response.setStatus(200);
		response.getWriter().write(saveUserProfileRespStr);
		logger.info("Save_User_Profile_Resp:" + saveUserProfileRespStr);
	}
	
	/**
	 * Formats response message for "{@value #SAVE_USER_PROFILE}" actions.
	 * 
	 * @return The XML response message
	 */
	private String generateSaveUserProfileResp(){
		return 
			"<message action=\"save_user_profile_resp\">\n"
			+ "<result>success</result>\n"
			+ "</message>";
	}
	
	/**
	 * Handles "{@value #GET_SCENARIOS}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleGetScenarios(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
	
		List<Scenario> scenarios = scnMgr.getScenarios();
		
		String getScenariosResp = 
			generateGetScenariosResp(scenarios);
		response.setStatus(200);
		response.getWriter().write(getScenariosResp);
		logger.info("Get_Scenarios_Resp:" + getScenariosResp);
	}
	
	/**
	 * Formats response message for "{@value #GET_SCENARIOS}" actions.
	 * 
	 * @param scenarios		The scenarios to return
	 * 
	 * @return The XML response message
	 */
	private String generateGetScenariosResp(List<Scenario> scenarios){
		
		StringBuilder sb = new StringBuilder();
		sb.append(
				"<message action=\"get_scenarios_resp\">\n"
				+ "<result>success</result>\n"); 
		if (scenarios != null) for (Scenario scenario : scenarios) {
			sb.append(scenarioResponseXml(scenario));
		}
		sb.append("</message>");
		
		return sb.toString();
	}
	
	/**
	 * Handles "{@value #INIT_DEFAULT_SESSION}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleInitDefaultSession(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
	
		Integer sessionId = evalInt(xpath, "//message/@sessionID", doc);
		if (sessionId == null) sessionId = 0;
		
		String site = evalString(xpath, 
				"//service/parameter[@id='Site_Location']/@value", 
				doc);
		
		Session defaultSession = null;
		if (sessionId == 0) {
			defaultSession = getNewSession(caller);
			if (defaultSession == null) {
				logger.info("Could not create default session");
				response.setStatus(400);
				response.getWriter().write("Could not create default session");
				return;
			}
			sessionId = defaultSession.getSessionID();
		}
		
		userMgr.updateUserSite(caller, site);
		
		response.setStatus(200);
		String initDefaultSessionResp = 
			generateInitDefaultSessionResp(sessionId);
		response.getWriter().write(initDefaultSessionResp);
		logger.info("Initialize_Default_Session_Resp:" 
				+ initDefaultSessionResp);
	}
	
	/**
	 * Formats response message for "{@value #INIT_DEFAULT_SESSION}" actions.
	 * 
	 * @param sessionId		The session that was initialized
	 * 
	 * @return The XML response message
	 */
	private String generateInitDefaultSessionResp(int sessionId) {
		
		return
			"<message action=\"initialize_default_session_resp\" " 
			+ "sessionID=\"" + sessionId + "\">\n"
			+ "<result>success</result>\n" 
			+ "</message>";
	}
	
	/**
	 * Handles "{@value #GET_SESSION_STATUS}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleGetSessionStatus(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
	
		Integer sessionId = evalInt(xpath, "//message/@sessionID", doc);
		if (sessionId == null) {
			response.setStatus(400);
			response.getWriter().write("The session ID is wrong");
			logger.info("Could not get session status, invalid session ID");
			return;
		}
		
		String status = sesMgr.getSessionStatus(sessionId);
		
		String getSessionStatusResp = 
			generateGetSessionStatusResp(sessionId, status);
		response.setStatus(200);
		response.getWriter().write(getSessionStatusResp);
		logger.info("get_Session_status_Resp:" + getSessionStatusResp);
	}
	
	/**
	 * Formats response message for "{@value #GET_SESSION_STATUS}" actions.
	 * 
	 * @param sessionId		The ID of the session
	 * @param status		The session status
	 * 
	 * @return The XML response message
	 */
	private String generateGetSessionStatusResp(int sessionId, String status){

		return 
			"<message action=\"get_session_status_resp\" " +
			"sessionID=\"" + sessionId + "\">\n"
			+ "<status>" + status +  "</status>\n" 
			+ "</message>";
	}
	
	/**
	 * Handles "{@value #START_AUTHORED_SESSION}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleStartAuthoredSession(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
	
		Integer sessionId = evalInt(xpath, "//message/@sessionID", doc);
		if (sessionId == null) {
			response.setStatus(400);
			response.getWriter().write("The session ID is wrong");
			logger.info("Could not start authored session, invalid session ID");
			return;
		}

		try {
			sesMgr.runSession(sessionId, caller);
		} catch (Exception e) {
			response.setStatus(400);
			response.getWriter().write(e.getMessage());
			logger.info("Could not start authored session: " + e.getMessage());
			return;
		}
		
		String startAuthoredSessionResp = 
			generateStartAuthoredSessionResp(sessionId);
		response.setStatus(200);
		response.getWriter().write(startAuthoredSessionResp);
		logger.info("start_authored_session_Resp:" + startAuthoredSessionResp);
	}
	
	/**
	 * Formats response message for "{@value #START_AUTHORED_SESSION}" actions.
	 * 
	 * @param sessionId		The ID of the session that was started
	 * 
	 * @return The XML response message
	 */
	private String generateStartAuthoredSessionResp(int sessionId) {
		
		return
			"<message action=\"start_authored_session_resp\" " 
			+ "sessionID=\"" + sessionId + "\">\n"
			+ "<result>success</result>\n" 
			+ "</message>";
	}
	
	/**
	 * Handles "{@value #END_SESSION}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleEndSession(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		//get session ID
		Integer sessionId = evalInt(xpath, "//message/@sessionID", doc);
		if (sessionId == null) {
			response.setStatus(400);
			response.getWriter().write("No session ID!");
			return;
		}
		
		sesMgr.endSession(sessionId, caller);
		
		response.setStatus(200);
		String endSessionResp = generateEndSessionResp(sessionId);
		response.getWriter().write(endSessionResp);
		logger.info("end_session_Resp:" + endSessionResp);
	}
	
	/**
	 * Formats response message for "{@value #END_SESSION}" actions.
	 * 
	 * @param sessionId		The ID of the session that was ended
	 * 
	 * @return The XML response message
	 */
	private String generateEndSessionResp(int sessionId) {
		return 
			"<message action=\"end_session_resp\" " 
			+ "sessionID=\"" + sessionId + "\">\n"
			+ "<result>success</result>\n" 
			+ "</message>";
	}
	
	/**
	 * Formats a scenario object as XML
	 * 
	 * @param scenario		The scenario object to format
	 * 
	 * @return an XML representation of the scenario object
	 */
	public static String scenarioResponseXml(Scenario scenario) {
		
		String lastModified = "";
		Date lm = scenario.getLastModified();
		if (lm != null) {
			lastModified = Long.toString(lm.getTime());
		}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<scenario ");
		addAttr(sb, "scenarioID", Integer.toString(scenario.getScenarioId()), REQUIRED);
		addAttr(sb, "name", scenario.getScenarioName(), REQUIRED);
		addAttr(sb, "lastModified", lastModified, REQUIRED);
		addAttr(sb, "authorName", scenario.getAuthorName(), REQUIRED);
		addAttr(sb, "description", scenario.getDescription(), REQUIRED);
		addAttr(sb, "deviceNames", scenario.getDeviceNames(), REQUIRED);
		addAttr(sb, "apnConnections", scenario.getApnParameters(), REQUIRED);
		sb.append("/>\n");
		
		return sb.toString();
		
	}
	
	/**
	 * Requests a new default session from the session manager
	 * 
	 * @param userName		The login ID of the user to request the session for
	 *  
	 * @return	the new session object
	 */
	private Session getNewSession(String userName) {
		return sesMgr.newSessionAgnostic(new Date(), userName);
	}
	
	private int startUnAuthoredSession(int sessionID, String resName){
//		Calendar startTime = Calendar.getInstance();
//		Calendar endTime = Calendar.getInstance();
//		startTime.setTimeInMillis(startTime.getTimeInMillis() + (1000 * 30));
//		endTime.setTimeInMillis(startTime.getTimeInMillis() + (1000 * 60 * 60));
//		subSession.setRequestedStartTime(startTime);
//		subSession.setRequestedEndTime(endTime);
//		
//		this.sesMgr.updateSession(subSession);
//		return true;
		String sessionName =  resName + " subsession";
		return this.createSubSession(sessionID, sessionName);
		
	}
	
	private int createSubSession(int sessionID, String sessionName){
		Session subSession = new Session();
		Calendar startTime = Calendar.getInstance();
		Calendar endTime = Calendar.getInstance();
		startTime.setTimeInMillis(startTime.getTimeInMillis() + (1000 * 30));
		endTime.setTimeInMillis(startTime.getTimeInMillis() + (1000 * 60 * 60));
		subSession.setRequestedStartTime(startTime);
		subSession.setRequestedEndTime(endTime);
		subSession.setName(sessionName);
		return this.sesMgr.createSubSession(sessionID, subSession);
	}
	
//	private void checkAndUpdateSelectableResources(User user){
//		if (user == null) return;
//		
//		int userId = user.getUserID();
//		String userName = user.getDName();
//		
//		List<Resource> selectableResList = resMgr.getResourcesByUserID(userId);
//		List<Resource> authResList = credMgr.getAuthorizedResources(userName);
//		HashSet<Integer> authResIdSet = new HashSet<Integer>();
//		
//		if(authResList == null){
//			return;
//		}
//		
//		for (Resource res : authResList) {
//			authResIdSet.add(res.getResourceID());
//		}
//		
//		if (selectableResList != null) {
//			HashSet<Integer> selectableResIdSet = new HashSet<Integer>();
//			for (Resource selRes : selectableResList) {
//				selectableResIdSet.add(selRes.getResourceID());
//				if (!authResIdSet.contains(selRes.getResourceID())) {
//					resMgr.removeSelectableResourceForUser(
//							userId, selRes.getResourceID());
//			    }
//			}
//			
//			for (Resource authRes : authResList) {
//				if (!selectableResIdSet.contains(authRes.getResourceID())) {
//					resMgr.addSelectedResourceForUser(
//							userId, authRes.getResourceID());
//				}
//			}
//			
//		}else{
//			for (Resource authRes : authResList) {
//				resMgr.addSelectedResourceForUser(
//						userId, authRes.getResourceID());
//			}
//		}
//	}
	
//	private Session getNewSession(String userName){
////		JaxWsProxyFactoryBean pf = new JaxWsProxyFactoryBean();
////	    pf.setServiceClass(SessionMgr.class);
////	    pf.setAddress("http://localhost:9004/SAVOIR_MgmtServices/services/SessionManagerWS");
////	    SessionMgr sgmt = (SessionMgr)pf.create();
//		//01-13-10 found the following method always return user moss Aaron. Later we need look into the probelm, 
//		//now I just use back the method to create a new session in the temp method below
//	    return sesMgr.newSessionAgnostic(
//	    		Calendar.getInstance().getTime(), userName);
//		
//	}
	
	//TODO determine whether this needs to be used? should it be moved to 
	// session admin servlet?
//	private void processNewAuthoredSession(Document doc, 
//			HttpServletResponse response, XPath xpath) 
//			throws XPathExpressionException, IOException {
//		Object result;
//		NodeList nodes;
//		Node node;
//		
//		/*
//		 * get username
//		 */
//		XPathExpression exprUserName = 
//			xpath.compile("//message/service/parameter[@id='username']/@value");
//		result = exprUserName.evaluate(doc, XPathConstants.NODESET);
//		nodes = (NodeList) result;
//		String userName  = null;
//		
//		if(nodes.getLength() == 1) {
//			userName = nodes.item(0).getNodeValue();
//		}
//		
//		/*
//		 * get session creation parameters
//		 */
//		XPathExpression exprSessionName = 
//			xpath.compile("//message/service/parameter[@id='sessionName']/@value");
//		result = exprSessionName.evaluate(doc, XPathConstants.NODESET);
//		nodes = (NodeList)result;
//		
//		String sessionName = null;
//		if (nodes.getLength() == 1) {
//			sessionName = nodes.item(0).getNodeValue();
//		} else {
//			response.setStatus(400);
//			response.getWriter().write("sessionName not found");
//			return;
//		}
//		
//		XPathExpression exprDescription = 
//			xpath.compile("//message/service/parameter[@id='description']/@value");
//		result = exprDescription.evaluate(doc, XPathConstants.NODESET);
//		nodes = (NodeList)result;
//		
//		String description = null;
//		if (nodes.getLength() == 1) {
//			description = nodes.item(0).getNodeValue();
//		}
//		//description is optional, do not fail on not present
//		
//		XPathExpression exprScenarioId = 
//			xpath.compile("//message/service/parameter[@id='scenarioID']/@value");
//		result = exprScenarioId.evaluate(doc, XPathConstants.NODESET);
//		nodes = (NodeList)result;
//		
//		int scenarioId = 0;
//		if (nodes.getLength() == 1) {
//			try {
//				scenarioId = Integer.parseInt(nodes.item(0).getNodeValue());
//			} catch (NumberFormatException e) {
//				response.setStatus(400);
//				response.getWriter().write("scenarioID not integer");
//				return;
//			}
//		} else {
//			response.setStatus(400);
//			response.getWriter().write("scenarioID not found");
//			return;
//		}
//		
//		XPathExpression exprStartTime = 
//			xpath.compile("//message/service/parameter[@id='startTime']/@value");
//		result = exprStartTime.evaluate(doc, XPathConstants.NODESET);
//		nodes = (NodeList)result;
//		
//		Date startTime = null;
//		if (nodes.getLength() == 1) {
//			try {
//				startTime = 
//					new Date(Long.parseLong(nodes.item(0).getNodeValue()));
//			} catch (NumberFormatException e) {
//				response.setStatus(400);
//				response.getWriter().write("startTime not integer");
//				return;
//			}
//		} else {
//			startTime = new Date(); //set to now if not present
//		}
//		
//		XPathExpression exprEndTime = 
//			xpath.compile("//message/service/parameter[@id='endTime']/@value");
//		result = exprEndTime.evaluate(doc, XPathConstants.NODESET);
//		nodes = (NodeList)result;
//		
//		Date endTime = null;
//		if (nodes.getLength() == 1) {
//			try {
//				endTime = 
//					new Date(Long.parseLong(nodes.item(0).getNodeValue()));
//			} catch (NumberFormatException e) {
//				response.setStatus(400);
//				response.getWriter().write("endTime not integer");
//				return;
//			}
//		} else {
//			response.setStatus(400);
//			response.getWriter().write("endTime not found");
//			return;
//		}
//		
//		/*
//		 * create session
//		 */
//		Session session = 
//			sesMgr.newSessionAuthored1(sessionName, description, scenarioId, 
//					startTime, endTime, userName);
//		if (session == null) {
//			//session creation failed
//			response.setStatus(400);
//			response.getWriter().write("session creation failed");
//			return;
//		}
//		int sessionId = session.getSessionID();
//		
//		/*
//		 * add authorizations
//		 */
//		XPathExpression exprUsers = xpath.compile("//message/service/user");
//		result = exprUsers.evaluate(doc, XPathConstants.NODESET);
//		nodes = (NodeList)result;
//		
//		XPathExpression exprUserId = xpath.compile("@userID");
//		XPathExpression exprRoleId = xpath.compile("@roleID");
//		int userId, roleId;
//		String resStr;
//		
//		for (int i = 0; i < nodes.getLength(); i++) {
//			node = nodes.item(i);
//			
//			userId = 0; roleId = 0;
//			resStr = "";
//			
//			resStr = (String)exprUserId.evaluate(node, XPathConstants.STRING);
//			try {
//				userId = Integer.parseInt(resStr);
//			} catch (NumberFormatException e) {
//				//skip user on non int ID (participant list will be returned 
//				// at end, so errors can be detected by user)
//				continue; 
//			}
//			
//			resStr = "";
//			resStr = (String)exprRoleId.evaluate(node, XPathConstants.STRING);
//			try {
//				roleId = Integer.parseInt(resStr);
//			} catch (NumberFormatException e) {
//				//skip user on non int ID (participant list will be returned 
//				// at end, so errors can be detected by user)
//				continue; 
//			}
//			
//			//authorize user
//			int rCode = sesMgr.setUserAuthorization(userId, sessionId, roleId);
//		}
//		
//		XPathExpression exprGroups = xpath.compile("//message/service/group");
//		result = exprGroups.evaluate(doc, XPathConstants.NODESET);
//		nodes = (NodeList)result;
//		
//		XPathExpression exprGroupId = xpath.compile("@groupID");
//		int groupId;
//		
//		for (int i = 0; i < nodes.getLength(); i++) {
//			node = nodes.item(i);
//			
//			groupId = 0; roleId = 0;
//			resStr = "";
//			
//			resStr = (String)exprGroupId.evaluate(node, XPathConstants.STRING);
//			try {
//				groupId = Integer.parseInt(resStr);
//			} catch (NumberFormatException e) {
//				//skip group on non int ID
//				continue; 
//			}
//			
//			resStr = "";
//			resStr = (String)exprRoleId.evaluate(node, XPathConstants.STRING);
//			try {
//				roleId = Integer.parseInt(resStr);
//			} catch (NumberFormatException e) {
//				//skip user on non int ID
//				continue; 
//			}
//			
//			//authorize user
//			sesMgr.setGroupAuthorization(groupId, sessionId, roleId); 
//		}
//		
//		/*
//		 * return session
//		 */
//		session = sesMgr.getSessionById(sessionId);
//		if (session == null) {
//			response.setStatus(400);
//			response.getWriter().write("could not retrieve created session");
//			return;
//		}
//		
//		String respStr = 
//			"<message action=\"new_authored_session_resp\">\n" 
//			+ "<result>success</result>\n"
//			+ sessionResponseXml(session)
//			+ "</message>";
//		response.setStatus(200);
//		response.getWriter().write(respStr);
//		logger.info("New_Authored_Session_Resp:" + respStr);
//	}
	
	
}
