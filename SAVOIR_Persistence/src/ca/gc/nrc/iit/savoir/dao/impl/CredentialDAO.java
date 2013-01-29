// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.iit.nrc.savoir.domain.Credential;
import ca.gc.iit.nrc.savoir.domain.CredentialAuthorization;
import ca.gc.iit.nrc.savoir.domain.CredentialParameter;
import ca.gc.iit.nrc.savoir.domain.CredentialSchema;
import ca.gc.iit.nrc.savoir.domain.CredentialSchemaParameter;
import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.iit.nrc.savoir.domain.CredentialAuthorization.CredentialAuthorizationRight;
import ca.gc.nrc.iit.savoir.dao.BaseDAO;
import ca.gc.nrc.iit.savoir.dao.ICredentialDAO;
import ca.gc.nrc.iit.savoir.dao.IResourceDAO;
import ca.gc.nrc.iit.savoir.dao.ITypesDAO;
import ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.IntRowMapper;

import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.addInts;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.getNullableDate;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.NOW_IS_BETWEEN_BEGIN_AND_END_TIMES;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.nullableDate;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.nullableInt;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.nullableType;
import static ca.gc.nrc.iit.savoir.utils.Constants.DATA_CORRUPTION;
import static ca.gc.nrc.iit.savoir.utils.Constants.NO_SUCH_ENTITY;
import static ca.gc.nrc.iit.savoir.utils.Constants.OTHER_ERROR;
import static ca.gc.nrc.iit.savoir.utils.Constants.SUCCESS;

@Transactional(readOnly = true, propagation = Propagation.REQUIRED,	isolation = Isolation.READ_COMMITTED)
@SuppressWarnings("unchecked")
public class CredentialDAO extends BaseDAO implements ICredentialDAO {

	ITypesDAO lookUpDAO;
	IResourceDAO resourceDAO;
	
	class CredentialRowMapper implements RowMapper {
		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			Credential c = new Credential();
			
			int credId = rs.getInt("CREDENTIAL_ID");
			c.setCredentialId(credId);
			c.setParameters(getCredentialParameters(credId));
			c.setDescription(rs.getString("DESCRIPTION"));
			
			return c;
		}
	}
	
	class CredentialParameterRowMapper implements RowMapper {
		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			CredentialParameter p = new CredentialParameter();
			
			p.setParameter(lookUpDAO.getParameterTypeById(rs.getString("PARAMETER_ID")));
			p.setValue(rs.getString("PARAMETER_VALUE"));
			
			return p;
		}
	}
	
	static class CredentialAuthorizationRowMapper implements RowMapper {
		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			CredentialAuthorization ca = new CredentialAuthorization();
			
			ca.setResourceId(rs.getInt("RESOURCE_ID"));
			//rs.getInt returns 0 for SQL NULL, exactly required behaviour
			ca.setUserId(rs.getInt("USER_ID"));
			ca.setGroupId(rs.getInt("GROUP_ID"));
			ca.setRights(parseRights(rs.getString("RIGHTS")));
			ca.setBeginTime(getNullableDate(rs, "BEGIN_TIME"));
			ca.setEndTime(getNullableDate(rs, "END_TIME"));
			
			return ca;
		}
		
		private static final HashMap<CredentialAuthorizationRight, Integer> RIGHTS_MAP
			= new HashMap<CredentialAuthorizationRight, Integer>();
		static {
			RIGHTS_MAP.put(CredentialAuthorizationRight.VIEW, 0);
			RIGHTS_MAP.put(CredentialAuthorizationRight.UPDATE, 1);
			RIGHTS_MAP.put(CredentialAuthorizationRight.DELETE, 2);
			RIGHTS_MAP.put(CredentialAuthorizationRight.GRANT_VIEW, 10);
			RIGHTS_MAP.put(CredentialAuthorizationRight.GRANT_UPDATE, 11);
			RIGHTS_MAP.put(CredentialAuthorizationRight.GRANT_DELETE, 12);
		}
		
		/**
		 * A somewhat mnemonic character for this right
		 */
		private static final char RIGHTS_CHAR(CredentialAuthorizationRight r) {
			switch(r) {
			case VIEW:
			case GRANT_VIEW:
				return 'V';
			case UPDATE:
			case GRANT_UPDATE:
				return 'U';
			case DELETE:
			case GRANT_DELETE:
				return 'D';
			default:
				return '*';
			}
		}
		
		/**
		 * Parses credential authorization rights from a rights string
		 * 
		 * @param s		The rights string in question
		 * 
		 * @return the rights encapsulated by that rights string
		 */
		public static List<CredentialAuthorizationRight> parseRights(String s) {
			List<CredentialAuthorizationRight> rights = 
				new ArrayList<CredentialAuthorizationRight>();
			
			for (Map.Entry<CredentialAuthorizationRight, Integer> e 
					: RIGHTS_MAP.entrySet()) {
				if (!(s.charAt(e.getValue()) == '-' 
						|| s.charAt(e.getValue()) == ' ')) {
					rights.add(e.getKey());
				}
			}
			
			return rights;
		}
		
		/**
		 * Transforms a list of CredentialAuthorizationRight into a DB-format 
		 * string
		 * 
		 * @param rights	the rights to transform
		 * 
		 * @return their string representation
		 */
		public static String toString(
				List<CredentialAuthorizationRight> rights) {
			char[] str = "---       ---       ".toCharArray();
			
			for (CredentialAuthorizationRight right : rights) {
				str[RIGHTS_MAP.get(right)] = RIGHTS_CHAR(right);
			}
			
			return new String(str);
		}
	}
	
	class CredentialSchemaRowMapper implements RowMapper {
		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			CredentialSchema s = new CredentialSchema();
			
			int schemaId = rs.getInt("CREDENTIAL_SCHEMA_ID");
			s.setSchemaId(schemaId);
			s.setParams(getCredentialSchemaParameters(schemaId));
			s.setDescription(rs.getString("DESCRIPTION"));
			
			return s;
		}
	}
	
	class CredentialSchemaParameterRowMapper implements RowMapper {
		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			CredentialSchemaParameter p = new CredentialSchemaParameter();
			
			p.setParameterType(
					lookUpDAO.getParameterTypeById(
							rs.getString("PARAMETER_ID")));
			p.setRequired(rs.getBoolean("IS_REQUIRED"));
			p.setAllowMultiple(rs.getBoolean("ALLOW_MULTIPLE"));
			
			return p;
		}
	}
	
	class ResourceIdRowMapper implements RowMapper {
		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			return resourceDAO.getResourceById(rs.getInt(1));
		}
	}
	
	@Override
	public Credential getCredentialById(int credentialId) {
		String sql = "SELECT * FROM CREDENTIAL WHERE CREDENTIAL_ID = ?";
		Object[] args = new Object[]{credentialId};
		int[] argTypes = new int[]{Types.INTEGER};
		
		try {
			return (Credential)this.template.queryForObject(sql, args, argTypes, new CredentialRowMapper());
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (DataAccessException e) {
			log.error("Persistence error on credential lookup", e);
			return null;
		}
	}
	
	@Override
	public List<Resource> getAuthorizedResources(int userId, 
			Set<Integer> groups) {
		
		//are we looking for credentials authorized to everyone?
		boolean getEveryone = groups.remove(0);
		
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT DISTINCT RESOURCE_ID FROM USER_RESOURCE " +
				"WHERE (USER_ID = ?");
		if (getEveryone) {
			sqlBuilder.append(" OR (USER_ID IS NULL AND GROUP_ID IS NULL)");
		}
		if (!groups.isEmpty()) {
			sqlBuilder.append(" OR GROUP_ID IN");
			addInts(sqlBuilder, groups);
		}
		sqlBuilder.append(") AND ").append(NOW_IS_BETWEEN_BEGIN_AND_END_TIMES);
		Object[] args = new Object[] {userId};
		
		try {
			return new ArrayList<Resource>(
					template.query(sqlBuilder.toString(), args, 
							new ResourceIdRowMapper()));
		} catch (DataAccessException e) {
			log.error("Persistence error on authorized resource lookup", e);
			return null;
		}
	}
	
	@Override
	public List<Credential> retrieveCredentials(int userId, Set<Integer> groups,
			int resourceId) {
		
		//are we looking for credentials authorized to everyone?
		boolean getEveryone = groups.remove(0);
		
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT DISTINCT c.CREDENTIAL_ID, c.DESCRIPTION ")
			.append("FROM CREDENTIAL as c, USER_RESOURCE as u ")
			.append("WHERE c.CREDENTIAL_ID = u.CREDENTIAL_ID " +
					"AND u.RESOURCE_ID = ? AND ")
			.append(NOW_IS_BETWEEN_BEGIN_AND_END_TIMES("u"))
			.append(" AND ( u.USER_ID = ?");
		if (getEveryone) {
			sqlBuilder.append(" OR (u.USER_ID IS NULL AND u.GROUP_ID IS NULL)");
		}
		if (!groups.isEmpty()) {
			sqlBuilder.append(" OR u.GROUP_ID IN ");
			addInts(sqlBuilder, groups);
		}
		sqlBuilder.append(" )");
		Object[] args = new Object[]{resourceId, userId};
		
		try {
			return new ArrayList<Credential>(
					template.query(sqlBuilder.toString(), args, 
							new CredentialRowMapper()));
		} catch (DataAccessException e) {
			log.error("Persistence error on credential lookup", e);
			return null;
		}
	}
	
	@Override
	public List<Integer> getUserCredentials(int userId, int resourceId) {
		String sql = "SELECT DISTINCT CREDENTIAL_ID FROM USER_RESOURCE " +
				"WHERE RESOURCE_ID = ? AND " +
				"(USER_ID = ? OR (USER_ID IS NULL AND GROUP_ID IS NULL)) AND " + 
				NOW_IS_BETWEEN_BEGIN_AND_END_TIMES;
		Object[] args = new Object[]{resourceId, userId};
		int[] argTypes = new int[]{Types.INTEGER, Types.INTEGER};
		
		try {
			return new ArrayList<Integer>(
					template.query(sql, args, argTypes, new IntRowMapper()));
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<Integer>();
		} catch (DataAccessException e) {
			log.error("Persistence error on credential lookup", e);
			return null;
		}
	}
	
	@Override
	public List<Integer> getGroupCredentials(Set<Integer> groups, 
			int resourceId) {
		//checks if we want to get credentials for the implicit "EVERYONE" 
		// group
		boolean getEveryone = groups.remove(0);
		
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT DISTINCT CREDENTIAL_ID FROM USER_RESOURCE " +
				"WHERE RESOURCE_ID = ? AND " + 
				NOW_IS_BETWEEN_BEGIN_AND_END_TIMES);
		if (getEveryone) {
			sqlBuilder.append("AND ((USER_ID IS NULL AND GROUP_ID IS NULL) " +
					"OR GROUP_ID IN ");
			addInts(sqlBuilder, groups);
			sqlBuilder.append(")");			
		} else {
			sqlBuilder.append("AND GROUP_ID IN ");
			addInts(sqlBuilder, groups);
		}
		Object[] args = new Object[]{resourceId};
		int[] argTypes = new int[]{Types.INTEGER};
		
		try {
			return new ArrayList<Integer>(
					template.query(sqlBuilder.toString(), args, argTypes, 
					new IntRowMapper()));
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<Integer>();
		} catch (DataAccessException e) {
			log.error("Persistence error on credential lookup", e);
			return null;
		}
	}
	
	@Transactional(readOnly = false)
	@Override
	public int addCredential(Credential credential) {
		String sql = "INSERT INTO CREDENTIAL (CREDENTIAL_ID, DESCRIPTION) " +
				"values (?,?)";
		Object[] args = new Object[] {credential.getCredentialId(), 
				credential.getDescription()};
		int[] argTypes = new int[]{Types.INTEGER, Types.VARCHAR};
		
		int updated;
		try {
			updated = this.template.update(sql, args, argTypes);
		} catch (DataAccessException e) {
			log.error("Persistence error on add credential", e);
			return OTHER_ERROR;
		}
		
		//return success for one update, error otherwise
		if (updated != 1) return OTHER_ERROR;
		
		//set parameters
		return setCredentialParameters(credential.getCredentialId(), 
				credential.getParameters());
	}
	
	@Transactional(readOnly = false)
	@Override
	public int updateCredential(int credentialId, String desc, 
			List<CredentialParameter> values) {
		String sql;
		Object[] args;
		
		//updating description
		if (desc != null) {
			sql = "UPDATE CREDENTIAL SET DESCRIPTION = ? " +
					"WHERE CREDENTIAL_ID = ?";
			args = new Object[] {desc, credentialId};
			
			//run update
			try {
				int updated = template.update(sql, args);
				switch (updated) {
				case 1:		//a single record updated - the correct behaviour
					break;
				case 0:		//no records updated (ergo no records match group ID)
					return NO_SUCH_ENTITY;
				default:	//multiple records updated (should not happen - means data corruption)
					log.fatal(updated + " records updated on query \"" + sql + "\".\n" +
								"Data corruption likely.");
					return DATA_CORRUPTION;
				}
			} catch (DataAccessException e) {
				log.error("Persistence error on credential update", e);
				return OTHER_ERROR;
			}
		}
		
		//updating values
		if (values != null) {
			//first clear existing values
			sql = "DELETE FROM CREDENTIAL_PARAMETER WHERE CREDENTIAL_ID = ?";
			args = new Object[] {credentialId};
			this.template.update(sql, args);
			
			//then set new values
			return setCredentialParameters(credentialId, values);
		}
		
		return SUCCESS;
	}
	
	@Transactional(readOnly = false)
	@Override
	public int removeCredential(int credentialId) {
		String sql = "DELETE FROM CREDENTIAL WHERE CREDENTIAL_ID = ?";
		Object[] args = new Object[] {credentialId};
		
		try {
			this.template.update(sql, args);
		} catch (DataAccessException e) {
			log.error("Persistence error on remove credential", e);
			return OTHER_ERROR;
		}
		
		return SUCCESS;
	}
	
	@Transactional(readOnly = false)
	@Override
	public int addAuthorization(int resourceId, int userId, int groupId,
			int credentialId, List<CredentialAuthorizationRight> rights,
			Date beginTime, Date endTime) {
		
		String sql = "INSERT INTO USER_RESOURCE " +
				"(RESOURCE_ID, USER_ID, GROUP_ID, CREDENTIAL_ID, RIGHTS, " +
				"BEGIN_TIME, END_TIME) " +
				"values (?,?,?,?,?,?,?)";
		Object[] args = new Object[] {resourceId, nullableInt(userId), 
				nullableInt(groupId), credentialId, 
				CredentialAuthorizationRowMapper.toString(rights),
				nullableDate(beginTime), nullableDate(endTime)};
		int[] argTypes = new int[]{Types.INTEGER, Types.INTEGER, Types.INTEGER, 
				Types.INTEGER, Types.CHAR, nullableType(beginTime), 
				nullableType(endTime)};
		
		int updated;
		try {
			updated = this.template.update(sql, args, argTypes);
		} catch (DataAccessException e) {
			log.error("Persistence error on add credential authorization", e);
			return OTHER_ERROR;
		}
		
		//return success for one update, error otherwise
		return updated == 1 ? SUCCESS : OTHER_ERROR;
	}
	
	@Transactional(readOnly = false)
	@Override
	public int removeAuthorization(int resourceId, int userId, int groupId, 
			int credentialId) {
		
		StringBuilder sqlBuilder = new StringBuilder();
		Object[] args = null; int[] argTypes = null;
		sqlBuilder.append("DELETE FROM USER_RESOURCE WHERE RESOURCE_ID = ? " +
				"AND CREDENTIAL_ID = ? ");
		if (userId == 0 && groupId == 0) {
			sqlBuilder.append("AND USER_ID is null AND GROUP_ID is null");
			args = new Object[]{resourceId, credentialId};
			argTypes = new int[]{Types.INTEGER, Types.INTEGER};
		} else if (userId != 0) {
			sqlBuilder.append("AND USER_ID = ? AND GROUP_ID is null");
			args = new Object[]{resourceId, credentialId, userId};
			argTypes = new int[]{Types.INTEGER, Types.INTEGER, Types.INTEGER};
		} else if (groupId != 0) {
			sqlBuilder.append("AND USER_ID is null AND GROUP_ID = ?");
			args = new Object[]{resourceId, credentialId, groupId};
			argTypes = new int[]{Types.INTEGER, Types.INTEGER, Types.INTEGER};
		}
		
		try {
			this.template.update(sqlBuilder.toString(), args, argTypes);
		} catch (DataAccessException e) {
			log.error("Persistence error on remove credential authorization", e);
			return OTHER_ERROR;
		}
		
		//return success
		return SUCCESS;
	}
	
	@Override
	public List<CredentialAuthorization> getAuthorizations(int credentialId) {
		String sql = "SELECT RESOURCE_ID, USER_ID, GROUP_ID, RIGHTS, BEGIN_TIME, END_TIME "
			+ "FROM USER_RESOURCE WHERE CREDENTIAL_ID = ?";
		Object[] args = new Object[]{credentialId};
		
		try {
			return new ArrayList<CredentialAuthorization>(
					template.query(sql, args, new CredentialAuthorizationRowMapper()));
		} catch (DataAccessException e) {
			log.error("Persistence error on credential authorization lookup", e);
			return null;
		}
	}
	
	@Override
	public List<CredentialAuthorization> getAuthorizationsByResource(
			int credentialId, int resourceId) {
		String sql = "SELECT RESOURCE_ID, USER_ID, GROUP_ID, RIGHTS, BEGIN_TIME, END_TIME "
			+ "FROM USER_RESOURCE WHERE RESOURCE_ID = ? AND CREDENTIAL_ID = ?";
		Object[] args = new Object[]{resourceId, credentialId};
		
		try {
			return new ArrayList<CredentialAuthorization>(
					template.query(sql, args, new CredentialAuthorizationRowMapper()));
		} catch (DataAccessException e) {
			log.error("Persistence error on credential authorization lookup", e);
			return null;
		}
	}
	
	@Override
	public CredentialSchema getSchemaByResource(int resourceId) {
		String sql = "SELECT c.CREDENTIAL_SCHEMA_ID, c.DESCRIPTION " +
				"FROM CREDENTIAL_SCHEMA AS c, RESOURCE_CREDENTIAL_SCHEMA AS r " +
				"WHERE r.CREDENTIAL_SCHEMA_ID = c.CREDENTIAL_SCHEMA_ID AND " +
				"r.RESOURCE_ID = ?";
		Object[] args = new Object[]{resourceId };
		
		try {
    		return (CredentialSchema) this.template.queryForObject(
    				sql, args, new CredentialSchemaRowMapper());
    	}catch (EmptyResultDataAccessException e){
    		return null;
    	}
	}
	
	@Override
	public void setCredentialSchema(int resourceId, int schemaId) {
		String sql = "INSERT INTO RESOURCE_CREDENTIAL_SCHEMA " +
				"(RESOURCE_ID, CREDENTIAL_SCHEMA_ID) VALUES (?, ?)";
		Object[] args = new Object[] {resourceId, schemaId};
		int[] argTypes = new int[] {Types.INTEGER, Types.INTEGER};
		
		try {
			this.template.update(sql, args, argTypes);
		} catch (DataAccessException e) {
			log.error("Persistence error on credential schema setting", e);
		}
	}
	
	@Override
	public List<Integer> getResourceForCredential(int credentialId) {
		String sql = "SELECT RESOURCE_ID FROM USER_RESOURCE WHERE CREDENTIAL_ID = ?";
		Object[] args = new Object[] {credentialId};
		
		try {
			return new ArrayList<Integer>(template.query(sql, args, new IntRowMapper()));
		} catch (DataAccessException e) {
			log.error("Persistence error on credential resource lookup", e);
			return null;
		}
	}
	
	@Override
	public int getNextCredentialId() {
		String sql = "SELECT MAX(CREDENTIAL_ID) + 1 FROM CREDENTIAL";
		
		try {
			//get DNames matching given
			return this.template.queryForInt(sql);
		} catch (DataAccessException e) {
			log.error("Data access error on max credentialID lookup", e);
			return 0;
		}
	}
	
	/**
	 * Gets parameters for a credential, given its credential ID
	 */
	protected List<CredentialParameter> getCredentialParameters(int credId) {
		String sql = "SELECT PARAMETER_ID, PARAMETER_VALUE FROM CREDENTIAL_PARAMETER "
			+ "WHERE CREDENTIAL_ID = ?";
		Object[] args = new Object[]{credId};
		
		try {
			return new ArrayList<CredentialParameter>(
					template.query(sql, args, new CredentialParameterRowMapper()));
		} catch (DataAccessException e) {
			log.error("Persistence error on credential parameter lookup", e);
			return null;
		}
	}
	
	/**
	 * Sets credential parameters
	 * @param credId	The credential to set them on
	 * @param values	The values to set - assumes at least one element
	 * @return 0 for success, !=0 for not success
	 */
	@Transactional(readOnly = false)
	protected int setCredentialParameters(int credId, List<CredentialParameter> values) {
		StringBuilder sql = new StringBuilder();
		Object[] args = new Object[values.size() * 3];
		int[] argTypes = new int[values.size() * 3];
		
		sql.append("INSERT INTO CREDENTIAL_PARAMETER " +
				"(CREDENTIAL_ID, PARAMETER_ID, PARAMETER_VALUE) values (?,?,?)");
		
		Iterator<CredentialParameter> iter = values.iterator();
		CredentialParameter p = iter.next();
		
		args[0] = credId;
		args[1] = p.getParameter().getId();
		args[2] = p.getValue();
		
		argTypes[0] = Types.INTEGER;
		argTypes[1] = Types.CHAR;
		argTypes[2] = Types.VARCHAR;
		
		for (int i = 1; i < values.size(); i++) {
			p = iter.next();
			
			sql.append(",(?,?,?)");
			
			args[3*i] = credId;
			args[3*i+1] = p.getParameter().getId();
			args[3*i+2] = p.getValue();
			
			argTypes[3*i] = Types.INTEGER;
			argTypes[3*i+1] = Types.CHAR;
			argTypes[3*i+2] = Types.VARCHAR;
		}
		
		try {
			this.template.update(sql.toString(), args, argTypes);
		} catch (DataAccessException e) {
			log.error("Persistence error on add credential parameter", e);
			return OTHER_ERROR;
		}
		
		return SUCCESS;
	}
	
	/**
	 * Gets parameters for a credential schema, given its ID
	 * @param schemaId	The ID of the credential schema
	 */
	protected List<CredentialSchemaParameter> getCredentialSchemaParameters(int schemaId) {
		String sql = "SELECT PARAMETER_ID, IS_REQUIRED, ALLOW_MULTIPLE " +
				"FROM CREDENTIAL_SCHEMA_PARAMETER WHERE CREDENTIAL_SCHEMA_ID = ?";
		Object[] args = new Object[]{schemaId};
		
		try {
			return new ArrayList<CredentialSchemaParameter>(
					template.query(sql, args, new CredentialSchemaParameterRowMapper()));
		} catch (DataAccessException e) {
			log.error("Persistence error on credential schema parameter lookup", e);
			return null;
		}
	}
	
	public ITypesDAO getLookUpDAO() {
		return lookUpDAO;
	}

	public void setLookUpDAO(ITypesDAO lookUpDAO) {
		this.lookUpDAO = lookUpDAO;
	}
	
	public IResourceDAO getResourceDAO() {
		return resourceDAO;
	}

	public void setResourceDAO(IResourceDAO resourceDAO) {
		this.resourceDAO = resourceDAO;
	}
}
