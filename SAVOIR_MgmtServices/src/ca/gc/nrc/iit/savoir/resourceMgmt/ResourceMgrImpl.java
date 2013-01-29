// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.resourceMgmt;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Set;

import javax.jws.WebService;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.QueryResultsRow;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import ca.gc.iit.nrc.savoir.domain.Constraint;
import ca.gc.iit.nrc.savoir.domain.Credential;
import ca.gc.iit.nrc.savoir.domain.CredentialAuthorization;
import ca.gc.iit.nrc.savoir.domain.CredentialParameter;
import ca.gc.iit.nrc.savoir.domain.Group;
import ca.gc.iit.nrc.savoir.domain.Person;
import ca.gc.iit.nrc.savoir.domain.PersonInfo;
import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.iit.nrc.savoir.domain.ResourceParameter;
import ca.gc.iit.nrc.savoir.domain.ResourcePreference;
import ca.gc.iit.nrc.savoir.domain.Session;
import ca.gc.iit.nrc.savoir.domain.User;
import ca.gc.iit.nrc.savoir.domain.CredentialAuthorization.CredentialAuthorizationRight;
import ca.gc.iit.nrc.savoir.domain.types.ParameterType;
import ca.gc.iit.nrc.savoir.domain.types.ResourceType;
import ca.gc.nrc.iit.savoir.model.MessageTransformer;
import ca.gc.nrc.iit.savoir.model.SavoirXml;
import ca.gc.nrc.iit.savoir.model.profile.Activity;
import ca.gc.nrc.iit.savoir.model.profile.Choice;
import ca.gc.nrc.iit.savoir.model.profile.Choices;
import ca.gc.nrc.iit.savoir.model.profile.Option;
import ca.gc.nrc.iit.savoir.model.profile.ResourceWidget;
import ca.gc.nrc.iit.savoir.model.profile.ServiceProfile;
import ca.gc.nrc.iit.savoir.model.profile.Widget;
import ca.gc.nrc.iit.savoir.model.profile.Choice.ChoiceType;
import ca.gc.nrc.iit.savoir.model.registration.AuthenticationInfo;
import ca.gc.nrc.iit.savoir.model.registration.DeviceInfo;
import ca.gc.nrc.iit.savoir.model.registration.JmsEndpoint;
import ca.gc.nrc.iit.savoir.model.registration.NetworkInfo;
import ca.gc.nrc.iit.savoir.model.registration.ProtocolEndpoint;
import ca.gc.nrc.iit.savoir.model.registration.RegistrationTicket;
import ca.gc.nrc.iit.savoir.model.registration.ServiceInfo;
import ca.gc.nrc.iit.savoir.model.registration.TcpEndpoint;
import ca.gc.nrc.iit.savoir.model.session.Action;
import ca.gc.nrc.iit.savoir.model.session.Message;
import ca.gc.nrc.iit.savoir.model.session.Parameter;
import ca.gc.nrc.iit.savoir.model.session.Service;
import ca.gc.nrc.iit.savoir.scheduler.SavoirScheduler;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CancelSessionReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CancelSessionReservationResponse;
import ca.gc.nrc.iit.savoir.sessionMgmt.SessionMgr;
import ca.gc.nrc.iit.savoir.sessionMgmt.SessionMgrImpl;
import ca.gc.nrc.iit.savoir.thresholdMgmt.MessageSender;
import ca.gc.nrc.iit.savoir.userMgmt.UserMgmtAuthorizer;
import ca.gc.nrc.iit.savoir.mgmtUtils.EmailUtils;
import ca.gc.nrc.iit.savoir.mgmtUtils.ProfileUtils;
import ca.gc.nrc.iit.savoir.mgmtUtils.RegistrationUtils;
import ca.gc.nrc.iit.savoir.mgmtUtils.RuleCompiler;
import ca.gc.nrc.iit.savoir.utils.XmlUtils;
import ca.gc.nrc.iit.savoir.credMgmt.CredMgr;
import ca.gc.nrc.iit.savoir.dao.impl.DAOFactory;

import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.ALREADY_EXISTS;
import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.DATA_CORRUPTION;
import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.INVALID_CALLER;
import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.INVALID_PARAMETERS;
import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.NO_USER;
import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.NO_GROUP;
import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.NO_SUCH_ENTITY;
import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.SUCCESS;
import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.UNAUTHORIZED;

/**
 * Implements {@link ResourceMgr} by maintaining resource state and providing 
 * database access.
 * <p>
 * <b>Bean Properties:</b>
 * <table>
 * <tr>	<td>{@code credMgr}			<td>a reference to the Credential Manager
 * <tr>	<td>{@code sessionMgr}		<td>a reference to the Session Manager
 * <tr>	<td>{@code messagesSender}	<td>Outbound bus interface of this SAVOIR 
 * 										instance
 * <tr>	<td>{@code savoirScheduler}	<td>Lightpath scheduling service associated 
 * 										with this SAVOIR instance
 * </table>
 */
@WebService(endpointInterface = "ca.gc.nrc.iit.savoir.resourceMgmt.ResourceMgr")
public class ResourceMgrImpl implements ResourceMgr {
	
	private static final Logger logger = Logger.getLogger(ResourceMgrImpl.class);
	
	private ResourceBundle properties = 
		ResourceBundle.getBundle("mgmtservices", Locale.getDefault());
	
	/** ID of credential schema for no authentication */
	private final int NO_AUTHN_SCHEMA = 
		Integer.parseInt(properties.getString(
				"credentialMgr.schema.none"));
	/** ID of credential schema for no authentication */
	private final int USERNAME_PASSWORD_SCHEMA = 
		Integer.parseInt(properties.getString(
				"credentialMgr.schema.unamePword"));
	/** ID of credential schema for no authentication */
	private final int USERNAME_PASSWORD_OPT_SCHEMA = 
		Integer.parseInt(properties.getString(
				"credentialMgr.schema.unamePwordOpt"));
	/** ID of empty credential (used because CredMgr needs a credential for 
	 * 	authorization, while many resources don't */
	private final int EMPTY_CREDENTIAL_ID = 
		Integer.parseInt(properties.getString("credentialMgr.emptyCredential"));
	/** read-only rights for a credential */
	private static final List<CredentialAuthorizationRight> CRED_VIEW_RIGHTS = 
		Arrays.asList(CredentialAuthorizationRight.VIEW);
	/** full admin rights for a credential */
	private static final List<CredentialAuthorizationRight> CRED_ADMIN_RIGHTS = 
		Arrays.asList(CredentialAuthorizationRight.VIEW, 
				CredentialAuthorizationRight.UPDATE,
				CredentialAuthorizationRight.DELETE,
				CredentialAuthorizationRight.GRANT_VIEW, 
				CredentialAuthorizationRight.GRANT_UPDATE,
				CredentialAuthorizationRight.GRANT_DELETE);
	/** Date that represents any time, as a start time */
	private static final Date ANY_START_TIME = null;
	/** Date that represents never, as an end time */
	private static final Date NO_END_TIME = null;
	
	/** Resource parameter name for activity ID */
	private static final String ACTIVITY_ID = "ACTIVITY_ID";
	/** Resource parameter name for activity name */
	private static final String ACTIVITY_NAME = "ACTIVITY_NAME";
	
	/** Generalized rules governing message sequencing order */
	private KnowledgeBase messageRules = null;
	{
		try {
//			messageRules = RuleCompiler.loadRules("MessageState.drl.bin");
			InputStream ruleFile = 
				getClass().getResourceAsStream("MessageState.drl.bin");
			messageRules = RuleCompiler.genKnowledgeBase(
					RuleCompiler.readKnowledgePackages(ruleFile));
		} catch (Exception e) {
			// Can't do anything about this, and it shouldn't happen.
			logger.fatal("Message sequencing rules failed to load", e);
		}
	}
	
	/**
	 * Holds all neccessary state for a the rulebase that handles message 
	 * sequencing. This rulebase is created on a per-session basis, so all data 
	 * within this object is scoped to a single session (the ID of which is 
	 * passed in on creation)
	 */
	private class SessionState {
		/** Running rule session */
		public StatefulKnowledgeSession ksession;
		/** Instance state objects, indexed by instance ID */
		public Map<InstanceId, InstanceState> instanceStates;
		/** Handles for instance state, indexed by instance ID */
		public Map<InstanceId, FactHandle> stateHandles;
		/** resource objects for this session, indexed by instance ID */
		public Map<InstanceId, Resource> resources;
		/** subsession IDs, indexed by instance ID */
		public Map<InstanceId, Integer> subsessionIds;
		/** sequencing number for next inserted message fact */
		public int seqNum;
		/** flag for whether the session is ending or not */
		public boolean sessionEnding;
		
		public SessionState(int sessionId) {
			ksession = messageRules.newStatefulKnowledgeSession();
			
			ksession.setGlobal("sessionId", sessionId);
			ksession.setGlobal("bus", messageSender);
			ksession.setGlobal("mgmtProxy", mgmtProxy);
			
			instanceStates = new HashMap<InstanceId, InstanceState>();
			stateHandles = new HashMap<InstanceId, FactHandle>();
			resources = new HashMap<InstanceId, Resource>();
			subsessionIds = new HashMap<InstanceId, Integer>();
			
			seqNum = 0;
			sessionEnding = false;
		}
	}
	
	/**
	 * Proxy to MgmtServices for message rulebase
	 */
	public class MgmtProxy {
		
		public void notifyResourceStateChanged(int sessionId, InstanceId iId, 
				ResourceState rs) {
			SessionState state = sessions.get(sessionId);
			if (state != null) updateResourceState(state, iId, rs);
		}
		
		/**
		 * Notifies session manager that all resources for a session are loaded
		 * 
		 * @param sessionId		ID of the session
		 */
		public void notifySessionLoaded(int sessionId) {
			//notify session mgr
			sessionMgr.sessionLoaded(sessionId);
		}
		
		/**
		 * Clears state when all resources for a session have been stopped
		 * 
		 * @param sessionId		ID of the session
		 */
		public void notifySessionEnded(int sessionId) {
			//remove session state
			SessionState state = sessions.remove(sessionId);
			//cleanup rules
			if (state != null) {
				state.ksession.halt();
				state.ksession.dispose();
				state.ksession = null;
			}
			//notify session mgr
			sessionMgr.sessionEnded(sessionId);
		}
	}
	
	/** Resource state for all running sessions */
	private Map<Integer, SessionState> sessions = 
		new HashMap<Integer, SessionState>();
	
	private SavoirScheduler savoirScheduler;
	
	private CredMgr credMgr;
	
	private SessionMgr sessionMgr;
	
	private MessageSender messageSender;
	
	private MgmtProxy mgmtProxy = new MgmtProxy();
	
	@Override
	public void loadResourcesForSession(int sessionId, String userName) {
		List<Integer> subsessionIds = DAOFactory.getDAOFactoryInstance()
			.getSessionDAO().getSubsessionIds(sessionId);
		
		//do nothing for no resources to load
		if (subsessionIds == null || subsessionIds.isEmpty()) return;
		
		/* initialize session state.
		 	Will insert new state, without overwriting any existing state, if 
			there is such */
		SessionState state = getStateForSession(sessionId);
		
		//load all resources in session
		for (int subsessionId : subsessionIds) {
			Resource r = DAOFactory.getDAOFactoryInstance().getResourceDAO()
				.getResourceBySubsessionId(subsessionId);
			if (r == null) continue;
			
			//resource instance ID
			InstanceId iId = new InstanceId(r.getResourceID(), 
					r.getParameterValue(ACTIVITY_ID));
			
			//put reference to this resource in session state
			state.resources.put(iId, r);
			state.subsessionIds.put(iId, subsessionId);
			
			//send authenticate / load messages
			loadEdgeDevice(r, sessionId, userName);
		}
		
		return;
	}
	
	@Override
	public void endSessionForResources(int sessionId, String userName) {
		//get session state
		SessionState state = sessions.get(sessionId);
		
		if (state == null) return;
		
		//set state to session ending
		state.sessionEnding = true;
		state.ksession.insert(new SessionEndingFlag());
		//for every resource in that session
		for (Map.Entry<InstanceId, Resource> e : state.resources.entrySet()) {
			InstanceId iId = e.getKey();
			InstanceState iState = getInstanceState(state, iId);
			Resource r = e.getValue();
			
			//first, get device-specific username for STOP
			String deviceUserId = "";
			if (userName != null && !userName.isEmpty()) {
				List<Credential> creds = credMgr.retrieveCredentials(
						r.getResourceID(), userName);
				if (creds != null && !creds.isEmpty()) {
					deviceUserId = creds.get(0).username();
				}
			}
			
			//find subsession ID for this resource
			Integer rSsId = state.subsessionIds.get(iId);
			
			String sId = rSsId == null ? 
					genSessionId(sessionId)
					: genSessionId(sessionId, rSsId);
			
			//insert a stop message 
			// (discarded by rules if the resource is already stopped)
			Message stopMsg = new Message().withAction(Action.STOP)
				.withSessionId(sId)
				.withService(new Service()
					.withId(Integer.toString(r.getResourceID()))
					.withActivityId(iId.getActivityId())
					.withServiceUserId(deviceUserId));
			queueMessage(state, iState, r.getEndpoint(), stopMsg);
			
			//insert an end session message
			// (only sent when all resources are stopped)
			Message endSessionMsg = new Message().withAction(Action.END_SESSION)
				.withSessionId(sId)
				.withService(new Service()
					.withId(Integer.toString(r.getResourceID()))
					.withActivityId(iId.getActivityId())
					.withServiceUserId(deviceUserId));
			queueMessage(state, iState, r.getEndpoint(), endSessionMsg);
		}
		
		//fire rules
		state.ksession.fireAllRules();
		//this is a try by YYH 
		//after the final fire, dispose the session
		state.ksession.halt();
		state.ksession.dispose();
		sessions.remove(sessionId);
		//end try
	}
	
	@Override
	public void abortSessionForResources(int sessionId) {
		//kill session state
		mgmtProxy.notifySessionEnded(sessionId);
	}
	
	@Override
	public boolean loadEdgeDevice(Resource r, int sessionID, String userName) {

		int resourceId = r.getResourceID();
		String resourceName = r.getResourceName();
		String activityId = r.getParameterValue(ACTIVITY_ID);
		String activityName = r.getParameterValue(ACTIVITY_NAME);
		String endpoint = r.getEndpoint();
		
		//load credential from CM and send username and password as 
		// "authenticate" message, if available
		List<Credential> creds = credMgr.retrieveCredentials(
				resourceId, userName);
		String uname, pword;
		if (creds == null || creds.isEmpty()) {
			uname = ""; pword = "";
		} else {
			Credential c = creds.get(0);
			uname = c.username(); pword = c.password();
		}
		
		String rId = Integer.toString(resourceId);
		
		//load messaging state for session
		SessionState state = getStateForSession(sessionID);
		
		//insert new state for resource
		//- get instance ID for resource
		InstanceId iId = new InstanceId(resourceId, activityId);
		
		//-insert resource state, if not present
		InstanceState iState = getInstanceState(state, iId);
		
		//generate session ID
		Integer ssId = state.subsessionIds.get(iId);
		String sId = (ssId == null) ?
				genSessionId(sessionID)
				: genSessionId(sessionID, ssId);
		
		//populate authenticate & load messages
		Message authMsg = new Message().withAction(Action.AUTHENTICATE)
					.withSessionId(sId)
					.withService(new Service()
						.withId(rId)
						.withName(resourceName)
						.withActivityId(activityId)
						.withActivityName(activityName)
						.withServiceUserId(uname)
						.withServicePassword(pword));
		
		Message loadMsg = new Message().withAction(Action.LOAD)
					.withSessionId(sId)
					.withService(new Service()
						.withId(rId)
						.withName(resourceName)
						.withActivityId(activityId)
						.withActivityName(activityName)
						.withServiceUserId(uname));
		
		//queue messages for send
		queueMessage(state, iState, endpoint, authMsg);
		queueMessage(state, iState, endpoint, loadMsg);
		
		//trigger message sending rules
		state.ksession.fireAllRules();
		logger.info("Rule get fired YYH");
		return true;
	}
	
	/**
	 * Gets state for the given session ID, if exists, creates it if not
	 * 
	 * @param sessionId		The session ID to get state for
	 * 
	 * @return the state for that session
	 */
	private SessionState getStateForSession(int sessionId) {
		//load messaging state for session
		SessionState state = sessions.get(sessionId);
		if (state == null) {
			//no state for this session - create new state
			state = new SessionState(sessionId);
			sessions.put(sessionId, state);
		}
		return state;
	}
	
	/**
	 * Puts a new InstanceState object into the rulebase (represents an 
	 * instance of a resource), and updates the proper maps in the session 
	 * state). If there is already a state for this instance, returns that 
	 * instead, without changing it
	 * 
	 * @param state		The session state into which to insert
	 * @param iId		The instance ID of the new instance state
	 * 
	 * @return the generated state object
	 */
	private InstanceState getInstanceState(SessionState state, InstanceId iId) {
		InstanceState iState = state.instanceStates.get(iId);
		if (iState == null) {
			//no state yet - create new state (initializes to INACTIVE)
			iState = new InstanceState(iId);
			state.instanceStates.put(iId, iState);
			
			//insert new state into rulebase, keeping track of fact handle
			FactHandle fh = state.ksession.insert(iState);
			state.stateHandles.put(iId, fh);
		}
		
		return iState;
	}
	
	/**
	 * Queues a message to be transmitted when the sequencing rules hold
	 * 
	 * @param state		The session state
	 * @param iState	The state object for this instance of the resource
	 * @param endpoint	The address to send the message to
	 * @param msg		The message to send
	 */
	private void queueMessage(SessionState state, InstanceState iState, 
			String endpoint, Message msg) {
		state.ksession.insert(
				new ResourceMessage(state.seqNum++, iState, endpoint, msg));
	}
	
	/** Map of available message actions, indexed by action name */
	private static final Map<String, Action> acts =
		new HashMap<String, Action>();
	static {
		for (Action act : Action.values()) {
			acts.put(act.toString(), act);
		}
	}
	
	private static EnumSet<Action> usesServiceUserId = 
		EnumSet.of(Action.AUTHENTICATE, Action.GET_STATUS, Action.LOAD, 
				Action.PAUSE, Action.RESUME, Action.SET_PARAMETER, 
				Action.START, Action.STOP);
	
	@Override
	public void controlDevice(int sessionId, String resourceId, 
			String activityId, String action, List<Parameter> parameters,
			String userName) {
		
		//get action
		Action act = null;
		try {
			act = acts.get(action);
		} catch (Exception ignored) {}
		
		//get resource parameters
		String resourceName = null, endpoint = null, activityName = null;
		int rId = -1;
		try {
			rId = Integer.parseInt(resourceId); 
		} catch (NumberFormatException ignored) {}
		
		//get state for resource
		SessionState state = getStateForSession(sessionId);
		InstanceId iId = new InstanceId(rId, activityId);
		InstanceState iState = getInstanceState(state, iId);
		
		//get resource
		Resource resource = state.resources.get(iId);

		if (resource != null) {
			resourceName = resource.getResourceName();
			endpoint = resource.getEndpoint();
			activityName = resource.getParameterValue(ACTIVITY_NAME);
		}
		
		//get subsession ID
		Integer rSsId = state.subsessionIds.get(iId);
		
		//generate session IDs (may send multiple messages if user session ID
		// is included, and has multiple values)
		List<String> sIds;
		if (rSsId == null) {
			//no subsession ID
			sIds = Arrays.asList(genSessionId(sessionId));
		} else {
			//we do have a subsession ID
			
			//NOTE: commented out to preserve string-equality on all messages 
			// sent to the same ED instance. If, at some later point, we decide 
			// to send multiple start messages to EDs (one per user), and to 
			// give the EDs the user session ID at that time, uncomment the 
			// code below
//			if (act == Action.START) {
//				//also add user session ID to start messages
//				List<Integer> uSIds = 
//					sessionMgr.getUserSessionIdsForSubsession(sessionId, rSsId);
//				if (uSIds == null || uSIds.isEmpty()) {
//					sIds = Arrays.asList(genSessionId(sessionId));
//				} else {
//					sIds = new ArrayList<String>(uSIds.size());
//					for (int uSId : uSIds) {
//						sIds.add(genSessionId(sessionId, rSsId, uSId));
//					}
//				}
//			} else {
				sIds = Arrays.asList(genSessionId(sessionId, rSsId));
//			}
		}
		
		//construct message (for each of the possibly multiple session IDs)
		for (String sId : sIds) {
			
			Message msg = new Message().withAction(act)
			.withSessionId(sId)
			.withService(new Service()
			.withId(resourceId)
			.withName(resourceName)
			.withActivityId(activityId)
			.withActivityName(activityName));
			
			if (usesServiceUserId.contains(act)) {
				//these messages have usernames on them
				String deviceUserId = "";
				if (resource != null && userName != null && !userName.isEmpty()) {
					List<Credential> creds = credMgr.retrieveCredentials(
							resource.getResourceID(), userName);
					if (creds != null && !creds.isEmpty()) {
						deviceUserId = creds.get(0).username();
					}
				}
				
				//set username
				msg.getService().setServiceUserId(deviceUserId);
			}
			
			if (act == Action.SET_PARAMETER) {
				//set parameters for setParameter message
				msg.getService().setParameters(parameters);
			}
			
			//send message		
			queueMessage(state, iState, endpoint, msg);
		}
		
		//trigger message sending rules
		state.ksession.fireAllRules();
	}
	
	@Override
	public boolean unloadEdgeDevice(Resource r, int sessionId) {
		
		//get state for resource
		SessionState state = getStateForSession(sessionId);
		InstanceId iId = new InstanceId(r.getResourceID(), 
				r.getParameterValue(ACTIVITY_ID));
		InstanceState iState = getInstanceState(state, iId);
		Integer rSsId = state.subsessionIds.get(iId);
		
		//set state to ending
		state.sessionEnding = true;
		state.ksession.insert(new SessionEndingFlag());
		//construct end session message
		String sId = (rSsId == null) ? 
				genSessionId(sessionId) : genSessionId(sessionId, rSsId); 
		Message msg = new Message().withAction(Action.END_SESSION)
			.withSessionId(sId);
		
		//send end session message
		queueMessage(state, iState, r.getEndpoint(), msg);
		
		//trigger message sending rules
		state.ksession.fireAllRules();
		
		return true;
	}
	
	/**
	 * Generates a session ID string from an integer session ID
	 * 
	 * @param sessionId		The session ID
	 * 
	 * @return a session ID of the form sessionId-subsessionId
	 */
	private String genSessionId(int sessionId) {
		return sessionId + "-0000-0000";
	}
	
	/**
	 * Generates a session ID string from an integer session ID and 
	 * subsession ID 
	 * 
	 * @param sessionId		The session ID
	 * @param subsessionId	The subsession ID
	 * 
	 * @return a session ID of the form sessionId-subsessionId
	 */
	private String genSessionId(int sessionId, int subsessionId) {
		return sessionId + "-" + subsessionId + "-0000";
	}
	
	/**
	 * Generates a session ID string from an integer session ID,  
	 * subsession ID, and user session ID
	 * 
	 * @param sessionId		The session ID
	 * @param subsessionId	The subsession ID
	 * @param userSessionId	The user's default session ID
	 * 
	 * @return a session ID of the form sessionId-subsessionId-userSessionId
	 */
	private String genSessionId(int sessionId, int subsessionId, 
			int userSessionId) {
		return sessionId + "-" + subsessionId + "-" + userSessionId;
	}

	@Override
	public boolean loadNetworkResources(Session s) {
		//added comments at 10-04-09 this should be truely load action not activation because there was a active session operation
		return true;
		//end add
//		ActivateSessionReservation request = new ActivateSessionReservation();
//		request.setSession(s);
//		ActivateSessionReservationResponse response = savoirScheduler
//				.activateSessionReservation(request);
//		 
//		return response.isSuccessful();
	}

	@Override
	public boolean unloadNetworkResources(int sessionID) {
		CancelSessionReservation request = new CancelSessionReservation();
		Session endedSession = DAOFactory.getDAOFactoryInstance().getSessionDAO().getSessionById(sessionID);
		request.setSession(endedSession);
		CancelSessionReservationResponse response = savoirScheduler
				.cancelReservation(request);
		return response.isSuccessful();
	}
	
	/* Actions that change the resource state */
	private static Set<Action> stateChanging = 
		EnumSet.of(Action.AUTHENTICATE, Action.LOAD, Action.START, 
				Action.RESUME, Action.PAUSE, Action.STOP, Action.END_SESSION);
	
	@Override
	public void deviceStateChanged(int sessionId, int resourceId, 
			String activityId, Action newState) {
		
		//escape hatch for no change to state
		if (!stateChanging.contains(newState)) return;
			
		//load up the state for this resource, as well as the handle for its 
		// fact in the knowledge base - escape if we don't have state for this
		// session
		SessionState state = sessions.get(sessionId);
		if (state == null) {
			return;
		}
		
		InstanceId iId = new InstanceId(resourceId, activityId);
		
		//set the resource's state to the new value, and update the knowledge 
		// base
		switch (newState) {
		case AUTHENTICATE:
			updateResourceState(state, iId, ResourceState.AUTHENTICATED);
			break;
		
		case LOAD:
			updateResourceState(state, iId, ResourceState.LOADED);
			break;
		
		case START:
		case RESUME:
			updateResourceState(state, iId, ResourceState.RUNNING);
			break;
		
		case PAUSE:
			updateResourceState(state, iId, ResourceState.PAUSED);
			break;
			
		case STOP:
			updateResourceState(state, iId, ResourceState.STOPPED);
			break;
		
		case END_SESSION:
			//remove this resource from the state machine (it's done with)
			removeResourceFromState(state, iId);
			//if this is the last resource, we notify that the session is ended
			if (state.resources.isEmpty()) {
				mgmtProxy.notifySessionEnded(sessionId);
				return;	//notify session ended will null the rule reference.
			}
			break;
		}
		
		//trigger rules to act on update
		state.ksession.fireAllRules();
	}
	
	@Override
	public void deviceResponseFailure(int sessionId, int resourceId, 
			String activityId, Action failedAct) {
		
		//only care about "stop" and "endSession at the moment"
		if (!EnumSet.of(Action.STOP, Action.END_SESSION).contains(failedAct)) {
			return;
		}
		
		//get state for this session (escape if none such)
		SessionState state = sessions.get(sessionId);
		if (state == null) return;
		
		//if the session is not ending, no special handling here
		if (!state.sessionEnding) {
			return;
		}
		
		//load up the state for this resource, as well as the handle for its 
		// fact in the knowledge base
		InstanceId iId = new InstanceId(resourceId, activityId);
		
		//remove this resource from the state machine (we're ending the session 
		// and this failure notification suggests that the resource is out of 
		// it anyway)
		removeResourceFromState(state, iId);
		//if this is the last resource, we notify that the session is ended
		if (state.resources.isEmpty()) {
			mgmtProxy.notifySessionEnded(sessionId);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(state.resources.size() + " resources left in session");
			for (Resource r : state.resources.values()) {
				sb.append(" [" + r.getResourceID() + "]`" + 
						r.getResourceName() + "' left in session");
			}
		}
		
		//trigger rules to act on update
		state.ksession.fireAllRules();
	}
	
	/**
	 * Removes a resource from session state, properly handling all details
	 * 
	 * @param state		The session state to remove it from
	 * @param iId		The identifier for the resource
	 * @param iState	The state of that resource
	 * @param fh		The fact handle for that resource's state
	 */
	private void removeResourceFromState(SessionState state, InstanceId iId) {
		//setting the resource state to inactive clears the messages from the 
		// knowledge base too
		updateResourceState(state, iId, ResourceState.INACTIVE);
		
		FactHandle fh = state.stateHandles.get(iId);
		//then remove the fact for this instance state (representing the 
		// resource instance) from the knowledge base
		state.ksession.retract(fh);
		//and the resource from the list of resources on this session
		state.resources.remove(iId);
	}
	
	/**
	 * Updates the state of all messages with a given instance ID
	 * 
	 * @param state		The session state
	 * @param iId		The instance ID
	 * @param newState	The new resource state (if {@code INACTIVE}, messages 
	 * 					will be removed from knowledge base)
	 */
	private void updateResourceState(SessionState state, InstanceId iId, 
			ResourceState newState) {
		//get the instance state and its fact handle
		InstanceState iState = getInstanceState(state, iId);
		FactHandle fh = state.stateHandles.get(iId);
		
		//update the instance state
		if (iState != null && fh != null) {
			iState.setState(newState);
			state.ksession.update(fh, iState);
		}
		
		//get all the messages with this instance ID and tell them to update too
		QueryResults messages = 
			state.ksession.getQueryResults("messagesByInstaceId", 
					new Object[]{iId});
		if (messages != null) for (QueryResultsRow record : messages) {
			fh = record.getFactHandle("message");
			
			if (newState == ResourceState.INACTIVE) {
				//inactive, remove messages
				state.ksession.retract(fh);
			} else {
				//active state, update messages
				Object message = record.get("message");
				state.ksession.update(fh, message);
			}
		}
	}

	public void setSavoirScheduler(SavoirScheduler savoirScheduler) {
		this.savoirScheduler = savoirScheduler;
	}

	public SavoirScheduler getSavoirScheduler() {
		return savoirScheduler;
	}
	
	public void setMessageSender(MessageSender ms) {
		this.messageSender = ms;
	}
	
	public void setCredMgr(CredMgr cm) {
		this.credMgr = cm;
	}
	
	public void setSessionMgr(SessionMgr sm) {
		this.sessionMgr = sm;
	}
	
	@Override
	public List<ResourceType> getResourceTypes() {
		return DAOFactory.getDAOFactoryInstance().getTypesDAO()
			.getAllResourceTypes();
	}
	
	@Override
	public int addResourceType(String typeId, String typeName, 
			String description, String typeClass, String userName) {
		
		if (typeId == null || typeId.isEmpty() 
				|| typeName == null || typeName.isEmpty()) 
			return INVALID_PARAMETERS;
		
		//authorize user
		if (userName == null 
				|| !DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(userName)) {
			logger.debug(
					"add resource type called by invalid caller \"" + 
					userName + "\"");
			return INVALID_CALLER;
		}
		if (!UserMgmtAuthorizer.getAuthorizer().isSysadmin(userName)) {
			logger.debug("caller \"" + userName + "\" unauthorized to add " +
					"resource type");
			return UNAUTHORIZED;
		}
		
		//check for uniqueness of resource type
		ResourceType rt = DAOFactory.getDAOFactoryInstance().getTypesDAO()
			.getResourceTypeById(typeId);
		
		if (rt != null) {
			logger.debug("Resource type with ID \"" + typeId + "\" already " +
					"exists. Add resource type failed.");
			return ALREADY_EXISTS;
		}
		
		rt = new ResourceType(typeId, typeName, description, typeClass);
		
		//update DB
		DAOFactory.getDAOFactoryInstance().getTypesDAO().addResourceType(rt);
		
		return SUCCESS;
	}
	
	@Override
	public List<Resource> getResourcesByType(String type) {
		return DAOFactory.getDAOFactoryInstance().getResourceDAO()
				.getResourcesByType(type);
	}
	
	@Override
	public TicketHandle getTicketByResourceId(int resourceId) {
		
		return getHandleForResource(DAOFactory.getDAOFactoryInstance()
				.getResourceDAO().getResourceById(resourceId));		
	}
	
	@Override
	public List<TicketHandle> getTicketsByResourceType(String type) {
		
		List<TicketHandle> handles = new ArrayList<TicketHandle>();
		
		List<Resource> rs = DAOFactory.getDAOFactoryInstance().getResourceDAO()
				.getResourcesByType(type);
		
		if (rs != null) for (Resource r : rs) {
			TicketHandle h = getHandleForResource(r);
			if (h != null) handles.add(h);
		}
		
		return handles;
	}
	
	/**
	 * Gets the ticket handle corresponding to a given resource
	 * 
	 * @param r		The resource to get the handle for
	 * 
	 * @return the ticket handle for the given resource, or null for no ticket 
	 * 			(includes case where resource is null)
	 */
	private TicketHandle getHandleForResource(Resource r) {
		if (r == null) return null;
		
		String resourceId = Integer.toString(r.getResourceID());
		String resourceName = r.getResourceName();
		
		String localPath = 
			RegistrationUtils.getTicketPath(resourceId, resourceName);
		
		//no ticket at ticket path
		if (!new File(localPath).exists()) return null;
		
		//substitute web path for local, if applicable
		String webUri = RegistrationUtils.getWebUri(localPath);
		
		return new TicketHandle(resourceId, resourceName, webUri);
	}
	
	@Override
	public int newResourceRegistration(String xml) {
		
		//parse ticket
		RegistrationTicket ticket = null;
		Document doc = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(new InputSource(
					new StringReader(xml)));
			doc.getDocumentElement().normalize();
			
			SavoirXml obj = MessageTransformer.fromXml(doc);
			if (obj == null || !(obj instanceof RegistrationTicket)) {
				logger.debug("Invalid registration ticket\n" + xml);
				return INVALID_PARAMETERS;
			}
			ticket = (RegistrationTicket)obj;
		} catch (Exception ex) {
			logger.debug("Invalid registration ticket\n" + xml, ex);
			return INVALID_PARAMETERS;
		}
		
		ServiceInfo service = ticket.getService();
		
		if (service == null) {
			logger.debug(
					"Invalid registration ticket - service element invalid\n" 
					+ xml);
			return INVALID_PARAMETERS;
		}
		
		String resourceName = service.getName();
		if (resourceName == null || resourceName.isEmpty()) {
			logger.debug(
					"Invalid registration ticket - no resource name\n" + xml);
			return INVALID_PARAMETERS;
		}
		
		//add to database (preliminarly)
		Resource r = new Resource();
		r.setResourceName(resourceName);
		r.setDescription(service.getDescription());
		ResourceType pendingType = new ResourceType();
		pendingType.setId("REGISTRATION_PENDING");
		r.setResourceType(pendingType);
		
		logger.info("resource name = " + r.getResourceName() + " type = " + r.getResourceType().getId() );
		
		int rId = DAOFactory.getDAOFactoryInstance().getResourceDAO()
				.addResource(r);
		String resourceId = Integer.toString(rId);
		
		//add resource ID to registration ticket
		XPath xpath = XPathFactory.newInstance().newXPath();
		Node serviceNode = 
			XmlUtils.evalNode(xpath, "//newService/service", doc);
		String modifiedTicket = xml;
		if (serviceNode != null) {
			XmlUtils.setAttribute(serviceNode, "ID", resourceId);
			modifiedTicket = XmlUtils.toXmlString(doc);
		}
		
		//store registration ticket in registration repository
		RegistrationUtils.storeTicket(resourceId, resourceName, modifiedTicket);
		
		//notify SAVOIR administrator
		Map<String, String> msgVars = new HashMap<String,String>();
		msgVars.put("deviceName", resourceName);
		msgVars.put("deviceId", resourceId);
		
		String adminName = properties.getString("savoir.admin.name");
		msgVars.put("contactName", adminName);
		String adminEmail = properties.getString("savoir.admin.email");
		
		String subject = EmailUtils.message(
				properties.getString("registration.admin.subject"), msgVars);
		String msg = EmailUtils.message(
				properties.getString("registration.admin.message"), msgVars);
		
		String attachmentName = EmailUtils.message(
				properties.getString("registration.admin.attachment.name"), 
				msgVars);
		String attachmentDesc = EmailUtils.message(
				properties.getString("registration.admin.attachment.desc"), 
				msgVars);
		String attachmentPath = 
			RegistrationUtils.getTicketPath(resourceId, resourceName);
		EmailAttachment attachment = EmailUtils.attachment(
				attachmentName, attachmentDesc, attachmentPath);
		
		try {
			EmailUtils.email(adminEmail, adminName, subject, msg, attachment)
					.send();
		} catch (EmailException e) {
			logger.error("Could not notify SAVOIR admin (" + adminName + " -- " + adminEmail + ") of pending device " +
					"registration. Attachment: path = " + attachmentPath + " name = " + attachmentName + " Desc = " + attachmentDesc, e);
		}
		
		return rId;
	}

	@Override
	public int completeResourceRegistration(String xml) {

		//parse ticket
		RegistrationTicket ticket;
		try {
			SavoirXml obj = MessageTransformer.fromXml(xml);
			
			if (obj == null || !(obj instanceof RegistrationTicket)) {
				logger.debug(
						"Invalid registration ticket\n" + xml);
				return INVALID_PARAMETERS;
			}
			ticket = (RegistrationTicket)obj;
		} catch (Exception e) {
			logger.debug(
					"Invalid registration ticket\n" + xml, e);
			return INVALID_PARAMETERS;
		}
		
		//check all parameters are present on ticket
		ServiceInfo service = ticket.getService();
		if (service == null) {
			logger.debug(
					"Invalid registration ticket - service element invalid\n" 
					+ xml);
			return INVALID_PARAMETERS;
		}
		
		NetworkInfo network = ticket.getNetwork();
		if (network == null) {
			logger.debug(
					"Invalid registration ticket - network element invalid\n" 
					+ xml);
			return INVALID_PARAMETERS;
		}
		
		ProtocolEndpoint serviceEndpoint = network.getToService();
		if (serviceEndpoint == null) {
			logger.debug(
					"Invalid registration ticket - no service-bound " +
					"transport information\n" + xml);
			return INVALID_PARAMETERS;
		}
		
		ProtocolEndpoint savoirEndpoint = network.getToSavoir();
		if (savoirEndpoint == null) {
			logger.debug(
					"Invalid registration ticket - no SAVOIR-bound " +
					"transport information\n" + xml);
			return INVALID_PARAMETERS;
		}
		
		DeviceInfo device = ticket.getDevice();
		if (device == null) {
			logger.debug(
					"Invalid registration ticket = device element invalid\n" 
					+ xml);
			return INVALID_PARAMETERS;
		}
		
		String resourceId = service.getId();
		if (resourceId == null || resourceId.isEmpty()) {
			logger.debug(
					"Invalid registration ticket - no resource ID\n" + xml);
			return INVALID_PARAMETERS;
		}
		
		int rId = 0;
		try {
			rId = Integer.parseInt(resourceId);
		} catch (NumberFormatException e) {
			logger.debug(
					"Invalid registration ticket - resource ID not integer\n" 
					+ xml);
			return INVALID_PARAMETERS;
		}
		
		String resourceName = service.getName();
		if (resourceName == null || resourceName.isEmpty()) {
			logger.debug(
					"Invalid registration ticket - no resource name\n" + xml);
			return INVALID_PARAMETERS;
		}
		
		//retrieve stored resource
		Resource resource = 
			DAOFactory.getDAOFactoryInstance().getResourceDAO()
				.getResourceById(rId);
		
		if (resource == null) {
			logger.debug("No resource exists for purportedly complete " +
					"registration ticket\n" + xml);
			return NO_SUCH_ENTITY;
		}
		
		
		
		//set parameters from ticket on resource
		// (see SAVOIR_Persistence/models/docs/DB_Param_Names.txt for mapping 
		// details)
		resource.setResourceName(resourceName);
		ResourceType resourceType = new ResourceType();
		resourceType.setId(service.getType());
		resource.setResourceType(resourceType);
		resource.setDescription(service.getDescription());
		
		List<ResourceParameter> parameters = new ArrayList<ResourceParameter>();
		
		//variables used to generate the summary connection information for the 
		// email to the device author
		Map<String, String> connectionVars = new HashMap<String, String>();
		
		ResourceParameter protocol = new ResourceParameter();
		ParameterType protocolType = new ParameterType();
		protocolType.setId("PROTOCOL");
		protocol.setParameter(protocolType);
		
		switch (serviceEndpoint.getProtocol()) {
		case TCP_SOCKET:
			TcpEndpoint tcpEndpoint = (TcpEndpoint)serviceEndpoint;
			
			protocol.setValue("TCP");
			parameters.add(protocol);
			
			ResourceParameter ipAddr = new ResourceParameter();
			ParameterType ipType = new ParameterType();
			ipType.setId("SERVICE_IP_ADDRESS");
			ipAddr.setParameter(ipType);
			String ipAddrStr = tcpEndpoint.getIpAddress();
			ipAddr.setValue(ipAddrStr);
			connectionVars.put("toDeviceIpAddress", ipAddrStr);
			parameters.add(ipAddr);
			
			ResourceParameter portNum = new ResourceParameter();
			ParameterType portType = new ParameterType();
			portType.setId("SERVICE_PORT_NUMBER");
			portNum.setParameter(portType);
			String portNumStr = Integer.toString(tcpEndpoint.getPortNumber());
			portNum.setValue(portNumStr);
			connectionVars.put("toDevicePortNumber", portNumStr);
			parameters.add(portNum);
			
			break;
		
		case JMS:
			JmsEndpoint jmsEndpoint = (JmsEndpoint)serviceEndpoint;
			
			protocol.setValue("JMS");
			parameters.add(protocol);
			
			ResourceParameter jmsTopic = new ResourceParameter();
			ParameterType topicType = new ParameterType();
			topicType.setId("SERVICE_TOPIC");
			jmsTopic.setParameter(topicType);
			String jmsTopicStr = jmsEndpoint.getJmsTopic();
			jmsTopic.setValue(jmsTopicStr);
			connectionVars.put("toDeviceTopic", jmsTopicStr);
			parameters.add(jmsTopic);
			
			break;
		
		default:
			logger.debug("Unknown service transport \"" + 
					serviceEndpoint.toString() + "\" to contact edge service");
			return INVALID_PARAMETERS;
		}
		
		//generate SAVOIR connection details string for email to device 
		// author
		String savoirConnectionInfo;
		switch (savoirEndpoint.getProtocol()) {
		case TCP_SOCKET:
			TcpEndpoint tcpEndpoint = (TcpEndpoint)savoirEndpoint;
			
			connectionVars.put("toSavoirIpAddress", tcpEndpoint.getIpAddress());
			connectionVars.put("toSavoirPortNumber", 
					Integer.toString(tcpEndpoint.getPortNumber()));
			
			savoirConnectionInfo = EmailUtils.message(
					properties.getString("registration.savoirConnection.tcp"), 
					connectionVars);
			
			break;
		
		case JMS:
			JmsEndpoint jmsEndpoint = (JmsEndpoint)savoirEndpoint;
			
			connectionVars.put("connectionMethod", 
					jmsEndpoint.getConnectionMethod());
			connectionVars.put("brokerUri", jmsEndpoint.getJmsUri());
			connectionVars.put("toSavoirTopic", jmsEndpoint.getJmsTopic());

			savoirConnectionInfo = EmailUtils.message(
					properties.getString("registration.savoirConnection.jms"), 
					connectionVars);
			
			break;
		
		default:
			logger.debug("Unknown transport \"" + 
					savoirEndpoint.toString() + "\" to contact SAVOIR");
			savoirConnectionInfo = "";
		}
		
		resource.setParameters(parameters);
		
		//get person ID for contact, create if necessary
		Person contact;
		
		String contactUsername = service.getContactUser();
		if (contactUsername != null) {
			//contact is an existing SAVOIR user
			
			User contactUser = DAOFactory.getDAOFactoryInstance().getUserDAO()
					.getUserByDN(contactUsername);
			
			if (contactUser == null) {
				logger.debug("Invalid contact user ID for ED author\n" + xml);
				return INVALID_PARAMETERS;
			}
			
			contact = contactUser.getPerson();
			
		} else {
			//contact is a person that must be added to the system
			
			PersonInfo contactInfo = service.getContactInfo();
			if (contactInfo == null) {
				logger.debug("No valid contact information for ED author\n" 
						+ xml);
				return INVALID_PARAMETERS;
			}
			
			contact = new Person();
			contact.setPersonId(DAOFactory.getDAOFactoryInstance()
					.getPersonDAO().getNextPersonId());
			contact.setPersonInfo(contactInfo);
			
			DAOFactory.getDAOFactoryInstance().getPersonDAO()
					.addPerson(contact);
		}
		
		resource.setContact(contact);
		
		//get constraints from ticket, add to resource
		List<Constraint> constraints = new ArrayList<Constraint>();
		
		int maxUsers = device.getMaxSimultaneousUsers();
		if (maxUsers > 0) {
			Constraint userConstraint = new Constraint();
			
			userConstraint.setId("MAX_CONCURRENT_USERS");
			userConstraint.setResourceId(resourceId);
			userConstraint.setConfigArgs(Integer.toString(maxUsers));
			
			constraints.add(userConstraint);
		}
		
		resource.setConstraints(constraints);
		
		//get authentication type from ticket
		int credentialSchemaId;
		AuthenticationInfo deviceAuthn = device.getAuthentication();
		
		if (deviceAuthn == null) {
			logger.info("device Auth is null");
			credentialSchemaId = NO_AUTHN_SCHEMA;
		
		} else {
			logger.info("device Auth type = " + deviceAuthn.getType().toString());
			switch (deviceAuthn.getType()) {
			case USERNAME_PASSWORD:
				credentialSchemaId = 
					(deviceAuthn.isRequired()) ? 
							USERNAME_PASSWORD_SCHEMA 
							: USERNAME_PASSWORD_OPT_SCHEMA;
				break;
				
			case BLANK:
				credentialSchemaId = NO_AUTHN_SCHEMA;
				
				break;

			default:
				logger.debug("Unknown device authentication type\n" 
						+ xml);
				return INVALID_PARAMETERS;
			}
		}
		
		//update resource in DB
		DAOFactory.getDAOFactoryInstance().getResourceDAO()
			.updateResource(resource);
		DAOFactory.getDAOFactoryInstance().getCredentialDAO()
			.setCredentialSchema(rId, credentialSchemaId);
		
		//store updated registration ticket in repository
		RegistrationUtils.storeTicket(resourceId, resourceName, xml);
		
		//TODO email device contact with completed ticket
		Map<String, String> msgVars = new HashMap<String, String>();
		msgVars.put("deviceId", resourceId);
		msgVars.put("deviceName", resourceName);
		msgVars.put("deviceType", resourceType.getId());
		msgVars.put("savoirConnectionInfo", savoirConnectionInfo);
		
		PersonInfo contactInfo = contact.getPersonInfo();
		
		String contactName = 
			contactInfo.getFName() + " " + contactInfo.getLName();
		msgVars.put("contactName", contactName);
		String contactEmail = contactInfo.getEmail1();
		
		String subject = EmailUtils.message(
				properties.getString("registration.device.subject"), msgVars);
		String msg = EmailUtils.message(
				properties.getString("registration.device.message"), msgVars);
		
		String attachmentName = EmailUtils.message(
				properties.getString("registration.device.attachment.name"), 
				msgVars);
		String attachmentDesc = EmailUtils.message(
				properties.getString("registration.device.attachment.desc"), 
				msgVars);
		String attachmentPath = 
			RegistrationUtils.getTicketPath(resourceId, resourceName);
		EmailAttachment attachment = EmailUtils.attachment(
				attachmentName, attachmentDesc, attachmentPath);
		
		try {
			EmailUtils.email(
					contactEmail, contactName, subject, msg, attachment)
				.send();
		} catch (EmailException e) {
			logger.error("Could not notify device author of completed " +
					"device registration.", e);
		}
		
		return SUCCESS;
	}

	@Override
	public int removeResource(int resourceId, String userName) {
		
		//validate input
		if (resourceId <= 0) return INVALID_PARAMETERS;
		
		//authorize user
		if (userName == null 
				|| !DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(userName)) {
			logger.debug(
					"remove resource called by invalid caller \"" + 
					userName + "\"");
			return INVALID_CALLER;
		}
		if (!UserMgmtAuthorizer.getAuthorizer().isSysadmin(userName)) {
			logger.debug("caller \"" + userName + "\" unauthorized to remove " +
					"resource");
			return UNAUTHORIZED;
		}
		
		int ret = DAOFactory.getDAOFactoryInstance().getResourceDAO()
				.removeResource(resourceId);
		
		switch (ret) {
		case 1:		//1 resource removed, good
			return SUCCESS;
		case 0:		//no resources removed, assume none with that ID
			return NO_SUCH_ENTITY;
		default:	//something very strange in the DB
			return DATA_CORRUPTION;
		}
	}

	public Resource getEndPointByClientIPaddresses(List<String> addresses)
			throws HostConnectedToMultipleEndPointsException {

		List<Resource> resources = new ArrayList<Resource>();

		for (String address : addresses) {
			resources.addAll(DAOFactory.getDAOFactoryInstance()
					.getResourceDAO().getResourcesByTypeAndParameterValue(
							"LP_END_POINT", "CONNECTED_IP", address));
			resources.addAll(DAOFactory.getDAOFactoryInstance()
					.getResourceDAO().getResourcesByTypeAndParameterValue(
							"LP_END_POINT", "CONNECTED_MAC", address));
			System.out.println(resources.size());
		}

		if (resources.size() > 1) {
			throw new HostConnectedToMultipleEndPointsException();
		} else if (resources.size() == 1) {
			return resources.get(0);
		}

		return null;
	}

	@Override
	public Resource getResourceByTypeAndParameterValue(String type,
			String parameter, String value) {

		List<Resource> resources = DAOFactory.getDAOFactoryInstance()
				.getResourceDAO().getResourcesByTypeAndParameterValue(type,
						parameter, value);
		if (resources != null && resources.size() != 0)
			return resources.get(0);
		else
			return null;
	}
	
	@Override
	public Resource getResourceById(int resourceId) {
		return DAOFactory.getDAOFactoryInstance().getResourceDAO()
			.getResourceById(resourceId);
	}
	
	public List<Resource> getResourcesBySessionID(int sessionID){
		return DAOFactory.getDAOFactoryInstance()
			.getResourceDAO().getResourcesBySessionID(sessionID);
	}

	//WIDGET STUFF
	@Override
	public void updateWidgetStateForUser(int resourceId, 
			ResourcePreference preference, String userName) {
		
		//authorize user
		if (userName == null 
				|| !DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(userName)) {
			logger.debug(
					"get authorized resources called by invalid caller \"" + 
					userName + "\"");
			return;
		}
		
		int userId = DAOFactory.getDAOFactoryInstance().getUserDAO()
			.getUserID(userName);
		
		//update DB
		DAOFactory.getDAOFactoryInstance().getResourceDAO()
				.updateUserPreference(userId, resourceId, preference);
	}
	
	@Override
	public List<ResourceWidget> getWidgetsForUser(String userName) {
		// This function has a problem returning all the information back to
		// the caller - any options that are added to a choice are not
		// sent back to the caller. The reason is unknown. The work around
		// for this issue was to have the client parse the profile message
		// to obtain the widget and activities information. This also is
		// a better design since the profiles are not stored in the database.
		// Thus, management services are not required to access them
		// The result of this is that the widget code is commented out below
		
		//authorize user
		if (userName == null 
				|| !DAOFactory.getDAOFactoryInstance().getUserDAO()
					.isDName(userName)) {
			logger.debug(
					"get resources by user called by invalid caller \"" + 
					userName + "\"");
			return Collections.emptyList();
		}
		
		int userId = DAOFactory.getDAOFactoryInstance().getUserDAO()
				.getUserID(userName);
		
		//get resources that user is authorized on
		List<Resource> authorized = credMgr.getAuthorizedResources(userName);
		//get resources the user has declared a preference for
		Map<Integer, ResourcePreference> prefs = 
			DAOFactory.getDAOFactoryInstance().getResourceDAO()
				.getUserPreferences(userId);
		
		if (prefs == null) 
			prefs = new LinkedHashMap<Integer, ResourcePreference>();
		
		//list of widgets corresponding to these resources
		List<ResourceWidget> widgets = new ArrayList<ResourceWidget>();
		
		if (authorized != null) for (Resource r : authorized) {
			int resourceId = r.getResourceID();
			logger.info("Returned resource = " + resourceId);
//			String serviceId = Integer.toString(resourceId);
			String serviceName = r.getResourceName();
			
			//get profile for this resource (try next if none such)
//			ServiceProfile profile = 
//				ProfileUtils.getProfile(serviceId, serviceName);
//			if (profile == null) continue;
//			
//			logger.info("Got a profile - whoo hoo!!!!");
//			//get widget from profile (try next if none such)
//			Widget widget = profile.getWidget();
//			if (widget == null) continue;
			
			ResourceWidget rWidget = new ResourceWidget()
					.withResourceId(resourceId)
					.withResourceName(serviceName);
			
			//lookup user preference for widget
			ResourcePreference preferredState;
			if (prefs.containsKey(resourceId)) {
				//existing preference - remove from list rather than get so 
				// that preferences for unauthorized resources can be cleaned 
				// up at end
				preferredState = prefs.remove(resourceId);
			} else {
				//no existing preference - set to the default, MAXIMIZED
				preferredState = ResourcePreference.MAXIMIZED;
				DAOFactory.getDAOFactoryInstance().getResourceDAO()
					.addUserPreference(userId, resourceId, preferredState);
			}
			rWidget.setState(preferredState);
			
//			Choices choices = widget.getChoices();
//			if (choices != null) for (Choice choice : choices) {
//				//replace activity type with single-selection
//				if (ChoiceType.ACTIVITY == choice.getType()) {
//					//for each activity
//					List<Activity> acts = profile.getActivities();
//					if (acts != null) for (Activity act : acts) {
//						String name = act.getName();
//						String pValue = act.getParamValue();
//						String pId = act.getParamId();
//						
//						//take the name and parameter value, if available
//						if (name != null && !name.isEmpty() 
//								&& pValue != null && !pValue.isEmpty()) {
//							//and add an option with those values
//							choice.addOption(
//									new Option().withName(name)
//										.withParamId(pId)
//										.withParamValue(pValue));
//						}
//					}
//				}
//			}
			
			//add to list of widgets to return
			widgets.add(rWidget);
		}
		
		//remove preferences for unauthorized resources (since we removed all 
		// the authorized resources that have widgets, anything left must be 
		// unauthorized
		for (int rId : prefs.keySet()) {
			DAOFactory.getDAOFactoryInstance().getResourceDAO()
				.removeUserPreference(userId, rId);
		}
		
		return widgets;
	}
	
	@Override
	public int setUserAuthorization(int userId, int resourceId, 
			List<CredentialParameter> credential, String userName) {
		//validate input parameters
		if (userId <= 0 || resourceId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		//check that subject user and resource exist
		User user = DAOFactory.getDAOFactoryInstance().getUserDAO()
				.getUserById(userId);
		if (user == null) return NO_SUCH_ENTITY;
		
		Resource resource = DAOFactory.getDAOFactoryInstance().getResourceDAO()
				.getResourceById(resourceId);
		if (resource == null) return NO_SUCH_ENTITY;
		
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
		if (!UserMgmtAuthorizer.getAuthorizer().isSysadmin(userName)) {
			logger.debug("caller \"" + userName + 
					"\" unauthorized to add user authorization");
			return UNAUTHORIZED;
		}
		
		if (credential == null || credential.isEmpty()) {
			//no device authentication
			
			//check if user already has empty credential
			List<Integer> existingCreds = DAOFactory.getDAOFactoryInstance()
					.getCredentialDAO().getUserCredentials(userId, resourceId);
			
			boolean foundEmpty = false;
			if (existingCreds != null) for (int credId : existingCreds) {
				if (EMPTY_CREDENTIAL_ID == credId) {
					foundEmpty = true;
					break;
				}
			}
			
			//if not, authorize user with empty credential
			if (!foundEmpty) {
				DAOFactory.getDAOFactoryInstance().getCredentialDAO()
					.addAuthorization(resourceId, userId, NO_GROUP, 
							EMPTY_CREDENTIAL_ID, CRED_VIEW_RIGHTS, 
							ANY_START_TIME, NO_END_TIME);
			}
			
		} else {
			//device authentication required
			
			//insert new credential
			Credential cred = new Credential();
			int credentialId = DAOFactory.getDAOFactoryInstance()
					.getCredentialDAO().getNextCredentialId();
			cred.setCredentialId(credentialId);
			cred.setParameters(credential);
			cred.setDescription("");
			DAOFactory.getDAOFactoryInstance().getCredentialDAO()
				.addCredential(cred);
			
			//authorize user with new credential
			DAOFactory.getDAOFactoryInstance().getCredentialDAO()
				.addAuthorization(resourceId, userId, NO_GROUP, credentialId, 
						CRED_ADMIN_RIGHTS, ANY_START_TIME, NO_END_TIME);
		}
		
		return SUCCESS;
	}
	
	@Override
	public int setGroupAuthorization(int groupId, int resourceId, 
			List<CredentialParameter> credential, String userName) {
		//validate input parameters
		if (groupId < 0 || resourceId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		//check that subject group and resource exist
		Group group = DAOFactory.getDAOFactoryInstance().getGroupDAO()
				.getGroupById(groupId);
		if (group == null) return NO_SUCH_ENTITY;
		
		Resource resource = DAOFactory.getDAOFactoryInstance().getResourceDAO()
				.getResourceById(resourceId);
		if (resource == null) return NO_SUCH_ENTITY;
		
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
		if (!UserMgmtAuthorizer.getAuthorizer().isSysadmin(userName)) {
			logger.debug("caller \"" + userName + 
					"\" unauthorized to add group authorization");
			return UNAUTHORIZED;
		}
		
		if (credential == null || credential.isEmpty()) {
			//no device authentication
			
			//check if group already has empty credential
			Set<Integer> groupSet = 
				new HashSet<Integer>(Collections.singleton(groupId));
			List<Integer> existingCreds = DAOFactory.getDAOFactoryInstance()
					.getCredentialDAO().getGroupCredentials(
							groupSet, resourceId);
			
			boolean foundEmpty = false;
			if (existingCreds != null) for (int credId : existingCreds) {
				if (EMPTY_CREDENTIAL_ID == credId) {
					foundEmpty = true;
					break;
				}
			}
			
			//if not, authorize group with empty credential
			if (!foundEmpty) {
				DAOFactory.getDAOFactoryInstance().getCredentialDAO()
					.addAuthorization(resourceId, NO_USER, groupId, 
							EMPTY_CREDENTIAL_ID, CRED_VIEW_RIGHTS, 
							ANY_START_TIME, NO_END_TIME);
			}
			
		} else {
			//device authentication required
			
			//insert new credential
			Credential cred = new Credential();
			int credentialId = DAOFactory.getDAOFactoryInstance()
					.getCredentialDAO().getNextCredentialId();
			cred.setCredentialId(credentialId);
			cred.setParameters(credential);
			cred.setDescription("");
			DAOFactory.getDAOFactoryInstance().getCredentialDAO()
				.addCredential(cred);
			
			//authorize group with new credential
			DAOFactory.getDAOFactoryInstance().getCredentialDAO()
				.addAuthorization(resourceId, NO_USER, groupId, credentialId, 
						CRED_ADMIN_RIGHTS, ANY_START_TIME, NO_END_TIME);
		}
		
		return SUCCESS;
	}
	
	@Override
	public int unsetUserAuthorization(int userId, int resourceId, 
			String userName) {

		//validate input parameters
		if (userId <= 0 || resourceId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		//check that subject user and resource exist
		User user = DAOFactory.getDAOFactoryInstance().getUserDAO()
				.getUserById(userId);
		if (user == null) return NO_SUCH_ENTITY;
		
		Resource resource = DAOFactory.getDAOFactoryInstance().getResourceDAO()
				.getResourceById(resourceId);
		if (resource == null) return NO_SUCH_ENTITY;
		
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
		if (!UserMgmtAuthorizer.getAuthorizer().isSysadmin(userName)) {
			logger.debug("caller \"" + userName + 
					"\" unauthorized to remove user authorization");
			return UNAUTHORIZED;
		}
		
		//remove user authorization
		List<Integer> existingCreds = DAOFactory.getDAOFactoryInstance()
			.getCredentialDAO().getUserCredentials(userId, resourceId);
		
		if (existingCreds != null) for (int credId : existingCreds) {
			//remove user's credential authorization
			DAOFactory.getDAOFactoryInstance().getCredentialDAO()
				.removeAuthorization(resourceId, userId, NO_GROUP, credId);
			
			//remove that credential from the DB if no one else uses it
			if (credId != EMPTY_CREDENTIAL_ID) {
				List<CredentialAuthorization> stillAuthorized = 
					DAOFactory.getDAOFactoryInstance().getCredentialDAO()
						.getAuthorizations(credId);
				
				if (stillAuthorized == null || stillAuthorized.isEmpty()) {
					DAOFactory.getDAOFactoryInstance().getCredentialDAO()
						.removeCredential(credId);
				}
			}
		}
		
		return SUCCESS;		
	}

	@Override
	public int unsetGroupAuthorization(int groupId, int resourceId, 
			String userName) {
		//validate input parameters
		if (groupId < 0 || resourceId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		//check that subject group and resource exist
		Group group = DAOFactory.getDAOFactoryInstance().getGroupDAO()
				.getGroupById(groupId);
		if (group == null) return NO_SUCH_ENTITY;
		
		Resource resource = DAOFactory.getDAOFactoryInstance().getResourceDAO()
				.getResourceById(resourceId);
		if (resource == null) return NO_SUCH_ENTITY;
		
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
		if (!UserMgmtAuthorizer.getAuthorizer().isSysadmin(userName)) {
			logger.debug("caller \"" + userName + 
					"\" unauthorized to remove group authorization");
			return UNAUTHORIZED;
		}
		
		//remove group authorization
		Set<Integer> groupSet = 
			new HashSet<Integer>(Collections.singleton(groupId));
		List<Integer> existingCreds = DAOFactory.getDAOFactoryInstance()
				.getCredentialDAO().getGroupCredentials(
						groupSet, resourceId);
		
		if (existingCreds != null) for (int credId : existingCreds) {
			//remove that group's credential authorization
			DAOFactory.getDAOFactoryInstance().getCredentialDAO()
				.removeAuthorization(resourceId, NO_USER, groupId, credId);
			
			//remove that credential from the DB if no one else uses it
			if (credId != EMPTY_CREDENTIAL_ID) {
				List<CredentialAuthorization> stillAuthorized = 
					DAOFactory.getDAOFactoryInstance().getCredentialDAO()
						.getAuthorizations(credId);
				
				if (stillAuthorized == null || stillAuthorized.isEmpty()) {
					DAOFactory.getDAOFactoryInstance().getCredentialDAO()
						.removeCredential(credId);
				}
			}
		}
		
		return SUCCESS;	
	}
}
