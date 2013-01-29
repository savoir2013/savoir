// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.iit.nrc.savoir.domain.Group;
import ca.gc.iit.nrc.savoir.domain.GroupNode;
import ca.gc.iit.nrc.savoir.domain.Role;
import ca.gc.iit.nrc.savoir.domain.UserIDName;
import ca.gc.nrc.iit.savoir.dao.BaseDAO;
import ca.gc.nrc.iit.savoir.dao.IGroupDAO;
import ca.gc.nrc.iit.savoir.dao.IRoleDAO;
import ca.gc.nrc.iit.savoir.dao.impl.AuthorizationDAO.IntRole;
import ca.gc.nrc.iit.savoir.dao.impl.AuthorizationDAO.IntRoleRowMapper;
import ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.IntRowMapper;
import ca.gc.nrc.iit.savoir.dao.impl.UserDAO.UserIDNameRowMapper;

import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.addInts;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.NOW_IS_BETWEEN_BEGIN_AND_END_TIMES;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.nullableDate;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.nullableType;
import static ca.gc.nrc.iit.savoir.utils.Constants.DATA_CORRUPTION;
import static ca.gc.nrc.iit.savoir.utils.Constants.INVALID_PARAMETERS;
import static ca.gc.nrc.iit.savoir.utils.Constants.NO_SUCH_ENTITY;
import static ca.gc.nrc.iit.savoir.utils.Constants.OTHER_ERROR;
import static ca.gc.nrc.iit.savoir.utils.Constants.SUCCESS;

@Transactional(readOnly = true, propagation = Propagation.REQUIRED,	isolation = Isolation.READ_COMMITTED)
@SuppressWarnings("unchecked")
public class GroupDAO extends BaseDAO implements IGroupDAO {
	
	private IRoleDAO roleDAO;
	
	class GroupRowMapper implements RowMapper {
    	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
    		Group group = new Group();    		
    		group.setGroupId(rs.getInt("GROUP_ID"));
    		group.setGroupName(rs.getString("GROUP_NAME"));
    		group.setDescription(rs.getString("DESCRIPTION"));
    		
    		return group;
    	}
    }
	
	class GroupNodeRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			GroupNode gn = new GroupNode();
			int groupId = rs.getInt("GROUP_ID");
			
			gn.setGroup(new Group(groupId, rs.getString("GROUP_NAME"), rs.getString("DESCRIPTION")));
			gn.setMembers(getMembersByGroupId(groupId));
			gn.setSubgroups(getSubgroupsByGroupId(groupId));
			
			return gn;
		}
	}
	
	public IRoleDAO getRoleDAO() {
		return roleDAO;
	}

	public void setRoleDAO(IRoleDAO roleDAO) {
		this.roleDAO = roleDAO;
	}
	
	@Transactional(readOnly = false)
    @Override
    public void addGroup(Group g) {
    	String sql = "INSERT INTO SAVOIR.GROUP (GROUP_ID, GROUP_NAME, DESCRIPTION) values (?,?,?)";
    	Object[] args = new Object[]{g.getGroupId(), g.getGroupName(), g.getDescription()};
    	int[] argTypes = new int[]{Types.INTEGER, Types.CHAR, Types.VARCHAR};
    	this.template.update(sql, args, argTypes);
    }
    
	@Transactional(readOnly = false)
	@Override
	public void removeGroup(int groupId) {
		String sql = "DELETE FROM SAVOIR.GROUP WHERE GROUP_ID = ?";
		Object[] args = new Object[] { groupId };
		this.template.update(sql, args);
	}
	
	@Transactional(readOnly = false)
	@Override
	public int updateGroup(int id, String name, String desc) {
		if (id <= 0) {
			return INVALID_PARAMETERS;
		}
		
		//build SQL query
		//initialize query and argument list
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("UPDATE SAVOIR.GROUP SET ");
		Vector<Object> argBuilder = new Vector<Object>();
		
		//for each field, check if non-null, and, if so, add to query and arg list
		if (name != null) {
			sqlBuilder.append("GROUP_NAME = ?,");
			argBuilder.addElement(name);
		}
		if (desc != null) {
			sqlBuilder.append("DESCRIPTION = ?,");
			argBuilder.addElement(desc);
		}
		
		//trim final comma, (if none, no change, return invalid parameters)
		int lastIndex = sqlBuilder.length() - 1;
		if (sqlBuilder.charAt(lastIndex) == ',') {
			sqlBuilder.deleteCharAt(lastIndex);
		} else {
			return INVALID_PARAMETERS;
		}
		
		//Finalize query and argument list
		sqlBuilder.append(" WHERE GROUP_ID = ?");
		argBuilder.addElement(id);
		
		//reformat query and arguments
		String sql = sqlBuilder.toString();
		Object[] args = argBuilder.toArray(new Object[argBuilder.size()]);
		
		try {
			int updated = template.update(sql, args);
			switch (updated) {
			case 1:		//a single record updated - the correct behaviour
				return SUCCESS;
			case 0:		//no records updated (ergo no records match group ID)
				return NO_SUCH_ENTITY;
			default:	//multiple records updated (should not happen - means data corruption)
				log.fatal(updated + " records updated on query \"" + sql + "\".\n" +
							"Data corruption likely.");
				return DATA_CORRUPTION;
			}
		} catch (DataAccessException e) {
			log.error("Persistence error on Group update", e);
			return OTHER_ERROR;
		}
	}
	
	@Override
	public Group getGroupById(int groupId) {
		if (groupId == 0) {
			//EVERYONE pseudo-group
			return new Group(0, "EVERYONE", "All users");
		}
		
		String sql = "SELECT * FROM SAVOIR.GROUP WHERE GROUP_ID = ?";
		Object[] args = new Object[] {groupId};
		int[] argTypes = new int[] {Types.INTEGER};
		
		try {
			return (Group)template.queryForObject(sql, args, argTypes, 
					new GroupRowMapper());
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (DataAccessException e) {
			log.error("Persistence error on Group lookup", e);
			return null;
		}
	}
	
	@Override
	public Set<Group> getGroupsById(Set<Integer> groups) {
    	StringBuilder sqlBuilder = new StringBuilder();
    	sqlBuilder.append("SELECT * FROM SAVOIR.GROUP WHERE GROUP_ID IN ");
    	addInts(sqlBuilder, groups);
    	
    	try {
			return new TreeSet<Group>(
					template.query(sqlBuilder.toString(), new GroupRowMapper()));
    	} catch (EmptyResultDataAccessException e) {
			return new TreeSet<Group>();
    	}  catch (DataAccessException e) {
			log.error("Persistence error on Group search", e);
			return null;
		}
	}
    
	@Transactional(readOnly = false)
    @Override
    public void addUserAuthorization(int userId, int roleId, int groupId,
    		boolean isMember, Date beginTime, Date endTime) {
    	String sql = "INSERT INTO SAVOIR.USER_GROUP (USER_ID, GROUP_ID, ROLE_ID, "
    		+ "IS_MEMBER, BEGIN_TIME, END_TIME) values (?,?,?,?,?,?)";
    	Object[] args = new Object[]{userId, groupId, roleId, 
    			isMember, nullableDate(beginTime), nullableDate(endTime)};
    	int[] argTypes = new int[]{Types.INTEGER, Types.INTEGER, Types.INTEGER, 
    			Types.BOOLEAN, nullableType(beginTime), nullableType(endTime)};
    	this.template.update(sql, args, argTypes);
    }
    
	@Transactional(readOnly = false)
    @Override
    public void addGroupAuthorization(int subgroupId, int roleId, int groupId,
    		boolean isSubgroup) {
    	String sql = "INSERT INTO SAVOIR.SUBGROUP_GROUP (SUBGROUP_ID, GROUP_ID, ROLE_ID, "
    		+ "IS_SUBGROUP) values (?,?,?,?)";
    	Object[] args = new Object[]{subgroupId, groupId, roleId, isSubgroup}; 
    	int[] argTypes = new int[]{Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.BOOLEAN};
    	this.template.update(sql, args, argTypes);
    }
    
	@Override
	public Set<Integer> getDirectSupergroups(Set<Integer> groups) {
		//validate input
		//this expression evaluates to true if the group is null, empty, 
		// or contains only a single null element
		if (groups == null || groups.isEmpty() || (groups.remove(null) && groups.isEmpty())) {
			//return empty set
			return new HashSet<Integer>();
		}
		
		StringBuilder sqlBuilder = new StringBuilder();
		
		sqlBuilder.append(
				"SELECT GROUP_ID FROM SUBGROUP_GROUP WHERE IS_SUBGROUP = true AND SUBGROUP_ID IN ");
		addInts(sqlBuilder, groups);
		
		try {
			return new HashSet<Integer>(template.query(sqlBuilder.toString(), new IntRowMapper()));
		}  catch (DataAccessException e) {
			log.error("Persistence error on Group search", e);
			return null;
		}
	}

	@Override
	public Set<Integer> getDirectSubgroups(Set<Integer> groups) {
		//validate input
		//this expression evaluates to true if the group is null, empty, 
		// or contains only a single null element
		if (groups == null || groups.isEmpty() || (groups.remove(null) && groups.isEmpty())) {
			//return empty set
			return new HashSet<Integer>();
		}
		
		StringBuilder sqlBuilder = new StringBuilder();
		
		sqlBuilder.append(
				"SELECT SUBGROUP_ID FROM SUBGROUP_GROUP WHERE IS_SUBGROUP = true AND GROUP_ID IN ");
		addInts(sqlBuilder, groups);
		
		try {
			return new HashSet<Integer>(template.query(sqlBuilder.toString(), new IntRowMapper()));
		}  catch (DataAccessException e) {
			log.error("Persistence error on Group search", e);
			return null;
		}
	}

	@Override
	public Set<Integer> getMembers(Set<Integer> groups) {
		//validate input
		//this expression evaluates to true if the group is null, empty, 
		// or contains only a single null element
		if (groups == null || groups.isEmpty() 
				|| (groups.remove(null) && groups.isEmpty())) {
			//return empty set
			return new HashSet<Integer>();
		}
		
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT USER_ID FROM USER_GROUP " +
				"WHERE IS_MEMBER = true AND "
				+ NOW_IS_BETWEEN_BEGIN_AND_END_TIMES + " AND GROUP_ID IN ");
		addInts(sqlBuilder, groups);
		
		try {
			return new HashSet<Integer>(
					template.query(sqlBuilder.toString(), new IntRowMapper()));
		}  catch (DataAccessException e) {
			log.error("Persistence error on Group search", e);
			return null;
		}
	}
	
	@Override
	public Set<Integer> getAuthorizedUsersByRole(int groupId, Set<Integer> roles) {
		//validate input
		//this expression evaluates to true if roles is null, empty, 
		// or contains only a single null element
		if (roles == null || roles.isEmpty() || (roles.remove(null) && roles.isEmpty())) {
			//return empty set
			return new HashSet<Integer>();
		}
		
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT USER_ID FROM USER_GROUP WHERE " + NOW_IS_BETWEEN_BEGIN_AND_END_TIMES + 
				" AND GROUP_ID = ? AND ROLE_ID IN ");
		addInts(sqlBuilder, roles);
		Object[] args = new Object[]{groupId};
		
		try {
			return new HashSet<Integer>(template.query(
					sqlBuilder.toString(), args, new IntRowMapper()));
		}  catch (DataAccessException e) {
			log.error("Persistence error on role members search", e);
			return null;
		}
	}
	
	@Override
	public Set<Integer> getAuthorizedSubgroupsByRole(int groupId, Set<Integer> roles) {
		//validate input
		//this expression evaluates to true if roles is null, empty, 
		// or contains only a single null element
		if (roles == null || roles.isEmpty() || (roles.remove(null) && roles.isEmpty())) {
			//return empty set
			return new HashSet<Integer>();
		}
		
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT SUBGROUP_ID FROM SUBGROUP_GROUP WHERE "
				+ "GROUP_ID = ? AND ROLE_ID IN ");
		addInts(sqlBuilder, roles);
		Object[] args = new Object[]{groupId};
		
		try {
			return new HashSet<Integer>(template.query(
					sqlBuilder.toString(), args, new IntRowMapper()));
		}  catch (DataAccessException e) {
			log.error("Persistence error on role subgroups search", e);
			return null;
		}
	}
	
	@Override
	public GroupNode getUserGraphByGroupId(int groupId) {
		String sql = "SELECT * FROM SAVOIR.GROUP WHERE GROUP_ID = ?";
		Object[] args = new Object[]{groupId};
		int[] argt = new int[]{Types.INTEGER};
		
		try {
			return (GroupNode) template.queryForObject(sql, args, argt, new GroupNodeRowMapper());
		} catch (DataAccessException e) {
			log.error("Data access error on group lookup", e);
			return null;
		}
	}
	
	private List<UserIDName> getMembersByGroupId(int groupId) {
		String sql = 
			"SELECT u.USER_ID, u.DISTINGUISHED_NAME, p.LAST_NAME, p.FIRST_NAME "
			+ "FROM USER as u, PERSON as p, USER_GROUP as g "
			+ "WHERE p.PERSON_ID = u.PERSON_ID AND u.USER_ID = g.USER_ID AND "
			+ "g.GROUP_ID = ? AND g.IS_MEMBER = true AND " 
			+ NOW_IS_BETWEEN_BEGIN_AND_END_TIMES("u")
			+ " AND " + NOW_IS_BETWEEN_BEGIN_AND_END_TIMES("g");
		Object[] args = new Object[]{groupId};
		int[] argt = new int[]{Types.INTEGER};
		
		List<UserIDName> nodes;
		try {
			nodes = template.query(sql, args, argt, new UserIDNameRowMapper());
		} catch (DataAccessException e) {
			log.error("Data access error on group lookup", e);
			return null;
		}
		Collections.sort(nodes);
		return nodes;
	}
	
	private List<GroupNode> getSubgroupsByGroupId(int groupId) {
		String sql = "SELECT g.GROUP_ID, g.GROUP_NAME, g.DESCRIPTION " +
				"FROM SAVOIR.GROUP as g, SUBGROUP_GROUP as s " +
				"WHERE g.GROUP_ID = s.SUBGROUP_ID AND s.GROUP_ID = ? AND s.IS_SUBGROUP = true";
		Object[] args = new Object[]{groupId};
		int[] argt = new int[]{Types.INTEGER};
		
		List<GroupNode> nodes;
		try {
			nodes = template.query(sql, args, argt, new GroupNodeRowMapper());
		} catch (DataAccessException e) {
			log.error("Data access error on group lookup", e);
			return null;
		}
		Collections.sort(nodes);
		return nodes;
	}
	
	@Override
	public Map<Integer, Role> getAuthorizedGroups(int group) {
		String sql = "SELECT SUBGROUP_ID, ROLE_ID FROM SUBGROUP_GROUP WHERE GROUP_ID = ?";
		Object[] args = new Object[]{group};
		List<IntRole> authz;
		
		try {
			//get user IDs matching given DName
			authz = (List<IntRole>) template.query(sql, args, new IntRoleRowMapper(roleDAO));
		} catch (DataAccessException e) {
			log.error("Data access error on group authorization lookup", e);
			return null;
		}
		
		Map<Integer, Role> results = new LinkedHashMap<Integer, Role>();
		// convert list of IntRole into map entries
		for (IntRole ir : authz) {
			results.put(ir.id, ir.role);
		}
		
		return results;
	}
	
	@Override
	public Map<Integer, Role> getAuthorizedUsers(int group) {
		String sql = "SELECT USER_ID, ROLE_ID FROM USER_GROUP WHERE GROUP_ID = ? AND "
			+ NOW_IS_BETWEEN_BEGIN_AND_END_TIMES;
		Object[] args = new Object[]{group};
		List<IntRole> authz;
		
		try {
			//get user IDs matching given DName
			authz = (List<IntRole>) template.query(sql, args, new IntRoleRowMapper(roleDAO));
		} catch (DataAccessException e) {
			log.error("Data access error on user authorization lookup", e);
			return null;
		}
		
		Map<Integer, Role> results = new LinkedHashMap<Integer, Role>();
		// convert list of IntRole into map entries
		for (IntRole ir : authz) {
			results.put(ir.id, ir.role);
		}
		
		return results;
	}
	
	@Override
	public Map<Integer, Role> getAuthorizations(int group) {
		String sql = "SELECT GROUP_ID, ROLE_ID FROM SUBGROUP_GROUP WHERE SUBGROUP_ID = ?";
		Object[] args = new Object[]{group};
		List<IntRole> authz;
		
		try {
			authz = (List<IntRole>) template.query(sql, args, new IntRoleRowMapper(roleDAO));
		} catch (DataAccessException e) {
			log.error("Data access error on group authorization lookup", e);
			return null;
		}
		
		Map<Integer, Role> results = new LinkedHashMap<Integer, Role>();
		// convert list of KeyValue into map entries
		for (IntRole ir : authz) {
			results.put(ir.id, ir.role);
		}
		
		return results;
	}
	
	@Override
	public Map<Integer, Set<Role>> getGroupAuthorizations(Set<Integer> groups) {
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT GROUP_ID, ROLE_ID FROM SUBGROUP_GROUP WHERE SUBGROUP_ID IN ");
		addInts(sqlBuilder, groups);
		
		List<IntRole> authz;
		
		try {
			//get user IDs matching given DName
			authz = (List<IntRole>) template.query(sqlBuilder.toString(), new IntRoleRowMapper(roleDAO));
		} catch(EmptyResultDataAccessException e) {
			return new HashMap<Integer, Set<Role>>();
		} catch (DataAccessException e) {
			log.error("Data access error on user authorization lookup", e);
			return null;
		}
		
		Map<Integer, Set<Role>> results = 
			new LinkedHashMap<Integer, Set<Role>>();
		// convert list of KeyValue into map entries
		for (IntRole ir : authz) {
			Set<Role> existing = results.get(ir.id);
			
			//check if we already have an authorization on this group 
			if(existing != null) {
				existing.add(ir.role);
			} else {
				existing = new LinkedHashSet<Role>();
				existing.add(ir.role);
				results.put(ir.id, existing);
			}
		}
		
		return results;
	}
	
	@Override
	public Map<Integer, Set<Role>> getSessionAuthorizations(Set<Integer> groups) {
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT SESSION_ID, ROLE_ID FROM USER_SESSION WHERE GROUP_ID IN ");
		addInts(sqlBuilder, groups);
		
		List<IntRole> authz;
		
		try {
			//get user IDs matching given DName
			authz = (List<IntRole>) template.query(sqlBuilder.toString(), new IntRoleRowMapper(roleDAO));
		} catch(EmptyResultDataAccessException e) {
			return new HashMap<Integer, Set<Role>>();
		} catch (DataAccessException e) {
			log.error("Data access error on user authorization lookup", e);
			return null;
		}
		
		Map<Integer, Set<Role>> results = new HashMap<Integer, Set<Role>>();
		// convert list of KeyValue into map entries
		for (IntRole ir : authz) {
			Set<Role> existing = results.get(ir.id);
			
			//check if we already have an authorization on this group 
			if(existing != null) {
				existing.add(ir.role);
			} else {
				existing = new LinkedHashSet<Role>();
				existing.add(ir.role);
				results.put(ir.id, existing);
			}
		}
		
		return results;
	}
	
	@Override
	public Map<Integer, Set<Role>> getAuthoredSessionAuthorizations(Set<Integer> groups) {
		boolean hasEveryone = groups.remove(0);
		
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT S.SESSION_ID, U.ROLE_ID " +
				"FROM SAVOIR.SESSION AS S, USER_SESSION AS U " +
				"WHERE S.SESSION_ID = U.SESSION_ID ");
		
		if (hasEveryone) {
			sqlBuilder.append("AND (U.GROUP_ID IN ");
			addInts(sqlBuilder, groups);
			sqlBuilder.append(" OR (U.USER_ID IS NULL AND U.GROUP_ID IS NULL))");
		} else {
			sqlBuilder.append("AND U.GROUP_ID IN ");
			addInts(sqlBuilder, groups);
		}
		
		sqlBuilder.append(" AND S.SCENARIO_ID IS NOT NULL");
		
		List<IntRole> authz;
		
		try {
			//get user IDs matching given DName
			authz = (List<IntRole>) template.query(sqlBuilder.toString(), new IntRoleRowMapper(roleDAO));
		} catch(EmptyResultDataAccessException e) {
			return new HashMap<Integer, Set<Role>>();
		} catch (DataAccessException e) {
			log.error("Data access error on user authorization lookup", e);
			return null;
		}
		
		Map<Integer, Set<Role>> results = new HashMap<Integer, Set<Role>>();
		// convert list of KeyValue into map entries
		for (IntRole ir : authz) {
			Set<Role> existing = results.get(ir.id);
			
			//check if we already have an authorization on this group 
			if(existing != null) {
				existing.add(ir.role);
			} else {
				existing = new LinkedHashSet<Role>();
				existing.add(ir.role);
				results.put(ir.id, existing);
			}
		}
		
		return results;
	}
	
	@Transactional(readOnly = false)
	@Override
	public int addAuthorization(int subgroupId, int roleId, int groupId, boolean isSubgroup) {
		if (subgroupId <= 0 || roleId <= 0 || groupId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		String sql = "INSERT INTO SUBGROUP_GROUP (SUBGROUP_ID, GROUP_ID, ROLE_ID, IS_SUBGROUP) " +
				"values (?,?,?,?)";
		Object[] args = new Object[] {subgroupId, groupId, roleId, isSubgroup}; 
		int[] argTypes = new int[]{Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.BOOLEAN};
		
		int updated;
		try {
			updated = this.template.update(sql, args, argTypes);
		} catch (DataAccessException e) {
			log.error("Persistence error on add subgroup authorization", e);
			return OTHER_ERROR;
		}
		
		//return success for one update, error otherwise
		return updated == 1 ? SUCCESS : OTHER_ERROR;
	}

	@Transactional(readOnly = false)
	@Override
	public int updateAuthorization(int subgroupId, int roleId, int groupId, boolean isSubgroup) {
		if (subgroupId <= 0 || roleId <= 0 || groupId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		String sql = "UPDATE SUBGROUP_GROUP SET ROLE_ID = ?, IS_SUBGROUP = ? "
			+ "WHERE SUBGROUP_ID = ? AND GROUP_ID = ?";
		Object[] args = new Object[] {roleId, isSubgroup, subgroupId, groupId};
		int[] argTypes = new int[]{Types.INTEGER, Types.BOOLEAN, Types.INTEGER, Types.INTEGER};
		
		int updated;
		try {
			updated = this.template.update(sql, args, argTypes);
		} catch (DataAccessException e) {
			log.error("Persistence error on add user membership", e);
			return OTHER_ERROR;
		}
		
		//return success for one update, error otherwise
		return updated == 1 ? SUCCESS : OTHER_ERROR;
	}
	
	@Transactional(readOnly = false)
	@Override
	public int removeAuthorization(int subgroupId, int groupId) {
		if (subgroupId <= 0 || groupId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		String sql = "DELETE FROM SUBGROUP_GROUP WHERE SUBGROUP_ID = ? AND GROUP_ID = ?";
		Object[] args = new Object[] {subgroupId, groupId};
		int[] argTypes = new int[]{Types.INTEGER, Types.INTEGER};
		
		int updated;
		try {
			updated = this.template.update(sql, args, argTypes);
		} catch (DataAccessException e) {
			log.error("Persistence error on remove group authorization", e);
			return OTHER_ERROR;
		}
		
		//return success for one update, error otherwise
		return updated == 1 ? SUCCESS : OTHER_ERROR;
	}

	@Override
	public Set<Integer> getGrouplessUsers() {
		String sql = "SELECT USER_ID FROM SAVOIR.USER WHERE USER_ID NOT IN "
			+ "(SELECT USER_ID FROM USER_GROUP WHERE IS_MEMBER = true AND "
			+ NOW_IS_BETWEEN_BEGIN_AND_END_TIMES + ")";
		
		try {
			return new HashSet<Integer>(template.query(sql, new IntRowMapper()));
		}  catch (DataAccessException e) {
			log.error("Persistence error on groupless users search", e);
			return null;
		}
	}
	
	@Override
	public Set<Integer> getRootGroups() {
		String sql = "SELECT GROUP_ID FROM SAVOIR.GROUP WHERE GROUP_ID NOT IN "
			+ "(SELECT SUBGROUP_ID FROM SUBGROUP_GROUP WHERE IS_SUBGROUP = true)";
		
		try {
			return new HashSet<Integer>(template.query(sql, new IntRowMapper()));
		}  catch (DataAccessException e) {
			log.error("Persistence error on root groups search", e);
			return null;
		}
	}
	
	@Override
	public Set<Integer> getAllUsers() {
		String sql = "SELECT USER_ID FROM SAVOIR.USER";
		
		try {
			return new HashSet<Integer>(template.query(sql, new IntRowMapper()));
		}  catch (DataAccessException e) {
			log.error("Persistence error on all users search", e);
			return null;
		}
	}
	
	@Override
	public Set<Integer> getAllGroups() {
		String sql = "SELECT GROUP_ID FROM SAVOIR.GROUP";
		
		try {
			Set<Integer> groups = 
				new HashSet<Integer>(template.query(sql, new IntRowMapper()));
			groups.add(0);
			return groups;
		}  catch (DataAccessException e) {
			log.error("Persistence error on all groups search", e);
			return null;
		}
	}

	@Override
	public int getNextGroupId() {
		String sql = "SELECT MAX(GROUP_ID) + 1 FROM SAVOIR.GROUP";
		
		try {
			//get DNames matching given
			return this.template.queryForInt(sql);
		} catch (DataAccessException e) {
			log.error("Data access error on max groupID lookup", e);
			return 0;
		}
	}
}
