// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package scenarioAdmin;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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

import OAuth.Validator;
import broker.SavoirWebBroker;

import ca.gc.iit.nrc.savoir.domain.Scenario;
import ca.gc.nrc.iit.savoir.scenarioMgmt.ScenarioCompilerOutput;
import ca.gc.nrc.iit.savoir.scenarioMgmt.ScenarioMgr;

import static broker.SavoirWebBroker.scenarioResponseXml;
import static ca.gc.nrc.iit.savoir.utils.XmlUtils.*;

/**
 * Servlet for the special purpose of scenario submission.
 * 
 * @author Aaron Moss
 */
public class SavoirScenarioAdmin extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/*
	 * Scenario admin actions (file upload is assumed to be submit scenario)
	 */
	/** Action to remove a scenario from the SAVOIR server */
	public static final String REMOVE_SCENARIO = 	"remove_scenario";
	/** Action to get the list of removable scenarios */
	public static final String GET_REMOVABLE = 		"get_removable_scenarios";
	/** Action to submit a scenario to the SAVOIR server */
	public static final String SUBMIT_SCENARIO = 	"submit_scenario";
	
	private static final Logger logger = 
		Logger.getLogger(SavoirScenarioAdmin.class);
	static String[] CONFIG_FILES = new String[] { "classpath:cxf.xml" };

	static ApplicationContext ac = new ClassPathXmlApplicationContext(
			CONFIG_FILES);
	
	private ScenarioMgr scnMgr;
	private Validator validator;
	
	public SavoirScenarioAdmin() {
		super();
		scnMgr = (ScenarioMgr)ac.getBean("scenarioMgrClient");
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
	 * Takes a scenario file, submitted as a file object from a HTML form 
	 * submission, and submits it to the scenario manager.
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {
		
		//ensure this is actually a file upload action
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		
		if (!isMultipart) {
			
			//not a scenario file upload
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
					//this is an uploaded file - the scenario file
					logger.info("File uploaded to scenario admin servlet");
					try {
						//read into String
						String fileStr = Streams.asString(item.openStream());
						
						//handle scenario submission
						handleSubmitScenario(fileStr, response);
						return;
						
					} catch (IOException e) {
						logger.error("error copying file", e);
						
						response.setStatus(400);
						response.getWriter().write("error copying file");
						return;
					}
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
			HttpServletResponse response) throws IOException, ServletException {
		
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
		
			if (REMOVE_SCENARIO.equalsIgnoreCase(actionStr)) {
				handleRemoveScenario(doc, caller, response, xpath);
			} else if (GET_REMOVABLE.equalsIgnoreCase(actionStr)) {
				handleGetRemovable(doc, caller, response, xpath);
			} else if (SUBMIT_SCENARIO.equalsIgnoreCase(actionStr)) {
				//get <scenario> child, convert to full document string
				Node scnNode = evalNode(xpath, "//message/scenario", doc);
				if (scnNode != null) {
					Document scnDoc = toDocument(scnNode);
					String scnXml = toXmlString(scnDoc);
					handleSubmitScenario(scnXml, response);
				} else {
					response.setStatus(400);
					response.getWriter().write(
							"No scenario found on \"submit scenario\"");
					return;
				}
			} else {
				response.setStatus(400);
				response.getWriter().write("Unrecognized message action \"" + 
						actionStr + "\"!");
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			response.setStatus(400);
			response.getWriter().write(ex.getMessage());
		}
	}
	
	/**
	 * Handles "{@value #REMOVE_SCENARIO}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleRemoveScenario(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		LinkedHashMap<Integer, Integer> removeReturns = 
			new LinkedHashMap<Integer, Integer>();
		
		NodeList scnNodes = evalNodeList(xpath, "//message/scenario", doc);
		if (scnNodes != null) for (int i = 0; i < scnNodes.getLength(); i++) {
			Integer scenarioId = evalInt(xpath, "@id", scnNodes.item(i));
			if (scenarioId == null) continue;
			
			removeReturns.put(
					scenarioId, scnMgr.removeScenario(scenarioId, caller));
		}
		
		String removeScenarioResp = generateRemoveScenarioResp(removeReturns);
		response.setStatus(200);
		response.getWriter().write(removeScenarioResp);
		logger.info("Remove_Scenario_Resp:" + removeScenarioResp);
	}
	
	/**
	 * Formats response message for "{@value #REMOVE_SCENARIO}" actions.
	 * 
	 * @param retCodes			The return codes from the remove scenario 
	 * 							calls, indexed by scenario ID
	 * 
	 * @return The XML response message
	 */
	private static String generateRemoveScenarioResp(
			LinkedHashMap<Integer, Integer> retCodes) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<message ");
		addAttr(sb, "action", "remove_scenario_resp", REQUIRED);
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
				case -1:	//no such scenario
					reason = "no_such_scenario";
					break;
				case -2:	//invalid parameters
					reason = "invalid_parameters";
					break;
				case -5:	//sessions still defined on scenario
					reason = "scenario_in_use";
					break;
				case -7:	//file I/O error
					reason = "file_io_error";
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
	 * Handles "{@value #GET_REMOVABLE}" actions.
	 * 
	 * @param doc		The XML document containing action parameters
	 * @param caller	The username of the authenticated caller
	 * @param response	The response object to return
	 * @param xpath		An XPath evaluator for this thread
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleGetRemovable(Document doc, String caller, 
			HttpServletResponse response, XPath xpath) throws IOException {
		
		//get removable scenarios
		List<Scenario> removable = scnMgr.getRemovableScenarios(caller);
		
		//format and return
		String getRemovableResp = generateGetRemovableResp(removable);
		response.setStatus(200);
		response.getWriter().write(getRemovableResp);
		logger.info("Get_Removable_Scenario_Resp:" + getRemovableResp);
	}
	
	/**
	 * Formats response message for "{@value #GET_REMOVABLE}" actions.
	 * 
	 * @param removable		The list of removable scenarios
	 * 
	 * @return The XML response message
	 */
	private static String generateGetRemovableResp(List<Scenario> removable) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(
				"<message action=\"get_removable_scenarios_resp\">\n"
				+ "<result>success</result>\n"); 
		if (removable != null) for (Scenario scenario : removable) {
			sb.append(scenarioResponseXml(scenario));
		}
		sb.append("</message>");
		
		return sb.toString();
	}
	
	/**
	 * Handles "{@value #SUBMIT_SCENARIO}" actions.
	 * 
	 * @param xml		The XML document containing the scenario
	 * @param response	The response object to return
	 * 
	 * @throws IOException on error writing to response stream
	 */
	private void handleSubmitScenario(String xml, HttpServletResponse response) 
			throws IOException {
		
		logger.info("Scenario file:\n" + xml);
		
		//write output from submission
		ScenarioCompilerOutput submitResp = 
			scnMgr.submitScenario(xml);
		
		String submitScenarioResp = generateSubmitScenarioResp(submitResp);
		response.setStatus(200);
		response.getWriter().write(submitScenarioResp);
		logger.info("Submit_Scenario_Resp:" + submitScenarioResp);
		return;
	}
	
	/**
	 * Formats response message for "{@value #SUBMIT_SCENARIO}" actions.
	 * 
	 * @param submitResp		The output of the scenario compiler for the 
	 * 							submitted scenario
	 * 
	 * @return The XML response message
	 */
	private static String generateSubmitScenarioResp(
			ScenarioCompilerOutput submitResp) {
		
		Scenario sc = submitResp.getScenario();
		List<String> warnings = submitResp.getWarnings();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<message ");
		addAttr(sb, "action", "submit_scenario_resp", REQUIRED);
		sb.append(">\n");
		
		if (sc == null) {
			//scenario null on failure
			addTextNode(sb, "result", "failure", REQUIRED);
		} else {
			//scenario non-null on probable success
			addTextNode(sb, "result", "success", REQUIRED);
			sb.append(SavoirWebBroker.scenarioResponseXml(sc));
		}
		
		if (warnings != null) for (String warning : warnings) {
			addTextNode(sb, "warning", warning, REQUIRED);
			sb.append('\n');
		}
		
		sb.append("</message>");
		
		return sb.toString();
	}
}
