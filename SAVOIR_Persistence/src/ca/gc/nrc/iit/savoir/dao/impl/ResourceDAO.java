// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.iit.nrc.savoir.domain.Constraint;
import ca.gc.iit.nrc.savoir.domain.Person;
import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.iit.nrc.savoir.domain.ResourcePreference;
import ca.gc.nrc.iit.savoir.dao.BaseDAO;
import ca.gc.nrc.iit.savoir.dao.IConstraintDAO;
import ca.gc.nrc.iit.savoir.dao.IParametersDAO;
import ca.gc.nrc.iit.savoir.dao.IPersonDAO;
import ca.gc.nrc.iit.savoir.dao.IResourceDAO;
import ca.gc.nrc.iit.savoir.dao.ISiteDAO;
import ca.gc.nrc.iit.savoir.dao.ITypesDAO;
import ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.IntRowMapper;

import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.MAX_NAME_LENGTH;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.MAX_DESC_LENGTH;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.nullableInt;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.nullableType;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.truncate;

@Transactional(readOnly = true, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class ResourceDAO extends BaseDAO implements IResourceDAO {

	private IParametersDAO paramDAO;
	
	private IPersonDAO personDAO;

	private ITypesDAO lookUpDAO;

	private ISiteDAO siteDAO;

	private IConstraintDAO constraintDAO;

	protected final static Log log = LogFactory.getLog(ResourceDAO.class);
	
	private static final int GET_SESSION_ID_FROM_DATA = -1;

	// warning: this RowMapper gets the default values for the parameters
	// anonymous inner class to map recordset rows to Resource objects
	class ResourceRowMapper implements RowMapper {

		private int sessionID = 0;

		public ResourceRowMapper() {
		}

		public ResourceRowMapper(int sessionID) {
			this.sessionID = sessionID;
		}

		public Resource mapRow(ResultSet rs, int rowNum) throws SQLException {
			int sId = (sessionID == GET_SESSION_ID_FROM_DATA) ? 
					rs.getInt("SESSION_ID")
					: sessionID;
			
			Resource r = new Resource();
			int resourceId = rs.getInt("RESOURCE_ID");
			
			r.setResourceID(resourceId);
			r.setDescription(rs.getString("DESCRIPTION"));
			r.setResourceName(rs.getString("RESOURCE_NAME"));
			r.setContact(personDAO.getPersonById(
					rs.getInt("CONTACT_PERSON_ID")));
			r.setParameters(paramDAO.getParametersByResourceIDAndSessionID(
					resourceId, sId));
			if (sId != 0) {
				r.setDefaultParameters(
						paramDAO.getDefaultParametersByResourceID(resourceId));
			}
			r.setResourceType(lookUpDAO.getResourceTypeById(rs
					.getString("RESOURCE_TYPE_ID")));
			r.setConstraints(constraintDAO.getConstraintsByResourceID(
					resourceId));
			return r;
		}
	}
	
	/**
	 * Data struct pairing resource ID with user preference state
	 */
	private static class ResourcePreferenceElement {
		/** User's ID */
		public int userId; 
		/** ID of resource */
		public int resourceId;
		/** User's preference on it */
		public ResourcePreference preference;
	}
	
	private static class ResourcePreferenceRowMapper implements RowMapper {
		
		public ResourcePreferenceElement mapRow(ResultSet rs, int rowNum) 
			throws SQLException {
			
			ResourcePreferenceElement rp = new ResourcePreferenceElement();
			
			rp.userId = rs.getInt("USER_ID");
			rp.resourceId = rs.getInt("RESOURCE_ID");
			ResourcePreference pref;
			try {
				pref = ResourcePreference.valueOf(rs.getString("SELECTED"));
			} catch (IllegalArgumentException e) {
				pref = null;
			} catch (NullPointerException e) {
				pref = null;
			}
			rp.preference = pref;
			
			return rp;
		}
	}

	@Transactional(readOnly = false)
	@Override
	public int addResource(Resource r) {

		String sql = "INSERT INTO RESOURCE (RESOURCE_TYPE_ID, RESOURCE_NAME, " +
				"CONTACT_PERSON_ID, DESCRIPTION) VALUES (?,?,?,?)";

		Person contact = r.getContact();
		int contactId = (contact == null) ? 0 : contact.getPersonId();
		
		Object[] args = new Object[] { 
				r.getResourceType().getId(),
				truncate(r.getResourceName(), MAX_NAME_LENGTH),
				nullableInt(contactId),
				truncate(r.getDescription(), MAX_DESC_LENGTH) };
		int[] argTypes = new int[] { Types.VARCHAR, Types.VARCHAR, 
				nullableType(contactId), Types.VARCHAR };
		this.template.update(sql, args, argTypes);

		r.setResourceID(this.template.queryForInt("SELECT LAST_INSERT_ID()"));

		paramDAO.saveParameters(r.getResourceID(), r.getParameters(), 0);
		List<Constraint> constraints = r.getConstraints();
		if (constraints != null) for (Constraint constraint : constraints) {
			constraintDAO.addConstraint(constraint);
		}

		return r.getResourceID();

	}
	
	@Transactional(readOnly = false)
	@Override
	public int removeResource(int resourceId) {
		constraintDAO.removeConstraintsByResourceID(resourceId);
		
		String sql = "DELETE FROM SAVOIR.RESOURCE WHERE RESOURCE_ID = ?";
		Object[] args = new Object[] { resourceId };
		return this.template.update(sql, args);
		
	}
	
	@Transactional(readOnly = false)
	@Override
	public int updateResource(Resource r) {
		
		String sql = 
			"UPDATE SAVOIR.RESOURCE " +
				"SET RESOURCE_TYPE_ID = ?, RESOURCE_NAME = ?, " +
				"CONTACT_PERSON_ID = ?, DESCRIPTION = ? " +
				"WHERE RESOURCE_ID = ?";
		
		Person contact = r.getContact();
		int contactId = (contact == null) ? 0 : contact.getPersonId();
		
		Object[] args = new Object[]{ r.getResourceType().getId(), 
				truncate(r.getResourceName(), MAX_NAME_LENGTH), 
				nullableInt(contactId), 
				truncate(r.getDescription(), MAX_DESC_LENGTH), 
				r.getResourceID()};
		int[] argTypes = new int[]{ Types.CHAR, Types.VARCHAR, 
				nullableType(contactId), Types.VARCHAR, Types.INTEGER};
		
		int updated = this.template.update(sql, args, argTypes);
		
		switch (updated) {
		case 0:		//no resource updated, ergo none such exists
			return updated;

		case 1:		//one resource updated - correct behaviour
			
			//clear and reset parameters and constraints to new values
			paramDAO.clearParameters(r.getResourceID(), 0);
			paramDAO.saveParameters(r.getResourceID(), r.getParameters(), 0);
			constraintDAO.removeConstraintsByResourceID(r.getResourceID());
			List<Constraint> constraints = r.getConstraints();
			if (constraints != null) for (Constraint constraint : constraints) {
				constraintDAO.addConstraint(constraint);
			}
			
			return updated;
		
		default:	//multiple resources updated - likely data corruption
			log.fatal(updated + " records updated on query \"" + sql + "\".\n" +
				"Data corruption likely.");
			return updated;
		}
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Resource> getResourcesBySessionID(int sessionID) {
		String sql = "SELECT R.*, RS.SESSION_ID " 
				+ "FROM RESOURCE AS R, SESSION_RESOURCE AS RS "
				+ "WHERE R.RESOURCE_ID = RS.RESOURCE_ID AND " 
				+ "(RS.SESSION_ID = ? OR RS.SESSION_ID IN " 
				+ "(SELECT SUBSESSION_ID FROM SESSION_RELATIONSHIP WHERE " 
				+ "MASTER_SESSION_ID = ?))";
		log.info(sql);
		Object[] args = new Object[] { sessionID, sessionID };
		try {
			List<Resource> list;
			list = (List<Resource>) this.template.query(sql, args,
					new ResourceRowMapper(GET_SESSION_ID_FROM_DATA));
			log.info(list);
			return list;
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<Resource>();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getResourceSubsessionId(int resourceId, 
			int sessionId) {
		String sql = "SELECT RS.SESSION_ID "
			+ "FROM SESSION_RESOURCE AS RS, SESSION_RELATIONSHIP AS SR "
			+ "WHERE RS.RESOURCE_ID = ? AND RS.SESSION_ID = SR.SUBSESSION_ID "
			+ "AND SR.MASTER_SESSION_ID = ?";
		Object[] args = new Object[] { resourceId, sessionId };
		try {
			List<Integer> list;
			list = (List<Integer>) this.template.query(sql, args,
					new IntRowMapper());
			return list;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public Resource getResourceBySubsessionId(int subsessionId) {
		String sql = "SELECT R.* FROM RESOURCE AS R, SESSION_RESOURCE AS RS "
			+ "WHERE R.RESOURCE_ID = RS.RESOURCE_ID AND RS.SESSION_ID = ?"; 
	log.info(sql);
	Object[] args = new Object[] { subsessionId };
	try {
		Resource r;
		r = (Resource) this.template.queryForObject(sql, args,
				new ResourceRowMapper(subsessionId));
		return r;
	} catch (EmptyResultDataAccessException e) {
		return null;
	}
	}
	
	@Transactional(readOnly = false)
	@Override
	public void addResourceToSession(int resourceId, int sessionId) {
		String sql = "INSERT INTO SAVOIR.SESSION_RESOURCE " +
				"(SESSION_ID, RESOURCE_ID) VALUES (?,?)";
		Object[] args = new Object[]{sessionId, resourceId};
		int[] argTypes = new int[]{Types.INTEGER, Types.INTEGER};
		
		this.template.update(sql, args, argTypes);
	}

	@Override
	public Resource getResourceById(int resourceId) {
		String sql = "SELECT * FROM RESOURCE WHERE RESOURCE_ID = ?";
		Object[] args = new Object[] { resourceId };
		try {
			Resource r;
			r = (Resource) this.template.queryForObject(sql, args,
					new ResourceRowMapper());
			return r;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Resource> getResourceByIdAndSessionID(int resourceId, 
			int sessionId) {
		
		String sql = "SELECT R.*, SR.SESSION_ID "
			+ "FROM RESOURCE AS R, SESSION_RESOURCE AS SR "
			+ "WHERE R.RESOURCE_ID = ? AND R.RESOURCE_ID = SR.RESOURCE_ID "
			+ "AND SR.SESSION_ID IN (SELECT SUBSESSION_ID " 
			+ "FROM SESSION_RELATIONSHIP WHERE MASTER_SESSION_ID = ?)";
		Object[] args = new Object[] { resourceId, sessionId };
		try {
			List<Resource> r;
			r = (List<Resource>) this.template.query(sql, args,
					new ResourceRowMapper(GET_SESSION_ID_FROM_DATA));
			return r;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Resource> getResourcesByType(String typeID) {
		String sql = "SELECT * FROM RESOURCE WHERE RESOURCE_TYPE_ID LIKE ?";
		log.info(sql);
		Object[] args = new Object[] { typeID };
		try {
			List<Resource> list;
			list = (List<Resource>) this.template.query(sql, args,
					new ResourceRowMapper());
			log.info(list);
			return list;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Resource> getResourcesByTypeAndParameterValue(String typeID,
			String parameterID, String parameterValue) {
		String sql = "SELECT DISTINCT R.* FROM RESOURCE AS R,RESOURCE_PARAMETER AS T "
				+ "WHERE R.RESOURCE_TYPE_ID LIKE ? "
				+ "AND R.RESOURCE_ID = T.RESOURCE_ID "
				+ "AND T.PARAMETER_ID LIKE ? " + "AND T.PARAMETER_VALUE LIKE ?";

		if (parameterID.equals("SITE_LOCATION")) {
			sql = "SELECT DISTINCT R.* FROM RESOURCE AS R,RESOURCE_PARAMETER AS T "
					+ "WHERE R.RESOURCE_TYPE_ID LIKE ? "
					+ "AND R.RESOURCE_ID = T.RESOURCE_ID "
					+ "AND T.PARAMETER_ID LIKE ? "
					+ "AND T.PARAMETER_VALUE IN (SELECT SITE_ID FROM SITE WHERE SITE_DESCRIPTION = ?)";
		}

		Object[] args = new Object[] { typeID, parameterID, parameterValue };
		try {
			List<Resource> list;
			list = (List<Resource>) this.template.query(sql, args,
					new ResourceRowMapper());
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Resource> getAPNReservedBetween(Calendar startTime,
			Calendar endTime) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		String sql = "SELECT DISTINCT R.* FROM RESOURCE AS R, CALENDAR AS C "
				+ "WHERE C.RESOURCE_ID = R.RESOURCE_ID AND ("

				+ "(C.START_TIME <= '"
				+ formatter.format(startTime.getTime())
				+ "' AND '"
				+ formatter.format(endTime.getTime())
				+ "' <= C.END_TIME ) "
				+ " OR "
				+

				" (C.START_TIME <= '"
				+ formatter.format(startTime.getTime())
				+ "' AND '"
				+ formatter.format(endTime.getTime())
				+ "' >= C.END_TIME 	AND '"
				+ formatter.format(startTime.getTime())
				+ "' < C.END_TIME)"
				+

				" OR "
				+

				" (C.START_TIME >= '"
				+ formatter.format(startTime.getTime())
				+ "' AND '"
				+ formatter.format(endTime.getTime())
				+ "' <= C.END_TIME  AND '"
				+ formatter.format(endTime.getTime())
				+ "' > C.START_TIME)"
				+

				" OR "
				+

				" (C.START_TIME >= '"
				+ formatter.format(startTime.getTime())
				+ "' AND '"
				+ formatter.format(endTime.getTime())
				+ "' >= C.END_TIME)" +
						")";

		log.info(sql);
		try {
			List<Resource> list;
			list = (List<Resource>) this.template.query(sql, new ResourceRowMapper());
			return list;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<Integer, ResourcePreference> getUserPreferences(int userId) {
		String sql = "SELECT * FROM SAVOIR.USER_RESOURCE_PREFERENCE " +
				"WHERE USER_ID = ?";
		log.info(sql);
		Object[] args = new Object[]{userId};
		int[] argTypes = new int[]{Types.INTEGER};
		
		try {
			List<ResourcePreferenceElement> list = this.template.query(
					sql, args, argTypes, new ResourcePreferenceRowMapper());
			
			if (list == null) return null;
			Map<Integer, ResourcePreference> map = 
				new LinkedHashMap<Integer, ResourcePreference>();
			for (ResourcePreferenceElement el : list) {
				map.put(el.resourceId, el.preference);
			}
			return map;
		} catch (EmptyResultDataAccessException e) {
			return new LinkedHashMap<Integer, ResourcePreference>();
		} catch (DataAccessException e) {
			log.error("Error on get user preferences query", e);
			return null;
		}
	}
	
	@Override
	public ResourcePreference getUserPreferenceForResource(int userId, 
			int resourceId) {
		String sql = "SELECT * FROM SAVOIR.USER_RESOURCE_PREFERENCE " +
				"WHERE USER_ID = ? AND RESOURCE_ID = ?";
		log.info(sql);
		Object[] args = new Object[]{userId, resourceId};
		int[] argTypes = new int[]{Types.INTEGER, Types.INTEGER};
		
		try {
			ResourcePreferenceElement el = 
				(ResourcePreferenceElement)this.template.queryForObject(
					sql, args, argTypes, new ResourcePreferenceRowMapper());
			return (el == null) ? null : el.preference;
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (DataAccessException e) {
			log.error("Error on get user preferences query", e);
			return null;
		}
	}
	
	@Transactional(readOnly = false)
	@Override
	public void addUserPreference(int userId, int resourceId, 
			ResourcePreference state) {
		
		String sql = "INSERT INTO SAVOIR.USER_RESOURCE_PREFERENCE " +
				"VALUES (?, ?, ?)";
		log.info(sql);
		Object[] args = new Object[]{userId, resourceId, state.toString()};
		int[] argTypes = new int[]{Types.INTEGER, Types.INTEGER, Types.VARCHAR};
		
		this.template.update(sql, args, argTypes);
	}
	
	@Transactional(readOnly = false)
	@Override
	public void updateUserPreference(int userId, int resourceId, 
			ResourcePreference state) {
		
		String sql = "UPDATE SAVOIR.USER_RESOURCE_PREFERENCE " +
				"SET SELECTED = ? WHERE USER_ID = ? AND RESOURCE_ID = ?";
		log.info(sql);
		Object[] args = new Object[]{state.toString(), userId, resourceId};
		int[] argTypes = new int[]{Types.VARCHAR, Types.INTEGER, Types.INTEGER};
		
		this.template.update(sql, args, argTypes);
	}
	
	@Transactional(readOnly = false)
	@Override
	public void removeUserPreference(int userId, int resourceId) {
		String sql = "DELETE FROM SAVOIR.USER_RESOURCE_PREFERENCE " +
				"WHERE USER_ID = ? AND RESOURCE_ID = ?";
		log.info(sql);
		Object[] args = new Object[]{userId, resourceId};
		int[] argTypes = new int[]{Types.INTEGER, Types.INTEGER};
		
		this.template.update(sql, args, argTypes);
	}

	public IParametersDAO getParamDAO() {
		return paramDAO;
	}
	
	public IPersonDAO getPersonDAO() {
		return personDAO;
	}

	public ITypesDAO getLookUpDAO() {
		return lookUpDAO;
	}

	public void setParamDAO(IParametersDAO paramDAO) {
		this.paramDAO = paramDAO;
	}
	
	public void setPersonDAO(IPersonDAO personDAO) {
		this.personDAO = personDAO;
	}

	public void setLookUpDAO(ITypesDAO lookUpDAO) {
		this.lookUpDAO = lookUpDAO;
	}

	public ISiteDAO getSiteDAO() {
		return siteDAO;
	}

	public void setSiteDAO(ISiteDAO siteDAO) {
		this.siteDAO = siteDAO;
	}

	public IConstraintDAO getConstraintDAO() {
		return constraintDAO;
	}

	public void setConstraintDAO(IConstraintDAO constraintDAO) {
		this.constraintDAO = constraintDAO;
	}
	
}
