// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.thresholdMgmt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.jws.WebService;

import org.apache.log4j.Logger;

import Services.EdgeServicesPrototype;

import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.nrc.iit.savoir.model.MessageTransformer;
import ca.gc.nrc.iit.savoir.model.SavoirXml;
import ca.gc.nrc.iit.savoir.model.profile.ServiceProfile;
import ca.gc.nrc.iit.savoir.model.session.Action;
import ca.gc.nrc.iit.savoir.model.session.Message;
import ca.gc.nrc.iit.savoir.model.session.Notification;
import ca.gc.nrc.iit.savoir.model.session.Service;
import ca.gc.nrc.iit.savoir.resourceMgmt.ResourceMgr;
import ca.gc.nrc.iit.savoir.sessionMgmt.SessionMgr;
import ca.gc.nrc.iit.savoir.utils.FileLogger;
import ca.gc.nrc.iit.savoir.utils.OverdueCollector;
import ca.gc.nrc.iit.savoir.utils.OverdueListener;
import ca.gc.nrc.iit.savoir.mgmtUtils.ProfileUtils;

/**
 * Manages SAVOIR's connection to the message bus.
 * Instances of this class are responsible for handling both incoming and 
 * outgoing messages on the SAVOIR bus. Also handles tracking acknowledgments 
 * of sent messages, and notifying the user on failure of an edge device to 
 * respond, or "{@code success="0"}" (failure) responses.
 * <p>
 * <b>Bean Properties:</b>
 * <table>
 * <tr>	<td>{@code resourceMgr}
 * 		<td>a reference to the Resource Manager
 * <tr>	<td>{@code sessionMgr}
 * 		<td>a reference to the Session Manager
 * <tr>	<td>{@code scenarioMgr}
 * 		<td>a reference to the Scenario Manager
 * <tr>	<td>{@code edgeServicesPrototype}
 * 		<td>Endpoint of service on Bus that actually routes messages
 * <tr>	<td>{@code deployment}
 * 		<td>String representing the server the management services are deployed 
 * 			on: one of "production" for the production server, or 
 * 			"development" for the development server (defaults to "production")
 * <tr>	<td>{@code maxAge}
 * 		<td>Maximum time (in ms) to keep messages that have been sent, but not 
 * 			acknowledged
 * <tr>	<td>{@code granularity}
 * 		<td>Time (in ms) to wait before running garbage collector
 * </table>
 * 
 * @author Aaron Moss
 */
@WebService(endpointInterface = "ca.gc.nrc.iit.savoir.thresholdMgmt.ThresholdMgr")
public class ThresholdMgrImpl implements ThresholdMgr, MessageSender, 
		OverdueListener<Integer> {

	private static final Logger logger = 
		Logger.getLogger(ThresholdMgrImpl.class);
	
	private ResourceBundle resources = 
		ResourceBundle.getBundle("mgmtservices", Locale.getDefault());
	
	/** Represents production server */
	private static final String PRODUCTION = "production";
	/** Represents development server */
	private static final String DEVELOPMENT = "development";
	
	private SessionMgr sessionMgr;
	private ResourceMgr resourceMgr;
	private EdgeServicesPrototype edgeServicesPrototype;
	
	/** Collects messages that have not been acked.
	 *  Stores references to message IDs, notifying this class when the 
	 *  Message corresponding to that ID is overdue for acknowledgement */
	private OverdueCollector<Integer> gc;
	
	/** Keeps a server side log of the messages to the log topic, per session */
	private final Map<Integer, FileLogger> serverSideLogs;
	/** cleans memory for session logs */
	private OverdueCollector<Integer> loggerCollector;
	
	/** server the MgmtServices are deployed on */
	private String deployment;
	
	/** Messages that have yet to be acknowledged */
	private Map<Integer, Message> unacknowledged;
	/** DEBUG start messages that haven't been acked - this is only here until 
	 *  EDs all implement spec 4.3 and include notifications on startResponse */
	private Map<Integer, Message> unackedStartDEBUG;
	
	
	public ThresholdMgrImpl() {
		unacknowledged = new HashMap<Integer, Message>();
		unackedStartDEBUG = new LinkedHashMap<Integer, Message>();
		gc = new OverdueCollector<Integer>(this, "UnackedMsgGC");
		
		serverSideLogs = new HashMap<Integer, FileLogger>();
		//clear server side loggers from memory after 2min of inactivity, 
		// sweeping every 30sec
		loggerCollector = new OverdueCollector<Integer>(
				new OverdueListener<Integer>(){
					@Override
					public void notifyOverdue(Integer obj) {
						ThresholdMgrImpl.this.serverSideLogs.remove(obj);
					}
				}, 120000, 30000, "ServerSideLoggerGC");
	}
	
	
	public void setSessionMgr(SessionMgr sessionMgr) {
		this.sessionMgr = sessionMgr;
	}
	
	public void setResourceMgr(ResourceMgr resourceMgr) {
		this.resourceMgr = resourceMgr;
	}
	
	public void setEdgeServicesPrototype(EdgeServicesPrototype edgSvrPro){
		edgeServicesPrototype = edgSvrPro;
	}
	
	public void setDeployment(String deployment) {
		this.deployment = deployment;
	}
	
	public void setMaxAge(long maxAge) {
		gc.setMaxAge(maxAge);
	}
	
	public void setGranularity(long granularity) {
		gc.setGranularity(granularity);
	}
	

	@Override
	public void handleIncoming(String message) {
		
		//parse the incoming message as a SAVOIR object
		SavoirXml xmlObj;
		try {
			xmlObj = MessageTransformer.fromXml(message);
		} catch (Exception e) {
			logger.error("Message parsing failed:*" + message + "*", e);
			return;
		}
		
		//fail on parse fail
		if (xmlObj == null) return;
		
		//this is a standard ED command & control message
		if (xmlObj.getClass() == Message.class) {
			Message msg = (Message)xmlObj;
		
			//get session ID
			Service s = msg.getService();
			sendLogMessage(getLogTopic(msg.getSessionId()), msg);
			int sessionId = parseSessionId(msg.getSessionId());
			
			//DEBUG just in case "startResponse" doesn't come with notification 
			// element
			boolean rmNotifiedDEBUG = false;
			
			//handle message acknowledgement (can come back on multiple types 
			// of message)
			Notification n = s.getNotification();
			if (n != null) {
				//message ID of acknowledged message
				int messageId = 0;
				try {
					messageId = Integer.parseInt(n.getMessageId());
				} catch (NumberFormatException e) {
					return;	//invalid input
				}
				//remove from unacknowledged cache
				Message acked = unacknowledged.remove(messageId);
				unackedStartDEBUG.remove(messageId);
				if (acked != null) {
					//withdraw from garbage collection
					gc.withdraw(messageId);
					
					//don't trust ED BI developers to correctly report
					Action ackedAct = acked.getAction();
					
					int resourceId = 0;
					try {
						resourceId = Integer.parseInt(s.getId());
					} catch (NumberFormatException ignored) {}
					String activityId = s.getActivityId();
					
					if (n.isSuccess() == false) {
						//acknowlegement failure
						handleMessageFail(acked, n.getMessage());
						
					} else /* if (n.isSuccess() == true) */ {
						//acknowledgement success
						//notify resource manager of update to resource 
						// state
						resourceMgr.deviceStateChanged(sessionId, 
								resourceId, activityId, ackedAct);
						rmNotifiedDEBUG = true;
					}
				}
			}
			
			//special handling for certain message types
			switch (msg.getAction()) {
			
			case REPORT_STATUS:
				//notify session manager of update to resource parameters
				s = msg.getService();
				if (s != null) {
					sessionMgr.updateResourceParameters(sessionId, s.getId(), 
							s.getName(), s.getActivityId(), s.getParameters());					
				}
				break;
				
			case START_RESPONSE:
				//send launch message to system tray, if applicable
				s = msg.getService();
				String path = s.getPath();
				
				if (path != null) {
					//send launch message to appropriate MySavoir instances
					sendLaunchMessage(baseSessionId(msg.getSessionId()), 
							s.getId(), s.getServiceUserId(), path);			
				}
				
				//DEBUG if RM not notified of start, notify
				if (!rmNotifiedDEBUG) {
					//this path gets into if the startResponse didn't have a 
					// notification element
					int resourceId = 0;
					try {
						resourceId = Integer.parseInt(s.getId());
					} catch (NumberFormatException ignored) {}
					String activityId = s.getActivityId();
					
					//find message ID of start message
					int messageId = 0;
					for (Map.Entry<Integer, Message> ent : unackedStartDEBUG.entrySet()) {
						Message mDBG = ent.getValue();
						Service sDBG = mDBG.getService();
						if (msg.getSessionId().equals(mDBG.getSessionId()) 
								&& s.getId().equals(sDBG.getId()) 
								&& s.getActivityId().equals(sDBG.getActivityId())) {
							//same resource
							
							//count this as an acknowledgement
							messageId = ent.getKey();
							
							break;
						}
					}
					//count this as an acknowledgement
					if (messageId != 0) {
						//remove from unacknowledged cache
						unacknowledged.remove(messageId);
						unackedStartDEBUG.remove(messageId);
						//withdraw from garbage collection
						gc.withdraw(messageId);
					}
					
					resourceMgr.deviceStateChanged(sessionId, 
							resourceId, activityId, Action.START);
					rmNotifiedDEBUG = true;
				}
				
				break;
				
			}
		} else if (xmlObj.getClass() == ServiceProfile.class) {
			//parse reportProfile message
			ServiceProfile profile = (ServiceProfile)xmlObj;
			
			//write updated profile to disk
			ProfileUtils.updateProfile(profile.getId(), profile, message);
		}
	}
	
	/** Copy this message to the logging stream */
	public static final boolean LOG = true;
	/** Do not copy this message to the logging stream */
	public static final boolean NO_LOG = false;
	
	@Override
	public int sendLaunchMessage(String sessionId, String serviceId, 
			String userId, String path) {
		
		//send to all listeners on this session
		String endpoint = sessionTopic(sessionId);
		
		//replace service/resource ID with resource type ID
		String resourceType = serviceId;
		try {
			int servId = Integer.parseInt(serviceId);
			Resource res = resourceMgr.getResourceById(servId);
			if (res != null) resourceType = res.getResourceType().getId();
		} catch (NumberFormatException ignored) {
		}
		
		//populate message
		Message msg = new Message()
				.withAction(Action.LAUNCH)
				.withSessionId(sessionId)
				.withService(new Service()
					.withId(resourceType)
					.withServiceUserId(userId)
					.withPath(path));
		
		//send message
		return sendMessage(endpoint, msg, /*NO_LOG*/ LOG /*DEBUG*/);
	}
	
	@Override
	public int sendNotifyMessage(String sessionId, String serviceId, 
			String notification) {
		
		//send to all listeners on this session
		String endpoint = sessionTopic(sessionId);
		
		//populate message
		Message msg = new Message()
			.withAction(Action.NOTIFY)
			.withSessionId(sessionId)
			.withService(new Service()
				.withId(serviceId)
				.withNotification(new Notification()
					.withMessage(notification)));
		
		//send message
		return sendMessage(endpoint, msg, /*NO_LOG*/ LOG /*DEBUG*/ );
	}
	
	@Override
	public int sendSubscribeMessage(String subscribeId, String targetId) {
		// send to target endpoint
		String endpoint = sessionTopic(targetId);
		
		//populate message
		Message msg = new Message()
			.withAction(Action.SUBSCRIBE)
			.withSessionId(subscribeId);
		
		//send message
		return sendMessage(endpoint, msg, /*NO_LOG*/ LOG /*DEBUG*/);
	}
	
	/** Current message sequence number */
	private static int seqNum = 0;
	
	/** Message actions for which an acknowledgement is expected */
	private static final Set<Action> ackExpected = 
		EnumSet.of(Action.AUTHENTICATE, Action.END_SESSION, Action.LOAD,
				Action.PAUSE, Action.RESUME, Action.SET_PARAMETER, 
				Action.STOP, Action.START);
	
	@Override
	public int sendMessage(String endpoint, Message msg, boolean log) {
		//set sequence number
		int mId = seqNum++;
		msg.setId(mId);
		
		String xml = MessageTransformer.toXml(msg);
		
		//set logging endpoint
		if (log) {
			endpoint += "," + getLogTopic(msg.getSessionId());
			getServerSideLog(msg.getSessionId()).log(xml);
		}		
		
		//send message
		toWire(endpoint, xml);
		
		//log
		logger.info("Threshold Manager Message sent is:" +  msg + "\n");
		
		//wait for acknowledgement
		if (ackExpected.contains(msg.getAction())) { 
			//put into map of un-acked messages
			unacknowledged.put(mId, msg);
			if (msg.getAction() == Action.START) unackedStartDEBUG.put(mId, msg);
			
			//submit message to overdue collector
			gc.submit(mId);
		}
		
		//return ID of sent message
		return mId;
	}
	
	/**
	 * Sends a logging message
	 * 
	 * @param endpoint	The logging topic to send to
	 * @param msg		The message to log
	 */
	public void sendLogMessage(String endpoint, Message msg) {
		String xml = MessageTransformer.toXml(msg);
		getServerSideLog(msg.getSessionId()).log(xml);
		toWire(endpoint, xml);
	}
	
	/**
	 * Serializes a message to the wire
	 * 
	 * @param endpoint	The address(es) of the message (comma-separated)
	 * @param msg		The message to send
	 */
	private final void toWire(String endpoint, String msg) {
		//compose message to edgeServicesPrototype
		String routerStr = "<router>" + endpoint + "</router>";
		String outbound = 
			"<innermessage>" + routerStr + msg + "</innermessage>"; 
		
		//send message
		edgeServicesPrototype.sendOutBoundMsg(outbound);
	}
	
	@Override
	public void sessionStarting(int sessionId) {
		//get rid of old logger, if applicable
		FileLogger oldLogger = serverSideLogs.remove(sessionId);
		if (oldLogger != null) {
			loggerCollector.withdraw(sessionId);
		}
		
		//insert new logger
		createServerSideLog(sessionId);
		//The above puts the new logger in the map, and since we'll rely on 
		// sessionEnding() to trigger its removal, we won't add it to the 
		// loggerCollector
	}
	
	@Override
	public void sessionEnding(int sessionId) {
		//submit logger for cleanup if applicable
		if (serverSideLogs.containsKey(sessionId)) { 
			loggerCollector.submit(sessionId);
		}
	}
	
	/**
	 * Gets the file logger for the log topic for a session. If no such log, 
	 * creates one.
	 * 
	 * @param sessionId			The session ID to get the log topic for
	 * 
	 * @return the file logger for that log topic
	 */
	private FileLogger getServerSideLog(String sessionId) {
		int sId = parseSessionId(sessionId);
		FileLogger logger = serverSideLogs.get(sId);
		if (logger == null) {
			//no logger for this session topic, make a new one
			createServerSideLog(sId);
			loggerCollector.submit(sId);
		} else {
			//already have a logger for this, put it to back of collection queue
			loggerCollector.touch(sId);
		}
		return logger;
	}
	
	/** timestamp format for server side log names */
	private static final DateFormat loggerTimestamp = 
		new SimpleDateFormat("yyyyMMdd-HHmmss");
	
	/**
	 * Creates a new server side log for the given session ID
	 * 
	 * @param sessionId		The session ID to create the log for
	 * 
	 * @return the created logger object
	 */
	private FileLogger createServerSideLog(int sessionId) {
		String loggerName = 
			sessionId + "_" + loggerTimestamp.format(new Date());
		String fileName = 
			resources.getString("repos.log") + loggerName + ".log";
		FileLogger logger = new FileLogger(loggerName, fileName);
		serverSideLogs.put(sessionId, logger);
		return logger;
	}
	
	/**
	 * Gets the base name of the logging topic. The session ID of the 
	 * appropriate session should be appended to this base name.
	 * 
	 * @param sid		The session ID that you want the log topic for
	 *  
	 * @return The logging topic, set appropriately for production or 
	 * 		development deployment (defaults to production)
	 */
	private String getLogTopic(String sid) {
		//first part of SID (master session ID)
		int dashInd = sid.indexOf('-');
		//SID stripped down to master session ID
		String baseSid = (dashInd == -1) ? sid : sid.substring(0, dashInd);
		
		if (PRODUCTION.equals(deployment)) {
			return "jms://topic:savoirLog" + baseSid;
		} else if (DEVELOPMENT.equals(deployment)) {
			return "jms://topic:savoirLogDev" + baseSid;
		} else {
			return "jms://topic:savoirLog" + baseSid;
		}
	}

	/**
	 * Generates the endpoint for the JMS topic for the given session
	 * 
	 * @param sid		ID of the session the topic is about
	 * 
	 * @return	JMS endpoint of the topic for that session, set appropriately 
	 * 		for production or development deployment (defaults to production)
	 */
	private String sessionTopic(String sid) {
		if (PRODUCTION.equals(deployment)) {
			return "jms://topic:savoir" + sid;
		} else if (DEVELOPMENT.equals(deployment)) {
			return "jms://topic:savoirDev" + sid;
		} else {
			return "jms://topic:savoir" + sid;
		}
	}
	
	/**
	 * @return the URI of the JMS topic for broadcast to all users, set 
	 * 		appropriately for production or development deployment (defaults to 
	 * 		production)
	 */
	private String broadcastTopic() {
		if (PRODUCTION.equals(deployment)) {
			return "jms://topic:savoirCommon";
		} else if (DEVELOPMENT.equals(deployment)) {
			return "jms://topic:savoirCommonDev";
		} else {
			return "jms://topic:savoirCommon";
		}
	}
	
	/**
	 * Gets integer session ID from a session ID string
	 * 
	 * @param sessionId		The session ID
	 * 
	 * @return the integer session ID
	 */
	private int parseSessionId(String sessionId) {
		int ind = sessionId.indexOf('-');
		String s = (ind == -1) ? sessionId : sessionId.substring(0, ind);
		
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	
	/**
	 * Gets just the base session ID from a session ID string
	 * 
	 * @param sessionId		The session ID
	 * 
	 * @return the master session ID component of that string
	 */
	private String baseSessionId(String sessionId) {
		//trim all empty parts out
		while (sessionId.endsWith("-0000")) {
			//trim trailing blanks from session ID
			sessionId = 
				sessionId.substring(0, sessionId.length() - "-0000".length());
		}
		return sessionId;
	}
	
	/**
	 * Notifies the user of an unacknowledged message.
	 * 
	 * @param messageId		The ID of the message that has not been acknowledged
	 */
	public void notifyOverdue(Integer messageId) {
		if (messageId == null) return;
		
		//retrieve unacknowledged message
		Message msg = unacknowledged.remove(messageId);
		unackedStartDEBUG.remove(messageId);
		if (msg == null) return;
		
		//failure notification string
		String notification = String.format(
				"%s failed to respond to \"%s\" message #%s", 
				activityString(msg.getService()),
				msg.getAction().toString(),
				msg.getId()
		);
		
		handleMessageFail(msg, notification);
	}
	
	/**
	 * Call when a message has either been acknowledged with success="0", or 
	 * passed the acknowledgement timeout.
	 * 
	 * @param msg				The message that failed
	 * @param notification		The notification to send to the user (optional)
	 */
	private void handleMessageFail(Message msg, String notification) {
		//get resource parameters
		int sessionId = parseSessionId(msg.getSessionId());
		Action failedAct = msg.getAction(); 

		Service serv = msg.getService();
		int resourceId = 0;
		try {
			resourceId = Integer.parseInt(serv.getId());
		} catch (NumberFormatException ignored) {}
		
		//generate notification message, if none set
		if (notification == null || notification.isEmpty()) {
			//customize for action
			switch (failedAct) {
			case LOAD:
				notification = "Session aborted. Load of"; break;
			case AUTHENTICATE:
				notification = "Session aborted. Authentication to"; break;
			case END_SESSION:
				notification = "End Session to"; break;
			case PAUSE:
				notification = "Pause of"; break;
			case RESUME:
				notification = "Resume of"; break;
			case SET_PARAMETER:
				notification = "Set Parameter to"; break;
			case STOP:
				notification = "Stop of"; break;
			case START:
				notification = "Start of"; break;
			default:
				notification = "Message to"; break;	
			}
			
			//add activity
			notification += " " + activityString(serv) + " failed.";
		}
		
		//edge device failed to load
		if (failedAct == Action.AUTHENTICATE || failedAct == Action.LOAD) {
			//abort session
			sessionMgr.endSessionPriv(sessionId);
		}
		
		//notify resource manager of failure
		resourceMgr.deviceResponseFailure(sessionId, resourceId, 
				serv.getActivityId(), msg.getAction());
		
		//notify user of failure
		sendNotifyMessage(baseSessionId(msg.getSessionId()), serv.getId(), 
				notification);
	}
	
	/**
	 * Gets an identifier string for an activity
	 * 
	 * @param serv		The service defining the activity
	 * 
	 * @return a human readable name for the activity on that service
	 */
	private static String activityString(Service serv) {
		if (serv == null) return "";
		
		String serviceName = serv.getName();
		if (serviceName == null) 
			serviceName = "service " + serv.getId();
		
		String activityName = serv.getActivityName();
		if (activityName == null) 
			activityName = "activity " + serv.getActivityId();
		
		return serviceName + "-" + activityName; 
	}
	
	protected void finalize() throws Throwable {
		super.finalize();
		gc.stop();
		loggerCollector.stop();
	}
}
