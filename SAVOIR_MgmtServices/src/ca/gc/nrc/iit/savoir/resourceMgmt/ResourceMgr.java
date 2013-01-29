// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.resourceMgmt;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.WebFault;

import ca.gc.iit.nrc.savoir.domain.CredentialParameter;
import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.iit.nrc.savoir.domain.ResourcePreference;
import ca.gc.iit.nrc.savoir.domain.Session;
import ca.gc.iit.nrc.savoir.domain.types.ResourceType;
import ca.gc.nrc.iit.savoir.model.profile.ResourceWidget;
import ca.gc.nrc.iit.savoir.model.session.Action;
import ca.gc.nrc.iit.savoir.model.session.Parameter;
import ca.gc.nrc.iit.savoir.resourceMgmt.HostConnectedToMultipleEndPointsException;

/**
 * Controls edge devices and other resources.
 * In addition to keeping track of the state of edge devices allocated to 
 * sessions, and the messages to be sent to those devices, also handles 
 * network resource allocation and deallocation (through delegation to the 
 * scheduling service), as well as maintaining widget state for the UI.  
 */
@WebService
@WebFault(name = "HostConnectedToMultipleEndPointsException", 
		faultBean = "ca.gc.nrc.iit.savoir.resourceMgmt.HostConnectedToMultipleEndPointsException")
public interface ResourceMgr
{
	
	/** 
	 * Loads the resources for a given session
	 * 
	 * @param sessionId		The ID of the session to load the resources for
	 * @param userName		The user loading the session
	 */
	public void loadResourcesForSession(
			@WebParam(name = "sessionId") int sessionId, 
			@WebParam(name = "userName") String userName);
	
	/**
	 * Stops and ends session for all resources in session
	 * 
	 * @param sessionId		Session to end
	 * @param userName		Calling user
	 */
	public void endSessionForResources(
			@WebParam(name = "sessionId") int sessionId, 
			@WebParam(name = "userName") String userName);
	
	/**
	 * Clears state for all resources in session. Does not send end session 
	 * messages. Use only if resource state has become inconsistent.
	 * 
	 * @param sessionId		Session to abort
	 */
	public void abortSessionForResources(
			@WebParam(name = "sessionId") int sessionId);
	
	/**
	 * Loads a specific resource. Builds and sends "load" and "authenticate" 
	 * messages.
	 * 
	 * @param resource		The resource to start
	 * @param sessionID		The session to load it on
	 * @param userName		The user to load it for
	 * 
	 * @return true for success, false for failure
	 */
	public boolean loadEdgeDevice(
			@WebParam(name = "resource") Resource resource, 
			@WebParam(name = "sessionID") int sessionID, 
			@WebParam(name = "userName") String userName);
	
	/**
	 * Unloads a specific resource. Builds and sends "endSession" message. 
	 * 
	 * @param resource		The resource to stop
	 * @param sessionID		The session to stop it on
	 * 
	 * @return true for success, false for failure
	 */
	public boolean unloadEdgeDevice(
			@WebParam(name = "resource") Resource resource, 
			@WebParam(name = "sessionID") int sessionID);
	
	/**
	 * TODO ??? 
	 */
	public boolean loadNetworkResources(
			@WebParam(name = "session") Session session);

	/**
	 * Releases network resources for a session.
	 * 
	 * @param sessionID		The session to release resources for
	 * 
	 * @return true for success, false for failure
	 */
	public boolean unloadNetworkResources(
			@WebParam(name = "sessionID") int sessionID);
	
	/**
	 * Sends a control message to a device
	 * 
	 * @param sessionId		The session this message is on
	 * @param resourceId	The device to be controlled
	 * @param activityId	The activity being used
	 * @param action		The action to perform
	 * @param params		Optional parameters
	 * @param userId		Optional user ID to bind to
	 */
	public void controlDevice(@WebParam(name = "sessionId") int sessionId, 
			@WebParam(name = "resourceId") String resourceId, 
			@WebParam(name = "activityId") String activityId, 
			@WebParam(name = "action") String action, 
			@WebParam(name = "parameters") List<Parameter> parameters,
			@WebParam(name = "userId") String userId);
	
	/**
	 * Callback to notify the RM that a resource has changed its state.
	 * 
	 * @param sessionId		The session the resource is on
	 * @param resourceId	The ID of the resource
	 * @param activityId	The ID of the running activity
	 * @param newState		The new state (corresponds to the outgoing message 
	 * 						action that requests a change to that state)  
	 */
	public void deviceStateChanged(@WebParam(name = "sessionId") int sessionId, 
			@WebParam(name = "resourceId") int resourceId, 
			@WebParam(name = "activityId") String activityId, 
			@WebParam(name = "newState") Action newState);
	
	/**
	 * Callback to notify that a device either failed to respond to a message, 
	 * or responded with a failure.
	 * 
	 * @param sessionId		The session the resource is on
	 * @param resourceId	The ID of the resource
	 * @param activityId	The ID of the running activity
	 * @param failedAct		The action of the message that was not responded to
	 */
	public void deviceResponseFailure(
			@WebParam(name = "sessionId") int sessionId, 
			@WebParam(name = "resourceId") int resourceId, 
			@WebParam(name = "activityId") String activityId, 
			@WebParam(name = "failState") Action failedAct);
	
	/**
	 * Gets a list of all resource types
	 * 
	 * @return a list of all resource types defined on the system, empty for 
	 * 			none such, null for error
	 */
	public List<ResourceType> getResourceTypes();
	
	/**
	 * Adds a new resource type.
	 * 
	 * @param typeId			The unique String ID of the type (will fail if 
	 * 							a type with this ID already exists)
	 * @param typeName			The name of the type (human-readable, should be 
	 * 							unique, but not required to be) 
	 * @param description		A short description of this resource type
	 * @param typeClass			The resource class of this type
	 * @param userName			The calling user (must be sysadmin)
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#ALREADY_EXISTS} 
	 * 				for type with this ID already exists
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for {@code typeId} or {@code typeName} null or empty
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized (non-sysadmin) caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int addResourceType(
			@WebParam(name = "typeId") String typeId, 
			@WebParam(name = "typeName") String typeName, 
			@WebParam(name = "description") String description,
			@WebParam(name = "typeClass") String typeClass, 
			@WebParam(name = "userName") String userName);
	
	/**
	 * Gets all resources with a given resource type
	 * 
	 * @param type	The resource type of the resources to get
	 * 
	 * @return a list of resources having resource type {@code type}
	 */
	@WebResult(name = "Resource")
	public List<Resource> getResourcesByType(
			@WebParam(name = "resourceType") String type);
	
	/**
	 * Gets the registration ticket handle for a given resource
	 * 
	 * @param resourceId	The ID of the resource to get the registration 
	 * 						ticket handle for
	 * 
	 * @return a handle to the registration ticket for the resource with the 
	 * 			given ID, null for no such resource or other error
	 */
	@WebResult(name = "Ticket")
	public TicketHandle getTicketByResourceId(
			@WebParam(name = "resourceId") int resourceId);
	
	/**
	 * Gets the registration ticket handles for all resources with the given 
	 * type.
	 * 
	 * @param type			The resource type of the resources for which to get 
	 * 						registration ticket handles
	 * 
	 * @return a list of handles to registration tickets of resources having 
	 * 			the given resource type, empty for no such type or other error
	 */
	@WebResult(name = "Ticket")
	public List<TicketHandle> getTicketsByResourceType(
			@WebParam(name = "resourceType") String type);
	
	/**
	 * Submits a new device registration ticket to SAVOIR for handling. SAVOIR 
	 * will allocate a resource ID for this device, and notify its human 
	 * administrator of the pending resource registration. 
	 * 
	 * @param registrationTicket	The XML format of the registration ticket
	 * 
	 * @return	<ul>
	 * 			<li>the newly allocated resource ID of this device on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				on malformed registration ticket
	 * 			<li>&lt;0 for other error
	 * 			</ul>
	 */
	public int newResourceRegistration(
			@WebParam(name = "registrationTicket") String registrationTicket);
	
	/**
	 * Will fill in a new resource's parameters from its completed registration 
	 * ticket. Triggered by the human administrator when the registration 
	 * ticket in the pending queue has been finalized. This will notify the 
	 * device author of the completion of device registration.
	 * 
	 * @param registrationTicket	The XML format of the registration ticket
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS}
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY}
	 * 				on initial resource does not exist for completed ticket
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS}
	 * 				on malformed registration ticket
	 * 			<li>&lt;0 for other error
	 * 			</ul>
	 */
	public int completeResourceRegistration(
			@WebParam(name = "registrationTicket") String registrationTicket);
	
	/**
	 * Removes a resource from the SAVOIR system.
	 * 
	 * @param resourceId		The ID of the resource to remove
	 * @param userName			The username of the caller (will fail if this 
	 * 							is not a sysadmin user)
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for no such resource
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for {@code resourceId} <= 0
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int removeResource(
			@WebParam(name = "resourceId") int resourceId, 
			@WebParam(name = "userName") String userName);

	/**
	 * Gets the resource having the given IP address, if available
	 * 
	 * @param addresses		A list of L2 IP addresses
	 * 
	 * @return The resource having that IP address
	 * 
	 * @throws HostConnectedToMultipleEndPointsException on multiple resources 
	 * 		have this IP
	 */
	@WebResult(name = "Resource")
	public Resource getEndPointByClientIPaddresses(
			@WebParam(name = "address") List<String> addresses)
			throws HostConnectedToMultipleEndPointsException;

	/**
	 * Gets all the resources of a given type that have a parameter set to a 
	 * certain value.
	 * 
	 * @param type			The resource type of the resources to get
	 * @param parameter		The ID of the parameter under consideration
	 * @param value			The value of the parameter to consider
	 * 
	 * @return a resource having that type and that parameter set to that 
	 * 		value, if available
	 */
	@WebResult(name = "Resource")
	public Resource getResourceByTypeAndParameterValue(
			@WebParam(name = "resourceType") String type,
			@WebParam(name = "parameter") String parameter,
			@WebParam(name = "value") String value);
	
	/**
	 * Gets the resource with this ID
	 * 
	 * @param resourceID		The ID of the resource to get
	 * 
	 * @return the resource with that resource ID, if available
	 */
	@WebResult(name = "Resource")
	public Resource getResourceById(
			@WebParam(name = "resourceID") int resourceID);
	
	/**
	 * Gets all resources belonging to a session
	 * 
	 * @param sessionID		The ID of the session in question
	 * 
	 * @return a list of resources that are used in that session
	 */
	@WebResult(name = "Resource")
	public List<Resource> getResourcesBySessionID(
			@WebParam(name = "sessionId") int sessionId);
	
	/**
	 * For a resource a user is authorized to see, updates their preferred view 
	 * of it in the UI.
	 * 
	 * @param resourceId		The ID of the resource
	 * @param preference		The new preferred UI state
	 * @param userName			The name of the user
	 */
	public void updateWidgetStateForUser(
			@WebParam(name = "resourceId") int resourceId, 
			@WebParam(name = "preference") ResourcePreference preference, 
			@WebParam(name = "userName") String userName);
	
	
	/**
	 * Gets UI widgets for all resources the user is authorized to see.
	 * 
	 * @param userName			The name of the user
	 * 
	 * @return A list of widgets the user is authorized to view, null for error.
	 */
	@WebResult(name = "Widget")
	public List<ResourceWidget> getWidgetsForUser(
			@WebParam(name = "userName") String userName);
	
	/**
	 * Authorizes a user on a resource with a given credential. If the 
	 * credential is null, will use an empty credential (TODO when credential 
	 * management is split from resource authorization, we'll drop the 
	 * credential on this method, and add a check in the credential manager 
	 * about storing credentials for resources you're not authorized on) 
	 * 
	 * @param userId		The user's ID (cannot be that of the caller) 
	 * @param resourceId	The ID of the resource to be authorized
	 * @param credential	The credential to store with the authorization, 
	 * 						represented as a list of name-value pairs
	 * 						(null or empty for no device authentication needed)
	 * @param username		The username of the caller (must be a sysadmin)
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for user or resource does not exist
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for invalid {@code userId}, {@code resourceId}
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int setUserAuthorization(@WebParam(name="userId") int userId, 
			@WebParam(name="resourceId") int resourceId, 
			@WebParam(name="credential") List<CredentialParameter> credential,
			@WebParam(name = "username") String username);
	
	/**
	 * Authorizes a group on a resource with a given credential. If the 
	 * credential is null, will use an empty credential (TODO when credential 
	 * management is split from resource authorization, we'll drop the 
	 * credential on this method, and add a check in the credential manager 
	 * about storing credentials for resources you're not authorized on)
	 * 
	 * @param groupId		The group's ID. A group ID of {@code 0} represents 
	 * 						all users.
	 * @param resourceId	The ID of the resource
	 * @param credential	The credential to store with the authorization, 
	 * 						represented as a list of name-value pairs
	 * 						(null or empty for no device authentication needed)
	 * @param username		The username of the caller (must be a sysadmin)
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for group or resource does not exist
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for invalid {@code groupId}, {@code resourceId}
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int setGroupAuthorization(@WebParam(name="groupId") int groupId, 
			@WebParam(name="resourceId") int resourceId, 
			@WebParam(name="credential") List<CredentialParameter> credential,
			@WebParam(name = "username") String username);
	
	/**
	 * Removes a user's authorization on a given resource. Will eject from 
	 * storage any credentials on this resource that the user is the sole 
	 * possessor of. 
	 * 
	 * @param userId		The user's ID
	 * @param resourceId	The resource's ID
	 * @param userName		The user calling this (must be a sysadmin)
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for user or resource does not exist or user had no 
	 * 				authorization on resource
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for invalid {@code userId}, {@code resourceId}
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int unsetUserAuthorization(@WebParam(name="userId") int userId, 
			@WebParam(name="resourceId") int resourceId, 
			@WebParam(name = "userName") String userName);
	
	/**
	 * Removes a user's authorization on a given resource. Will eject from 
	 * storage any credentials on this resource that the group is the sole 
	 * possessor of.
	 * 
	 * @param groupId		The group's ID. A value of {@code 0} removes 
	 * 						authorizations that are explicitly assigned to the 
	 * 						group of all users (This is <b>not</b> all 
	 * 						authorizations assigned on this resource, but 
	 * 						rather authorizations explicitly assigned to 
	 * 						"everyone").
	 * @param resourceId	The resource's ID
	 * @param userName		The user calling this (must be sysadmin)
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for group or resource does not exist or group had no 
	 * 				authorization on resource
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for invalid {@code groupId}, {@code resourceId} 
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int unsetGroupAuthorization(@WebParam(name="groupId") int groupId, 
			@WebParam(name="resourceId") int resourceId, 
			@WebParam(name = "userName") String userName);
}
