// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;

import ca.gc.iit.nrc.savoir.domain.Credential;
import ca.gc.iit.nrc.savoir.domain.CredentialAuthorization;
import ca.gc.iit.nrc.savoir.domain.CredentialParameter;
import ca.gc.iit.nrc.savoir.domain.CredentialSchema;
import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.iit.nrc.savoir.domain.CredentialAuthorization.CredentialAuthorizationRight;

public interface ICredentialDAO {

	/**
	 * Gets the credential corresponding to the given ID
	 * 
	 * @param credentialId		The ID of the credential to get
	 * 
	 * @return the credential having the given ID, null for no such credential or other error
	 */
	public Credential getCredentialById(int credentialId);
	
	/**
	 * Gets all the resources the given user + groups have authorizations on
	 *  
	 * @param userId	The user's ID
	 * @param groups	The groups' IDs
	 * 
	 * @return a list of authorized resources
	 */
	public List<Resource> getAuthorizedResources(int userId, Set<Integer> groups);
	
	/**
	 * Checks if a user is authorized on a resource
	 * 
	 * @param userId		The user to check authorization for
	 * @param resourceId	The resource to check authorization on
	 * 
	 * @return a list of the IDs of all credentials that user has directly on 
	 * 		that resource, empty for none such, null for error
	 */
	public List<Integer> getUserCredentials(int userId, int resourceId);
	
	/**
	 * Checks if any of a set of groups is authorized on a resource
	 * 
	 * @param groups		The groups to check authorization for
	 * @param resourceId	The resource to check authorization on
	 * 
	 * @return a list of the IDs of all credentials that any of those groups 
	 * 		have directly on that resource, empty for none such, null for error
	 */
	public List<Integer> getGroupCredentials(Set<Integer> groups, 
			int resourceId);
	
	/**
	 * Gets all the credentials the given user + groups are allowed to view on 
	 * the given resource
	 * 
	 * @param userId		The user's ID
	 * @param groups		The groups' IDs
	 * @param resourceId	The ID of the resource
	 * 
	 * @return a list of available credentials
	 */
	public List<Credential> retrieveCredentials(int userId, Set<Integer> groups, int resourceId);
	
	/**
	 * Creates a new credential
	 * 
	 * @param credential	The credential to create
	 * 
	 * @return 0 for success, !=0 for not success
	 */
	public int addCredential(Credential credential);
	
	/**
	 * Updates credentials
	 * 
	 * @param credentialId	ID of credential to update
	 * @param desc			New description, (null for keep old) 
	 * @param values		Updated credentials (null for keep old)
	 * 
	 * @return 0 for success, !=0 for not success
	 */
	public int updateCredential(int credentialId, String desc, List<CredentialParameter> values);
	
	
	/**
	 * Removes a credential
	 * 
	 * @param credentialId	The ID of the credential
	 * 
	 * @return 0 for success, != 0 for not success
	 */
	public int removeCredential(int credentialId);
	
	/**
	 * Gets all the authorizations on this credential
	 * 
	 * @param credentialId	The credential in question
	 * 
	 * @return a list of CredentialAuthorization on this credential,
	 * 			empty list for credentialId <= 0
	 */
	public List<CredentialAuthorization> getAuthorizations(int credentialId);
	
	/**
	 * Gets all the authorizations on this credential for this resource
	 * 
	 * @param credentialId	The credential in question
	 * @param resourceId	The resource in credential
	 * 
	 * @return a list of CredentialAuthorization on this credential & resource,
	 * 			empty list for credentialId <= 0
	 */
	public List<CredentialAuthorization> getAuthorizationsByResource(
			int credentialId, int resourceId);
	
	/**
	 * Assigns credentials.
	 * If both {@code userId} and {@code groupId} are 0, will authorize all 
	 * users on the credential
	 * 
	 * @param resourceId	The resource the credentials are granted on
	 * @param userId		The user granted credentials (0 for none)
	 * @param groupId		The group granted credentials (0 for none)
	 * @param credentialId	The credential being granted
	 * @param rights		The rights being granted
	 * @param beginTime		The time the granted rights become valid
	 * @param endTime		The time the granted rights expire
	 */
	public int addAuthorization(int resourceId, int userId, int groupId, 
			int credentialId, List<CredentialAuthorizationRight> rights, 
			Date beginTime, Date endTime);
	
	/**
	 * Unassigns credentials.
	 * If both {@code userId} and {@code groupId} are 0, removes authorizations 
	 * explicitly granted to everyone (note that this does <b>not</b> remove 
	 * all authorizations on this credential, but rather any single 
	 * authorization granted to "all users") 
	 * 
	 * @param resourceId	The resource the credentials are granted on
	 * @param userId		The user granted credentials (0 for none)
	 * @param groupId		The group granted credentials (0 for none)
	 * @param credentialId	The credential being granted
	 */
	public int removeAuthorization(int resourceId, int userId, int groupId, 
			int credentialId);
	
	/**
	 * @param resourceId	The ID of a resource
	 * 
	 * @return the credential schema for that resource, null for none such
	 */
	public CredentialSchema getSchemaByResource(int resourceId);
	
	/**
	 * Sets the credential schema for a given resource.
	 * 
	 * @param resourceId	The ID of the resource
	 * @param schemaId		The ID of the credential schema
	 */
	public void setCredentialSchema(int resourceId, int schemaId);
	
	/**
	 * @param credentialId	The ID of a credential
	 * 
	 * @return	a list of resource IDs associated with that credential
	 */
	public List<Integer> getResourceForCredential(int credentialId);
	
	/**
	 * @return the next credential ID in the series (the current max + 1) 
	 * 		(0 for error)
	 */
	public int getNextCredentialId();
}
