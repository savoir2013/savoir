// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.credMgmt;

import java.util.Date;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import ca.gc.iit.nrc.savoir.domain.Credential;
import ca.gc.iit.nrc.savoir.domain.CredentialAuthorization;
import ca.gc.iit.nrc.savoir.domain.CredentialParameter;
import ca.gc.iit.nrc.savoir.domain.CredentialSchema;
import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.iit.nrc.savoir.domain.CredentialAuthorization.CredentialAuthorizationRight;

/**
 * Manages users' credentials on resources.
 * Any SAVOIR resource may have its own local user authentication methods 
 * (username and password, PKI-based keypair, ID number and PIN, etc.). As 
 * SAVOIR may connect many such resources in a single session, it is an 
 * undesirable burden on our end users to supply their authentication 
 * credentials to each of these resources individually. The credential manager 
 * is designed to solve this problem.
 * <p>
 * The basic sort of data the credential manager deals with is "credentials". 
 * Credentials are stored internally as name-value pairs, thus can be very 
 * flexibly defined (for information on schemas for credentials, see 
 * {@link CredSchemaValidator}). Users and groups of users are mapped to the 
 * resources they have authorizations on by the use of credentials.
 * <p>
 * The credential manager also provides functionality to manage the stored 
 * credentials themselves. Other users or groups can be given the rights to 
 * view (that is, use), update, or delete individual credentials in the 
 * keystore. This provides end users flexibility to, for instance, assign a 
 * single credential to a large group of users, but only allow a single user to 
 * change it.
 * 
 * @author Aaron Moss
 */
@WebService
public interface CredMgr {
	
	
	/**
	 * Gets the resources the caller has authorization on.
	 * 
	 * @param userName	The user name of the user requesting authorized 
	 * 					resources
	 * 
	 * @return	All resources the caller has authorization on.
	 * 			Empty list for unauthenticated caller
	 */
	@WebResult(name = "Resource")
	public List<Resource> getAuthorizedResources(
			@WebParam(name = "userName") String userName);
	
	/**
	 * Retrieves a user's credentials
	 * 
	 * @param resource			The resource to retrieve credentials for
	 * @param userName			The name of the user to retrieve credentials for
	 * 
	 * @return a list of all the credentials the user is authorized to access
	 * 			empty list for invalid resource ID, unauthenticated caller
	 */
	@WebResult(name = "Credential")
	public List<Credential> retrieveCredentials(
			@WebParam(name = "resource") int resource, 
			@WebParam(name = "userName") String userName); 
	
	/**
	 * Gets information about authorization on a credential
	 * 
	 * @param credentialId	The ID of the credential to get information about
	 * @param userName		The login ID of the user to get authorizations for
	 * 
	 * @return	the resource ID, authorized users and groups, their beginning 
	 * 			and ending times, and their access levels (should return empty 
	 * 			list for unauthorized user or invalid credential). Any 
	 * 			credential authorization with both {@code userId == 0} and 
	 * 			{@code groupId == 0} is assigned to all users.
	 */
	@WebResult(name = "CredentialAuthorization")
	public List<CredentialAuthorization> getCredentialAuthorizations(
			@WebParam(name="credentialId") int credentialId, 
			@WebParam(name = "userName") String userName);
	
	/**
	 * Stores credentials for a service
	 * 
	 * @param resource		the resource to store the credentials on
	 * @param desc			a description of the credentials (if this is null, 
	 * 						will set a default value)
	 * @param creds			the credentials to store
	 * @param beginTime		The time these credentials become valid (null for 
	 * 						"now")
	 * @param endTime		The time these credentials expire (null for "never")
	 * @param userName		The user storing credentials
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				on creds null or resource <= 0
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#PRECONDITION_ERROR} 
	 * 				on invalid resource, or endTime before beginTime
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SCHEMA_VALIDATION_FAILS} 
	 * 				for credentials fail validation (assuming there is a schema 
	 * 				stored for this resource)
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int storeCredentials(@WebParam(name="resource") int resource, 
			@WebParam(name="desc") String desc, 
			@WebParam(name="creds") List<CredentialParameter> creds,
			@WebParam(name="beginTime") Date beginTime, 
			@WebParam(name="endTime") Date endTime, 
			@WebParam(name = "userName") String userName);
	
	/**
	 * Credential storage for an aggregate of users.
	 * 
	 * @param group			the group to store for (0 for all users)
	 * @param resource		the resource to store the credentials on
	 * @param desc			a description of the credentials
	 * 						(if this is null, will set a default value)
	 * @param creds			the credentials to store
	 * @param beginTime		The time these credentials become valid (null for 
	 * 						"now")
	 * @param endTime		The time these credentials expire (null for "never")
	 * @param userName		The user storing credentials
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				on creds null or group < 0 or resource <= 0
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#PRECONDITION_ERROR} 
	 * 				on endTime before beginTime
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SCHEMA_VALIDATION_FAILS} 
	 * 				for credentials fail validation (assuming there is a schema 
	 * 				stored for this resource)
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int adminStoreCredentials(@WebParam(name="group") int group, 
			@WebParam(name="resource") int resource, 
			@WebParam(name="desc") String desc, 
			@WebParam(name="creds") List<CredentialParameter> creds, 
			@WebParam(name="beginTime") Date beginTime, 
			@WebParam(name="endTime") Date endTime, 
			@WebParam(name = "userName") String userName);
	
	/**
	 * Updates stored credentials
	 * 
	 * @param credentialId	The ID of the credentials to update
	 * @param desc			A description of the credentials (empty string for 
	 * 						no change)
	 * @param creds			The updated credentials (empty list for no change)
	 * @param userName		The user updating credentials
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				on creds null or credId <= 0
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SCHEMA_VALIDATION_FAILS} 
	 * 				for credentials fail validation (assuming there is a schema 
	 * 				stored for this resource)
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int updateCredentials(
			@WebParam(name="credentialId") int credentialId, 
			@WebParam(name="desc") String desc, 
			@WebParam(name="creds") List<CredentialParameter> creds, 
			@WebParam(name = "userName") String userName);
	
	/**
	 * Removes credentials from storage
	 * 
	 * @param credentialId	The ID of the credentials to remove
	 * @param userName		The user removing credentials
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				on credId or resource <= 0
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int removeCredentials(
			@WebParam(name="credentialId") int credentialId, 
			@WebParam(name = "userName") String userName);
	
	/**
	 * Updates rights on credentials to another user.
	 * If the user does not have an authorization on that credential set, one 
	 * is started. If the user does have an authorization on that credential 
	 * set, the new values are set. At least one of {@code userTo} and 
	 * {@code groupTo} must be equal to {@code 0}; if both are equal to 
	 * {@code 0}, the credential is granted to all users.
	 * 
	 * @param userTo		the ID of the user we are granting to (0 for none)
	 * @param groupTo		the ID of the group we are granting to (0 for none)
	 * @param resourceOn	the service we are granting the credential on
	 * @param credentialOn	the ID of the credential we are granting
	 * @param granting		the rights this user is being granted 
	 * @param beginTime		The time a rights grant becomes valid (null for now)
	 * @param endTime		The time a rights grant expires	(null for never)
	 * @param userName		The user granting the credentials
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				on {@code granting} null; {@code resourceOn} or 
	 * 				{@code credOn} <= 0; {@code userTo} or {@code groupTo} < 0
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#PRECONDITION_ERROR} 
	 * 				on {@code endTime} before {@code beginTime}, 
	 * 				{@code resourceOn} not a valid resource, both 
	 * 				{@code userTo}, {@code groupTo} != 0, {{@code UPDATE}, 
	 * 				{@code DELETE}} granted without {@code VIEW}, 
	 * 				<code>GRANT_<i>X</i></code> granted without 
	 * 				<i>{@code X}</i> (<i>{@code X}</i> one of {{@code VIEW}, 
	 * 				{@code UPDATE}, {@code DELETE}})
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int grantCredentials(@WebParam(name="userTo") int userTo, 
			@WebParam(name="groupTo") int groupTo, 
			@WebParam(name="resourceOn") int resourceOn, 
			@WebParam(name="credentialOn") int credentialOn,
			@WebParam(name="granting") List<CredentialAuthorizationRight> granting, 
			@WebParam(name="beginTime") Date beginTime, 
			@WebParam(name="endTime") Date endTime, 
			@WebParam(name = "userName") String userName);
	
	/**
	 * Removes rights on credentials from another user or group.
	 * At least one of {@code userFrom} and {@code groupFrom} must be equal to 
	 * {@code 0}; if both are equal to {@code 0}, any credentials explicitly 
	 * granted to everyone are revoked (note that this does <b>not</b> revoke 
	 * the granted credential from all users that it is granted to, but only 
	 * revokes a single grant to "all users").
	 * 
	 * @param userFrom		the ID of the user we are revoking from (0 for none)
	 * @param groupFrom		the ID of the group we are revoking from (0 for 
	 * 						none)
	 * @param resourceOn	the service we are revoking the credential on
	 * @param credentialOn	the ID of the credential we are revoking
	 * @param userName		the user revoking the credential
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				{@code resourceOn} or {@code credOn} <= 0; {@code userFrom} 
	 * 				or {@code groupFrom} < 0
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#PRECONDITION_ERROR} 
	 * 				for {@code resourceOn} not a valid resource, both 
	 * 				{@code userTo}, {@code groupTo} != 0
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int revokeCredentials(@WebParam(name="userFrom") int userFrom,
			@WebParam(name="groupFrom") int groupFrom, 
			@WebParam(name="resourceOn") int resourceOn, 
			@WebParam(name="credentialOn") int credentialOn, 
			@WebParam(name = "userName") String userName);
	
	
	/**
	 * Gets the credential schema for a resource
	 * 
	 * @param resourceId	The resource to get the schema for
	 * 
	 * @return The schema for the resource. null for no schema, resource invalid
	 */
	@WebResult(name="CredentialSchema")
	public CredentialSchema getCredentialSchemaByResource(
			@WebParam(name="resourceId") int resourceId);
}
