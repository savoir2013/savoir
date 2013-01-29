// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.userMgmt;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import ca.gc.iit.nrc.savoir.domain.Group;
import ca.gc.iit.nrc.savoir.domain.GroupAuthorization;
import ca.gc.iit.nrc.savoir.domain.GroupNode;
import ca.gc.iit.nrc.savoir.domain.PersonInfo;
import ca.gc.iit.nrc.savoir.domain.Role;
import ca.gc.iit.nrc.savoir.domain.Site;
import ca.gc.iit.nrc.savoir.domain.User;
import ca.gc.iit.nrc.savoir.domain.UserIDName;
import ca.gc.iit.nrc.savoir.domain.Role.Right;

/**
 * Manages users, and their groups (collectively "the user graph"), and the 
 * management rights of various users and groups on the user graph.
 * 
 * @author Aaron Moss
 */
@WebService
public interface UserMgr
{	
	
	//--------------------------------------------------------
	// User Create-Read-Update-Delete operations
	//--------------------------------------------------------
	
	/**
	 * Creates a new SAVOIR User.
	 * 
	 * @param lName		The user's surname
	 * @param fName		The user's given name
	 * @param userID	The user's login ID
	 * @param password	The user's password
	 * @param beginTime	The time the user becomes valid
	 * @param endTime	The time the user ceases to be valid
	 * @param info		A list of {@code String}s defining various contact 
	 * 					information for the user. It should be even in length, 
	 * 					such that {@code info[i]} is the field name for the 
	 * 					value in {@code info[i+1]}. All fields are optional, 
	 * 					Any member of 
	 * 					{@link ca.gc.nrc.iit.savoir.mgmtUtils.Constants.UserInfoFields} 
	 * 					is a valid field name.
	 * @param caller	The caller of this method
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#ALREADY_EXISTS} 
	 * 				for user already exists
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				<ul>
	 * 				<li>for any of the following fields null: {@code userID}, 
	 * 					{@code password}</li>
	 * 				<li>for any of the following fields empty: {@code userID}, 
	 * 					{@code lName} and {@code fName}</li>
	 * 				</ul></li>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#PRECONDITION_ERROR} 
	 * 				for {@code endTime} before {@code beginTime}
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int createUser(
			@WebParam(name="lName") String lName, 
			@WebParam(name="fName") String fName, 
			@WebParam(name="userID") String userID, 
			@WebParam(name="password") String password, 
			@WebParam(name="beginTime") Date beginTime, 
			@WebParam(name="endTime") Date endTime,
			@WebParam(name="siteID") int siteID,
			@WebParam(name="info") List<String> info,
			@WebParam(name="roleID") int roleID, 
			@WebParam(name="caller") String caller);
	
	/**
	 * Creates a new SAVOIR User
	 * 
	 * @param user		Contains the user's loginID (in the {@code DName} 
	 * 					field), password, begin and end times.
	 * @param userInfo	All optional information for the user - must include 
	 * 					non-null {@code lName} and {@code fName} fields
	 * @param caller	The caller of this method
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#ALREADY_EXISTS} 
	 * 				for user already exists
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				<ul>
	 * 				<li>for any of the following fields null: 
	 * 					{@code user.userID}, {@code user.password}</li>
	 * 				<li>for any of the following fields empty: 
	 * 					{@code user.userID}, {@code userInfo.lName} and 
	 * 					{@code userInfo.fName}</li>
	 * 				</ul></li>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#PRECONDITION_ERROR} 
	 * 				for {@code user.endTime} before {@code user.beginTime}
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int createUser1(
			@WebParam(name="user") User user, 
			@WebParam(name="userInfo") PersonInfo userInfo, 
			@WebParam(name="caller") String caller);
	
	/**
	 * Updates demographic information for a SAVOIR user
	 * 
	 * @param userID	The user's ID #
	 * @param userInfo	All new optional information for the user - fields not 
	 * 					to be updated shall be null
	 * @param caller	The caller of this method
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for no such user
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				<ul>
	 * 				<li>for {@code userID} <= 0</li>
	 * 				<li>for null {@code userInfo}, all {@code userInfo} fields 
	 * 					null</li>
	 * 				</ul></li>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#PRECONDITION_ERROR} 
	 * 				on both {@code userInfo.lName} and {@code userInfo.fName} 
	 * 				empty
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int updateUser(
			@WebParam(name="userID") int userID,
			@WebParam(name="userInfo") PersonInfo userInfo, 
			@WebParam(name="caller") String caller);
	
	/**
	 * Updates user information for a SAVOIR user.
	 * 
	 * @param userId		The user's ID #
	 * @param loginId		The user's new login ID	(null, or same as previous 
	 * 						for no change)
	 * @param password		The user's new password (null for no change)
	 * @param beginTime		The new time the user becomes valid	(same as 
	 * 						previous for no change)
	 * @param endTime		The new time the user ceases to be valid (same as 
	 * 						previous for no change)
	 * @param caller	The caller of this method
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for no such user, {@code loginId} already taken by another 
	 * 				user
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for {@code userId} <= 0
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#PRECONDITION_ERROR} 
	 * 				for {@code endTime} and {@code beginTime} != null, and 
	 * 				{@code endTime} before {@code beginTime}
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int adminUpdateUser(@WebParam(name="userId") int userId,
			@WebParam(name="loginId") String loginId,
			@WebParam(name="password") String password,
			@WebParam(name="beginTime") Date beginTime, 
			@WebParam(name="endTime") Date endTime, 
			@WebParam(name="caller") String caller);
	
	
	/**
	 * Removes a user from the SAVOIR system 
	 * 
	 * @param userID	The user's ID #
	 * @param caller	The caller of this method
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for no such user
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for {@code userID} <= 0
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int deleteUser(@WebParam(name="userID") int userID, 
			@WebParam(name="caller") String caller);
	
	/**
	 * Gets the demographic information for a given set of users
	 * 
	 * @param userID	The users' ID #'s (will return zero-size list if null)
	 * @param caller	The caller of this method
	 * 
	 * @return	the demographic information for each user, in the same order as 
	 * 			the input list. Any invalid, null, or nonpositive ID string 
	 * 			will yield a null record.
	 */
	@WebResult(name = "PersonInfo")
	public List<PersonInfo> getUserData(@WebParam(name="userID") int[] userID, 
			@WebParam(name="caller") String caller);
	
	/**
	 * Gets the name and user ID of all users the calling user is authorized to 
	 * view
	 * 
	 * @param caller	The caller of this method
	 * 
	 * @return a list of {@code UserIDName}, containing user ID, surname, and 
	 * 		given name
	 */
	@WebResult(name = "UserIDName")
	public List<UserIDName> getUsers(@WebParam(name="caller") String caller);
	
	/**
	 * Gets administrative information for all users the calling user is 
	 * authorized to see
	 * 
	 * @param caller	The caller of this method
	 * 
	 * @return a list of {@code User} objects, with the {@code password} field 
	 * 		stripped (as there is no reason to send this back to the client)
	 */
	@WebResult(name = "User")
	public List<User> adminGetUsers(@WebParam(name="caller") String caller);
	
	/**
	 * Gets the name and user ID of all users the calling user is authorized to 
	 * view and that fall within the input set
	 * 
	 * @param userID	The set of user IDs information is desired for (will 
	 * 					return empty if null)
	 * @param caller	The caller of this method
	 * 
	 * @return A list of {@code UserIDName}, containing user ID, surname, and 
	 * 		given name, as long as user ID falls inside the {@code userID} 
	 * 		parameter
	 */
	@WebResult(name = "UserIDName")
	public List<UserIDName> getUsersByID(@WebParam(name="userID") int[] userID, 
			@WebParam(name="caller") String caller);
	
	/**
	 * Gets administrative information for all users the calling user is 
	 * authorized to see that fall within the input set
	 * 
	 * @param userID	The set of user IDs information is desired for (will 
	 * 					return empty if null)
	 * @param caller	The caller of this method
	 * 
	 * @return a list of {@code User} objects, with the {@code password} field 
	 * 		stripped (as there is no reason to send this back to the client), 
	 * 		as long as user ID falls within the {@code userID} parameter
	 */
	@WebResult(name = "User")
	public List<User> adminGetUsersByID(@WebParam(name="userID") int[] userID, 
			@WebParam(name="caller") String caller);
	
	//--------------------------------------------------------
	// Group Create-Read-Update-Delete operations
	//--------------------------------------------------------
	
	/**
	 * Creates a new SAVOIR Group. 
	 * This method will also give the calling user the "admin" role (full 
	 * rights) on that group.
	 * 
	 * @param name		The name of the group to create
	 * @param desc		The description of the group to create (optional)
	 * @param caller	The caller of this method
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for {@code name} null or empty
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int createGroup(@WebParam(name="name") String name,
			@WebParam(name="desc") String desc, 
			@WebParam(name="caller") String caller);
	
	/**
	 * Creates a new SAVOIR Group. 
	 * This method will also give the calling user the "admin" role (full 
	 * rights) on that group.
	 * 
	 * @param group		The group to create (returns 
	 * 					{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 					for null)
	 * @param caller	The caller of this method
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for {@code group.name} null or empty
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int createGroup1(@WebParam(name="group") Group group, 
			@WebParam(name="caller") String caller);
	
	/**
	 * Updates information for a SAVOIR group
	 * 
	 * @param groupId	The group ID
	 * @param name		The new name of the group - null for no change
	 * @param desc		The new description of the group - null for no change
	 * @param caller	The caller of this method
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for no such group
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for invalid {@code groupId}, empty {@code name}, or both 
	 * 				{@code name} and {@code desc} null
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int updateGroup(@WebParam(name="groupId") int groupId,
			@WebParam(name="name") String name,
			@WebParam(name="desc") String desc, 
			@WebParam(name="caller") String caller);
	
	/**
	 * Removes a group from the SAVOIR system
	 * 
	 * @param groupId	The group's ID
	 * @param caller	The caller of this method
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for no such group
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for invalid {@code groupId}
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int deleteGroup(@WebParam(name="groupId") int groupId, 
			@WebParam(name="caller") String caller);
	
	/**
	 * Gets the full user graph the calling user is authorized to view
	 * 
	 * @param caller	The caller of this method
	 * 
	 * @return A graph of all groups the calling user can view, and their 
	 * 		members
	 */
	@WebResult(name = "GroupNode")
	public List<GroupNode> getGroups(@WebParam(name="caller") String caller);
	
	/**
	 * Gets the members of a group
	 * 
	 * @param groupId	The ID of the group in question (a value of 0 denotes 
	 * 					the implicit root group - the users returned will be 
	 * 					those who have no groups)
	 * @param caller	The caller of this method
	 * 
	 * @return The names and user IDs of the members of this group
	 */
	@WebResult(name = "UserIDName")
	public List<UserIDName> getMembers(@WebParam(name="groupId") int groupId, 
			@WebParam(name="caller") String caller);
	
	/**
	 * Gets the subgroups of a group
	 * 
	 * @param groupId	The ID of the group in question (a value of 0 denotes 
	 * 					the implicit root group - the groups returned will be 
	 * 					the top-level groups of the user graph)
	 * @param caller	The caller of this method
	 * 
	 * @return All subgroups of this group
	 */
	@WebResult(name = "Group")
	public List<Group> getSubgroups(@WebParam(name="groupId") int groupId, 
			@WebParam(name="caller") String caller);
	
	//--------------------------------------------------------
	// User / Group Authorization Create-Delete operations
	//--------------------------------------------------------
	
	/**
	 * Adds a user to a group with a given role.
	 * If the user already has a role on the group, will change it to this one.
	 * 
	 * @param userId		The user's ID
	 * 						(cannot be that of the caller, with the exception 
	 * 						that the caller may set or unset their own 
	 * 						membership in the group, so long as they do not
	 * 						change their role.) (caller must possess rights >= 
	 * 						both subject's original and new role if changing 
	 * 						role, and {@code UPDATE_MEMBERS} rights to add or 
	 * 						remove a user's membership)
	 * @param roleId		The role's ID
	 * @param groupId		The ID of the group to be added to
	 * @param isMember		Is the user to become a member of the group?
	 * @param beginTime		Time this authorization starts (optional)
	 * @param endTime		Time this authorization terminates (null for never)
	 * @param caller		The caller of this method
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for user, role, or group does not exist
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for invalid {@code userId}, {@code roleId}, {@code groupId}
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#PRECONDITION_ERROR} 
	 * 				for {@code endTime} before {@code beginTime}
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int setUserAuthorization(@WebParam(name="userId") int userId, 
			@WebParam(name="groupId") int groupId, 
			@WebParam(name="roleId") int roleId, 
			@WebParam(name="isMember") boolean isMember,
			@WebParam(name="beginTime") Date beginTime,
			@WebParam(name="endTime") Date endTime, 
			@WebParam(name="caller") String caller);
	
	/**
	 * Adds a group to another group with a given role.
	 * If the added group already has a role on the other group, will change it 
	 * to this one.
	 * 
	 * @param subgroupId	The added group's ID
	 * 						(if changing an existing authorization, cannot be 
	 * 						the group which grants management rights to edit 
	 * 						authorizations, and caller must possess rights >= 
	 * 						both subject's original and new role)
	 * @param roleId		The role's ID
	 * @param groupId		The ID of the group to be added to.
	 * 						(this group cannot have any authorization chain 
	 * 						which would authorize it on the added group, as 
	 * 						this would result in a cycle)
	 * @param isSubgroup	Is the added group to become a subgroup?
	 * @param caller		The caller of this method
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for subgroup, role, or group does not exist
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for invalid {@code subgroupId}, {@code roleId}, 
	 * 				{@code groupId}
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#PRECONDITION_ERROR} 
	 * 				for added group already has authorization chain from other 
	 * 				group (cycle in the graph)
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int setGroupAuthorization(@WebParam(name="subgroupId") int subgroupId, 
			@WebParam(name="groupId") int groupId,
			@WebParam(name="roleId") int roleId,
			@WebParam(name="isSubgroup")  boolean isSubgroup, 
			@WebParam(name="caller") String caller);
	
	/**
	 * Removes a user's authorization on a given group
	 * 
	 * @param userId		The user's ID (cannot be that of the caller, or 
	 * 						user with more rights than caller)
	 * @param groupId		The group's ID
	 * @param caller		The caller of this method
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for user or group does not exist or user was not member of 
	 * 				group
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for invalid {@code userId}, {@code groupId}
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int unsetUserAuthorization(@WebParam(name="userId") int userId, 
			@WebParam(name="groupId") int groupId, 
			@WebParam(name="caller") String caller);
	
	/**
	 * Removes a group's authorization on another group
	 * 
	 * @param subgroupId	The removed group's ID
	 * 						(cannot be the group which grants management rights 
	 * 						to remove authorizations, and caller must possess 
	 * 						rights >= subject)
	 * @param groupId		The other group's ID
	 * @param caller		The caller of this method
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for either group does not exist or the removed group was 
	 * 				not a member of the other group
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for invalid {@code subgroupId}, {@code groupId}
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int unsetGroupAuthorization(@WebParam(name="subgroupId") int subgroupId, 
			@WebParam(name="groupId") int groupId, 
			@WebParam(name="caller") String caller);
	
	//--------------------------------------------------------
	// Role Create-Read-Update-Delete operations
	//--------------------------------------------------------
	
	/**
	 * Creates a new SAVOIR Role.
	 * 
	 * @param roleName	The name of the new role
	 * @param rights	The rights encapsulated by this role
	 * @param desc		A short, human-readable description of this role
	 * @param caller	The caller of this method
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#ALREADY_EXISTS} 
	 * 				for role already exists,
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for {@code roleName} or {@code rights} null or empty or 
	 * 				invalid {@code groupId}
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int createRole(@WebParam(name="roleName") String roleName, 
			@WebParam(name="rights") Set<Right> rights, 
			@WebParam(name="desc") String desc, 
			@WebParam(name="caller") String caller);
	
	/**
	 * Creates a new SAVOIR Role.
	 * 
	 * @param role		The object encapsulating the new role
	 * @param caller	The caller of this method
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#ALREADY_EXISTS} 
	 * 				for role already exists,
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for {@code role.roleName} or {@code role.rights} null or 
	 * 				empty or {@code role} null
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int createRole1(@WebParam(name="role") Role role, 
			@WebParam(name="caller") String caller);
	
	/**
	 * Updates an existing Role.
	 * Note that the role used to provide the management rights to perform the 
	 * update cannot be updated.
	 * 
	 * @param roleId	The Role ID
	 * @param roleInfo	The changes - all fields should be null besides changed 
	 * 					(will only check {@code roleName}, {@code rights}, 
	 * 					{@code description})
	 * @param caller	The caller of this method
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for no such role
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for invalid {@code roleId}, null {@code roleInfo}, or all 
	 * 				{@code roleInfo} fields null
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int updateRole(@WebParam(name="roleId") int roleId, 
			@WebParam(name="roleInfo") Role roleInfo, 
			@WebParam(name="caller") String caller);
	
	/**
	 * Removes a role from the SAVOIR system.
	 * Note that this will fail if there are any users that have this role on 
	 * any object.
	 * 
	 * @param roleId	The role's ID
	 * @param caller	The caller of this method
	 * 
	 * @return	<ul>
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#SUCCESS} 
	 * 				on success
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#NO_SUCH_ENTITY} 
	 * 				for no such role,
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_PARAMETERS} 
	 * 				for invalid {@code roleId}
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#PRECONDITION_ERROR} 
	 * 				for role still has members assigned to it
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#INVALID_CALLER} 
	 * 				for unauthenticated caller
	 * 			<li>{@value ca.gc.nrc.iit.savoir.mgmtUtils.Constants#UNAUTHORIZED} 
	 * 				for unauthorized caller
	 * 			<li>!=0 for other error
	 * 			</ul>
	 */
	public int deleteRole(@WebParam(name="roleId") int roleId, 
			@WebParam(name="caller") String caller);
	
	/**
	 * Gets all the roles in the system
	 * 
	 * @return a list of roles defined on the system
	 */
	@WebResult(name = "Role")
	public List<Role> getRoles();
	
	/**
	 * Gets the roles users have on a particular group
	 * 
	 * @param groupId	The group ID 
	 * @param caller	The caller of this method
	 * 
	 * @return a map of user IDs to roles defined on that group
	 * 			empty for invalid {@code groupId}, or unauthenticated or 
	 * 			unauthorized caller
	 */
	@WebResult(name = "GroupAuthorization")
	public List<GroupAuthorization> getUserAuthorizations(
			@WebParam(name="groupId") int groupId,
			@WebParam(name="caller") String caller);
	
	/**
	 * Gets the roles subgroups have on a particular group
	 * 
	 * @param groupId	The group ID of the supergroup
	 * @param caller	The caller of this method
	 * 
	 * @return a map of group IDs to roles defined on the supergroup
	 * 			empty for invalid {@code groupId}, or unauthenticated or 
	 * 			unauthorized caller
	 */
	@WebResult(name = "GroupAuthorization")
	public List<GroupAuthorization> getGroupAuthorizations(
			@WebParam(name="groupId") int groupId,
			@WebParam(name="caller") String caller);
	
//added at 09-16-09
	/**
	 * @return list of available sites that a user could be located at
	 */
	@WebResult(name = "Site")
	public List<Site> getSites();
	
	/**
	 * Sets a user's site location
	 * 
	 * @param userName		The username of the user in question
	 * @param siteUniqDes	The unique description string of the site
	 */
	public void updateUserSite(
			@WebParam(name = "userName") String userName,
			@WebParam(name = "uniqsiteDes") String siteUniqDes);
	
	/**
	 * Gets the {@code User} object for the given username.
	 * 
	 * @param userName	The username of the user to retrieve.
	 * 
	 * @return the {@code User} object for this user, with the password 
	 * 		stripped for security.
	 */
	@WebResult(name = "User")
	public User getUserByName(@WebParam(name = "userName") String userName);

	/**
	 * Gets the user's role on the system
	 * 
	 * @param username		The username of the user to get the role for
	 * 
	 * @return The user's system role, null for unauthenticated/invalid caller
	 */
	@WebResult(name = "Role")
	public Role getUserRole(String username);
}
