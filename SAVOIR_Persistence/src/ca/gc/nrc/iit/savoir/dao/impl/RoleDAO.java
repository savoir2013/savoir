// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.iit.nrc.savoir.domain.GroupAuthorization;
import ca.gc.iit.nrc.savoir.domain.Role;
import ca.gc.iit.nrc.savoir.domain.Role.Right;
import ca.gc.nrc.iit.savoir.dao.BaseDAO;
import ca.gc.nrc.iit.savoir.dao.IRoleDAO;
import ca.gc.nrc.iit.savoir.dao.impl.AuthorizationDAO.AuthorizationRowMapper;
import ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.IntRowMapper;

import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.NOW_IS_BETWEEN_BEGIN_AND_END_TIMES;
import static ca.gc.nrc.iit.savoir.utils.Constants.DATA_CORRUPTION;
import static ca.gc.nrc.iit.savoir.utils.Constants.INVALID_PARAMETERS;
import static ca.gc.nrc.iit.savoir.utils.Constants.NO_SUCH_ENTITY;
import static ca.gc.nrc.iit.savoir.utils.Constants.OTHER_ERROR;
import static ca.gc.nrc.iit.savoir.utils.Constants.SUCCESS;

@Transactional(readOnly = true, propagation = Propagation.REQUIRED,	isolation = Isolation.READ_COMMITTED)
@SuppressWarnings("unchecked")
public class RoleDAO extends BaseDAO implements IRoleDAO {

	/**
	 * Maps content of ROLE table to Role object
	 */
	class RoleRowMapper implements RowMapper {
    	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
    		Role role = new Role();
    		
    		int roleId = rs.getInt("ROLE_ID");
    		role.setRoleId(roleId);
    		role.setRoleName(rs.getString("ROLE_NAME"));
    		role.setRights(getRightsByRoleId(roleId));
    		role.setDescription(rs.getString("DESCRIPTION"));
    		
    		return role;
    	}
	}
	
	/**
	 * Maps RIGHT_ID to Right enum.
	 * The names of the Right enums correspond exactly to the RIGHT_IDs, 
	 * so this is just a matter of getting the enum with the specified name. 
	 */
	class RightsRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			return Right.valueOf(Right.class, rs.getString("RIGHT_ID").trim());
    	}
	}
	
	/**
	 * Maps four fields to a GroupAuthorization 
	 * the first, an integer, the ID of the group,
	 * the second, an integer, being the ID of the authorized party,
	 * the third, again an integer, the ID of a role of the authorization, 
	 * and the fourth, a boolean defining whether the authorization is a membership/subgrouping 
	 */
	class GroupAuthorizationRowMapper 
		extends AuthorizationRowMapper<GroupAuthorization> {
		
		public GroupAuthorizationRowMapper() {
			super(RoleDAO.this, GroupAuthorization.class);
		}
		
		@Override
		protected void addInfo(ResultSet rs, int rowNum, 
				GroupAuthorization ga) throws SQLException {
			ga.setContained(rs.getBoolean(4));
		}
		
		
	}
	
	@Transactional(readOnly = false)
	@Override
	public void addRole(Role r) {
		String sql = "INSERT INTO SAVOIR.ROLE (ROLE_ID, ROLE_NAME, DESCRIPTION) VALUES (?,?,?)";
    	Object[] args = new Object[]{r.getRoleId(), r.getRoleName(), r.getDescription()};
    	int[] argTypes = new int[]{Types.INTEGER, Types.CHAR, Types.VARCHAR};
    	this.template.update(sql, args, argTypes);
    	
    	setRights(r.getRoleId(), r.getRights());
	}

	@Transactional(readOnly = false)
	@Override
	public void removeRole(int roleId) {
		String sql = "DELETE FROM SAVOIR.ROLE where ROLE_ID = ?";
		Object[] args = new Object[] {roleId};
		this.template.update(sql, args);
	}
	
	@Transactional(readOnly = false)
	@Override
	public int updateRole(int roleId, Role ri) {
		if (roleId <= 0 || ri == null) {
			return INVALID_PARAMETERS;
		}
		
		//build SQL query
		//initialize query and argument list
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("UPDATE ROLE SET ");
		Vector<Object> argBuilder = new Vector<Object>();
		
		//for each field, check if non-null, and, if so, add to query and arg list
		String t;
		if ((t = ri.getRoleName()) != null) {
			sqlBuilder.append("ROLE_NAME = ?,");
			argBuilder.addElement(t);
		}
		if ((t = ri.getDescription()) != null) {
			sqlBuilder.append("DESCRIPTION = ?,");
			argBuilder.addElement(t);
		}
		
		//trim final comma
		int lastIndex = sqlBuilder.length() - 1;
		if (sqlBuilder.charAt(lastIndex) == ',') {
			sqlBuilder.deleteCharAt(lastIndex);
		}
		
		//finalize query and argument list
		sqlBuilder.append(" WHERE ROLE_ID = ?");
		argBuilder.addElement(roleId);
		
		//reformat query and arguments
		String sql = sqlBuilder.toString();
		Object[] args = argBuilder.toArray(new Object[argBuilder.size()]);
		
		try {
			int updated = template.update(sql, args);
			switch (updated) {
			case 1:		//a single record updated - the correct behaviour
				break;
			case 0:		//no records updated (ergo no records match role ID / group ID)
				return NO_SUCH_ENTITY;
			default:	//multiple records updated (should not happen - means data corruption)
				log.fatal(updated + " records updated on query \"" + sql + "\".\n" +
							"Data corruption likely.");
				return DATA_CORRUPTION;
			}
		} catch (DataAccessException e) {
			log.error("Persistence error on role update", e);
			return OTHER_ERROR;
		}
		
		setRights(roleId, ri.getRights());
		
		return SUCCESS;
	}
	
	/**
	 * Sets the given rights on the given role in the DB
	 * @param roleId	The role to set the rights on
	 * @param rights	The rights to set
	 */
	@Transactional(readOnly = false)
	private void setRights(int roleId, Set<Right> rights) {
		if (!rights.isEmpty()) {
			StringBuilder sqlBuilder = new StringBuilder();
			sqlBuilder.append("INSERT INTO SAVOIR.ROLE_RIGHTS (ROLE_ID, RIGHT_ID) VALUES (?,?)");
			int numArgs = rights.size() * 2;
			Object[] args = new Object[numArgs];
			int[] argTypes = new int[numArgs];
			
			Iterator<Right> iter = rights.iterator();
			Right right = iter.next();
			
			args[0] = roleId;
			args[1] = right.name();
			argTypes[0] = Types.INTEGER;
			argTypes[1] = Types.CHAR;
			
			for (int i = 2; i < numArgs; i += 2) {
				right = iter.next();
				
				sqlBuilder.append(", (?,?)");
				args[i] = roleId;
				args[i+1] = right.name();
				argTypes[i] = Types.INTEGER;
	    		argTypes[i+1] = Types.CHAR;
			}
			
			this.template.update(sqlBuilder.toString(), args, argTypes);
		}
	}
	
	@Override
	public Role getRoleById(int roleId) {
    	String sql = "SELECT * FROM ROLE WHERE ROLE_ID = ?";
    	Object[] args = new Object[]{roleId};
    	try {
    		Role r;
    		r = (Role) this.template.queryForObject(sql, args, new RoleRowMapper());
    		return r;
    	}catch (EmptyResultDataAccessException e){
    		return null;
    	}
	}
	
	@Override
	public Set<Integer> getUsersByRole(int roleId, int groupId) {
		String sql = "SELECT USER_ID FROM USER_GROUP WHERE ROLE_ID = ? AND GROUP_ID = ? AND "
			+ NOW_IS_BETWEEN_BEGIN_AND_END_TIMES;
		Object[] args = new Object[]{roleId, groupId};
		
		try {
			return new HashSet<Integer>(template.query(sql, args, new IntRowMapper()));
		}  catch (DataAccessException e) {
			log.error("Persistence error on users by role search", e);
			return null;
		}
	}
	
	@Override
	public Set<Integer> getGroupsByRole(int roleId, int groupId) {
		String sql = "SELECT SUBGROUP_ID FROM SUBGROUP_GROUP WHERE ROLE_ID = ? AND GROUP_ID = ?";
		Object[] args = new Object[]{roleId, groupId};
		
		try {
			return new HashSet<Integer>(template.query(sql, args, new IntRowMapper()));
		}  catch (DataAccessException e) {
			log.error("Persistence error on groups by role search", e);
			return null;
		}
	}
	
	@Override
	public List<Role> getRoles() {
		String sql = "SELECT * FROM ROLE";
		
		try {
			return (List<Role>) template.query(sql, new RoleRowMapper());
		}  catch (DataAccessException e) {
			log.error("Persistence error on role lookup", e);
			return null;
		}
	}
	
	@Override
	public List<GroupAuthorization> getUserAuthorizationsByGroup(int groupId) {
		String sql = "SELECT GROUP_ID, USER_ID, ROLE_ID, IS_MEMBER FROM USER_GROUP " +
				"WHERE GROUP_ID = ? AND " + NOW_IS_BETWEEN_BEGIN_AND_END_TIMES;
		Object[] args = new Object[] {groupId};
		
		try {
			return (List<GroupAuthorization>) 
				template.query(sql, args, new GroupAuthorizationRowMapper());
		} catch (DataAccessException e) {
			log.error("Persistence error on user authorization retrieval by group", e);
			return null;
		}
	}
	
	@Override
	public List<GroupAuthorization> getGroupAuthorizationsBySupergroup(int groupId) {
		String sql = "SELECT GROUP_ID, SUBGROUP_ID, ROLE_ID, IS_SUBGROUP " +
				"FROM SUBGROUP_GROUP WHERE GROUP_ID = ?";
		Object[] args = new Object[] {groupId};
		
		try {
			return (List<GroupAuthorization>) 
				template.query(sql, args, new GroupAuthorizationRowMapper());
		} catch (DataAccessException e) {
			log.error("Persistence error on subgroup authorization retrieval by group", e);
			return null;
		}
	}
	
	@Override
	public int getNextRoleId() {
		String sql = "SELECT MAX(ROLE_ID) + 1 FROM ROLE";
		
		try {
			//get DNames matching given
			return this.template.queryForInt(sql);
		} catch (DataAccessException e) {
			log.error("Data access error on max roleID lookup", e);
			return 0;
		}
	}
	
	private Set<Right> getRightsByRoleId(int roleId) {
		String sql = "SELECT RIGHT_ID FROM ROLE_RIGHTS WHERE ROLE_ID = ?";
		Object[] args = new Object[] {roleId};
		
		try {
			return new LinkedHashSet<Right>(template.query(sql, args, new RightsRowMapper()));
		} catch (EmptyResultDataAccessException e) {
			return new HashSet<Right>();
		} catch (DataAccessException e) {
			log.error("Data access error on rights lookup by role", e);
			return null;
		}
	}
}
