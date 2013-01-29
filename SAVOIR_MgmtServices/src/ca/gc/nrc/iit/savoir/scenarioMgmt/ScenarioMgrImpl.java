// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jws.WebService;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import ca.gc.iit.nrc.savoir.domain.Scenario;
import ca.gc.nrc.iit.savoir.dao.impl.DAOFactory;
import ca.gc.nrc.iit.savoir.model.profile.ServiceProfile;
import ca.gc.nrc.iit.savoir.model.session.Parameter;
import ca.gc.nrc.iit.savoir.model.session.Service;
import ca.gc.nrc.iit.savoir.resourceMgmt.ResourceMgr;
import ca.gc.nrc.iit.savoir.scenarioMgmt.parser.ApnReservation;
import ca.gc.nrc.iit.savoir.scenarioMgmt.parser.ScenarioParseState;
import ca.gc.nrc.iit.savoir.scenarioMgmt.parser.ScenarioParser;
import ca.gc.nrc.iit.savoir.scenarioMgmt.parser.ApnReservation.ApnConnection;
import ca.gc.nrc.iit.savoir.sessionMgmt.SessionMgr;
import ca.gc.nrc.iit.savoir.userMgmt.UserMgmtAuthorizer;
import ca.gc.nrc.iit.savoir.utils.FileUtils;
import ca.gc.nrc.iit.savoir.mgmtUtils.KnowledgeBuilderException;
import ca.gc.nrc.iit.savoir.mgmtUtils.ProfileUtils;
import ca.gc.nrc.iit.savoir.mgmtUtils.RuleCompiler;
import ca.gc.nrc.iit.savoir.utils.XmlUtils;

import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.FILE_IO_ERROR;
import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.INVALID_CALLER;
import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.INVALID_PARAMETERS;
import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.NO_SUCH_ENTITY;
import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.PRECONDITION_ERROR;
import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.SUCCESS;
import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.UNAUTHORIZED;


/**
 * Implementation of the Scenario Manager.
 * This implementation of the Scenario Manager uses the Drools rule engine to 
 * orchestrate scenarios. Its basic approach is to assert 
 * {@link ca.gc.nrc.iit.savoir.model.session.Service Service}s and 
 * {@link ca.gc.nrc.iit.savoir.model.session.Parameter Parameter}s into the knowledge 
 * base as they are receieved, and then let the rules respond by directing the 
 * resources (This takes place through the {@link MgmtProxy} internal class). 
 * At runtime, this class keeps a list of 
 * {@link ScenarioMgrImpl.ScenarioRuntime} objects, indexed by the ID of the 
 * authored session running them. This runtime object keeps references to the 
 * knowledge base and knowledge session of the executing rules, as well as maps 
 * mapping {@code Service} and {@code Parameter} objects to their Drools 
 * {@code FactHandle}s.
 * <p>
 * This class also includes the capability of storing scenarios, provided as 
 * XML files following 
 * the scenario schema. These files are first parsed by 
 * {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen the rule generator}, then 
 * the scenario XML, the generated rules, and a compiled binary of those 
 * generated rules are written to the SAVOIR scenario and rule repositories.
 * <p>
 * <b>Bean Properties:</b>
 * <table>
 * <tr>	<td>{@code resourceMgr}		<td>a reference to the Resource Manager 
 * <tr>	<td>{@code sessionMgr}		<td>a reference to the Session Manager
 * </table>
 * 
 * @author Aaron Moss
 * 
 * @see <a href="http://downloads.jboss.com/drools/docs/5.0.1.26597.FINAL/drools-expert/html/index.html">Drools documentation</a>
 */
@WebService(endpointInterface = "ca.gc.nrc.iit.savoir.scenarioMgmt.ScenarioMgr")
public class ScenarioMgrImpl implements ScenarioMgr {

	private static final Logger logger = 
		Logger.getLogger(ScenarioMgrImpl.class);
	
	/** format to use for Drools date literals */
	public static final String DROOLS_DATE_FORMAT = "yyyy-MMM-dd HH:mm:ss";
	static {
		System.setProperty("drools.dateformat", DROOLS_DATE_FORMAT);
	}
	
	private ResourceMgr resourceMgr;
	private SessionMgr sessionMgr;
	
	private ResourceBundle resources = 
		ResourceBundle.getBundle("mgmtservices", Locale.getDefault());
	
	/*
	 * Utility structs for holding together scenario information 
	 */
	/**
	 * Unique identifier for an activity.
	 * Composed of the ID of the service, as well as the activity ID 
	 * on the service.
	 */
	private static class ActivityUid {
		private String serviceId;
		private String activityId;
		
		public ActivityUid(String serviceId, String endpoint) {
			this.serviceId = serviceId;
			this.activityId = endpoint;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof ActivityUid) {
				ActivityUid aId = (ActivityUid)o;
				return this.serviceId.equals(aId.serviceId) ?
						this.activityId.equals(activityId) :
						false;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			//XORs hashcodes of service and endpoint
			return serviceId.hashCode() ^ activityId.hashCode();
		}
	}
	
	/**
	 * Short tuple-style generator for an ActivityId
	 * @param serviceId		The service ID
	 * @param activityId	The ID that identifies the activity on the service
	 * @return a new ActivityUid with that service ID and activity ID
	 */
	private ActivityUid aId(String serviceId, String activityId) {
		return new ActivityUid(serviceId, activityId);
	}
	
	/**
	 *	Represents running scenario 
	 */
	private static class ScenarioRuntime {
		/** knowledge base for this scenario */
		public KnowledgeBase kbase;
		/** running session for this scenario */
		public StatefulKnowledgeSession ksession;
		
		/** Services currently active in the rulebase */
		public Map<ActivityUid, Service> insertedServices
			= new HashMap<ActivityUid, Service>();
		/** Handles for Service facts */
		public Map<Service, FactHandle> serviceHandles
			= new HashMap<Service, FactHandle>();
		/** Handles for Parameter facts */
		public Map<Parameter, FactHandle> paramterHandles
			= new HashMap<Parameter, FactHandle>();
		
		/** Has this scenario been started yet */
		public boolean started = false;
	}
	
	/**
	 *	Object to give rule engine to act as proxy to MgmtServices 
	 */
	public class MgmtProxy {
				
		/**
		 * Sends a control message to a device
		 * @param sessionId		The session this message is on
		 * @param resourceId	The device to be controlled
		 * @param activityId	The activity being used
		 * @param action		The action to perform
		 * @param parameters	Optional parameters
		 * @param userId		Optional user ID to bind to
		 */
		public void controlDevice(int sessionId, String resourceId, 
				String activityId, String action, List<Parameter> parameters,
				String userId) {
			resourceMgr.controlDevice(sessionId, resourceId, activityId, 
					action, parameters, userId);
		}
		
		/**
		 * Called when the scenario terminates
		 * @param sessionId		The session ID
		 */
		public void endSession(int sessionId, String userId) {
			sessionMgr.endSession(sessionId, userId);
		}
		
	}
	
	/** Proxy for management services to hand to rule engine */
	private MgmtProxy mgmtProxy;
	
	/** Store of active scenarios, by session ID */
	private Map<Integer, ScenarioRuntime> scenarios;
	
	/** XPath evaluator to use */
	private final XPath xpath;
	/** expression to derive the scenario element from a scenario XML file */
	private final XPathExpression scenarioExpr; 
	/** expression to derive the apn element from a scenario XML file */
	private final XPathExpression apnExpr;
	
	public ScenarioMgrImpl() {
		mgmtProxy = new MgmtProxy();
		scenarios = new HashMap<Integer, ScenarioRuntime>();
		
		xpath = XPathFactory.newInstance().newXPath();
		XPathExpression xpe = null;
		try {
			xpe = xpath.compile("/scenario");
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		scenarioExpr = xpe;
		
		xpe = null;
		try {
			xpe = xpath.compile("/scenario/apn");
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		apnExpr = xpe;
		
	}
	
	public void setResourceMgr(ResourceMgr resourceMgr) {
		this.resourceMgr = resourceMgr;
	}
	
	public void setSessionMgr(SessionMgr sessionMgr) {
		this.sessionMgr = sessionMgr;
	}

	@Override
	public boolean loadScenario(int sessionId, String userName) {
		//get scenario record from database
		Scenario scn = DAOFactory.getDAOFactoryInstance().getScenarioDAO()
				.getScenarioBySessionId(sessionId);
		if (scn == null) return false;
		//make new runtime object
		ScenarioRuntime sr = new ScenarioRuntime();
		sr.kbase = null;

		try {
			//load rules for scenario
			sr.kbase = RuleCompiler.loadRules(scn.genRuleBinaryUri());
		} catch (Exception e) {
			logger.error("Rules failed to load from `" + 
					scn.genRuleBinaryUri() + "`", e);
			return false;
		}
		
		//set up new knowledge session for scenario rules
		sr.ksession = sr.kbase.newStatefulKnowledgeSession();
		//Just an try by YYH for end session bug 03-08-11
		//sr.ksession.setGlobal("mgmtProxy", mgmtProxy);
		sr.ksession.setGlobal("mgmtProxy", new MgmtProxy());
		//end try
		sr.ksession.setGlobal("sessionId", sessionId);
		sr.ksession.setGlobal("userId", userName);
		
		//store runtime object
		scenarios.put(sessionId, sr);
		
		return true;
	}

	@Override
	public void startScenario(int sessionId) {
		final ScenarioRuntime scn = scenarios.get(sessionId);
		//start scenario running, if not already started
		if (scn != null && !scn.started) {
			scn.started = true;
			
			//fireUntilHalt() blocks the thread, so I spin it off into its own
			new Thread(){
				public void run() {
					scn.ksession.fireUntilHalt();
				}
			}.start();
		}
		
	}
	
	@Override
	public void endScenario(int sessionId) {
		ScenarioRuntime scn = scenarios.remove(sessionId);
		if (scn != null) {
			//terminate Drools processes
			scn.ksession.halt();
			scn.ksession.dispose();
		}
	}

	@Override
	public void enterResource(int sessionId, String serviceId, 
			String serviceName, String activityId, List<Parameter> parameters) {
		ScenarioRuntime scn = scenarios.get(sessionId);
		if (scn != null) {
			//get service
			ActivityUid activity = aId(serviceId, activityId);
			Service service = scn.insertedServices.get(activity);
			
			//if we haven't seen this service before, insert it as a fact
			if (service == null) {
				service = new Service()
						.withId(serviceId)
						.withName(serviceName)
						.withActivityId(activityId);
				
				FactHandle fh = scn.ksession.insert(service);
				scn.insertedServices.put(activity, service);
				scn.serviceHandles.put(service, fh);
			}
			
			//for each incoming parameter
			if (parameters != null) for (Parameter parameter : parameters) {
				//XXX temporary fix for demo - ignore "?" values 
				if ("?".equals(parameter.getValue())) continue;
				//XXX end temporary fix
				
				//look up in knowledge base (all inserted parameters for a 
				// Service are associated with that Service object)
				Parameter inKbParam = service.getParameter(parameter.getId());
				
				if (inKbParam == null) {
					//parameter is not in knowledge base - insert
					
					//look in cache for profile
					ServiceProfile profile = 
						ProfileUtils.getProfile(serviceId, service.getName());
					
					//load (typed) parameter from profile (this falls back to 
					// type inference if there is no profile, or no such 
					// parameter defined on that profile)
					Parameter p = ProfileUtils.getParamter(profile, activityId, 
							parameter.getId(), parameter.getValue());
					
					//add to service and insert in knowledge base
					service.addParameter(p);
					FactHandle fh = scn.ksession.insert(p);
					scn.paramterHandles.put(p, fh);
				} else {
					//parameter is in knowledge base - update
					inKbParam.setValue(parameter.getValue());
					FactHandle fh = scn.paramterHandles.get(inKbParam);
					scn.ksession.update(fh, inKbParam);
				}
			}
		}
	}
	
	@Override
	public List<Scenario> getScenarios() {
		List<Scenario> scns =  DAOFactory.getDAOFactoryInstance()
			.getScenarioDAO().getAllScenarios();
		
		return subUris(scns);
	}
	
	@Override
	public List<Scenario> getScenariosByIds(List<Integer> ids) {
		List<Scenario> scns = DAOFactory.getDAOFactoryInstance()
			.getScenarioDAO().getScenariosByIds(ids);
		
		return subUris(scns);
	}
	
	@Override
	public List<Scenario> getRemovableScenarios(String userName) {
		
		//authorize user
		if (userName == null 
				|| !DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(userName)) {
			logger.debug(
					"get removable scenarios called by invalid caller \"" + 
					userName + "\"");
			return null;
		}
		
		List<Scenario> removable = DAOFactory.getDAOFactoryInstance()
				.getScenarioDAO().getRemovableScenarios();
		
		if (UserMgmtAuthorizer.getAuthorizer().isSysadmin(userName)) {
			//sysadmin can remove all removable scenarios
			return removable;
		} else {
			//normal user can only remove scenarios they authored themselves
			List<Scenario> userRemovable = new ArrayList<Scenario>();
			
			if (removable != null) for (Scenario scn : removable) {
				if (userName.equals(scn.getAuthorId())) {
					userRemovable.add(scn);
				}
			}
			
			return userRemovable;
		}
	}
	
	/**
	 * Substitutes local file paths for web URIs.
	 * 
	 * @param scns		List of scenarios to modify
	 * 
	 * @return the same scenarios, with their XML and rule URIs modified to use 
	 * 		web paths instead of local file paths
	 */
	private List<Scenario> subUris(List<Scenario> scns) {
		String filePrefix = resources.getString("repos.filePrefix");
		if (filePrefix == null) filePrefix = "";
		
		int afterFilePrefix = filePrefix.length();
		
		String webPrefix = resources.getString("repos.webPrefix");
		if (webPrefix == null) webPrefix = "";
		
		for (Scenario scn : scns) {
			//swap local URIs for network
			String xmlUri = scn.getXmlUri();
			if (xmlUri != null && xmlUri.startsWith(filePrefix)) {
				scn.setXmlUri(webPrefix + xmlUri.substring(afterFilePrefix));
			}
			
			String ruleUri = scn.getRuleUri();
			if (ruleUri != null && ruleUri.startsWith(filePrefix)) {
				scn.setRuleUri(webPrefix + ruleUri.substring(afterFilePrefix));
			}
		}
		
		return scns;
	}
	
	@Override
	public ScenarioCompilerOutput submitScenario(String xml) {
		//initialize error list
		List<String> errors = new ArrayList<String>();
		
		ScenarioParseState scn = null;
		try {
			//parse scenario XML
			List<String> warnings = new ArrayList<String>();
			scn = ScenarioParser.parseXml(xml, warnings);
			for (String s : warnings) {
				errors.add("WARNING: " + s);
			}
		} catch (ParserConfigurationException e) {
			String errMsg = e.getLocalizedMessage();
			errors.add("Fatal system error" + 
					(errMsg == null ? "" : ": " + errMsg));
			return new ScenarioCompilerOutput(null, errors);
		} catch (SAXException e) {
			String errMsg = e.getMessage();
			errors.add("XML format error" + 
					(errMsg == null ? "" : ": " + errMsg));
			return new ScenarioCompilerOutput(null, errors);
		} catch (IOException e) {
			String errMsg = e.getLocalizedMessage();
			errors.add("Fatal system error" + 
					(errMsg == null ? "" : ": " + errMsg));
			return new ScenarioCompilerOutput(null, errors);
		}
		
		//get new scenario ID
		int scenarioId = DAOFactory.getDAOFactoryInstance().getScenarioDAO()
			.maxScenarioId() + 1;
		
		//apply to parsed state
		scn.setScenarioId(scenarioId);
		Node scenarioNode = XmlUtils.evalNode(scenarioExpr, scn.getDocument());
		if (scenarioNode != null) {
			XmlUtils.setAttribute(scenarioNode, 
					"scenarioId", Integer.toString(scenarioId));
		}
		
		
		//base filename for scenario files
		String fileBase = baseFilename(scenarioId, scn.getScenarioName());

		
		//filename for scenario XML
		String xmlFileName = 
			resources.getString("repos.scenario") + fileBase + ".xml";
		
		try {
			//write scenario file to disk
			String modifiedXml = XmlUtils.toXmlString(scn.getDocument());
			FileUtils.writeFile(xmlFileName, modifiedXml);
		} catch (IOException e) {
			String errMsg = e.getLocalizedMessage();
			errors.add(String.format("Scenario file \"%s\" could not be " +
					"written%s", 
					xmlFileName, (errMsg == null ? "" : ": " + errMsg)));
			return new ScenarioCompilerOutput(null, errors);
		}
		
		//filename for scenario rules 
		String ruleFileName = 
			resources.getString("repos.rule") + fileBase + ".drl";
		
		try {
			//write rule file to disk
			FileUtils.writeFile(ruleFileName, scn.getRules().toString());
		} catch (IOException e) {
			String errMsg = e.getLocalizedMessage();
			errors.add(String.format("Rule file \"%s\" could not be written%s", 
					ruleFileName, (errMsg == null ? "" : ": " + errMsg)));
			return new ScenarioCompilerOutput(null, errors);
		}
		
		try {
			//compile binary form of scenario rules
			// (in addition to being faster than compiling on each load, this 
			// has the benefit of ensuring at scenario authoring time that 
			// there are no errors in the scenario)
			RuleCompiler.compileRules(ruleFileName);
		} catch (KnowledgeBuilderException e) {
			for (KnowledgeBuilderError r : e.getErrors()) {
				errors.add("Rule compilation error: " + r.getMessage());
			}
			return new ScenarioCompilerOutput(null, errors);
		} catch (FileNotFoundException e) {
			String errMsg = e.getLocalizedMessage();
			errors.add(String.format("Compiled rule file \"%s\" could not be " +
					"opened for writing%s", 
					ruleFileName + ".bin", 
					(errMsg == null ? "" : ": " + errMsg)));
			return new ScenarioCompilerOutput(null, errors);
		} catch (IOException e) {
			String errMsg = e.getLocalizedMessage();
			errors.add(String.format("Compiled rule file \"%s\" could not be " +
					"written%s", 
					ruleFileName + ".bin", 
					(errMsg == null ? "" : ": " + errMsg)));
			return new ScenarioCompilerOutput(null, errors);
		}
		
		Scenario sc = new Scenario();
		sc.setScenarioId(scn.getScenarioId());
		sc.setScenarioName(scn.getScenarioName());
		sc.setLastModified(scn.getLastModified());
		sc.setAuthorId(scn.getAuthorId());
		sc.setAuthorName(scn.getAuthorName());
		sc.setDescription(scn.getDescription());
		
		sc.setXmlUri(xmlFileName);
		sc.setRuleUri(ruleFileName);
		
		//generate APN parameters
		boolean notFirst = false;
		
		StringBuilder apnConns = new StringBuilder();
		ApnReservation apn = scn.getSites();
		
		if (apn != null) for (ApnConnection conn : apn.getConnections()) {
			if (notFirst) apnConns.append("; ");
			else notFirst = true;
			
			//<source> to <dest>, <minBW>-<maxBW>MB
			apnConns.append(conn.getSourceName()).append(" to ")
				.append(conn.getDestName()).append(", ")
				.append(conn.getMinBandwidth()).append("-")
				.append(conn.getMaxBandwidth()).append("MB");
		}
		
		sc.setApnParameters(apnConns.toString());
		
		//generate device names
		StringBuilder deviceNames = new StringBuilder();
		List<Service> devices = scn.getServices();

		notFirst = false;
		if (devices != null) for (Service device : devices) {
			if (notFirst) deviceNames.append(", ");
			else notFirst = true;
			
			deviceNames.append(device.getName());
		}
		
		sc.setDeviceNames(deviceNames.toString());
		
		
		//write new scenario record to DB
		DAOFactory.getDAOFactoryInstance().getScenarioDAO().newScenario(sc);
		
		return new ScenarioCompilerOutput(sc, errors);
	}
	
	@Override
	public int removeScenario(int scenarioId, String userName) {
		
		if (scenarioId <= 0) return INVALID_PARAMETERS;
		
		//load scenario information
		Scenario sc = DAOFactory.getDAOFactoryInstance().getScenarioDAO()
				.getScenarioById(scenarioId);
		
		//no such scenario
		if (sc == null) return NO_SUCH_ENTITY;
		
		//check for sessions using this scenario
		List<Integer> sessionsUsing = DAOFactory.getDAOFactoryInstance()
				.getSessionDAO().getSessionIdsByScenarioId(scenarioId);
		
		if (sessionsUsing != null && !sessionsUsing.isEmpty()) {
			logger.info("Could not delete scenario #" + scenarioId + ", " + 
					sessionsUsing.size() + " session(s) use it.");
			return PRECONDITION_ERROR;
		}
		
		//authenticate & authorize caller
		if (userName == null 
				|| !DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(userName)) {
			logger.debug(
					"remove scenario called by invalid caller \"" + userName 
					+ "\"");
			return INVALID_CALLER;
		}
		
		//authorized user must be either scenario author or a sysadmin
		if (userName != sc.getAuthorId() && 
				!UserMgmtAuthorizer.getAuthorizer().isSysadmin(userName)) {
			logger.debug("caller \"" + userName + "\" unauthorized to " +
					"remove scenario");
			return UNAUTHORIZED;
		}
		
		//remove from database
		DAOFactory.getDAOFactoryInstance().getScenarioDAO()
			.removeScenario(scenarioId);
		
		//get filenames
		String fileBase = 
			baseFilename(sc.getScenarioId(), sc.getScenarioName());
		
		String xmlName = 
			resources.getString("repos.scenario") + fileBase + ".xml";
		String drlName = resources.getString("repos.rule") + fileBase + ".drl";
		String binName = drlName + ".bin";
		
		//remove files (this will attempt to delete all files, rather than 
		// stopping if one fails to delete)
		int retCode = SUCCESS;
		File[] files = new File[]{
				new File(xmlName), new File(drlName), new File(binName)};
		for (File file : files) {
			try {
				//if the file isn't there, no need to delete it
				if (!file.exists()) continue;
				
				if (!file.canWrite()) {
					logger.error("No write permissions to delete file `" + 
							file.getPath() + "`");
					retCode = FILE_IO_ERROR;
					continue;
				}
				
				boolean deleted = file.delete();
				
				if (!deleted) {
					logger.error("Could not delete file `" + file.getPath() + 
							"`");
					retCode = FILE_IO_ERROR;
					continue;
				}
				
			} catch (SecurityException e) {
				logger.error(
						"Could not delete file `" + file.getPath() + "`", e);
				retCode = FILE_IO_ERROR;
				continue;
			}
		}
		
		return retCode;
	}
	
	/**
	 * Gets the base of the filename for a given scenario. This method does not 
	 * guarantee that any files exist with that filename base.
	 * 
	 * @param scenarioId		The ID of the scenario
	 * @param scenarioName		The name of the scenario
	 * 
	 * @return The base filename of all fileystem files for the scenario. It 
	 * 			will not include the file extension or path.
	 */
	private static String baseFilename(int scenarioId, String scenarioName) {
		String fileBase = 
			scenarioId + "_" + FileUtils.filenameSafe(scenarioName);
		if (fileBase.length() > 120) fileBase = fileBase.substring(0, 120);
		return fileBase;
	}
}
