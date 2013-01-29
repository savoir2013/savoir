// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.sessionMgmt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jws.WebService;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import ca.gc.iit.nrc.savoir.domain.Connection;
import ca.gc.iit.nrc.savoir.domain.EndPoint;
import ca.gc.iit.nrc.savoir.domain.Participant;
import ca.gc.iit.nrc.savoir.domain.Person;
import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.iit.nrc.savoir.domain.ResourceParameter;
import ca.gc.iit.nrc.savoir.domain.Role;
import ca.gc.iit.nrc.savoir.domain.Scenario;
import ca.gc.iit.nrc.savoir.domain.Session;
import ca.gc.iit.nrc.savoir.domain.SessionAuthorization;
import ca.gc.iit.nrc.savoir.domain.User;
import ca.gc.iit.nrc.savoir.domain.Role.Right;
import ca.gc.iit.nrc.savoir.domain.types.ParameterType;
import ca.gc.nrc.iit.savoir.dao.impl.DAOFactory;
import ca.gc.nrc.iit.savoir.model.session.Parameter;
import ca.gc.nrc.iit.savoir.model.session.Service;
import ca.gc.nrc.iit.savoir.resourceMgmt.ResourceMgr;
import ca.gc.nrc.iit.savoir.scenarioMgmt.ScenarioMgr;
import ca.gc.nrc.iit.savoir.scenarioMgmt.parser.ApnReservation;
import ca.gc.nrc.iit.savoir.scenarioMgmt.parser.ScenarioParseState;
import ca.gc.nrc.iit.savoir.scenarioMgmt.parser.ScenarioParser;
import ca.gc.nrc.iit.savoir.scenarioMgmt.parser.ApnReservation.ApnConnection;
import ca.gc.nrc.iit.savoir.scheduler.SavoirScheduler;
import ca.gc.nrc.iit.savoir.scheduler.types.SessionReservationType;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.ActivateSessionReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.ActivateSessionReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CreateSessionReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CreateSessionReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.DeleteSessionReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.DeleteSessionReservationResponse;

import ca.gc.nrc.iit.savoir.userMgmt.UserMgmtAuthorizer;
import ca.gc.nrc.iit.savoir.utils.FileUtils;
import ca.gc.nrc.iit.server.IRegServer;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.GetReservationStatus;
import ca.gc.nrc.iit.savoir.thresholdMgmt.MessageSender;

import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.INVALID_CALLER;
import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.INVALID_PARAMETERS;
import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.SUCCESS;
import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.SUBJECT_UNAUTHORIZED;
import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.UNAUTHORIZED;

/**
 * Implements {@link SessionMgr} interface by handling authentication, 
 * authorization, and database access. Also keeps runtime state of session in a 
 * {@link SessionMgrImpl.SessionRuntime} object - the chief purpose of this is 
 * differentiating between authored and unauthored sessions without resorting 
 * to a database call.
 * <p>
 * <b>Bean Properties:</b>
 * <table>
 * <tr>	<td>{@code resourceMgr}		<td>a reference to the Resource Manager
 * <tr>	<td>{@code scenarioMgr}		<td>a reference to the Scenario Manager
 * <tr>	<td>{@code messagesSender}	<td>Outbound bus interface of this SAVOIR 
 * 										instance
 * <tr>	<td>{@code savoirScheduler}	<td>Lightpath scheduling service associated 
 * 										with this SAVOIR instance
 * </table>
 */
@WebService(endpointInterface = "ca.gc.nrc.iit.savoir.sessionMgmt.SessionMgr")
public class SessionMgrImpl implements SessionMgr {

	/** Name of admin role for new session creation */
	private static final int ADMIN_ROLE_ID = 1;
	
	private SavoirScheduler savoirScheduler;

	private ResourceMgr resourceMgr;

	private IRegServer regServer;

	private static final Logger logger = Logger.getLogger(SessionMgrImpl.class);
	
	private ScenarioMgr scenarioMgr;
	
	private MessageSender msgSender;


//	/**
//	 * set the scheduler for a session
//	 */
//	public boolean setScheduler(String sessionID, String sessionName,
//			long startTime, long endTime, String startCommand,
//			String endCommand, String hostedby) {
//		if (startTime == 0) // start the session now
//		{
//			Calendar calendar = Calendar.getInstance();
//			startTime = calendar.getTimeInMillis();
//		}
//
//		String sql1 = "insert into Session_Scheduler (SID,SName,StartEndFlag,Start_time,start_command,fired,Hostedby) values ('"
//				+ sessionID
//				+ "','"
//				+ sessionName
//				+ "',0,"
//				+ startTime
//				+ ",'"
//				+ startCommand + "',false,'" + hostedby + "')";
//		String sql2 = "insert into Session_Scheduler (SID,SName,StartEndFlag,Start_time,start_command,fired,Hostedby) values ('"
//				+ sessionID
//				+ "','"
//				+ sessionName
//				+ "',1,"
//				+ endTime
//				+ ",'"
//				+ endCommand + "',false,'" + hostedby + "')";
//		boolean ret = false;
//		try {
//			DbMgr dbMgr = new DbMgr();
//			ret = dbMgr.execute(sql1);
//			if (endTime != 0) // user specify an end time
//				ret = dbMgr.execute(sql2);
//			dbMgr.close();
//		} catch (Exception e) {
//			System.out.println(e.getMessage());
//		}
//		/*
//		 * AlarmListener updates
//		 */
//		String port = resources.getString("tcpipPort");
//		int iPort = Integer.parseInt(port);
//		try {
//			new TCPClient("localhost", iPort);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return ret;
//	}

//	public int updateScheduler(String sID, String sName, long time,
//			String command, int flag, String hostedby) {
//		int ret = 0;
//		if (time == 0) // start the session now
//		{
//			Calendar calendar = Calendar.getInstance();
//			time = calendar.getTimeInMillis();
//		}
//
//		String sql = "select updateScheduler('" + sID + "','" + sName + "',"
//				+ flag + "," + time + ",'" + command + "','" + hostedby
//				+ "') as ret";
//		try {
//			DbMgr dbMgr = new DbMgr();
//			ResultSet rs = dbMgr.query(sql);
//			if (rs.next())
//				ret = rs.getInt("ret");
//			dbMgr.close();
//		} catch (Exception e) {
//			System.out.println(e.getMessage());
//		}
//		/*
//		 * AlarmListener updates
//		 */
//		try {
//			new TCPClient("localhost", 42013);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return ret;
//	}

	public SavoirScheduler getSavoirScheduler() {
		return savoirScheduler;
	}

	public ResourceMgr getResourceMgr() {
		return resourceMgr;
	}

	public IRegServer getRegServer() {
		return regServer;
	}

	public void setSavoirScheduler(SavoirScheduler savoirScheduler) {
		this.savoirScheduler = savoirScheduler;
	}

	public void setResourceMgr(ResourceMgr resourceMgr) {
		this.resourceMgr = resourceMgr;
	}
	
	public void setMsgSender(MessageSender msgSender) {
		this.msgSender = msgSender;
	}

	public void setRegServer(IRegServer regServer) {
		this.regServer = regServer;
	}
	
	public void setSyncTimeout(long syncTimeout) {
		this.syncTimeout = syncTimeout;
	}
	
	public void setScenarioMgr(ScenarioMgr scMgr) {
		scenarioMgr = scMgr;
	}

	
	@Override
	public boolean scheduleSession(int sessionID, boolean automaticActivation) {
		logger.info("The subsessionNumber is " + String.valueOf(sessionID));
		Session session = DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.getSessionById(sessionID);

		CreateSessionReservation req = new CreateSessionReservation();
		req.setNewSession(session);
		req.setReservationType(SessionReservationType.immediate_reservation);
		req.setAutomaticActivation(automaticActivation);
		System.out.println("Reserving session between: "
				+ req.getNewSession().getRequestedStartTime().getTime()
						.toString()
				+ " and "
				+ req.getNewSession().getRequestedEndTime().getTime()
						.toString());

		CreateSessionReservationResponse response = savoirScheduler
				.requestReservation(req);

		return response.isSuccessful();
	}
	
	//added by yyh 11-05-10
	@Override
	public boolean scheduleSessionOnResType(
			int sessionID, 
			boolean automaticActivation,
			String resType) {
		logger.info("The subsessionNumber is " + String.valueOf(sessionID));
		Session session = DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.getSessionById(sessionID);
		//added for debugging by YYH
		for (Session sub : session.getSubSessions()){
			for (Connection c : sub.getConnections()) {
				logger.info("The master session ID = " + session.getSessionID());
				logger.info("The sub session ID = " + sub.getSessionID());
				logger.info("The connection ID = " + c.getConnectionID());
				logger.info("The connection source end point ID = " + c.getSourceEndPoint().getEndPointID());
				logger.info("The connection source network point resource ID = " + c.getSourceEndPoint().getNetworkEndPoint().getResourceID());
				logger.info("The start vertex = " + c.getSourceEndPoint().getNetworkEndPoint()
						.getParameterValue("LP_END_POINT_SWITCH_ID"));
				logger.info("The connection target end point ID = " + c.getTargetEndPoint().getEndPointID());
				logger.info("The connection target network point resource ID = " + c.getTargetEndPoint().getNetworkEndPoint().getResourceID());
				logger.info("The end vertex = " + c.getTargetEndPoint().getNetworkEndPoint()
						.getParameterValue("LP_END_POINT_SWITCH_ID"));
			}
		}
		//end debugging

		CreateSessionReservation req = new CreateSessionReservation();
		req.setNewSession(session);
		req.setReservationType(SessionReservationType.immediate_reservation);
		req.setAutomaticActivation(automaticActivation);
		req.setResType(resType);
		logger.info("Reserving session between: "
				+ req.getNewSession().getRequestedStartTime().getTime()
						.toString()
				+ " and "
				+ req.getNewSession().getRequestedEndTime().getTime()
						.toString());

		CreateSessionReservationResponse response = savoirScheduler
				.requestReservation(req);

		return response.isSuccessful();
	}

	@Override
	public boolean cancelSession(int sessionID) {
		Session session = DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.getSessionById(sessionID);

		DeleteSessionReservation req = new DeleteSessionReservation();
		req.setSession(session);

		System.out.println("Stopping session: "
				+ req.getSession().getSessionID());

		DeleteSessionReservationResponse response = savoirScheduler
				.deleteReservation(req);

		return response.isSuccessful();
	}

	@Override
	public String getSessionStatus(int sessionID) {
		Session session = DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.getSessionById(sessionID);
		// the object name is misleading here, should be changed later
//		DeleteSessionReservation req = new DeleteSessionReservation();
		GetReservationStatus req = new GetReservationStatus();
		req.setSession(session);
		String response = savoirScheduler.getReservationStatus(req);
		return response;
	}

	private static class UserSessionInfo {
		/** ID of the session */
		public int sessionId;
		/** count of the users logged in on this session */
		public int count;
		
		/**
		 * Initializes a new UserSessionInfo with the given session ID and 
		 * count 1
		 * 
		 * @param sessionId		The session ID
		 */
		public UserSessionInfo(int sessionId) {
			this.sessionId = sessionId;
			count = 1;
		}
	}
	
	/** Map of usernames to default session IDs */
	private Map<String, UserSessionInfo> userDefaultSessions = 
		new HashMap<String, UserSessionInfo>();
	
	@Override
	public Session newSessionAgnostic(Date endTime, String userName) {
		//check that user is not already logged in
		UserSessionInfo sesnInfo = userDefaultSessions.get(userName);
		
		if (sesnInfo != null) {
			//user already logged in - return existing session (after 
			// accounting for extra login)
			sesnInfo.count++;
			return DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.getSessionById(sesnInfo.sessionId);
		}
		
		Session session = new Session();
		session.setAccepted(false);
		String defaultSessionName = 
			"User " + userName + " Unauthorized_DefaultSession";
		session.setName(defaultSessionName);
		session.setStatus(Session.RUNNING);
		session.setRequestedBy(DAOFactory.getDAOFactoryInstance().getUserDAO()
				.getUserByDN(userName));
		session.setSubmissionDate(Calendar.getInstance());
		session.setRequestedStartTime(Calendar.getInstance());
		
		int sessionId = DAOFactory.getDAOFactoryInstance().getSessionDAO()
			.addSession(session);
		session = DAOFactory.getDAOFactoryInstance().getSessionDAO()
			.getSessionById(sessionId);
		
		if (session != null) {
			//session added correctly - add session ID to logged in list
			userDefaultSessions.put(userName, new UserSessionInfo(sessionId));
		}
		
		return stripPasswords(session);
	}
	
	@Override
	public void endSessionAgnostic(String userName) {
		//get session info for user's default session
		UserSessionInfo sesnInfo = userDefaultSessions.get(userName);
		if (sesnInfo != null) {
			if (sesnInfo.count > 1) {
				//multiple logins by this user, remove this one
				sesnInfo.count--;
			} else {
				//singular login - remove session from list of logged in users
				userDefaultSessions.remove(userName);
				
				//update state to finished
				DAOFactory.getDAOFactoryInstance().getSessionDAO()
					.updateSessionStatus(sesnInfo.sessionId, Session.FINISHED);
				
				//store end time in DB
				Session sesn = DAOFactory.getDAOFactoryInstance()
					.getSessionDAO().getSessionById(sesnInfo.sessionId);
				if (sesn != null) {
					sesn.setRequestedEndTime(Calendar.getInstance());
					DAOFactory.getDAOFactoryInstance().getSessionDAO()
							.updateSession(sesn);
				}
			}
		}
	}
	
	
//	//added by yyh 10-05-10
//	@Override
//	public Session getNewAuthoredSessionAgnostic(String userName, 
//			String sessionName, Date startTime, Date endTime) {
//		Session session = new Session();
//		session.setAccepted(false);
//		session.setName(sessionName);
//		session.setStatus(Session.RUNNING);
//		session.setRequestedBy(DAOFactory.getDAOFactoryInstance().getUserDAO()
//				.getUserByDN(userName));
//		Calendar startTimeCal = Calendar.getInstance();
//		Calendar endTimeCal = Calendar.getInstance();
//		startTimeCal.setTime(startTime);
//		endTimeCal.setTime(endTime);
//		session.setSubmissionDate(Calendar.getInstance());
//		session.setRequestedStartTime(startTimeCal);
//		session.setRequestedEndTime(endTimeCal);
//		return DAOFactory.getDAOFactoryInstance().getSessionDAO()
//				.getSessionById(
//						DAOFactory.getDAOFactoryInstance().getSessionDAO()
//								.addSession(session));
//		
//	}
	
	@Override
	public Session newSessionAuthored(String sessionName, String description,
			int scenarioId, Date startTime, Date endTime, String userName) {
		//0 - authentication
		if (userName == null) return null;		//unauthenticated
		User caller = DAOFactory.getDAOFactoryInstance().getUserDAO()
				.getUserByDN(userName);
		if (caller == null) return null;		//no such user
		
		//1- parse scenario file
		//1a - load scenario
		Scenario scn = DAOFactory.getDAOFactoryInstance().getScenarioDAO()
				.getScenarioById(scenarioId);
		if (scn == null) return null;			//no such scenario
		
		//1b - parse XML from cache
		// TODO - might be able to optimize this somewhat by just grabbing an 
		//		InputStream for the file, and using SAX+XPath directly on that
		//		for just the properties we want. Look into it if this method 
		//		runs slowly.
		String xml = null;
		try {
			xml = FileUtils.readFile(scn.getXmlUri());
		} catch (IOException e) {
			return null;
		}
		
		ScenarioParseState scnDef;
		try {
			scnDef = ScenarioParser.parseXml(xml, null);
		} catch (ParserConfigurationException e) {
			return null;
		} catch (SAXException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
		
		//2- create master session
		Session session = new Session();
		session.setAccepted(false);
		session.setName(sessionName);
		session.setDescription(description);
		session.setScenarioId(scenarioId);
		session.setStatus(Session.SCHEDULED);
		session.setSubmissionDate(Calendar.getInstance());
		session.setRequestedBy(caller);
		
		Calendar startCal = null;
		if (startTime != null) {
			startCal = Calendar.getInstance();
			startCal.setTime(startTime);
		}
		session.setRequestedStartTime(startCal);
		
		Calendar endCal = null;
		if (endTime != null) {
			endCal = Calendar.getInstance();
			endCal.setTime(endTime);
		}
		session.setRequestedEndTime(endCal);
		
		int sessionId = DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.addSession(session);
		
		//3- get resources (EDs and APN) from scenario, add subsessions, 
		//		resource mappings, activity ID/name in DB
		ApnReservation apn = scnDef.getSites();
		List<Connection> connections = findAllConnections(apn);
		
		List<Service> services = scnDef.getServices();
		if (services != null) for (Service service : services) {
			//get resource ID
			int resourceId = 0;
			try {
				resourceId = Integer.parseInt(service.getId());
			} catch (NumberFormatException e) {
				continue;
			}
			
			//get resource name
			String resourceName = service.getName();
			if (resourceName == null) resourceName = "";
			
			//get activity parameters
			String activityId = service.getActivityId();
			String activityName = service.getActivityName();
			
			//create subsession for resource
			Session subsession = new Session();
			subsession.setName(
					sessionName + "_" + resourceName + "_Subsession");
			subsession.setRequestedStartTime(startCal);
			subsession.setRequestedEndTime(endCal);
			subsession.setConnections(connections);
			int subsessionId = createSubSession(sessionId, subsession);
			
			//add resource to subsession
			DAOFactory.getDAOFactoryInstance().getResourceDAO()
				.addResourceToSession(resourceId, subsessionId);
			
			//add activity ID, name to resource parameters for subsession
			List<ResourceParameter> params = new ArrayList<ResourceParameter>();
			if (activityId != null) {
				ResourceParameter param = new ResourceParameter();
				param.setParameter(
						new ParameterType("ACTIVITY_ID", null, null));
				param.setValue(activityId);
				params.add(param);
			}
			if (activityName != null) {
				ResourceParameter param = new ResourceParameter();
				param.setParameter(
						new ParameterType("ACTIVITY_NAME", null, null));
				param.setValue(activityName);
				params.add(param);
			}
			if (!params.isEmpty()) {
				DAOFactory.getDAOFactoryInstance().getParametersDAO()
					.saveParameters(resourceId, params, /*sessionId*/
							subsessionId);
			}
		}
		
		//3b - activate APN
		boolean apnResult = true;
		if (apn != null) {
			apnResult = scheduleSessionOnResType(sessionId, false, 
					apn.getReservationMethod().toString());
			if (apnResult == true) {
				apnResult = activateSession(sessionId);
			}
		}
		
		//4- authorize session creator on session
		DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.addUserAuthorization(
						caller.getUserID(), ADMIN_ROLE_ID, sessionId);
		
		//5- return created session
		return stripPasswords(DAOFactory.getDAOFactoryInstance()
				.getSessionDAO().getSessionById(sessionId));
	}
	
	/**
	 * Gets all the connections from the APN data stored on a scenario
	 * 
	 * @param data		The data item from the scenario
	 * 
	 * @return	a list of connection objects constructed from that data
	 */
	private List<Connection> findAllConnections(ApnReservation data) {
		
		if (data == null) return null;
		
		List<Connection> connections = new ArrayList<Connection>();
		
		List<ApnConnection> connData = data.getConnections();
		if (connData != null) for (ApnConnection connDatum : connData) {
			Resource sourceLP = getApnResource(connDatum.getSourceName()); 
			Resource destLP = getApnResource(connDatum.getDestName());
			
			EndPoint sourceEndPoint = new EndPoint();
			EndPoint targetEndPoint = new EndPoint();
			Connection connection = new Connection();
			
			sourceEndPoint.setNetworkEndPoint(sourceLP);
			sourceEndPoint.setEndPointType(EndPoint.CLIENT);
			targetEndPoint.setNetworkEndPoint(destLP);
			targetEndPoint.setEndPointType(EndPoint.SERVER);
			
			connection.setLpNeeded(true);
			try {
			connection.setBwRequirement(
					Double.valueOf(connDatum.getMaxBandwidth()));
			} catch (NumberFormatException e) {
				continue;
			}
			try {
			connection.setMinBwRequirement(
					Double.valueOf(connDatum.getMinBandwidth()));
			} catch (NumberFormatException e) {
				continue;
			}
			connection.setSourceEndPoint(sourceEndPoint);
			connection.setTargetEndPoint(targetEndPoint);
			connection.setDirectionality("TWO-WAY");
			
			connections.add(connection);
		}
		
		return connections;
	}
	
	/**
	 * Gets the resource for a lightpath endpoint, given its site name
	 * 
	 * @param siteName		The name of the source site
	 * 
	 * @return	the resource for that APN site, null for none such or error
	 */
	private Resource getApnResource(String siteName) {
		
		List<Resource> resources = DAOFactory.getDAOFactoryInstance()
				.getResourceDAO().getResourcesByTypeAndParameterValue(
						"LP_END_POINT", "SITE_LOCATION", siteName);
		
		if (resources != null && resources.size() != 0)
			return resources.get(0);
		else
			return null;
	}

	@Override
	public int createSubSession(int sessionID, Session subSession) {
		//get parent session
		Session parentSession = DAOFactory.getDAOFactoryInstance()
				.getSessionDAO().getSessionById(sessionID);
		//clone fields into subsession
		subSession.setRequestedBy(parentSession.getRequestedBy());
		subSession.setSubmissionDate(Calendar.getInstance());
		subSession.setStatus(Session.SCHEDULED);
		//create new (sub)session
		return DAOFactory.getDAOFactoryInstance().getSessionDAO().addSession(
				subSession, sessionID);
	}

	@Override
	public boolean activateSession(int sessionID) {
		Session parentSession = DAOFactory.getDAOFactoryInstance()
				.getSessionDAO().getSessionById(sessionID);
		ActivateSessionReservationResponse response = 
			new ActivateSessionReservationResponse();
		ActivateSessionReservation req = new ActivateSessionReservation();
		req.setSession(parentSession);
		response = savoirScheduler.activateSessionReservation(req);
		return response.isSuccessful();
	}
	
	@Override
	public List<Session> getAllSessions(String userName) {
		//check for non-null username
		if (userName == null) {
			//unauthenticated caller, return empty list
			return new ArrayList<Session>();
		}
		
		//get session IDs the caller is authorized to view
		Set<Session> authorized = 
			UserMgmtAuthorizer.getAuthorizer().allAuthorizedSessions(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(userName), 
				Right.SCENARIO_RUN);
		
		return stripPasswords(new ArrayList<Session>(authorized));
	}
	
	@Override
	public List<Session> getCurrentSessions(String userName) {
		//get session IDs the caller is authorized to view
		Set<Session> authorized = 
			UserMgmtAuthorizer.getAuthorizer().allAuthorizedSessions(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(userName), 
				Right.SCENARIO_RUN);
		
		//list of sessions to return
		List<Session> sessions = new ArrayList<Session>();
		//get the current time
		Calendar now = Calendar.getInstance();
		
		for (Session session : authorized) {
			Calendar endTime = session.getRequestedEndTime();
			//filter authorized sessions for those ending after now
			if (endTime == null || endTime.after(now)) {
				sessions.add(session);
			}
		}
		
		return stripPasswords(sessions);
	}

	@Override
	public List<Session> getJoinableSessions(String userName) {
		if (userName == null) {
			//unauthenticated caller, return empty list
			return new ArrayList<Session>();
		}
		
		// TODO
		// 1- query the loaded/running sessions that the user identified by
		// userName is a participant of.

		
//		List<Session> result = new ArrayList<Session>();

		//get sessions in proper state
//		List<Session> sessions = DAOFactory.getDAOFactoryInstance()
//				.getSessionDAO().getSessionsByStatus(Session.LOADING, Session.RUNNING);
		
		//get session IDs the caller is authorized to join
		Set<Session> authorized = 
			UserMgmtAuthorizer.getAuthorizer().allAuthorizedSessions(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(userName), 
				Right.SCENARIO_RUN);
		
		//get list of authorized sessions
		List<Session> joinable = new ArrayList<Session>();
		Set<String> validStatuses = 
			new HashSet<String>(
					Arrays.asList(Session.LOADING, Session.RUNNING));
		
		for (Session session : authorized) {
			if (validStatuses.contains(session.getStatus())) {
				joinable.add(session);
			}
		}
		
		return stripPasswords(joinable);
	}

	@Override
	public List<Session> getLoadableSessions(String userName) {
		// TODO
		// we might need to provide time limits for the search: from now to a
		// certain time in the future: end of the day, end of the week or end of
		// the month
		// 1- query the scheduled sessions that the user identified by userName
		// is a tutor of and that are happening within the time limits.

//		List<Session> result = new ArrayList<Session>();

//		List<Session> sessions = DAOFactory.getDAOFactoryInstance()
//				.getSessionDAO().getSessionsByStatus(Session.SCHEDULED);
		
		if (userName == null) {
			//unauthenticated caller, return empty list
			return new ArrayList<Session>();
		}
		
		Set<Session> authorized = 
			UserMgmtAuthorizer.getAuthorizer().allAuthorizedSessions(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(userName), 
				Right.AUTHORED_RUN);
		
		List<Session> loadable = new ArrayList<Session>();
		for (Session session : authorized) {
			if (Session.SCHEDULED.equals(session.getStatus())) {
				loadable.add(session);
			}
		}
		
		return stripPasswords(loadable);

	}
	
	@Override
	public List<Session> getRemovableSessions(String userName) {
		
		//check for non-null username
		if (userName == null) {
			logger.info("null username for getRemovableSessions()");
			//unauthenticated caller, return empty list
			return new ArrayList<Session>();
		}
		
		//get session IDs the caller is authorized to view
		Set<Session> authorized = 
			UserMgmtAuthorizer.getAuthorizer().allAuthorizedSessions(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(userName), 
				Right.SCENARIO_EDIT);
		
		return stripPasswords(new ArrayList<Session>(authorized));
		
	}
	
	/**
	 * Clears user passwords from a single session
	 * 
	 * @param session		The session to strip the users' password data from
	 */
	private Session stripPasswords(Session session) {
		if (session == null) return null;
		
		session.getRequestedBy().setPassword(null);
		
		List<Participant> participants = session.getAuthorizedUsers();
		if (participants != null) for (Participant participant : participants) {
			participant.getUser().setPassword(null);
		}
		
		stripPasswords(session.getSubSessions());
		
		return session;
	}
	
	/**
	 * Clears user passwords from multiple sessions
	 * 
	 * @param sessions		The sessions to strip the users' password data from 
	 */
	private List<Session> stripPasswords(List<Session> sessions) {
		if (sessions != null) for (Session session : sessions) {
			stripPasswords(session);
		}
		
		return sessions;
	}

	@Override
	public void joinSession(int sessionID, String userName) throws Exception {
		// 1- check if user allowed to join
		// 2- update state of the participant
		// 3- notify people involved in the session
		
		if (userName == null) {
			//unauthenticated caller
			// TODO: create a new specific exception
			throw new Exception("No user given");
		}
		
		Session s = DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.getSessionById(sessionID);
		
		//check user authorization to join
		User caller = 
			DAOFactory.getDAOFactoryInstance().getUserDAO()
				.getUserByDN(userName);
		if (UserMgmtAuthorizer.getAuthorizer().isAuthorizedOnSession(
				caller.getUserID(), Right.SCENARIO_RUN, sessionID)) {
			//set caller's participant status as "joined"
			for (Participant p : s.getAuthorizedUsers()) {
				if (p.getUser().getUserID() == caller.getUserID()) {
					p.setStatus(Participant.JOINED);
					// notify everyone involved in the sessions
					//update shifted to another method 10-05-09
					//DAOFactory.getDAOFactoryInstance().getSessionDAO().updateSessionParticipant(sessionID, p);
					return;
				}
			}
			//caller not in participant list
			//TODO add new particpant for caller, set status to "joined"
			return;
		} else {
			// TODO: create a new specific exception
			throw new Exception("User not allowed to join");
		}
		
//		Person person = DAOFactory.getDAOFactoryInstance().getUserDAO()
//				.getUserByDN(userName).getPerson();
//
//		for (Participant p : s.getParticipants()) {
//			if (p.getPerson().getPersonId() == person.getPersonId()) {
//				p.setStatus(Participant.JOINED);
//				// notify everyone involved in the sessions
//				//update shit to another method 10-05-09
//				//DAOFactory.getDAOFactoryInstance().getSessionDAO().updateSessionParticipant(sessionID, p);
//				return;
//			}
//		}
//
//		// TODO: create a new specific exception
//		throw new Exception("User not allowed to join");

	}
	
	@Override
	public int removeSession(int sessionId, String userName) {
		
		if (sessionId <= 0) return INVALID_PARAMETERS;
		
		if (userName == null) return INVALID_CALLER;
		
		User caller = 
			DAOFactory.getDAOFactoryInstance().getUserDAO()
				.getUserByDN(userName);
		
		if (caller == null) {
			logger.debug("remove session called by invalid caller \"" + 
					userName + "\"");
			
			return INVALID_CALLER;
		}
		
		if (!UserMgmtAuthorizer.getAuthorizer().isAuthorizedOnSession(
				caller.getUserID(), Right.SCENARIO_EDIT, sessionId)) {
			logger.debug("caller \"" + userName + "\" unauthorized to " +
					"remove session");
			return UNAUTHORIZED;
		}
		
		DAOFactory.getDAOFactoryInstance().getSessionDAO()
			.removeSession(sessionId);
		
		return SUCCESS;
	}
	
	/**
	 * Contains runtime information for a session.
	 */
	private static class SessionRuntime {
		/** a reference to this session */
		public Session session;
		/** true for authored session, false for unauthored */
		public boolean authored;
		/** userName of user who requested session */
		public String userName;
		
		public SessionRuntime() {
			session = null;
			authored = false;
			userName = null;
		}
	}
	
	/** runtime information for sessions, indexed by session ID */
	private Map<Integer, SessionRuntime> sessions 
			= new HashMap<Integer, SessionRuntime>();

	@Override
	public void loadSession(int sessionID, String userName) throws Exception {
		// TODO Auto-generated method stub
		// 0- check if the time has come for the session to be loaded
		// 1- load rules
		// 2- load edge devices
		// 3- load network resources
		// 4- update sessions status
		// 5- check edge devices subscriptions
		// 6- notify people involved that session is loaded
		
		if (userName == null) {
			//unauthenticated caller
			// TODO: create a new specific exception
			throw new Exception("No user given");
		}
		
		//get session from DB
		Session session = DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.getSessionById(sessionID);
		
		//check user authorization
		if (UserMgmtAuthorizer.getAuthorizer().isAuthorizedOnSession(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(userName), 
				Right.AUTHORED_RUN, sessionID)) {
			
			//build new runtime for session
			SessionRuntime runtime = new SessionRuntime();
			sessions.put(sessionID, runtime);
			runtime.session = session;
			runtime.userName = userName;
			
			// 1- load rules
			runtime.authored = 
				scenarioMgr.loadScenario(sessionID, runtime.userName);
			
			//changed at 10-04-09 for demo because the session getResources method just return the connection based information.
			//they are different from the return from calling resource manager's getResourceBySessionID 
//			for (Resource r : session.getResources()) {
//				// 2- load resource
//				resourceMgr.loadEdgeDevice(r, session.getSessionID());
//			}

			// 2- load resources for session
			resourceMgr.loadResourcesForSession(sessionID, userName);
			
			// 3- activate the network through resource manager that calls
			// the scheduler
			resourceMgr.loadNetworkResources(session);

			// 4- the scheduler updates the session status if the activation
			// is successful...

			// 5- check edge devices subscriptions
			//not sure this is realistic

			// 6- notify people involved that session is loaded
			//commented out by Aaron 02-Mar-2010
//			ca.gc.nrc.iit.savoir.message.bindings.Message mm = MessageHelper
//					.createMessage(MessageHelper.NOTIFY, session
//							.getSessionID());
//
//			Service s = (new ObjectFactory()).createService();
//			Parameter par = (new ObjectFactory()).createParameter();
//			par.setValue("Session has been loaded");
//
//			s.getParameter().add(par);
//
//			mm.getService().add(s);
//
//			try {
//				BusTools.sendMessage(JaxbTool.ObjectToXMLString(mm), null,
//						null);
//			} catch (JAXBException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			return;
		} else {
			// TODO: create a new specific exception
			throw new Exception("User not allowed to load");
		}

	}
	
	@Override
	public void beginSession(int sessionId, String userName) {
		//get runtime object for session
		SessionRuntime runtime = sessions.get(sessionId);
		if (runtime == null) {
			return;
		}
		
		//check authorization
		if (userName == null) return;
		if (!UserMgmtAuthorizer.getAuthorizer().isAuthorizedOnSession(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(userName), 
				Right.AUTHORED_RUN, sessionId)) {
			return;
		}
	
		// 1- trigger start rule in the ScM
		if (runtime.authored) {
			scenarioMgr.startScenario(sessionId);
		}
		
		// TODO Auto-generated method stub
		// 2- update session status
		DAOFactory.getDAOFactoryInstance().getSessionDAO()
			.updateSessionStatus(sessionId, Session.RUNNING);
		
		// 3- notify session started
	}
	
	@Override
	public void runSession(int sessionId, String userName) throws Exception {
		if (userName == null) {
			//unauthenticated caller
			throw new Exception("No user given");
		}
		
		//check if session already running, join if so
		SessionRuntime runtime = sessions.get(sessionId);
		
		if (runtime != null) {
//			//already running, join instead
//			joinSession(sessionId, userName);
//			return;
			
			//already running, lock out
			throw new Exception("Session already running.");
		}
		
		//check user authorization to run session
		if (!UserMgmtAuthorizer.getAuthorizer().isAuthorizedOnSession(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(userName), 
				Right.AUTHORED_RUN, sessionId)) {
			throw new Exception("User not allowed to run session");
		}
		
		//load session from DB
		Session session = DAOFactory.getDAOFactoryInstance().getSessionDAO()
			.getSessionById(sessionId);
		
		//construct session runtime
		runtime = new SessionRuntime();
		sessions.put(sessionId, runtime);
		runtime.session = session;
		runtime.userName = userName;
		
		// 1- load rules
		runtime.authored = 
			scenarioMgr.loadScenario(sessionId, runtime.userName);
		
		if (runtime.authored) {
			//authored session
			msgSender.sessionStarting(sessionId);
			
			//subscribe users that belong to this session to the appropriate 
			// feeds. Also subscribe starting user to feeds no one else 
			// subscribes to.

			//get starting user's default session ID
			UserSessionInfo callerSesn = userDefaultSessions.get(userName);
			//done by socket proxy
//			if (callerSesn != null) {
//				//subscribe to main session topic
//				msgSender.sendSubscribeMessage(sessionId + "-0000-0000", 
//						Integer.toString(callerSesn.sessionId));
//			}
			
			//get subsessions
			List<Session> subsessions = session.getSubSessions();
			if (subsessions != null && !subsessions.isEmpty()) {
				for (Session subsession : subsessions) {
					int subsessionId = subsession.getSessionID();
					
					boolean hasParticipant = false;	//any users subscribed?
					
					//get session participants
					List<Participant> participants = 
						subsession.getAuthorizedUsers();
					if (participants != null && !participants.isEmpty()) {
						for (Participant p : participants) {
							//get usernames of participants
							String username = p.getUser().getDName();
							//get default session of participant (not null 
							// implies logged in)
							UserSessionInfo sesnInfo = 
								userDefaultSessions.get(username);
							//tell to subscribe to special feeds
							if (sesnInfo != null) {
								
								String userAddrId = 
									Integer.toString(sesnInfo.sessionId);
								//done by socket proxy
//								msgSender.sendSubscribeMessage(
//										sessionId + "-" + subsessionId, 
//										userAddrId);
								msgSender.sendSubscribeMessage(
										sessionId + "-" + subsessionId + "-" + 
											userAddrId, 
										userAddrId);
								
								hasParticipant = true;
							}
						}
					} 
					
					if (!hasParticipant) {
						//no participants, subscribe calling user
						
						if (callerSesn != null) {
							//subscribe calling user to subsession topic
							String callerSessionId = 
								Integer.toString(callerSesn.sessionId);
//							msgSender.sendSubscribeMessage(
//									sessionId + "-" + subsessionId, 
//									callerSessionId);
							
							msgSender.sendSubscribeMessage(
									sessionId + "-" + subsessionId + 
										"-" + callerSessionId, 
									Integer.toString(callerSesn.sessionId));
						}
					}
					
				}
			}
			
			//load resources
			resourceMgr.loadResourcesForSession(sessionId, userName);
			//once all resources loaded, callback will start scenario rules
			
			//update session status
			DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.updateSessionStatus(sessionId, Session.RUNNING);
		} else /* unauthored session */ {
			// update session status
			DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.updateSessionStatus(sessionId, Session.RUNNING);
		}
		
		// 2- TODO have starter join session
	}

	
	@Override
	public void endSession(int sessionId, String userName) {
		// 1- unload rules
		// 2- stop edge devices
		// 3- unload network
		// 4- notify session ended
		
		if (userName == null || userName.isEmpty()) return;
		
		//free memory for session runtime state
		SessionRuntime runtime = sessions.remove(sessionId);
		if (runtime == null) return;
		
		//check user authorization (accounting for computer calls)
		if (!userName.equals(runtime.userName) && 
				!UserMgmtAuthorizer.getAuthorizer().isSysadmin(userName)) {
			//undo session removal
			sessions.put(sessionId, runtime);
			return;
		}
		
		endSession(runtime);
	}
	
	@Override
	public void endSessionPriv(int sessionId) {
		//free memory for session runtime state
		SessionRuntime runtime = sessions.remove(sessionId);
		if (runtime == null) return;
		
		endSession(runtime);
	}
	
	private void endSession(SessionRuntime runtime) {
		int sessionId = runtime.session.getSessionID();
		String userName = runtime.userName;
		
		//unload network resource through scheduling service called by resource 
		// manager
		resourceMgr.unloadNetworkResources(sessionId);
		
		// 1- unload rules
		if (runtime.authored) {
			scenarioMgr.endScenario(sessionId);
		}
		
		//2- send resources end session
		resourceMgr.endSessionForResources(sessionId, userName);
		
		//3- notify TM to stop logging for this session
		msgSender.sessionEnding(sessionId);
		
		//4- update session status in database
		String status;
		Calendar endTime = runtime.session.getRequestedEndTime();
		Calendar now = Calendar.getInstance();
		if (endTime == null || endTime.after(now)) {
			status = Session.PENDING;
		} else {
			status = Session.FINISHED;
		}
		DAOFactory.getDAOFactoryInstance().getSessionDAO()
			.updateSessionStatus(sessionId, status);
	}
	
	/** Threads blocking {@link #endSessionSync(int)} calls */
	private Map<Integer, Thread> syncBlocks = new HashMap<Integer, Thread>();
	/** maximum time for {@link #endSessionSync(int)} to block */
	private long syncTimeout = 180 * 1000;	//3 minutes, in ms
	
	@Override
	public void endSessionSync(final int sessionId, String userName) {
		//escape hatch for no such session
		if (!sessions.containsKey(sessionId)) return;
		endSession(sessionId, userName);
		Thread t = new Thread(){
			public void run() {
				//sleeps for up to syncTimeout, terminating if interrupted
				try {
					sleep(syncTimeout);
				} catch (InterruptedException e) {}
				//ensure this thread cleans up after itself
				syncBlocks.remove(sessionId);
			}
		};
		t.start();
		syncBlocks.put(sessionId, t);
		try {
			t.join();
		} catch (InterruptedException e) {
			return;
		}
	}
	
	@Override
	public void flushSession(int sessionId, String userName) {
		
		if (userName == null || userName.isEmpty()) return;
		
		//free memory for session runtime state
		SessionRuntime runtime = sessions.remove(sessionId);
		if (runtime == null) return;

		//check user authorization (accounting for computer calls)
		if (!userName.equals(runtime.userName) && 
				!UserMgmtAuthorizer.getAuthorizer().isSysadmin(userName)) {
			//undo session removal
			sessions.put(sessionId, runtime);
			return;
		}
		
		//unload network resources through scheduling service called by 
		// resource manager
		resourceMgr.unloadNetworkResources(sessionId);
		
		// unload rules
		if (runtime.authored) {
			scenarioMgr.endScenario(sessionId);
		}
		
		//kill resource manager state
		resourceMgr.abortSessionForResources(sessionId);
		
		//notify TM to stop logging for this session
		msgSender.sessionEnding(sessionId);
		
		//update session status in database
		String status;
		Calendar endTime = runtime.session.getRequestedEndTime();
		Calendar now = Calendar.getInstance();
		if (endTime == null || endTime.after(now)) {
			status = Session.PENDING;
		} else {
			status = Session.FINISHED;
		}
		DAOFactory.getDAOFactoryInstance().getSessionDAO()
			.updateSessionStatus(sessionId, status);
	}
	
	@Override
	public void sessionEnded(int sessionId) {
		//return from a syncronous call to endSession()
		Thread t = syncBlocks.remove(sessionId);
		if (t != null) t.interrupt();
	}
	
	@Override
	public void sessionLoaded(int sessionId) {
		// get state for session
		SessionRuntime runtime = sessions.get(sessionId);
		
		if (runtime != null && runtime.authored == true) {
			//trigger scenario rules for authored session
			scenarioMgr.startScenario(sessionId);
		}
	}
	
	@Override
	public void updateResourceParameters(int sessionId, String resourceId, 
			String resourceName, String instanceId, List<Parameter> params) {
		SessionRuntime runtime = sessions.get(sessionId);
		
		//pass update to resource parameters along to scenario manager, 
		// if authored session
		if (runtime != null && runtime.authored) {
			scenarioMgr.enterResource(
					sessionId, resourceId, resourceName, instanceId, params);
		}
	}
	
	@Override
	public List<Integer> getUserSessionIdsForSubsession(int sessionId, 
			int subsessionId) {
		SessionRuntime runtime = sessions.get(sessionId);
		
		if (runtime == null || !runtime.authored) {
			return null;
		}
		
		List<Integer> uSIds = new ArrayList<Integer>();
		
		List<Session> subsessions = runtime.session.getSubSessions();
		
		if (subsessions != null) for (Session subsession : subsessions) {
			//find correct subsession
			if (subsession.getSessionID() == subsessionId) {
				
				//get session participants
				List<Participant> participants = 
					subsession.getAuthorizedUsers();
				if (participants != null) for (Participant p : participants) {
					//get usernames of participants
					String username = p.getUser().getDName();
					//get default session of participant (implies logged in)
					UserSessionInfo sesnInfo = userDefaultSessions.get(username);
					//add to return list if available
					if (sesnInfo != null) uSIds.add(sesnInfo.sessionId);
				}
				
				break;
			}
		}
		
		return uSIds;
	}
	
//	@Override
//	public void notifyDock(String xmlMessage, List<String> parameters,
//			List<String> values) {
//
//		StringBuilder sb = new StringBuilder("http://localhost:8890");
//
//		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//		nvps.add(new BasicNameValuePair("savoirmsg", xmlMessage));
//
//		System.out.println(sb.toString());
//
//		HttpClient httpclient = new DefaultHttpClient();
//		HttpPost httpost = new HttpPost(sb.toString());
//
//		httpost.addHeader("contentType", "text/html;charset=UTF-8");
//
//		try {
//			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
//
//			System.out.println("executing request " + httpost.getURI());
//
//			// Create a response handler
//			ResponseHandler<String> responseHandler = new BasicResponseHandler();
//			String responseBody;
//
//			responseBody = httpclient.execute(httpost, responseHandler);
//			System.out.println(responseBody);
//
//			System.out.println("----------------------------------------");
//
//			// When HttpClient instance is no longer needed,
//			// shut down the connection manager to ensure
//			// immediate deallocation of all system resources
//			httpclient.getConnectionManager().shutdown();
//
//		} catch (ClientProtocolException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}

	@Override
	public List<Session> getSubSessionList(int parentSessionID){
		return stripPasswords(DAOFactory.getDAOFactoryInstance()
				.getSessionDAO().getSubsessions(parentSessionID));
	}

	@Override
	public void updateSession(Session updatedSession){
		DAOFactory.getDAOFactoryInstance().getSessionDAO().updateSession(updatedSession);
	}
	
	@Override
	public Session getSessionById(int sessionID){
		return stripPasswords(DAOFactory.getDAOFactoryInstance()
				.getSessionDAO().getSessionById(sessionID));
	}
	
	@Override
	public List<Session> getSessionListByName(String sessionName){
		return stripPasswords(DAOFactory.getDAOFactoryInstance()
				.getSessionDAO().getSessionListByName(sessionName));
	}
	
//	@Override
//	public void endSessionBySessionID(int sessionID){
//		Session endSession = DAOFactory.getDAOFactoryInstance().getSessionDAO().getSessionById(sessionID);
//		List<Participant> participants = endSession.getParticipants();
//		for(int i = 0; i < participants.size(); i++){
//			Participant theParticipant = participants.get(i);
//			User theUser = theParticipant.getUser();
//			Session theUserSession = DAOFactory.getDAOFactoryInstance().getSessionDAO().getSessionByStatusAndUserID(Session.RUNNING, theUser.getUserID());
//			theUserSession.setStatus(Session.FINISHED);
//			DAOFactory.getDAOFactoryInstance().getSessionDAO().updateSession(theUserSession);
//			
//		}
//		endSession.setStatus(Session.FINISHED);
//		DAOFactory.getDAOFactoryInstance().getSessionDAO().updateSession(endSession);
//		this.notifyServices.notifySessionStateChangeToUser(sessionID, "The session have been finished!");
//	}
//
//	public void startSessionBySessionID(int sessionID){
//		//send to threshhold manager
//		thresholdServices.triggerStartRule(sessionID);
//	}
	
	public void updateSessionPaticipantStatus(Session session, User user){
		for (Participant p : session.getAuthorizedUsers()) {
			if (p.getUser().getUserID() == user.getUserID()) {
				p.setStatus(Participant.JOINED);
				DAOFactory.getDAOFactoryInstance().getSessionDAO()
					.updateSessionParticipant(session.getSessionID(), p);
				return;
			}
		}
	}
	
	public Person getPersonByUserName(String userName){
		return DAOFactory.getDAOFactoryInstance().getUserDAO()
		.getUserByDN(userName).getPerson();
	}
	
	//--------------------------------------------------------
	// User / Group Authorization View-Create-Delete operations
	//--------------------------------------------------------
	
	@Override
	public List<SessionAuthorization> getUserAuthorizations(int sessionId, 
			String userName) {
		if(sessionId <= 0) {
			return Collections.emptyList();
		}
		
		//authenticate caller
		if (userName == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(userName)) {
			logger.debug("get user authorizations called by invalid caller " +
					"\"" + userName + "\"");
			//return invalid caller if cannot be authenticated, 
			// or does not match any record in the database
			return Collections.emptyList();
		}
		
		//check authorization
		if (!UserMgmtAuthorizer.getAuthorizer().isAuthorizedOnSession(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(userName),
				Right.VIEW_MEMBERS, sessionId)) {
			logger.debug("user \"" + userName + 
					"\" unauthorized to view user authorizations");
			return Collections.emptyList();
		}
		logger.trace("getUserAuthorizations called by \"" + userName + 
				"\" on \"" + sessionId + "\"");
		
		//get roles of authorized users
		Map<Integer, Role> authorizedUsers = 
			DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.getAuthorizedUsers(sessionId);
		
		//convert to list of session authorizations
		List<SessionAuthorization> ret = 
			new ArrayList<SessionAuthorization>(authorizedUsers.size());
		for (Map.Entry<Integer, Role> user : authorizedUsers.entrySet()) {
			SessionAuthorization authz = new SessionAuthorization();
			authz.setAuthorizedTo(user.getKey());
			authz.setAuthorizedOn(sessionId);
			authz.setRole(user.getValue());
			ret.add(authz);
		}
		
		return ret;
	}
	
	@Override
	public List<SessionAuthorization> getGroupAuthorizations(int sessionId, 
			String caller) {
		if(sessionId <= 0) {
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
		if (!UserMgmtAuthorizer.getAuthorizer().isAuthorizedOnSession(
				DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserID(caller),
				Right.VIEW_MEMBERS, sessionId)) {
			logger.debug("user \"" + caller + 
					"\" unauthorized to view group authorizations");
			return Collections.emptyList();
		}
		logger.trace("getGroupAuthorizations called by \"" + caller + 
				"\" on \"" + sessionId + "\"");
		
		//get roles of authorized groups
		Map<Integer, Role> authorizedGroups = 
			DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.getAuthorizedGroups(sessionId);
		
		//convert to list of session authorizations
		List<SessionAuthorization> ret = 
			new ArrayList<SessionAuthorization>(authorizedGroups.size());
		for (Map.Entry<Integer, Role> group : authorizedGroups.entrySet()) {
			SessionAuthorization authz = new SessionAuthorization();
			authz.setAuthorizedTo(group.getKey());
			authz.setAuthorizedOn(sessionId);
			authz.setRole(group.getValue());
			ret.add(authz);
		}
		
		return ret;
	}
	
	@Override
	public int setUserAuthorization(int userId, int sessionId, int roleId, 
			String userName) {
		//validate input parameters
		if (userId <= 0 || sessionId <= 0 || roleId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		//check caller authenticity
		if (userName == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(userName)) {
			logger.debug("add user authorization called by invalid caller " +
					"\"" + userName + "\"");
			//return invalid caller if cannot be authenticated, 
			// or does not match any record in the database
			return INVALID_CALLER;
		}
		
		//check caller authorization
		if (!UserMgmtAuthorizer.getAuthorizer()
				.isAuthorizedOnUserSessionAuthorization(
						DAOFactory.getDAOFactoryInstance().getUserDAO()
							.getUserID(userName), 
						userId, roleId, sessionId)) {
			logger.debug("caller \"" + userName + 
					"\" unauthorized to add user authorization");
			return UNAUTHORIZED;
		}
		
		//check subject authorization on resources in session
		List<Resource> resources = DAOFactory.getDAOFactoryInstance()
			.getResourceDAO().getResourcesBySessionID(sessionId);
		if (resources != null) for (Resource r : resources) {
			if (!UserMgmtAuthorizer.getAuthorizer()
					.userIsAuthorizedOnResource(userId, r.getResourceID())) {
				logger.debug("user with ID " + userId + " cannot be " +
						"authorized on session " + sessionId + " because " +
						"they lack authorization on resource " + 
						r.getResourceID());
				return SUBJECT_UNAUTHORIZED;
			}
		}
		
		//change authorization if already set, add new otherwise
		if (DAOFactory.getDAOFactoryInstance().getSessionDAO().
				getAuthorizedUsers(sessionId).get(userId) != null) {
			return DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.updateUserAuthorization(userId, roleId, sessionId);
		} else {
			return DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.addUserAuthorization(userId, roleId, sessionId);
		}
	}
	
	@Override
	public int setGroupAuthorization(int groupId, int sessionId, int roleId, 
			String userName) {
		//validate input parameters
		if (groupId < 0 || sessionId <= 0 || roleId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		//check caller authenticity
		if (userName == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(userName)) {
			logger.debug("add group authorization called by invalid caller " +
					"\"" + userName + "\"");
			//return invalid caller if cannot be authenticated, 
			// or does not match any record in the database
			return INVALID_CALLER;
		}
		
		//check caller authorization
		if (!UserMgmtAuthorizer.getAuthorizer()
				.isAuthorizedOnGroupSessionAuthorization(
						DAOFactory.getDAOFactoryInstance().getUserDAO()
							.getUserID(userName), 
				groupId, roleId, sessionId)) {
			logger.debug("caller \"" + userName + 
					"\" unauthorized to add group authorization");
			return UNAUTHORIZED;
		}
		
		//check subject authorization on resources in session
		List<Resource> resources = DAOFactory.getDAOFactoryInstance()
			.getResourceDAO().getResourcesBySessionID(sessionId);
		if (resources != null) for (Resource r : resources) {
			if (!UserMgmtAuthorizer.getAuthorizer()
					.groupIsAuthorizedOnResource(groupId, r.getResourceID())) {
				logger.debug("group with ID " + groupId + " cannot be " +
						"authorized on session " + sessionId + " because " +
						"they lack authorization on resource " + 
						r.getResourceID());
				return SUBJECT_UNAUTHORIZED;
			}
		}
		
		//change authorization if already set, add new otherwise
		if (DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.getAuthorizedGroups(sessionId).get(groupId) != null) {
			return DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.updateGroupAuthorization(groupId, roleId, sessionId);
		} else {
			return DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.addGroupAuthorization(groupId, roleId, sessionId);
		}
	}
	
	@Override
	public int unsetUserAuthorization(int userId, int sessionId, 
			String userName) {
		//validate input parameters
		if (userId <= 0 || sessionId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		//check caller authenticity
		if (userName == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(userName)) {
			logger.debug("remove user authorization called by invalid caller " +
					"\"" + userName + "\"");
			//return invalid caller if cannot be authenticated, 
			// or does not match any record in the database
			return INVALID_CALLER;
		}
		
		//check caller authorization
		if (!UserMgmtAuthorizer.getAuthorizer()
				.isAuthorizedOnUserSessionAuthorization(
						DAOFactory.getDAOFactoryInstance().getUserDAO()
							.getUserID(userName), 
						userId, 0, sessionId)) {
			logger.debug("caller \"" + userName + 
					"\" unauthorized to remove user authorization");
			return UNAUTHORIZED;
		}
		
		//remove user authorization
		return DAOFactory.getDAOFactoryInstance().getSessionDAO()
			.removeUserAuthorization(userId, sessionId);
	}

	@Override
	public int unsetGroupAuthorization(int groupId, int sessionId, 
			String userName) {
		//validate input parameters
		if (groupId < 0 || sessionId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		//check caller authenticity
		if (userName == null || 
				!DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(userName)) {
			logger.debug("remove group authorization called by invalid " +
					"caller \"" + userName + "\"");
			//return invalid caller if cannot be authenticated, 
			// or does not match any record in the database
			return INVALID_CALLER;
		}
		
		//check caller authorization
		if (!UserMgmtAuthorizer.getAuthorizer()
				.isAuthorizedOnGroupSessionAuthorization(
						DAOFactory.getDAOFactoryInstance().getUserDAO()
							.getUserID(userName), 
						groupId, 0, sessionId)) {
			logger.debug("caller \"" + userName + 
					"\" unauthorized to remove group authorization");
			return UNAUTHORIZED;
		}
		
		//remove group authorization
		return DAOFactory.getDAOFactoryInstance().getSessionDAO()
			.removeGroupAuthorization(groupId, sessionId);
	}
}
