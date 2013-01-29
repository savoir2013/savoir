// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.thresholdMgmt;

import ca.gc.nrc.iit.savoir.model.session.Message;

/**
 * Outgoing bus interface for SAVOIR.
 * Wraps outgoing messages from SAVOIR to the message bus, handling message sequence 
 * numbers, logging, and waiting for acknowledgements. All messages outbound 
 * from SAVOIR on the message bus should use this interface. 
 * 
 * @author Aaron Moss
 */
public interface MessageSender {

	/**
	 * Sends a launch message to MySavoir server bridge
	 * 
	 * @param sessionId		The session to launch
	 * @param serviceId		The service to launch
	 * @param userId		The user to launch for
	 * @param path			The path of the site/program to launch
	 * 
	 * @return the message ID of the sent message
	 */
	public int sendLaunchMessage(String sessionId, String serviceId, 
			String userId, String path);
	
	/**
	 * Sends a notify message to MySavoir server bridge
	 * 
	 * @param sessionId		The session to notify
	 * @param serviceId		The service to notify
	 * @param notification	The notification to send
	 * 
	 * @return the message ID of the sent message
	 */
	public int sendNotifyMessage(String sessionId, String serviceId, 
			String notification);
	
	/**
	 * Sends a message to MySavoir server bridge to subscribe to a given session feed
	 * 
	 * @param subscribeId	The session ID of the topic to subscribe to
	 * @param targetId		The session ID of the user to send the subscribe 
	 * 						message go
	 * 
	 * @return the message ID of the sent message
	 */
	public int sendSubscribeMessage(String subscribeId, String targetId);
	
	/**
	 * Sends a SAVOIR message to an edge device
	 * 
	 * @param endpoint		The edge device's address
	 * @param msg			The message to send
	 * @param log			Should this message be logged?
	 * 
	 * @return the message ID of the sent message
	 */
	public int sendMessage(String endpoint, Message msg, boolean log);
	
	/**
	 * Notify this message sender that a new session is starting. May be used 
	 * for logging, or other purposes.
	 *  
	 * @param sessionId		The ID of the new session
	 */
	public void sessionStarting(int sessionId);
	
	/**
	 * Notify this message sender that a session is ending. May be used for 
	 * logging, or other internal purposes.
	 * 
	 * @param sessionId		The ID of the ending session
	 */
	public void sessionEnding(int sessionId);
}
