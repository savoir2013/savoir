// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package sessionAdmin;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import OAuth.Validator;

import ca.gc.nrc.iit.savoir.sessionMgmt.SessionMgr;
import ca.gc.iit.nrc.savoir.domain.Group;
import ca.gc.iit.nrc.savoir.domain.Participant;
import ca.gc.iit.nrc.savoir.domain.PersonInfo;
import ca.gc.iit.nrc.savoir.domain.Session;
import ca.gc.iit.nrc.savoir.domain.User;

import static ca.gc.nrc.iit.savoir.utils.XmlUtils.*;

/**
 * Servlet implementation class SavoirSessionAdmin
 */
public class SavoirSessionAdmin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/** Action to store a new SAVOIR Session */
	public static final String STORE = 			"store_session";
	/** Action to get all currently joinable sessions */
	public static final String GET_JOINABLE = 	"get_joinable_session";
	/** Action to get all currently joinable sessions */
	public static final String GET_REMOVABLE = 	"get_removable_sessions";
	/** Action to get a previously-stored SAVOIR session */
	public static final String GET = 			"get_session";
	/** Action to remove a session from SAVOIR */
	public static final String REMOVE = 		"remove_session";
	/** Action to perform emergency stop on SAVOIR session */
	public static final String FLUSH =			"flush_session";
	
	private static final Logger logger = Logger.getLogger(SavoirSessionAdmin.class);
	static String[] CONFIG_FILES = new String[] { "classpath:cxf.xml" };

	static ApplicationContext ac = new ClassPathXmlApplicationContext(
			CONFIG_FILES);
    private SessionMgr sesMgr;
    private Validator validator;
    
    /** Integer ID of the role with just rights to run a session. Loaded from a 
     *  properties file. */
    private int runSessionRoleId;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SavoirSessionAdmin() {
        super();
        sesMgr = (SessionMgr) ac.getBean("sessionMgrClient");
        validator = (Validator) ac.getBean("userValidator");
        runSessionRoleId = Integer.parseInt(
        		ResourceBundle.getBundle("webbroker")
        			.getString("sessionMgr.runSessionRoleId"));
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
		
		String msg = request.getParameter("savoirmsg");
		logger.info("Received Session Admin MSG Req:"  + msg);
		String caller = validator.validateCaller(request);
		
		if (caller == null) {
			//caller not properly authenticated. Return unauthorized error.
			logger.error("Caller unauthorized");
			
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
			
			//get action
			String action = safeEvalString(xpath, "//message/@action", doc);
			
			/*
			 * special handling for STORE, GET_JOINABLE
			 */
			if (STORE.equalsIgnoreCase(action)) {
				//TODO store session
				//added by yyh more for demo purpose: for example message itself. 
				//The basic idea is session authoring and scenario authoring are separated. 
				//Scenario focus on more static stuff like rule
				//session focus on more dynamic stuff like session start/end time, APN connection reservation, participants,... etc
				//Store session will finish adding session and subsessions(for each edge service), try making network reservation
				//finally, return to client whether the session is created successfully or failure.
				handleStoreSession(doc, caller, response, xpath);
				return;
			} else if (GET_JOINABLE.equalsIgnoreCase(action)) {
				handleGetJoinableSession(doc, caller, response, xpath);
				return;
			} else if (GET_REMOVABLE.equalsIgnoreCase(action)) {
				handleGetRemovableSession(doc, caller, response, xpath);
				return;
			} else if (REMOVE.equalsIgnoreCase(action)) {
				handleRemoveSession(doc, caller, response, xpath);
				return;
			} else if (GET.equalsIgnoreCase(action)) {
				handleGetSession(doc, caller, response, xpath);
				return;
			} else if (FLUSH.equalsIgnoreCase(action)) {
				handleFlushSession(doc, caller, response, xpath);
				return;
			} else {
				response.setStatus(400);
				response.getWriter().write("Unsupported Action \"" + action + 
						"\"!");
				logger.error("Unsupported Action: " + action);
				return;
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			response.setStatus(400);
			logger.error("Exception thrown from commonDo!");
			//response.getWriter().write(ex.printStackTrace());
		}
	}
	
	/**
	 * Handles "{@value #STORE}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleStoreSession(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		Session masterAuthoredSession = null;
		
		try {
			String sessionName = "";
			String description = null;
			int scenarioId = 0;
			String startTimeStr = "";
			String endTimeStr = "";
			Date startTime = null;
			Date endTime = null;
			NodeList groups = null;
			NodeList users = null;
			
			sessionName = 
				evalString(xpath, "//message/session/sessionName", doc);
			description = 
				safeEvalString(xpath, "//message/session/description", doc);
			Integer scId = evalInt(xpath, "//message/session/scenarioId", doc);
			if (scId == null) {
				response.setStatus(400);
				response.getWriter().write("scenarioID not found");
				logger.error("Scenario ID not found!");
				return;
			} else {
				scenarioId = scId;
			}
			startTimeStr = 
				evalString(xpath, "//message/session/startTime", doc);
			endTimeStr = evalString(xpath, "//message/session/endTime", doc);
			
			long startTimeL;
			try {
				startTimeL = Long.parseLong(startTimeStr);
			} catch (NumberFormatException e) {
				startTimeL = 0;
			}
			startTime = (startTimeL == 0) ? null : new Date(startTimeL);
			
			long endTimeL;
			try {
				endTimeL = Long.parseLong(endTimeStr);
			} catch (NumberFormatException e) {
				endTimeL = 0;
			}
			endTime = (endTimeL == 0) ? null : new Date(endTimeL);
			
			//store new session (APN will be configured from scenario, if 
			// applicable)
			masterAuthoredSession = sesMgr.newSessionAuthored(sessionName, 
					description, scenarioId, startTime, endTime, caller);
			
			if (masterAuthoredSession == null) {
				//session failed to create properly
				String storeSessionResp = 
					generateStoreSessionResp(null, false, null, null);
				
				response.setStatus(400);
				response.getWriter().write(storeSessionResp);
				logger.error("Store session failed\n" + storeSessionResp);
				return;
			}
			
			int sessionId = masterAuthoredSession.getSessionID();
			
			List<String> unaddedGroups = new ArrayList<String>();
			
			//add authorizations
			groups = evalNodeList(xpath, "//message/session/groups/group", doc);
			if (groups != null && groups.getLength() > 0) {
				XPathExpression exprGroupId = xpath.compile("@groupId");
				
				for (int i = 0; i < groups.getLength(); i++) {
					Node groupNode = groups.item(i);
					Integer gId = evalInt(exprGroupId, groupNode);
					if (gId != null) {
						int authzCode = sesMgr.setGroupAuthorization(
								gId, sessionId, runSessionRoleId, caller);
						if (authzCode != /* success */ 0) {
							unaddedGroups.add(toXmlString(groupNode));
						}
					}
				}
			}
			
			List<String> unaddedUsers = new ArrayList<String>();
			
			users = evalNodeList(xpath, "//message/session/users/user", doc);
			if (users != null && users.getLength() > 0) {
				XPathExpression exprUserId = xpath.compile("@userId");
				
				for (int i = 0; i < users.getLength(); i++) {
					Node userNode = users.item(i);
					Integer uId = evalInt(exprUserId, userNode);
					if (uId != null) {
						int authzCode = sesMgr.setUserAuthorization(
								uId, sessionId, runSessionRoleId, caller);
						if (authzCode != /* success */ 0) {
							unaddedUsers.add(toXmlString(userNode));
						}
					}
				}
			}
			
			masterAuthoredSession = sesMgr.getSessionById(sessionId);
			
			if (masterAuthoredSession == null) {
				String storeSessionResp = generateStoreSessionResp(null, false, 
						unaddedUsers, unaddedGroups);
				
				response.setStatus(400);
				response.getWriter().write(storeSessionResp);
				logger.error("Store session failed\n" + storeSessionResp);
				return;
			} else {
				String storeSessionResp = generateStoreSessionResp(
						masterAuthoredSession, true, unaddedUsers, 
						unaddedGroups);
				
				response.setStatus(200);
				response.getWriter().write(storeSessionResp);
				logger.info("Store session Resp: " + storeSessionResp);
				return;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			String storeSessionResp = generateStoreSessionResp(null, false, 
					null, null);
			
			response.setStatus(400);
			response.getWriter().write(storeSessionResp);
			logger.error("Store session failed\n" + storeSessionResp);
			return;
		}
	}
	
	/**
	 * Formats response message for "{@value #STORE}" 
	 * actions.
	 * 
	 * @param session		The stored session
	 * @param success		Was the action successful?
	 * @param unaddedGroups	Groups that were requested to be authorized, but 
	 * 						were not (pass as the XML string value for the 
	 * 						node passed to authorize them)
	 * @param unaddedUsers	Users that were requested to be authorized, but 
	 * 						were not (pass as the XML string value for the 
	 * 						node passed to authorize them)
	 * 
	 * @return The XML response message
	 */
	private String generateStoreSessionResp(Session session, boolean success, 
			List<String> unaddedGroups, List<String> unaddedUsers) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("<message ");
		addAttr(sb, "action", "store_session_resp", REQUIRED);
		sb.append(">\n");
		
		if (success) {
			boolean existUnadded = 
				(unaddedGroups != null && !unaddedGroups.isEmpty()) 
						|| (unaddedUsers != null && !unaddedUsers.isEmpty());
			String result = existUnadded ? "partial" : "success" ;
			addTextNode(sb, "result", result, REQUIRED);
			sb.append("\n");
			if (existUnadded) {
				sb.append("<unadded>\n");
				if (unaddedGroups != null) for (String group : unaddedGroups) {
					sb.append(group).append('\n');
				}
				if (unaddedUsers != null) for (String user : unaddedUsers) {
					sb.append(user).append('\n');
				}
				sb.append("</unadded>\n");
			}
			sb.append(sessionResponseXml(session));
		} else {
			addTextNode(sb, "result", "failure", REQUIRED);
		}
		sb.append("</message>");
		
		return sb.toString();
	}
	
	/**
	 * Handles "{@value #GET_JOINABLE}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleGetJoinableSession(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
	
		//get session ID
		Integer parsedSid = evalInt(xpath, "//message/@sessionID", doc);
		if (parsedSid == null) {
			logger.error("No session ID found for getJoinableSession");
			response.setStatus(400);
			response.getWriter().write("No session ID found");
			return;
		}
		int sessionID = parsedSid;
		
		//get joinable sessions
		List<Session> joinableSessions = sesMgr.getCurrentSessions(caller);

		//generate and write response
		String joinableSessionResp = 
			generateGetJoinableSessionResp(sessionID, joinableSessions);
		response.setStatus(200);
		response.getWriter().write(joinableSessionResp);
		logger.info("Get_Joinable_Session_Resp:" + joinableSessionResp);
	}
	
	/**
	 * Formats response message for "{@value #GET_JOINABLE}" actions.
	 * 
	 * @param sessionId		The session ID the caller passed
	 * @param sessions		The list of sessions to return 
	 * 
	 * @return The XML response message
	 */
	private String generateGetJoinableSessionResp(int sessionId, 
			List<Session> sessions){
		StringBuilder sb = new StringBuilder();
		sb.append("<message action=\"get_joinable_session_resp\" " 
			+ "sessionID=\"").append(sessionId).append("\">\n"
			+ "<result>success</result>\n");
		if (sessions != null) for (Session session : sessions) {
			sb.append(sessionResponseXml(session));
		}
		sb.append("</message>");
		return sb.toString();
	}
	
	/**
	 * Handles "{@value #GET_REMOVABLE}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleGetRemovableSession(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
	
		//get removable sessions
		List<Session> removableSessions = sesMgr.getRemovableSessions(caller);

		//generate and write response
		String removableSessionResp = 
			generateGetRemovableSessionResp(removableSessions);
		response.setStatus(200);
		response.getWriter().write(removableSessionResp);
		logger.info("Get_Removable_Sessions_Resp:" + removableSessionResp);
		
	}
	
	/**
	 * Formats response message for "{@value #GET_REMOVABLE}" actions.
	 * 
	 * @param sessions		The list of sessions to return 
	 * 
	 * @return The XML response message
	 */
	private String generateGetRemovableSessionResp(List<Session> sessions) {
		StringBuilder sb = new StringBuilder();
		sb.append("<message action=\"get_removable_session_resp\" >\n"
			+ "<result>success</result>\n");
		if (sessions != null) for (Session session : sessions) {
			sb.append(sessionResponseXml(session));
		}
		sb.append("</message>");
		return sb.toString();
	}
	
	/**
	 * Handles "{@value #REMOVE}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleRemoveSession(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		LinkedHashMap<Integer, Integer> removeReturns = 
			new LinkedHashMap<Integer, Integer>();
		
		NodeList sesNodes = evalNodeList(xpath, "//message/session", doc);
		if (sesNodes != null) for (int i = 0; i < sesNodes.getLength(); i++) {
			Integer sessionId = evalInt(xpath, "@id", sesNodes.item(i));
			if (sessionId == null) continue;
			
			removeReturns.put(
					sessionId, sesMgr.removeSession(sessionId, caller));
		}
		
		String removeSessionResp = generateRemoveSessionResp(removeReturns);
		response.setStatus(200);
		response.getWriter().write(removeSessionResp);
		logger.info("Remove_Session_Resp:" + removeSessionResp);
	}
	
	/**
	 * Formats response message for "{@value #REMOVE}" actions.
	 * 
	 * @param retCodes			The return codes from the remove session calls, 
	 * 							indexed by session ID
	 * 
	 * @return The XML response message
	 */
	private static String generateRemoveSessionResp(
			LinkedHashMap<Integer, Integer> retCodes) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<message ");
		addAttr(sb, "action", "remove_session_resp", REQUIRED);
		sb.append(">\n");
		
		for (Map.Entry<Integer, Integer> e : retCodes.entrySet()) {
			sb.append("<result ");
			addAttr(sb, "resultId", e.getKey(), REQUIRED);
			
			int retCode = e.getValue();
			if (retCode == 0 /* success */) {
				sb.append(">success</result>\n");
			} else {
				String reason;
				switch (retCode) {
				case -1:	//no such session
					reason = "no_such_session";
					break;
				case -2:	//invalid parameters
					reason = "invalid_parameters";
					break;
				case -20:	//invalid caller
					reason = "invalid_caller";
					break;
				case -21:	//unauthorized
					reason = "unauthorized";
					break;
				default:
					reason = "unknown";
					break;
				}
				
				addAttr(sb, "reason", reason, OPTIONAL);
				sb.append(">failure</result>\n");
			}
		}
		
		sb.append("</message>");
		
		return sb.toString();		
	}
	
	/**
	 * Handles "{@value #GET}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleGetSession(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		//get session ID
		Integer sessionId = evalInt(xpath, "//message/@sessionID", doc);
		if (sessionId == null) {
			response.setStatus(400);
			response.getWriter().write("No session ID!");
			logger.error("No session ID!");
			return;
		}
		
		Session s = sesMgr.getSessionById(sessionId);
		
		String getSessionResp = generateGetSessionResp(s);
		response.setStatus(200);
		response.getWriter().write(getSessionResp);
		logger.info("Get_Session_Resp:" + getSessionResp);
	}
	
	/**
	 * Formats response message for "{@value #GET}" actions.
	 * 
	 * @param session		The session retrieved
	 * 
	 * @return The XML response message
	 */
	private String generateGetSessionResp(Session session) {
		StringBuilder sb = new StringBuilder();
		sb.append("<message action=\"get_session_resp\" >\n");
		if (session == null) {
			addTextNode(sb, "result", "failure", REQUIRED);
		} else {
			addTextNode(sb, "result", "success", REQUIRED);
			sb.append(sessionResponseXml(session));
		}
		sb.append("</message>");
		return sb.toString();
	}
	
	/**
	 * Handles "{@value #FLUSH}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleFlushSession(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		//get session ID
		Integer sessionId = evalInt(xpath, "//message/@sessionID", doc);
		if (sessionId == null) {
			response.setStatus(400);
			response.getWriter().write("No session ID!");
			logger.error("No session ID!");
			return;
		}
		
		sesMgr.flushSession(sessionId, caller);
		
		String flushSessionResp = generateFlushSessionResp();
		response.setStatus(200);
		response.getWriter().write(flushSessionResp);
		logger.info("Get_Session_Resp:" + flushSessionResp);
	}
	
	/**
	 * Formats response message for "{@value #FLUSH}" actions.
	 * 
	 * @return The XML response message
	 */
	private String generateFlushSessionResp() {
		StringBuilder sb = new StringBuilder();
		sb.append("<message action=\"flush_session_resp\" >\n");
		addTextNode(sb, "result", "success", REQUIRED);
		sb.append("</message>");
		return sb.toString();
	}
	
	/**
	 * Formats a session object as XML
	 * 
	 * @param session		The session object to format
	 * 
	 * @return an XML representation of the session object
	 */
	private String sessionResponseXml(Session session) {
		StringBuilder sb = new StringBuilder();
		
		//format parameters
		String authorName = "";
		User author = session.getRequestedBy();
		if (author != null) {
			PersonInfo p = author.getPerson().getPersonInfo();
			authorName = p.getFName() + " " + p.getLName();
		}
		
		String startTime = "", endTime = "", submissionTime = "";
		Calendar startCal = session.getRequestedStartTime();
		Calendar endCal = session.getRequestedEndTime();
		Calendar subCal = session.getSubmissionDate();
		if (startCal != null) {
			startTime = Long.toString(startCal.getTimeInMillis());
		}
		if (endCal != null) {
			endTime = Long.toString(endCal.getTimeInMillis());
		}
		if (subCal != null) {
			submissionTime = Long.toString(subCal.getTimeInMillis());
		}
		
		String scenarioId = "";
		Integer scId = session.getScenarioId();
		if (scId != null) {
			scenarioId = scId.toString();
		}
		
		String sessionId = Integer.toString(session.getSessionID());
		
		//add session parameters
		sb.append("\n<session ");
		addAttr(sb, "name", session.getName(), REQUIRED);
		addAttr(sb, "sessionID", sessionId, REQUIRED);
		addAttr(sb, "description", session.getDescription(), REQUIRED);
		addAttr(sb, "authorName", authorName, REQUIRED);
		addAttr(sb, "submissionTime", submissionTime, REQUIRED);
		addAttr(sb, "startTime", startTime, REQUIRED);
		addAttr(sb, "endTime", endTime, REQUIRED);
		addAttr(sb, "scenarioID", scenarioId, REQUIRED);
		sb.append(">\n");
		
		//add authorized groups on session
		sb.append("<groups>\n");
		List<Group> groups = session.getAuthorizedGroups();
		if (groups != null) for (Group group : groups) {
			if (group == null) continue;
			sb.append("<group ");
			addAttr(sb, "groupId", group.getGroupId(), REQUIRED);
			addAttr(sb, "name", group.getGroupName(), REQUIRED);
			addAttr(sb, "description", group.getDescription(), REQUIRED);
			sb.append("/>");
		}
		sb.append("</groups>\n");
		
		//add authorized users on session
		sb.append("<users>\n");
		List<Participant> users = session.getAuthorizedUsers();
		if (users != null) for (Participant user : users) {
			if (user == null) continue;
			sb.append("<user ");
			addAttr(sb, "userId", user.getUser().getUserID(), REQUIRED);
			addAttr(sb, "username", user.getUser().getDName(), REQUIRED);
			addAttr(sb, "status", user.getStatus(), REQUIRED);
			sb.append("/>");
		}
		sb.append("</users>\n");
		
		sb.append("</session>");
		
		return sb.toString();
	}

	
//	private List<Connection> findOutAllConnections(Document doc, XPath xpath) {
//		String apnSrcSite = "";
//		String apnDestSite = "";
//		String maxBwStr = "";
//		String minBwStr = "";
//		
//		List<Connection> connections =  new ArrayList<Connection>();
//		try {
//			NodeList resConnNodes = 
//				evalNodeList(xpath, "//message/session/apn/connection", doc);
//			
//			for (int i = 0; i < resConnNodes.getLength(); i++) {
//  
//				NodeList resConnProps = resConnNodes.item(i).getChildNodes();
////				logger.info("resConnProps.getLength() = " + resConnProps.getLength());
////				logger.info("resConnProps.item(0).getAttributes().getNamedItem(\"name\").getNodeValue()" + resConnProps.item(0).getAttributes()
////						.getNamedItem("name").getNodeValue());
//				int numberOfChild = resConnProps.getLength();
//				if ( numberOfChild == 4) {
//					apnSrcSite = resConnProps.item(0).getAttributes()
//							.getNamedItem("name").getNodeValue();
//					apnDestSite = resConnProps.item(1).getAttributes()
//							.getNamedItem("name").getNodeValue();
//					maxBwStr = resConnProps.item(2).getTextContent();
//					minBwStr = resConnProps.item(3).getTextContent();
//					
////					logger.info("apnSrcSite = " + apnSrcSite);
////					logger.info("apnDestSite = " + apnDestSite);
////					logger.info("maxBwStr = " + maxBwStr);
////					logger.info("minBwStr = " + minBwStr);
//
//					Resource sourceLP = this.resMgr
//							.getResourceByTypeAndParameterValue("LP_END_POINT",
//									"SITE_LOCATION", apnSrcSite);
////					logger.info("The source LP end point of site "
////									+ apnSrcSite
////									+ " parameter LP_END_POINT_SWITCH_ID = "
////									+ sourceLP
////											.getParameterValue("LP_END_POINT_SWITCH_ID"));
//					Resource destLP = this.resMgr
//							.getResourceByTypeAndParameterValue("LP_END_POINT",
//									"SITE_LOCATION", apnDestSite);
////					logger.info("The Dest LP end point of site "
////									+ apnDestSite
////									+ " parameter LP_END_POINT_SWITCH_ID = "
////									+ destLP
////											.getParameterValue("LP_END_POINT_SWITCH_ID"));
//					
//					EndPoint sourceEndPoint = new EndPoint();
//					EndPoint targetEndPoint = new EndPoint();
//					Connection connection = new Connection();
//					sourceEndPoint.setNetworkEndPoint(sourceLP);
//					sourceEndPoint.setEndPointType(EndPoint.CLIENT);
//					targetEndPoint.setNetworkEndPoint(destLP);
//					targetEndPoint.setEndPointType(EndPoint.SERVER);
//					connection.setLpNeeded(true);
//					connection.setBwRequirement(Double.valueOf(maxBwStr));
//					connection.setMinBwRequirement(Double.valueOf(minBwStr));
//					connection.setSourceEndPoint(sourceEndPoint);
//					connection.setTargetEndPoint(targetEndPoint);
//					connection.setDirectionality("TWO-WAY");
//					logger.info("The source LP end point of site "	+ apnSrcSite);
//					logger.info("The Dest LP end point of site " + apnDestSite);
//					logger.info("The max bandwidth of connection " + maxBwStr);
//					logger.info("The min bandwidth of connection " + minBwStr);
//					connections.add(connection);
//				}
//			}
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		return connections;
//	}
}
