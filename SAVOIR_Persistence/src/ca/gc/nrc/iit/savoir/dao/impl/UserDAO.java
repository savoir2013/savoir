// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.iit.nrc.savoir.domain.Role;
import ca.gc.iit.nrc.savoir.domain.User;
import ca.gc.iit.nrc.savoir.domain.UserIDName;
import ca.gc.nrc.iit.savoir.dao.BaseDAO;
import ca.gc.nrc.iit.savoir.dao.IPersonDAO;
import ca.gc.nrc.iit.savoir.dao.IRoleDAO;
import ca.gc.nrc.iit.savoir.dao.IUserDAO;
import ca.gc.nrc.iit.savoir.dao.impl.AuthorizationDAO.IntRole;
import ca.gc.nrc.iit.savoir.dao.impl.AuthorizationDAO.IntRoleRowMapper;
import ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.IntRowMapper;

import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.NOW_IS_BETWEEN_BEGIN_AND_END_TIMES;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.getNullableDate;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.nullableDate;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.nullableType;
import static ca.gc.nrc.iit.savoir.utils.Constants.*;

@Transactional(readOnly = true, propagation = Propagation.REQUIRED,	isolation = Isolation.READ_COMMITTED)
@SuppressWarnings("unchecked")
public class UserDAO extends BaseDAO implements IUserDAO {

	private IPersonDAO personDAO;
	
	private IRoleDAO roleDAO;
    
	class UserRowMapper implements RowMapper {
    	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
    		User user = new User();    		
    		
    		user.setUserID(rs.getInt("USER_ID"));
    		user.setDName(rs.getString("DISTINGUISHED_NAME"));
    		user.setPassword(rs.getString("SAVOIR_PASSWORD"));
    		user.setBeginTime(getNullableDate(rs, "BEGIN_TIME"));
    		user.setEndTime(getNullableDate(rs, "END_TIME"));
    		user.setPerson(personDAO.getPersonById(rs.getInt("PERSON_ID")));
    		user.setSiteId(rs.getInt("SITE_ID"));
    		user.setRole(roleDAO.getRoleById(rs.getInt("ROLE_ID")));
    		
    		return user;
    	}
    }
	
	static class UserIDNameRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			UserIDName uidn = new UserIDName();
			
			uidn.userID = rs.getInt("USER_ID");
			uidn.userName = rs.getString("DISTINGUISHED_NAME");
			uidn.surname = rs.getString("LAST_NAME");
			uidn.givenName = rs.getString("FIRST_NAME");
			
			return uidn;
		}
	}
    
	public IPersonDAO getPersonDAO() {
		return personDAO;
	}

	public void setPersonDAO(IPersonDAO personDAO) {
		this.personDAO = personDAO;
	}
	
	public IRoleDAO getRoleDAO() {
		return roleDAO;
	}

	public void setRoleDAO(IRoleDAO roleDAO) {
		this.roleDAO = roleDAO;
	}
	
	@Transactional(readOnly = false)
	@Override
    public void addUser(User u) {
		String sql = "INSERT INTO SAVOIR.USER (USER_ID, DISTINGUISHED_NAME, SAVOIR_PASSWORD" 
			+ ", BEGIN_TIME, END_TIME, PERSON_ID, SITE_ID, ROLE_ID) VALUES (?,?,?,?,?,?,?,?)";
		Object[] args = new Object[] {u.getUserID(), u.getDName(), u.getPassword(),
				nullableDate(u.getBeginTime()), nullableDate(u.getEndTime()), 
				u.getPerson().getPersonId(), u.getSiteId(), u.getRole().getRoleId()};
		int[] argTypes = new int[]{Types.INTEGER, Types.CHAR, Types.CHAR,
				nullableType(u.getBeginTime()), nullableType(u.getEndTime()), 
				Types.INTEGER, Types.INTEGER, Types.INTEGER};
		this.template.update(sql, args, argTypes);
	}

	@Override
	public User getUserById(int userId) {
    	String sql = "SELECT * FROM SAVOIR.USER WHERE USER_ID = ?";
    	Object[] args = new Object[]{userId};
    	try {
    		User s;
    		s = (User) this.template.queryForObject(sql, args, new UserRowMapper());
    		return s;
    	}catch (EmptyResultDataAccessException e){
    		return null;
    	}
	}
	
	@Transactional(readOnly = false)
	@Override
	public int updateUser(int userId, String loginId, String password, Date beginTime, Date endTime) {
		if (userId <= 0 || loginId == null || password == null) {
			return INVALID_PARAMETERS;
		}
		
		String sql = "UPDATE SAVOIR.USER SET DISTINGUISHED_NAME = ?, SAVOIR_PASSWORD = ?, " +
				"BEGIN_TIME = ?, END_TIME = ? WHERE USER_ID = ?";
		Object[] args = 
			new Object[] {loginId, password, nullableDate(beginTime), nullableDate(endTime), userId};
		int[] argTypes = new int[] {Types.CHAR, Types.CHAR, 
				nullableType(beginTime), nullableType(endTime), Types.INTEGER};
		
		try {
			int updated = template.update(sql, args, argTypes);
			switch (updated) {
			case 1:		//a single record updated - the correct behaviour
				return SUCCESS;
			case 0:		//no records updated (ergo no records match user ID)
				return NO_SUCH_ENTITY;
			default:	//multiple records updated (should not happen - means data corruption)
				log.fatal(updated + " records updated on query \"" + sql + "\".\n" +
							"Data corruption likely.");
				return DATA_CORRUPTION;
			}
		} catch (DataAccessException e) {
			log.error("Persistence error on User update", e);
			return OTHER_ERROR;
		}		
	}

	@Transactional(readOnly = false)
	@Override
	public void removeUser(int userId) {
		String sql = "DELETE FROM SAVOIR.USER WHERE USER_ID = ?";
		Object[] args = new Object[] { userId };
		this.template.update(sql, args);
	}

	@Override
	public User getUserByDN(String distinguishedName) {
		String sql = "SELECT * FROM SAVOIR.USER WHERE DISTINGUISHED_NAME = ?";
    	Object[] args = new Object[]{distinguishedName};
    	try {
    		User s;
    		s = (User) this.template.queryForObject(sql, args, new UserRowMapper());
    		return s;
    	}catch (EmptyResultDataAccessException e){
    		return null;
    	}
	}
	
	@Transactional(readOnly = false)
	@Override
	public void updateUserSite(String userName, int siteId){
		String sql = "UPDATE SAVOIR.USER SET SITE_ID = ? WHERE DISTINGUISHED_NAME = ?";
		Object[] args = new Object[]{siteId, userName};
		int[] argTypes = new int[] { Types.INTEGER, Types.VARCHAR };
		this.template.update(sql, args, argTypes);
	}

	// added 16-09-09 for integration of UserMgr v2
	
	@Override
	public boolean isDName(String name) {
		String sql = "SELECT count(*) FROM SAVOIR.USER WHERE DISTINGUISHED_NAME = ? AND " 
			+ NOW_IS_BETWEEN_BEGIN_AND_END_TIMES;
		Object[] args = new Object[]{name};
		
		try {
			//get DNames matching given
			int matches = this.template.queryForInt(sql, args);
			return matches >= 1;
		} catch (DataAccessException e) {
			log.error("Data access error on DName lookup", e);
			return false;
		}
	}
	
	@Override
	public int getUserID(String caller) {
		String sql = "SELECT USER_ID FROM SAVOIR.USER WHERE DISTINGUISHED_NAME = ? AND " 
			+ NOW_IS_BETWEEN_BEGIN_AND_END_TIMES;
		Object[] args = new Object[]{caller};
		
		try {
			//get user IDs matching given DName
			List<Integer> matches = (List<Integer>) template.query(sql, args, new IntRowMapper());
			//return first or empty
			return matches.isEmpty() ? 0 : matches.get(0);
		} catch (DataAccessException e) {
			log.error("Data access error on user ID lookup", e);
			return 0;
		}
	}

	@Override
	public Set<Integer> getMemberships(int userId) {
		String sql = "SELECT GROUP_ID FROM USER_GROUP WHERE USER_ID = ? AND IS_MEMBER = true AND " 
			+ NOW_IS_BETWEEN_BEGIN_AND_END_TIMES;
		Object[] args = new Object[]{userId};
		
		try {
			//get user IDs matching given DName
			Set<Integer> matches = new HashSet<Integer>(
					template.query(sql, args, new IntRowMapper()));
			return matches;
		} catch (DataAccessException e) {
			log.error("Data access error on user ID lookup", e);
			return null;
		}
	}

	@Override
	public Map<Integer, Role> getGroupAuthorizations(int userId) {
		String sql = "SELECT GROUP_ID, ROLE_ID FROM USER_GROUP WHERE USER_ID = ? AND "
			+ NOW_IS_BETWEEN_BEGIN_AND_END_TIMES;
		Object[] args = new Object[]{userId};
		List<IntRole> authz;
		
		try {
			authz = (List<IntRole>) template.query(sql, args, new IntRoleRowMapper(roleDAO));
		} catch (DataAccessException e) {
			log.error("Data access error on user authorization lookup", e);
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
	public Map<Integer, Role> getSessionAuthorizations(int userId) {
		String sql = "SELECT SESSION_ID, ROLE_ID FROM USER_SESSION WHERE USER_ID = ?";
		Object[] args = new Object[]{userId};
		List<IntRole> authz;
		
		try {
			authz = (List<IntRole>) template.query(sql, args, new IntRoleRowMapper(roleDAO));
		} catch (DataAccessException e) {
			log.error("Data access error on user authorization lookup", e);
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
	public Map<Integer, Role> getAuthoredSessionAuthorizations(int userId) {
		String sql = "SELECT S.SESSION_ID, U.ROLE_ID " +
				"FROM SAVOIR.SESSION AS S, USER_SESSION AS U " +
				"WHERE S.SESSION_ID = U.SESSION_ID AND U.USER_ID = ? " +
				"AND S.SCENARIO_ID IS NOT NULL";
		Object[] args = new Object[]{userId};
		List<IntRole> authz;
		
		try {
			authz = (List<IntRole>) template.query(sql, args, new IntRoleRowMapper(roleDAO));
		} catch (DataAccessException e) {
			log.error("Data access error on user authorization lookup", e);
			return null;
		}
		
		Map<Integer, Role> results = new LinkedHashMap<Integer, Role>();
		// convert list of KeyValue into map entries
		for (IntRole ir : authz) {
			results.put(ir.id, ir.role);
		}
		
		return results;
	}
	
	@Transactional(readOnly = false)
	@Override
	public int addAuthorization(int userId, int roleId, int groupId, boolean isMember,
			Date beginTime, Date endTime) {
		if (userId <= 0 || roleId <= 0 || groupId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		String sql = "INSERT INTO USER_GROUP " +
				"(USER_ID, GROUP_ID, ROLE_ID, IS_MEMBER, BEGIN_TIME, END_TIME) VALUES (?,?,?,?,?,?)";
		Object[] args = new Object[] {userId, groupId, roleId, isMember, 
				nullableDate(beginTime), nullableDate(endTime)};
		int[] argTypes = new int[]{Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.BOOLEAN,
				nullableType(beginTime), nullableType(endTime)};
		
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
	public int updateAuthorization(int userId, int roleId, int groupId, boolean isMember,
			Date beginTime, Date endTime) {
		if (userId <= 0 || roleId <= 0 || groupId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		String sql = "UPDATE USER_GROUP SET ROLE_ID = ?, IS_MEMBER = ?, BEGIN_TIME = ?, END_TIME = ? "
			+ "WHERE USER_ID = ? AND GROUP_ID = ?";
		Object[] args = new Object[] {roleId, isMember, nullableDate(beginTime), nullableDate(endTime),
				userId, groupId};
		int[] argTypes = new int[]{Types.INTEGER, Types.BOOLEAN, nullableType(beginTime), nullableType(endTime),
				Types.INTEGER, Types.INTEGER};
		
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
	public int removeAuthorization(int userId, int groupId) {
		if (userId <= 0 || groupId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		String sql = "DELETE FROM USER_GROUP WHERE USER_ID = ? AND GROUP_ID = ?";
		Object[] args = new Object[] {userId, groupId};
		int[] argTypes = new int[]{Types.INTEGER, Types.INTEGER};
		
		int updated;
		try {
			updated = this.template.update(sql, args, argTypes);
		} catch (DataAccessException e) {
			log.error("Persistence error on remove user authorization", e);
			return OTHER_ERROR;
		}
		
		//return success for one update, error otherwise
		return updated == 1 ? SUCCESS : OTHER_ERROR;
	}
	
	@Override
	public int getNextUserId() {
		String sql = "SELECT MAX(USER_ID) + 1 FROM SAVOIR.USER";
		
		try {
			//get DNames matching given
			return this.template.queryForInt(sql);
		} catch (DataAccessException e) {
			log.error("Data access error on max userID lookup", e);
			return 0;
		}
	}
	
	// end added
	//added at 10-03-09
	public User getUserByPersonID(int personID){
		String sql = "SELECT * FROM SAVOIR.USER WHERE PERSON_ID = ?";
    	Object[] args = new Object[]{personID};
    	try {
    		User s;
    		s = (User) this.template.queryForObject(sql, args, new UserRowMapper());
    		return s;
    	}catch (EmptyResultDataAccessException e){
    		return null;
    	}
	}
	//end added

	@Transactional(readOnly = false)
	@Override
	public void updateSystemRole(int userId, int roleId) {
		String sql = "UPDATE SAVOIR.USER SET ROLE_ID = ? WHERE USER_ID = ?";
		Object[] args = new Object[]{roleId, userId};
		int[] argTypes = new int[]{Types.INTEGER, Types.INTEGER};
		
		try {
			template.update(sql, args, argTypes);
		} catch (DataAccessException e) {
			log.error("DataAccessException on user role update", e);
		}
	}
}
