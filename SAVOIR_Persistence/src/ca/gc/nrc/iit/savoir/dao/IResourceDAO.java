// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.iit.nrc.savoir.domain.ResourcePreference;

public interface IResourceDAO {
	
	/**
	 * Adds a new resource to the database.
	 * 
	 * @param r				The resource to add (will ignore {@code resourceID} 
	 * 						if set.
	 * 
	 * @return the resource ID of the newly allocated resource
	 */
	public int addResource(Resource r);

	/**
	 * Removes a resource from the database
	 * 
	 * @param resourceId	The ID of the resource to remove
	 * 
	 * @return {@code 1} for success, {@code 0} for no such resource, any other 
	 * 			value is a serious database error.
	 */
	public int removeResource(int resourceId);
	
	/**
	 * Updates an existing resource in the database (chosen by the given 
	 * resource's {@code resourceID} field). All parameters on the resource 
	 * will be set in the database, and any existing resource parameters will 
	 * be overwritten with the new set.
	 * 
	 * @param r				The resource to update
	 * 
	 * @return {@code 1} for success, {@code 0} for no such resource, any other 
	 * 			value is a serious database error
	 */
	public int updateResource(Resource r);

	/**
	 * Gets the resource having the given ID.
	 * 
	 * @param resourceId	ID of the resource to retrieve
	 * 
	 * @return the resource having the given ID, null for none such
	 */
	public Resource getResourceById(int resourceId);
	
	/**
	 * Gets all resources belonging to a subsession of this master session, and 
	 * having a given resource ID.
	 * 
	 * @param resourceId	The resource ID
	 * @param sessionId		The master session ID
	 * 
	 * @return all resources belonging to a subsession of this master session, 
	 * 		and having a given resource ID - null for none such
	 */
	public List<Resource> getResourceByIdAndSessionID(int resourceId, 
			int sessionId);

	/**
	 * Gets all the resources having a given resource type
	 * 
	 * @param typeID		The ID of the resource type
	 * 
	 * @return a list of all resources having the given type, null for none 
	 * 			such or error 
	 */
	public List<Resource> getResourcesByType(String typeID);

	/**
	 * Gets all the resources having a given resource type, as well as a 
	 * parameter set with the given ID and value.
	 *  
	 * @param typeID			The ID of the resource type
	 * @param paramterID		The ID of the parameter
	 * @param parameterValue	The desired value of the parameter
	 * 
	 * @return a list of all resources fulfilling this request, null for none 
	 * 			such or error
	 */
	public List<Resource> getResourcesByTypeAndParameterValue(String typeID, 
			String paramterID, String parameterValue);
	
	/**
	 * Gets all resources belonging to a subsession of this master session
	 * 
	 * @param sessionID		The master session ID
	 * 
	 * @return all resources belonging to subsessions of that master session - 
	 * 		empty list for none such
	 */
	public List<Resource> getResourcesBySessionID(int sessionID);
	
	/**
	 * Gets all subsession IDs for a resource on a session
	 * 
	 * @param resourceID	The ID of the resource
	 * @param sessionID		The ID of the master session
	 * 
	 * @return the subsession IDs for that resource - null for none such
	 */
	public List<Integer> getResourceSubsessionId(int resourceID, int sessionID);
	
	/**
	 * Gets the single resource associated with a given subsession
	 * 
	 * @param subsessionID		The ID of the subsession
	 * 
	 * @return the resource associated with that subsession
	 */
	public Resource getResourceBySubsessionId(int subsessionID);
	
	//added by Aaron 27-05-2010
	/**
	 * Adds a resource to a session.
	 * @param resourceId	The resource ID
	 * @param sessionID		The session ID
	 */
	public void addResourceToSession(int resourceId, int sessionId);
	//end added
	
	public List<Resource> getAPNReservedBetween(Calendar startTime,
			Calendar endTime);
	
	/**
	 * Get a user's UI preferences.
	 * 
	 * @param userId		The ID of the user to get preferences for
	 * 
	 * @return a mapping of resource IDs to the {@link ResourcePreference} the 
	 * 			user has for that resource
	 */
	public Map<Integer, ResourcePreference> getUserPreferences(int userId);
	
	/**
	 * Get a user's UI preference for a specific resource
	 *  
	 * @param userId		The ID of the user to get the preference for 
	 * @param resourceId	The ID of the resource to get the preference for
	 * 
	 * @return the resource preference of that user for that resource, null if 
	 * 		none such
	 */
	public ResourcePreference getUserPreferenceForResource(int userId, 
			int resourceId);
	
	/**
	 * Records a state preference for a resource in a user's UI.
	 * 
	 * @param userId		The user to add the preference for
	 * @param resourceId	The resource for which a preference is expressed
	 * @param state			The preferred UI state.
	 */
	public void addUserPreference(int userId, int resourceId, 
			ResourcePreference state);
	
	/**
	 * Updates an existing preference for a resource in a user's UI.
	 * 
	 * @param userId		The user to update the preference for
	 * @param resourceId	The resource for which a preference is expressed
	 * @param selected		The preferred UI state.
	 */
	public void updateUserPreference(int userId, int resourceId, 
			ResourcePreference state);
	
	/**
	 * Removes the record of a user's UI preference for a specific resource 
	 * 
	 * @param userId		The user to remove the preference for
	 * @param resourceId	The resource for which a preference is expressed
	 */
	public void removeUserPreference(int userId, int resourceId);
}
