// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package resourceAdmin;

import java.awt.Image;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import ca.gc.iit.nrc.savoir.domain.CredentialParameter;
import ca.gc.iit.nrc.savoir.domain.types.ParameterType;
import ca.gc.iit.nrc.savoir.domain.types.ResourceType;
import ca.gc.nrc.iit.savoir.resourceMgmt.ResourceMgr;
import ca.gc.nrc.iit.savoir.resourceMgmt.TicketHandle;
import ca.gc.nrc.iit.savoir.utils.OverdueListener;
import ca.gc.nrc.iit.savoir.utils.TimedCache;

import OAuth.Validator;

import static ca.gc.nrc.iit.savoir.utils.XmlUtils.*;

public class SavoirResourceAdmin extends HttpServlet {
	private static final long serialVersionUID = -878090936295790412L;

	/** Action to get list of resource types */
	public static final String GET_TYPES = 		"get_resource_types";
	/** Action to add a new resource type */
	public static final String ADD_TYPE = 		"add_resource_type";
	/** Action to get tickets for pending registrations */
	public static final String GET_PENDING = 	"get_pending_registrations";
	/** Action to submit a new device registration */
	public static final String NEW_REGISTRATION = 
												"new_device_registration";
	/** Action to complete a device registration */
	public static final String COMPLETE_REGISTRATION = 
												"complete_registration";
	/** Action to authorize users or groups on a device */
	public static final String SET_AUTHZ = 		"set_authorizations";
	/** Action to deauthorize users or groups on a device */
	public static final String UNSET_AUTHZ = 	"unset_authorizations";
	
	
	private static final Logger logger = 
		Logger.getLogger(SavoirResourceAdmin.class);
	static String[] CONFIG_FILES = new String[] { "classpath:cxf.xml" };

	static ApplicationContext ac = new ClassPathXmlApplicationContext(
			CONFIG_FILES);
	
	private ResourceMgr resMgr;
	private Validator validator;
	
	/*
	 * set up image cache
	 */
	private static TimedCache<Long, Image> iconCache = 
		new TimedCache<Long, Image>(
			new OverdueListener<Map.Entry<Long, Image>>(){
				public void notifyOverdue(Map.Entry<Long, Image> collected) {
					logger.info("Image with ID " + collected.getKey() + 
							" failed to be used.");
				}
			},			/* log when images get dumped from cache, 
							likely an error */
			60 * 1000 	/* keep images for no more than 60 seconds */,
			30 * 1000 	/* check for expired images every 30 seconds */,
			"IconCache"	/* name of collection thread */
		);
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public SavoirResourceAdmin() {
        super();
        resMgr = (ResourceMgr) ac.getBean("resourceMgrClient");
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
		
		//ensure this is actually a file upload action
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		
		if (!isMultipart) {
			//not a registration ticket upload
			commonDo(request, response);
			return;
		}
		
		//build handler for uploaded data
		ServletFileUpload upload = new ServletFileUpload();
		try {
			FileItemIterator iter = upload.getItemIterator(request);
			
			while (iter.hasNext()) {
				FileItemStream item = iter.next();
				
				if (item.isFormField()) {
					if ("savoirmsg".equals(item.getFieldName())) {
						//savoir message form field (non file-upload command)
						String caller = validator.validateCaller(request);
						String msg = Streams.asString(item.openStream());
						logger.info("Received MSG Req:"  + msg);
						handleMsg(msg, caller, response);
						return;
					} else {
						//skip basic form fields
						continue;
					}
					
				} else {
					//this is an uploaded file
					//TODO handle
				}
			}
		} catch (FileUploadException e) {
			logger.error("error reading uploaded file", e);
			
			response.setStatus(400);
			response.getWriter().write("error reading uploaded file");
			return;
		}
	}
	
	/**
	 * Common handler for non-scenario file upload requests
	 * 
	 * @param request			The HTTP request
	 * @param response			The HTTP response
	 * 
	 * @throws IOException 
	 * @throws ServletException 
	 */
	private void commonDo(HttpServletRequest request, 
			HttpServletResponse response) throws IOException, ServletException {
		String msg = request.getParameter("savoirmsg");
		logger.info("Received MSG Req:"  + msg);
		String caller = validator.validateCaller(request);
		handleMsg(msg, caller, response);
	}
	
	/**
	 * Handles a SAVOIR message
	 * 
	 * @param msg					The message
	 * @param caller				The caller, (if null, will respond for 
	 * 								unauthorized caller)
	 * @param response				The HTTP response
	 * 
	 * @throws IOException 
	 * @throws ServletException 
	 */
	private void handleMsg(String msg, String caller,
			HttpServletResponse response) throws ServletException, IOException {
		
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
			
			//get action
			String action = safeEvalString(xpath, "//message/@action", doc);
			
			//handle action
			if (GET_TYPES.equalsIgnoreCase(action)) {
				handleGetTypes(doc, caller, response, xpath);
			} else if (ADD_TYPE.equalsIgnoreCase(action)) {
				handleAddType(doc, caller, response, xpath);
			} else if (GET_PENDING.equalsIgnoreCase(action)) {
				handleGetPending(doc, caller, response, xpath);
			} else if (NEW_REGISTRATION.equalsIgnoreCase(action)) {
				handleNewRegistration(doc, caller, response, xpath);
			} else if (COMPLETE_REGISTRATION.equalsIgnoreCase(action)) {
				handleCompleteRegistration(doc, caller, response, xpath);
			} else if (SET_AUTHZ.equalsIgnoreCase(action)) {
				handleSetAuthorizations(doc, caller, response, xpath);
			} else if (UNSET_AUTHZ.equalsIgnoreCase(action)) {
				handleUnsetAuthorizations(doc, caller, response, xpath);
			} else {
				response.setStatus(400);
				response.getWriter().write("Unrecognized message action \"" + 
						action + "\"!");
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			response.setStatus(400);
			//response.getWriter().write(ex.printStackTrace());
		}
	}
	
	/**
	 * Handles "{@value #GET_TYPES}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleGetTypes(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		Integer sId = evalInt(xpath, "//message/@sessionID", doc);
		
		List<ResourceType> types = resMgr.getResourceTypes();
		
		String getTypesResp = generateGetTypesResp(types, sId);
		response.setStatus(200);
		response.getWriter().write(getTypesResp);
		logger.info("Get_Resource_Types_Resp:" + getTypesResp);
	}
	
	/**
	 * Formats response message for "{@value #GET_TYPES}" actions.
	 * 
	 * @param types			The list of sessions to return 
	 * @param sessionId		The session ID the caller passed
	 * 
	 * @return The XML response message
	 */
	private String generateGetTypesResp(List<ResourceType> types, Integer sId) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("<message action=\"get_resource_types_resp\" ");
		addAttr(sb, "sessionID", sId, OPTIONAL);
		sb.append(">\n<result>success</result>\n");
		if (types != null) for (ResourceType type : types) {
			if (!"DEVICE".equals(type.getResourceClass())) continue;
			sb.append(resourceTypeResponseXml(type));
		}
		sb.append("</message>");
		
		return sb.toString();
	}
	
	/**
	 * Formats a resource type object as XML
	 * 
	 * @param type		The resource type object to format
	 * 
	 * @return an XML representation of the resource type object
	 */
	private String resourceTypeResponseXml(ResourceType type) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("<resourceType ");
		addAttr(sb, "id", type.getId(), REQUIRED);
		addAttr(sb, "name", type.getName(), REQUIRED);
		addAttr(sb, "description", type.getDescription(), REQUIRED);
		addAttr(sb, "resourceClass", type.getResourceClass(), OPTIONAL);
		sb.append("/>\n");
		
		return sb.toString();
	}
	
	/**
	 * Handles "{@value #ADD_TYPE}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleAddType(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		String typeId;
		String typeName;
		String description;
		String typeClass;
		
		Integer sId;
		
		sId = evalInt(xpath, "//message/@sessionID", doc);
		
		//parse parameters
		typeId = 
			safeEvalString(xpath, "//message/service/resourceType/@id", doc);
		if (typeId == null || typeId.isEmpty()) {
			response.setStatus(400);
			String failResp = generateAddTypeResp(-2 /*invalid parameters*/, 
					null, null, null, null, sId);
			response.getWriter().write(failResp);
			logger.info(failResp);
			return;
		}
		
		typeName = 
			safeEvalString(xpath, "//message/service/resourceType/@name", doc);
		if (typeName == null || typeName.isEmpty()) {
			response.setStatus(400);
			String failResp = generateAddTypeResp(-2 /*invalid parameters*/, 
					typeId, null, null, null, sId);
			response.getWriter().write(failResp);
			logger.info(failResp);
			return;
		}
		
		description = evalString(xpath, 
				"//message/service/resourceType/@description", doc);
		
		typeClass = evalString(xpath, 
				"//message/service/resourceType/@resourceClass", doc);
		
		//add type
		int retVal = resMgr.addResourceType(
				typeId, typeName, description, typeClass, caller);
		
		//format and return
		String addTypeResp = generateAddTypeResp(retVal, typeId, typeName, 
				description, typeClass, sId);
		response.setStatus(retVal == 0 ? 200 : 400);
		response.getWriter().write(addTypeResp);
		logger.info("Add_Resource_Type_Resp:" + addTypeResp);
	}
	
	/**
	 * Formats response message for "{@value #ADD_TYPE}" actions.
	 * 
	 * @param retVal			The return code from the add type call'
	 * @param typeId			The ID of the new type
	 * @param typeName			The name of the new type
	 * @param description		The type's description
	 * @param typeClass			The resource class of the new type
	 * @param sId				The session ID passed by the user
	 * 
	 * @return The XML response message
	 */
	private String generateAddTypeResp(int retVal, String typeId, 
			String typeName, String description, String typeClass, 
			Integer sId) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<message action=\"add_resource_type_resp\" ");
		if (sId != null) addAttr(sb, "sessionID", sId.toString(), OPTIONAL);
		sb.append(">\n");
		if (retVal == 0) {
			sb.append("<result>success</result>\n");
			sb.append(resourceTypeResponseXml(new ResourceType(
					typeId, typeName, description, typeClass)));
		} else {
			String reason;
			switch (retVal) {
			case -1:	//type already exists
				reason = "already_exists";
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
			
			sb.append("<result ");
			addAttr(sb, "reason", reason, OPTIONAL);
			sb.append(">failure</result>\n");
		}
		sb.append("</message>");
		
		return sb.toString();
	}
	
	/**
	 * Handles "{@value #GET_PENDING}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleGetPending(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		//get registration pending ticket handles
		List<TicketHandle> handles = 
			resMgr.getTicketsByResourceType("REGISTRATION_PENDING");
		
		String getPendingResp = generateGetPendingResp(handles);
		response.setStatus(200);
		response.getWriter().write(getPendingResp);
		logger.info("Get_Resource_Types_Resp:" + getPendingResp);
	}
	
	/**
	 * Formats response message for "{@value #GET_PENDING}" actions.
	 * 
	 * @param hanldes			The list of ticket handles to return 
	 * 
	 * @return The XML response message
	 */
	private String generateGetPendingResp(List<TicketHandle> handles) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("<message action=\"get_pending_registrations_resp\">\n");
		addTextNode(sb, "result", "success", REQUIRED);
		if (handles != null) for (TicketHandle handle : handles) {
			sb.append(ticketHandleResponseXml(handle));
		}
		sb.append("\n</message>");
		
		return sb.toString();
	}
	
	/**
	 * Formats a ticket handle as XML
	 * 
	 * @param handle		The ticket handle to format
	 * 
	 * @return an XML representation of the ticket handle
	 */
	private String ticketHandleResponseXml(TicketHandle handle) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("\n<resource ");
		addAttr(sb, "id", handle.getId(), REQUIRED);
		addAttr(sb, "name", handle.getName(), REQUIRED);
		addAttr(sb, "ticketUri", handle.getUri(), REQUIRED);
		sb.append("/>");
		
		return sb.toString();
	}
	
	/**
	 * Handles "{@value #NEW_REGISTRATION}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleNewRegistration(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		try {
			//get <newService> child, convert to full document string
			Node node = evalNode(xpath, "//message/newService", doc);
			if (node != null) {
				Document ticketDoc;
				ticketDoc = toDocument(node);
				String xml = toXmlString(ticketDoc);
				
				logger.info("Registration ticket:\n" + xml);
				
				int retCode = resMgr.newResourceRegistration(xml);
				
				String newRegistrationResp = 
					generateNewRegistrationResp(retCode);
				response.setStatus(200);
				response.getWriter().write(newRegistrationResp);
				logger.info("New_Device_Registration_Resp:" + 
						newRegistrationResp);
				return;
				
			} else {
				response.setStatus(400);
				response.getWriter().write(
						"No registration ticket found on " +
						"\"new_device_registration\"");
				logger.info("No registration ticket found on " +
						"\"new_device_registration\"");
				return;
			}
		} catch (ParserConfigurationException e) {
			response.setStatus(400);
			response.getWriter().write("Server Error.");
			logger.info("Error on new registration ticket", e);
			return;
		}
	}
	
	private String generateNewRegistrationResp(int retCode) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<message ");
		addAttr(sb, "action", "new_device_registration_resp", REQUIRED);
		sb.append(">\n");
		
		if (retCode >= 0) {
			//success
			sb.append("<result ");
			addAttr(sb, "resultId", retCode, REQUIRED);
			sb.append(">success</result>");
		} else {
			//error state
			String reason;
			switch (retCode) {
			case -2:	//invalid parameters
				reason = "malformed_ticket";
				break;
			default:
				reason = "unknown";
				break;
			}
			
			sb.append("<result ");
			addAttr(sb, "reason", reason, OPTIONAL);
			sb.append(">failure</result>\n");
		}
		
		sb.append("</message>");
		
		return sb.toString();
	}
	
	/**
	 * Handles "{@value #COMPLETE_REGISTRATION}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleCompleteRegistration(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		try {
			//get <newService> child, convert to full document string
			Node node = evalNode(xpath, "//message/newService", doc);
			if (node != null) {
				Document ticketDoc;
				ticketDoc = toDocument(node);
				String xml = toXmlString(ticketDoc);
				
				logger.info("Registration ticket:\n" + xml);
				
				int retCode = resMgr.completeResourceRegistration(xml);
				
				String completeRegistrationResp = 
					generateCompleteRegistrationResp(retCode);
				response.setStatus(200);
				response.getWriter().write(completeRegistrationResp);
				logger.info("Complete_Device_Registration_Resp:" + 
						completeRegistrationResp);
				return;
				
			} else {
				response.setStatus(400);
				response.getWriter().write(
						"No registration ticket found on " +
						"\"complete_device_registration\"");
				logger.info("No registration ticket found on " +
						"\"complete_device_registration\"");
				return;
			}
		} catch (ParserConfigurationException e) {
			response.setStatus(400);
			response.getWriter().write("Server Error.");
			logger.info("Error on completed registration ticket", e);
			return;
		}
	}
	
	private String generateCompleteRegistrationResp(int retCode) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<message ");
		addAttr(sb, "action", "complete_device_registration_resp", REQUIRED);
		sb.append(">\n");
		
		if (retCode == 0) {
			//success
			addTextNode(sb, "result", "success", REQUIRED);
		} else {
			//error state
			String reason;
			switch (retCode) {
			case -1:	//no such resource
				reason = "no_such_resource";
				break;
			case -2:	//invalid parameters
				reason = "malformed_ticket";
				break;
			default:
				reason = "unknown";
				break;
			}
			
			sb.append("<result ");
			addAttr(sb, "reason", reason, OPTIONAL);
			sb.append(">failure</result>\n");
		}
		
		sb.append("</message>");
		
		return sb.toString();
	}
	
	/**
	 * Handles "{@value #SET_AUTHZ}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleSetAuthorizations(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		LinkedHashMap<Integer, Integer> userCodes;
		LinkedHashMap<Integer, Integer> groupCodes;
		
		Integer resourceId = evalInt(xpath, "//message/resource/@id", doc);
		if (resourceId == null) {
			response.setStatus(400);
			response.getWriter().write("No resource ID");
			logger.info("set authorization on resource failed, no resource ID");
			return;
		}
		
		groupCodes = new LinkedHashMap<Integer, Integer>();
		NodeList groupNodes = 
			evalNodeList(xpath, "//message/resource/groups/group", doc);
		if (groupNodes != null) for (int i = 0; i < groupNodes.getLength(); i++) {
			Node groupNode = groupNodes.item(i);
			
			//set user ID
			Integer groupId = evalInt(xpath, "@id", groupNode);
			if (groupId == null) continue;	//no group ID, invalid XML
			
			//look for a credential assigned
			NodeList credNodes = 
				evalNodeList(xpath, "credential/parameter", groupNode);
			List<CredentialParameter> creds = getCredential(credNodes, xpath);
			
			int retCode = resMgr.setGroupAuthorization(
					groupId, resourceId, creds, caller);
			
			groupCodes.put(groupId, retCode);
		}
		
		userCodes = new LinkedHashMap<Integer, Integer>();
		NodeList userNodes = 
			evalNodeList(xpath, "//message/resource/users/user", doc);
		if (userNodes != null) for (int i = 0; i < userNodes.getLength(); i++) {
			Node userNode = userNodes.item(i);
			
			//set user ID
			Integer userId = evalInt(xpath, "@id", userNode);
			if (userId == null) continue;	//no user ID, invalid XML
			
			//look for a credential assigned
			NodeList credNodes = 
				evalNodeList(xpath, "credential/parameter", userNode);
			List<CredentialParameter> creds = getCredential(credNodes, xpath);
			
			int retCode = resMgr.setUserAuthorization(
					userId, resourceId, creds, caller);
			
			userCodes.put(userId, retCode);
		}
		
		String setAuthorizationsResp = 
			generateSetAuthorizationsResp(groupCodes, userCodes);
		response.setStatus(200);
		response.getWriter().write(setAuthorizationsResp);
		logger.info("Set_Authorizations_Resp: " + setAuthorizationsResp);
	}
	
	/**
	 * Formats response message for "{@value #SET_AUTHZ}" actions.
	 * 
	 * @param groupCodes		The return codes from the set group 
	 * 							authorization calls, indexed by group ID
	 * @param userCodes			The return codes from the set user 
	 * 							authorization calls, indexed by user ID
	 * 
	 * @return The XML response message
	 */
	private static String generateSetAuthorizationsResp(
			LinkedHashMap<Integer, Integer> groupCodes, 
			LinkedHashMap<Integer, Integer> userCodes) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<message ");
		addAttr(sb, "action", "set_authorizations_resp", REQUIRED);
		sb.append(">\n");
		
		for (Map.Entry<Integer, Integer> e : groupCodes.entrySet()) {
			sb.append("<result ");
			addAttr(sb, "groupId", e.getKey(), REQUIRED);
			
			int retCode = e.getValue();
			if (retCode == 0 /* success */) {
				sb.append(">success</result>\n");
			} else {
				String reason;
				switch (retCode) {
				case -1:	//no such group
					reason = "no_such_group";
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
		
		for (Map.Entry<Integer, Integer> e : userCodes.entrySet()) {
			sb.append("<result ");
			addAttr(sb, "userId", e.getKey(), REQUIRED);
			
			int retCode = e.getValue();
			if (retCode == 0 /* success */) {
				sb.append(">success</result>\n");
			} else {
				String reason;
				switch (retCode) {
				case -1:	//no such group
					reason = "no_such_user";
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
	 * Parses a credential from a list of parameter nodes
	 * 
	 * @param credNodes the list of parameter nodes
	 * @param xpath		The XPath evaluator to use
	 * 
	 * @return null for list null or empty, a list of credential parameters 
	 * 			parsed from the list otherwise
	 */
	private List<CredentialParameter> getCredential(NodeList credNodes, 
			XPath xpath) {
		
		if (credNodes == null || credNodes.getLength() == 0) return null;
		
		List<CredentialParameter> creds = new ArrayList<CredentialParameter>();
		
		for (int i = 0; i < credNodes.getLength(); i++) {
			Node credNode = credNodes.item(i);
			
			String id = safeEvalString(xpath, "@id", credNode);
			String value = safeEvalString(xpath, "@value", credNode);
			
			if (id == null || id.isEmpty() || value == null) continue;
			
			CredentialParameter p = new CredentialParameter();
			ParameterType pt = new ParameterType();
			pt.setId(id);
			p.setParameter(pt);
			p.setValue(value);
			
			creds.add(p);
		}
		
		if (creds.isEmpty()) return null;
		else return creds;
	}
	
	/**
	 * Handles "{@value #UNSET_AUTHZ}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleUnsetAuthorizations(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		LinkedHashMap<Integer, Integer> userCodes;
		LinkedHashMap<Integer, Integer> groupCodes;
		
		Integer resourceId = evalInt(xpath, "//message/resource/@id", doc);
		if (resourceId == null) {
			response.setStatus(400);
			response.getWriter().write("No resource ID");
			logger.info("unset authorization on resource failed, no resource ID");
			return;
		}
		
		groupCodes = new LinkedHashMap<Integer, Integer>();
		NodeList groupNodes = 
			evalNodeList(xpath, "//message/resource/groups/group", doc);
		if (groupNodes != null) for (int i = 0; i < groupNodes.getLength(); i++) {
			Node groupNode = groupNodes.item(i);
			
			//set user ID
			Integer groupId = evalInt(xpath, "@id", groupNode);
			if (groupId == null) continue;	//no group ID, invalid XML
			
			int retCode = resMgr.unsetGroupAuthorization(
					groupId, resourceId, caller);
			
			groupCodes.put(groupId, retCode);
		}
		
		userCodes = new LinkedHashMap<Integer, Integer>();
		NodeList userNodes = 
			evalNodeList(xpath, "//message/resource/users/user", doc);
		if (userNodes != null) for (int i = 0; i < userNodes.getLength(); i++) {
			Node userNode = userNodes.item(i);
			
			//set user ID
			Integer userId = evalInt(xpath, "@id", userNode);
			if (userId == null) continue;	//no user ID, invalid XML
			
			int retCode = resMgr.unsetUserAuthorization(
					userId, resourceId, caller);
			
			userCodes.put(userId, retCode);
		}
		
		String unsetAuthorizationsResp = 
			generateUnsetAuthorizationsResp(groupCodes, userCodes);
		response.setStatus(200);
		response.getWriter().write(unsetAuthorizationsResp);
		logger.info("Unset_Authorizations_Resp: " + unsetAuthorizationsResp);
	}
	
	/**
	 * Formats response message for "{@value #UNSET_AUTHZ}" actions.
	 * 
	 * @param groupCodes		The return codes from the set group 
	 * 							authorization calls, indexed by group ID
	 * @param userCodes			The return codes from the set user 
	 * 							authorization calls, indexed by user ID
	 * 
	 * @return The XML response message
	 */
	private static String generateUnsetAuthorizationsResp(
			LinkedHashMap<Integer, Integer> groupCodes, 
			LinkedHashMap<Integer, Integer> userCodes) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<message ");
		addAttr(sb, "action", "unset_authorizations_resp", REQUIRED);
		sb.append(">\n");
		
		for (Map.Entry<Integer, Integer> e : groupCodes.entrySet()) {
			sb.append("<result ");
			addAttr(sb, "groupId", e.getKey(), REQUIRED);
			
			int retCode = e.getValue();
			if (retCode == 0 /* success */) {
				sb.append(">success</result>\n");
			} else {
				String reason;
				switch (retCode) {
				case -1:	//no such group
					reason = "no_such_group";
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
		
		for (Map.Entry<Integer, Integer> e : userCodes.entrySet()) {
			sb.append("<result ");
			addAttr(sb, "userId", e.getKey(), REQUIRED);
			
			int retCode = e.getValue();
			if (retCode == 0 /* success */) {
				sb.append(">success</result>\n");
			} else {
				String reason;
				switch (retCode) {
				case -1:	//no such group
					reason = "no_such_user";
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
}
