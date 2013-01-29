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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.iit.nrc.savoir.domain.Connection;
import ca.gc.iit.nrc.savoir.domain.Group;
import ca.gc.iit.nrc.savoir.domain.Participant;
import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.iit.nrc.savoir.domain.Role;
import ca.gc.iit.nrc.savoir.domain.Session;
import ca.gc.iit.nrc.savoir.domain.SessionAuthorization;
import ca.gc.iit.nrc.savoir.domain.TimeSlot;
import ca.gc.nrc.iit.savoir.dao.BaseDAO;
import ca.gc.nrc.iit.savoir.dao.IConnectionDAO;
import ca.gc.nrc.iit.savoir.dao.IGroupDAO;
import ca.gc.nrc.iit.savoir.dao.IRoleDAO;
import ca.gc.nrc.iit.savoir.dao.ISessionDAO;
import ca.gc.nrc.iit.savoir.dao.IUserDAO;
import ca.gc.nrc.iit.savoir.dao.impl.AuthorizationDAO.AuthorizationRowMapper;
import ca.gc.nrc.iit.savoir.dao.impl.AuthorizationDAO.IntRole;
import ca.gc.nrc.iit.savoir.dao.impl.AuthorizationDAO.IntRoleRowMapper;
import ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.IntRowMapper;

import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.addInts;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.MAX_NAME_LENGTH;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.MAX_DESC_LENGTH;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.getNullableDate;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.nullableCalendar;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.nullableInt;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.nullableType;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.truncate;
import static ca.gc.nrc.iit.savoir.utils.Constants.INVALID_PARAMETERS;
import static ca.gc.nrc.iit.savoir.utils.Constants.OTHER_ERROR;
import static ca.gc.nrc.iit.savoir.utils.Constants.SUCCESS;

@Transactional(readOnly = true, propagation = Propagation.REQUIRED,	isolation = Isolation.READ_COMMITTED)
@SuppressWarnings("unchecked")
public class SessionDAO extends BaseDAO implements ISessionDAO {

	private IUserDAO userDAO;
	
	private IGroupDAO groupDAO;

	private IConnectionDAO connectionDAO;

	private IRoleDAO roleDAO;
	
	// the solution to retrieve datetimes stored in GMT format is temporary, we
	// need to find a better solution that doesn't use the resource intensive
	// split and parseInt functions
	class TimeSlotRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			TimeSlot ts = new TimeSlot();
			ts.setId(rs.getInt("CALENDAR_ID"));

			Calendar startTime = Calendar.getInstance(TimeZone
					.getTimeZone("UTC"));

			java.sql.Date sqlDate = rs.getDate("START_TIME");
			java.sql.Time sqlTime = rs.getTime("START_TIME");

			startTime.set(Integer.parseInt(sqlDate.toString().split("-")[0]),
					Integer.parseInt(sqlDate.toString().split("-")[1]) - 1,
					Integer.parseInt(sqlDate.toString().split("-")[2]), Integer
							.parseInt(sqlTime.toString().split(":")[0]),
					Integer.parseInt(sqlTime.toString().split(":")[1]), Integer
							.parseInt(sqlTime.toString().split(":")[2]));
			startTime.set(Calendar.MILLISECOND,0);
			
			ts.setStartTime(startTime);
			Calendar endTime = Calendar
					.getInstance(TimeZone.getTimeZone("UTC"));

			sqlDate = rs.getDate("END_TIME");
			sqlTime = rs.getTime("END_TIME");

			endTime.set(Integer.parseInt(sqlDate.toString().split("-")[0]),
					Integer.parseInt(sqlDate.toString().split("-")[1]) - 1,
					Integer.parseInt(sqlDate.toString().split("-")[2]), Integer
							.parseInt(sqlTime.toString().split(":")[0]),
					Integer.parseInt(sqlTime.toString().split(":")[1]), Integer
							.parseInt(sqlTime.toString().split(":")[2]));
			endTime.set(Calendar.MILLISECOND,0);
			
			ts.setEndTime(endTime);
			return ts;
		}
	}

	// inner class to map recordset rows to Participant objects
	class ParticipantRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			Participant p = new Participant();
			p.setStatus(rs.getString("PARTICIPANT_STATUS"));
//			Commented out 09-11-2009 by Aaron as part of SM/UM integration
//			p.setRole(rs.getString("PARTICIPANT_ROLE"));
			p.setUser(userDAO.getUserById(rs.getInt("USER_ID")));
			return p;
		}
	}
	
	// inner class to map group IDs to Group objects
	class GroupIdRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			return groupDAO.getGroupById(rs.getInt(1));
		}
	}
	
	// inner class to map recordset rows to SessionAuthorization objects
	// the first field should be the session ID (integer)
	// the second should be a user or group ID (integer)
	// the third should be a role ID defining the rights on the session (integer)
	class SessionAuthorizationRowMapper 
		extends AuthorizationRowMapper<SessionAuthorization> {
		
		public SessionAuthorizationRowMapper() {
			super(SessionDAO.this.roleDAO, SessionAuthorization.class);
		}
	}

	// inner class to map recordset rows to Session objects
	class SessionRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			Session s = new Session();
			int sessionId = rs.getInt("SESSION_ID");
			
			s.setName(rs.getString("SESSION_NAME"));
			s.setSessionID(sessionId);
			s.setDescription(rs.getString("SESSION_DESCRIPTION"));
			s.setRequestedBy(userDAO.getUserById(rs.getInt("USER_ID")));
			s.setConnections(connectionDAO.getConnectionsBySessionID(sessionId));
			s.setSubSessions(getSubsessions(sessionId));
			s.setStatus(rs.getString("SESSION_STATUS"));
			s.setAuthorizedUsers(getSessionParticipants(sessionId));
			s.setAuthorizedGroups(getSessionAuthorizedGroups(sessionId));
			//rs.getInt returns 0 for SQL NULL, I want Java null for SQL NULL
			int scenarioId = rs.getInt("SCENARIO_ID");
			s.setScenarioId(scenarioId == 0 ? null : scenarioId);

			Date startDate = getNullableDate(rs, "REQUESTED_START_TIME");
			if (startDate != null) {
				Calendar rStartTime = Calendar.getInstance();
				rStartTime.setTime(startDate);
				s.setRequestedStartTime(rStartTime);
			}
//			if (rs.getTimestamp("REQUESTED_START_TIME") != null) {
//				java.sql.Date sqlDate = rs.getDate("REQUESTED_START_TIME");
//				java.sql.Time sqlTime = rs.getTime("REQUESTED_START_TIME");
//
//				Calendar rStartTime = Calendar.getInstance(TimeZone
//						.getTimeZone("UTC"));
//				rStartTime.set(Integer
//						.parseInt(sqlDate.toString().split("-")[0]), Integer
//						.parseInt(sqlDate.toString().split("-")[1]) - 1,
//						Integer.parseInt(sqlDate.toString().split("-")[2]),
//						Integer.parseInt(sqlTime.toString().split(":")[0]),
//						Integer.parseInt(sqlTime.toString().split(":")[1]),
//						Integer.parseInt(sqlTime.toString().split(":")[2]));
//
//				rStartTime.set(Calendar.MILLISECOND,0);
//				
//				s.setRequestedStartTime(rStartTime);
//			}

			Date endDate = getNullableDate(rs, "REQUESTED_END_TIME");
			if (endDate != null) {
				Calendar rEndTime = Calendar.getInstance();
				rEndTime.setTime(endDate);
				s.setRequestedEndTime(rEndTime);
			}
//			if (rs.getTimestamp("REQUESTED_END_TIME") != null) {
//				java.sql.Date sqlDate = rs.getDate("REQUESTED_END_TIME");
//				java.sql.Time sqlTime = rs.getTime("REQUESTED_END_TIME");
//
//				Calendar rEndTime = Calendar.getInstance(TimeZone
//						.getTimeZone("UTC"));
//				rEndTime.set(
//						Integer.parseInt(sqlDate.toString().split("-")[0]),
//						Integer.parseInt(sqlDate.toString().split("-")[1]) - 1,
//						Integer.parseInt(sqlDate.toString().split("-")[2]),
//						Integer.parseInt(sqlTime.toString().split(":")[0]),
//						Integer.parseInt(sqlTime.toString().split(":")[1]),
//						Integer.parseInt(sqlTime.toString().split(":")[2]));
//
//				rEndTime.set(Calendar.MILLISECOND,0);
//				
//				s.setRequestedEndTime(rEndTime);
//			}

			Date submissionDate = getNullableDate(rs, "SUBMISSION_TIME");
			if (submissionDate != null) {
				Calendar submissionTime = Calendar.getInstance();
				submissionTime.setTime(submissionDate);
				s.setSubmissionDate(submissionTime);
			}
//			if (rs.getTimestamp("SUBMISSION_TIME") != null) {
//				java.sql.Date sqlDate = rs.getDate("SUBMISSION_TIME");
//				java.sql.Time sqlTime = rs.getTime("SUBMISSION_TIME");
//
//				Calendar submissionDate = Calendar.getInstance(TimeZone
//						.getTimeZone("UTC"));
//				submissionDate.set(Integer.parseInt(sqlDate.toString().split(
//						"-")[0]), Integer.parseInt(sqlDate.toString()
//						.split("-")[1]) - 1, Integer.parseInt(sqlDate
//						.toString().split("-")[2]), Integer.parseInt(sqlTime
//						.toString().split(":")[0]), Integer.parseInt(sqlTime
//						.toString().split(":")[1]), Integer.parseInt(sqlTime
//						.toString().split(":")[2]));
//
//				s.setSubmissionDate(submissionDate);
//			}
			if (isScheduled(sessionId)) {
				s.setTimeSlot(getScheduledTime(sessionId));
				s.setAccepted(true);
			}
			return s;
		}
	}

	@Override
	public List<Session> getSessionsByStatus(String... status) {
		String sql = "SELECT S.* FROM SESSION AS S " + "WHERE ";
		for (int i = 0; i < status.length; i++) {
			if (i == 0)
				sql += "S.SESSION_STATUS = ? ";
			else
				sql += "OR S.SESSION_STATUS = ? ";
		}
		log.info(sql);
//		Object[] args = new Object[status.length];
//		for(int j = 0; j < args.length; j++){
//			args[j] = status[j];
//		}
		try {
			List<Session> list;
			list = (List<Session>) this.template.query(sql, status,
					new SessionRowMapper());
			return list;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public List<Participant> getSessionParticipants(int sessionID) {
		String sql = "SELECT USER_ID, PARTICIPANT_STATUS FROM USER_SESSION "
				+ "WHERE SESSION_ID = ? AND PARTICIPANT_STATUS IS NOT NULL";
		log.info(sql);
		Object[] args = new Object[] { sessionID };
		try {
			List<Participant> list;
			list = (List<Participant>) this.template.query(sql, args,
					new ParticipantRowMapper());
			return list;
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<Participant>();
		}
	}
	
	@Override
	public List<Group> getSessionAuthorizedGroups(int sessionID) {
		String sql = "SELECT DISTINCT GROUP_ID FROM USER_SESSION " +
				"WHERE SESSION_ID = ? AND USER_ID IS NULL";
		log.info(sql);
		Object[] args = new Object[] { sessionID };
		int[] argTypes = new int[] {Types.INTEGER};
		
		try {
			List<Group> list;
			list = (List<Group>) this.template.query(sql, args, argTypes,
					new GroupIdRowMapper());
			return list;
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<Group>();
		}
	}

	@Override
	public boolean isScheduled(int sessionID) {
		String sql = "SELECT * FROM CALENDAR WHERE SESSION_ID = ?";
		Object[] args = new Object[] { sessionID };
		try {
			List<TimeSlot> ts;
			ts = (List<TimeSlot>) this.template.query(sql, args,
					new TimeSlotRowMapper());
			if (ts != null && ts.size() > 0) {
				return true;
			} else {
				return false;
			}
		} catch (EmptyResultDataAccessException e) {
			return false;
		}

	}

	@Override
	public TimeSlot getScheduledTime(int sessionID) {
		String sql = "SELECT * FROM CALENDAR WHERE SESSION_ID = ?";
		Object[] args = new Object[] { sessionID };
		try {
			TimeSlot ts;
			ts = (TimeSlot) this.template.queryForObject(sql, args,
					new TimeSlotRowMapper());
			return ts;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public List<Session> getSubsessions(int sessionID) {
		String sql = "SELECT S.* FROM SESSION AS S, SESSION_RELATIONSHIP AS SR "
				+ "WHERE SR.MASTER_SESSION_ID = ? AND S.SESSION_ID = SR.SUBSESSION_ID";
		log.info(sql);
		Object[] args = new Object[] { sessionID };
		try {
			List<Session> list;
			list = (List<Session>) this.template.query(sql, args,
					new SessionRowMapper());
			return list;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public List<Session> getRelevantSessionsInTimeInterval(
			Resource networkResource, Calendar startTime, Calendar endTime) {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss aa");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		String sql = "SELECT DISTINCT * FROM SESSION_NETWORK_RESOURCE AS S1"
				+ " WHERE RESOURCE_ID = "
				+ networkResource.getResourceID()
				+ "SELECT COUNT(*) FROM SESSION_RELATIONSHIP AS SR2, SESSION AS S2, CALENDAR AS C "
				+ "WHERE SR2.SUBSESSION_ID = S2.SESSION_ID AND SR2.MASTER_SESSION_ID = S1.SESSION_ID "
				+ "AND S2.SESSION_STATUS != '"
				+ Session.PENDING
				+ "' AND S2.SESSION_STATUS != '"
				+ Session.CANCELLED
				+ "' AND S2.SESSION_STATUS != '"
				+ Session.FINISHED
				+ "' AND S2.SESSION_ID = C.SESSION_ID AND ( "

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

				")" + " ) > 0";

		log.info(sql);
		try {
			List<Session> list;
			list = (List<Session>) this.template.query(sql,
					new SessionRowMapper());
			return list;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public List<Session> getRelevantSessionsInTimeInterval(Calendar startTime,
			Calendar endTime) {

		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss aa");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		String sql = "SELECT DISTINCT S1.* FROM SESSION AS S1, SESSION_RELATIONSHIP AS SR1 "
				+ "WHERE S1.SESSION_ID NOT IN (SELECT SUBSESSION_ID FROM SESSION_RELATIONSHIP) "
				+ "AND S1.SESSION_ID IN (SELECT SESSION_ID FROM SESSION_NETWORK_RESOURCE) "
				+ "AND ("
				+ "SELECT COUNT(*) FROM SESSION_RELATIONSHIP AS SR2, SESSION AS S2, CALENDAR AS C "
				+ "WHERE SR2.SUBSESSION_ID = S2.SESSION_ID AND SR2.MASTER_SESSION_ID = S1.SESSION_ID "
				+ "AND S2.SESSION_STATUS != '"
				+ Session.PENDING
				+ "' AND S2.SESSION_STATUS != '"
				+ Session.CANCELLED
				+ "' AND S2.SESSION_STATUS != '"
				+ Session.FINISHED
				+ "' AND S2.SESSION_ID = C.SESSION_ID AND ( "

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

				")" + " ) > 0";

		log.info(sql);
		try {
			List<Session> list;
			list = (List<Session>) this.template.query(sql,
					new SessionRowMapper());
			return list;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public Map<Integer, Integer> getNetworkReservationPerSession() {
		String sql = "SELECT * FROM SESSION_NETWORK_RESOURCE";
		return (Map<Integer, Integer>) this.template.query(sql,
				new ResultSetExtractor() {
					public Object extractData(ResultSet rs)
							throws SQLException, DataAccessException {
						Map<Integer, Integer> map = new HashMap<Integer, Integer>();
						while (rs.next()) {
							Integer col1 = rs.getInt("SESSION_ID");
							Integer col2 = rs.getInt("RESOURCE_ID");
							map.put(col1, col2);
						}
						return map;
					}
				});
	}

	@Override
	public Session getSessionById(int sessionId) {
		String sql = "SELECT * FROM SESSION WHERE SESSION_ID = ?";
		log.info(sql);
		Object[] args = new Object[] { sessionId };
		try {
			Session s;
			s = (Session) this.template.queryForObject(sql, args,
					new SessionRowMapper());
			return s;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Transactional(readOnly = false)
	@Override
	public int addSession(Session s) {
		return addSession(s, 0);
	}

	@Transactional(readOnly = false)
	@Override
	public int addSession(Session s, int masterSessionID) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		String sql = "INSERT INTO SAVOIR.SESSION (SESSION_NAME, "
				+ "SESSION_DESCRIPTION, USER_ID, REQUESTED_START_TIME, "
				+ "REQUESTED_END_TIME, SUBMISSION_TIME, SESSION_STATUS, "
				+ "SCENARIO_ID) VALUES (?,?,?,?,?,?,?,?)";
		log.info(sql);
		Object[] args = new Object[] {
				truncate(s.getName(), MAX_NAME_LENGTH),
				truncate(s.getDescription(), MAX_DESC_LENGTH),
				s.getRequestedBy().getUserID(),
				nullableCalendar(s.getRequestedStartTime()),
				nullableCalendar(s.getRequestedEndTime()),
				nullableCalendar(s.getSubmissionDate()), 
				s.getStatus(),
				s.getScenarioId() };

		int[] argTypes = new int[] { Types.VARCHAR, Types.VARCHAR, 
				Types.INTEGER, nullableType(s.getRequestedStartTime()), 
				nullableType(s.getRequestedEndTime()), 
				nullableType(s.getSubmissionDate()), Types.VARCHAR, 
				Types.INTEGER };

		this.template.update(sql, args, argTypes);

		s.setSessionID(this.template.queryForInt("SELECT LAST_INSERT_ID()"));

		if (masterSessionID != 0) {
			sql = "INSERT INTO SESSION_RELATIONSHIP (MASTER_SESSION_ID, SUBSESSION_ID) values (?,?)";
			log.info(sql);
			args = new Object[] { masterSessionID, s.getSessionID() };
			argTypes = new int[] { Types.INTEGER, Types.INTEGER };
			this.template.update(sql, args, argTypes);
		}
		if (s.getConnections() != null && s.getConnections().size() > 0) {
			for (Connection c : s.getConnections()) {
				connectionDAO.addConnection(c, s.getSessionID());
			}
		}
		if (s.getSubSessions() != null && s.getSubSessions().size() > 0) {
			for (Session ss : s.getSubSessions()) {
				addSession(ss, s.getSessionID());
			}
		}

		return s.getSessionID();
	}

	@Transactional(readOnly = false)
	@Override
	public int updateSession(Session s) {

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		if (s.getSubSessions() != null && s.getSubSessions().size() > 0)
			for (Session ss : s.getSubSessions())
				updateSession(ss);

		if (s.getConnections() != null && s.getConnections().size() > 0)
			for (Connection c : s.getConnections())
				connectionDAO.updateConnection(c);

		String sql = "UPDATE SAVOIR.SESSION SET "
				+ "SESSION_NAME = ?, SESSION_DESCRIPTION = ?, "
				+ "USER_ID = ?, REQUESTED_START_TIME = ?, REQUESTED_END_TIME = ?, "
				+ "SUBMISSION_TIME = ?, SESSION_STATUS = ?, SCENARIO_ID = ?, "
				+ "WHERE SESSION_ID = ?";
		log.info(sql);
		Object[] args = new Object[] {
				truncate(s.getName(), MAX_NAME_LENGTH), 
				truncate(s.getDescription(), MAX_DESC_LENGTH), 
				s.getRequestedBy().getUserID(),
				s.getRequestedStartTime() != null ? formatter.format(s
						.getRequestedStartTime().getTime()) : null,
				s.getRequestedEndTime() != null ? formatter.format(s
						.getRequestedEndTime().getTime()) : null,
				s.getSubmissionDate() != null ? formatter.format(s
						.getSubmissionDate().getTime()) : null, s.getStatus(),
				s.getScenarioId(), 
				s.getSessionID() };

		int[] argTypes = new int[] { Types.VARCHAR, Types.VARCHAR, 
				Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, 
				Types.VARCHAR, Types.INTEGER, Types.INTEGER };

		return this.template.update(sql, args, argTypes);

	}

	@Transactional(readOnly = false)
	@Override
	public void removeSession(int sessionId) {
		// start by deleting the subsessions : could be recursive
		// delete all the connections in the session then delete the session
		// itself
		
		removeSubsessions(new HashSet<Integer>(Collections.singleton(sessionId)));
	}
	
	/**
	 * Recursively removes all the subsessions of the given sessions, then the 
	 * sessions themselves.
	 * 
	 * @param sessionIds	The IDs of the sessions to remove.
	 */
	@Transactional(readOnly = false)
	private void removeSubsessions(Set<Integer> sessionIds) {
		
		//check for subsessions
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT SUBSESSION_ID FROM SESSION_RELATIONSHIP " +
				"WHERE MASTER_SESSION_ID IN ");
		addInts(sb, sessionIds);
		String sql = sb.toString();
		
		Set<Integer> subsessionIds;
		try {
			subsessionIds = new LinkedHashSet<Integer>(
					(List<Integer>)template.query(sql, new IntRowMapper()));
		} catch (EmptyResultDataAccessException e) {
			subsessionIds = null;
		}
		
		//if subsessions to remove, do so recursively
		if (subsessionIds != null && !subsessionIds.isEmpty()) {
			removeSubsessions(subsessionIds);
		}
		
		//remove original sessions
		sb = new StringBuilder();
		sb.append("DELETE FROM SAVOIR.SESSION WHERE SESSION_ID IN ");
		addInts(sb, sessionIds);
		sql = sb.toString();
		
		template.update(sql);
	}

	public IUserDAO getUserDAO() {
		return userDAO;
	}

	public void setUserDAO(IUserDAO userDAO) {
		this.userDAO = userDAO;
	}
	
	public IGroupDAO getGroupDAO() {
		return groupDAO;
	}

	public void setGroupDAO(IGroupDAO groupDAO) {
		this.groupDAO = groupDAO;
	}
	
	public IRoleDAO getRoleDAO() {
		return roleDAO;
	}

	public void setRoleDAO(IRoleDAO roleDAO) {
		this.roleDAO = roleDAO;
	}

	public IConnectionDAO getConnectionDAO() {
		return connectionDAO;
	}

	public void setConnectionDAO(IConnectionDAO connectionDAO) {
		this.connectionDAO = connectionDAO;
	}

	public static void main(String[] args) {

	}
	
	//added at 10-01-09
	@Override
	public List<Session> getSessionListByName(String sessionName){
		String sql = "SELECT S.* FROM SESSION AS S " + "WHERE S.SESSION_NAME = ?";
		log.info(sql);
		Object[] args = new Object[] { sessionName };
		try {
			List<Session> list;
			list = (List<Session>) this.template.query(sql, args,
					new SessionRowMapper());
			return list;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Transactional(readOnly = false)
	@Override
	public int updateSessionParticipant(int sessionID, Participant participant) {
		String sql = "UPDATE SAVOIR.USER_SESSION SET PARTICIPANT_STATUS = ? " +
				"WHERE SESSION_ID = ? AND USER_ID = ?";
		log.info(sql);
		Object[] args = new Object[] {participant.getStatus(), sessionID, 
				participant.getUser().getUserID()};
		int[] argTypes = new int[] {Types.VARCHAR, Types.INTEGER, Types.INTEGER};
		return this.template.update(sql, args, argTypes);
	}
	
	@Override
	public List<Participant> getSessionParticipantsByStatus(int sessionID,
			String participantStatus) {
		String sql = "SELECT USER_ID, PARTICIPANT_STATUS " +
				"FROM SAVOIR.USER_SESSION " +
				"WHERE SESSION_ID = ? AND PARTICIPANT_STATUS = ?";
		log.info(sql);
		Object[] args = new Object[] { sessionID, participantStatus};
		try {
			List<Participant> list;
			list = (List<Participant>) this.template.query(sql, args,
					new ParticipantRowMapper());
			return list;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public Session getSessionByStatusAndUserID(String status, int userID){
		String sql = "SELECT * FROM SESSION WHERE SESSION_STATUS = ? AND USER_ID = ?";
		log.info(sql);
		Object[] args = new Object[] { status, userID };
		try {
			Session s;
			s = (Session) this.template.queryForObject(sql, args,
					new SessionRowMapper());
			return s;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	// end add
	
	@Transactional(readOnly = false)
	@Override
	public void updateSessionStatus(int sessionId, String status) {
		String sql = 
			"UPDATE SESSION SET SESSION_STATUS = ? WHERE SESSION_ID = ?";
		log.info(sql);
		Object[] args = new Object[]{status, sessionId};
		int[] argTypes = new int[]{Types.CHAR, Types.INTEGER};
		
		this.template.update(sql, args, argTypes);
	}
	
	@Override
	public List<Integer> getSessionIdsByScenarioId(int scenarioId) {
		String sql = "SELECT SESSION_ID FROM SAVOIR.SESSION " +
				"WHERE SCENARIO_ID = ?";
		Object[] args = new Object[]{scenarioId};
		int[] argTypes = new int[]{Types.INTEGER};
		
		try {
			return (List<Integer>)template.query(sql, args, argTypes, 
					new IntRowMapper());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<Integer>();
		} catch (DataAccessException e) {
			log.error("Data access error on session lookup by scenario ID", e);
			return null;
		}
	}
	
	@Override
	public List<Integer> getSubsessionIds(int sessionId) {
		String sql = "SELECT SUBSESSION_ID FROM SESSION_RELATIONSHIP "
			+ "WHERE MASTER_SESSION_ID = ?";
		Object[] args = new Object[]{sessionId};
		
		try {
			return (List<Integer>)template.query(sql, args, 
					new IntRowMapper());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<Integer>();
		} catch (DataAccessException e) {
			log.error("Data access error on subsession lookup", e);
			return null;
		}
	}
	
	@Override
	public Map<Integer, Role> getAuthorizedUsers(int sessionId) {
		String sql = "SELECT USER_ID, ROLE_ID FROM USER_SESSION WHERE SESSION_ID = ?";
		Object[] args = new Object[]{sessionId};
		List<IntRole> authz;
		
		try {
			//get user IDs matching given DName
			authz = (List<IntRole>) template.query(sql, args, new IntRoleRowMapper(roleDAO));
		} catch (DataAccessException e) {
			log.error("Data access error on user session authorization lookup", e);
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
	public Map<Integer, Role> getAuthorizedGroups(int sessionId) {
		String sql = "SELECT GROUP_ID, ROLE_ID FROM USER_SESSION " +
				"WHERE SESSION_ID = ? AND (GROUP_ID IS NOT NULL OR (" +
				"GROUP_ID IS NULL AND USER_ID IS NULL))";
		Object[] args = new Object[]{sessionId};
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
	
	private static final String DEFAULT_PARTICIPANT_STATUS = "NOT-JOINED";
	
	@Transactional(readOnly = false)
	@Override
	public int addUserAuthorization(int userId, int roleId, int sessionId) {
		if (userId <= 0 || roleId <= 0 || sessionId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		String sql = "INSERT INTO USER_SESSION (SESSION_ID, USER_ID, ROLE_ID, PARTICIPANT_STATUS) " +
				"VALUES (?,?,?,?)";
		Object[] args = new Object[] {sessionId, userId, roleId, DEFAULT_PARTICIPANT_STATUS}; 
		int[] argTypes = new int[]{Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.VARCHAR};
		
		int updated;
		try {
			updated = this.template.update(sql, args, argTypes);
		} catch (DataAccessException e) {
			log.error("Persistence error on add user authorization", e);
			return OTHER_ERROR;
		}
		
		//return success for one update, error otherwise
		return updated == 1 ? SUCCESS : OTHER_ERROR;
	}

	@Transactional(readOnly = false)
	@Override
	public int updateUserAuthorization(int userId, int roleId, int sessionId) {
		if (userId <= 0 || roleId <= 0 || sessionId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		String sql = "UPDATE USER_SESSION SET ROLE_ID = ? WHERE USER_ID = ? AND SESSION_ID = ?";
		Object[] args = new Object[] {roleId, userId, sessionId};
		int[] argTypes = new int[]{Types.INTEGER, Types.INTEGER, Types.INTEGER};
		
		int updated;
		try {
			updated = this.template.update(sql, args, argTypes);
		} catch (DataAccessException e) {
			log.error("Persistence error on update user authorization", e);
			return OTHER_ERROR;
		}
		
		//return success for one update, error otherwise
		return updated == 1 ? SUCCESS : OTHER_ERROR;
	}
	
	@Transactional(readOnly = false)
	@Override
	public int removeUserAuthorization(int userId, int sessionId) {
		if (userId <= 0 || sessionId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		String sql = "DELETE FROM USER_SESSION WHERE USER_ID = ? AND SESSION_ID = ?";
		Object[] args = new Object[] {userId, sessionId};
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
	
	@Transactional(readOnly = false)
	@Override
	public int addGroupAuthorization(int groupId, int roleId, int sessionId) {
		if (groupId < 0 || roleId <= 0 || sessionId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		String sql = "INSERT INTO USER_SESSION " +
				"(SESSION_ID, GROUP_ID, ROLE_ID) VALUES (?,?,?)";
		Object[] args = new Object[] {sessionId, nullableInt(groupId), roleId}; 
		int[] argTypes = new int[]{Types.INTEGER, nullableType(groupId), 
				Types.INTEGER};
		
		int updated;
		try {
			updated = this.template.update(sql, args, argTypes);
		} catch (DataAccessException e) {
			log.error("Persistence error on add group authorization", e);
			return OTHER_ERROR;
		}
		
		//return success for one update, error otherwise
		return updated == 1 ? SUCCESS : OTHER_ERROR;
	}

	@Transactional(readOnly = false)
	@Override
	public int updateGroupAuthorization(int groupId, int roleId, int sessionId) {
		if (groupId < 0 || roleId <= 0 || sessionId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		String sql; Object[] args; int[] argTypes;
		if (groupId == 0) {
			//implicit all users group
			sql = "UPDATE USER_SESSION SET ROLE_ID = ? " +
					"WHERE GROUP_ID IS NULL AND USER_ID IS NULL " +
					"AND SESSION_ID = ?";
			args = new Object[] {roleId, sessionId};
			argTypes = new int[] {Types.INTEGER, Types.INTEGER};
		} else {
			//specific group
			sql = "UPDATE USER_SESSION SET ROLE_ID = ? " +
					"WHERE GROUP_ID = ? AND SESSION_ID = ?";
			args = new Object[] {roleId, groupId, sessionId};
			argTypes = new int[]{Types.INTEGER, Types.INTEGER, Types.INTEGER};
		}
		
		int updated;
		try {
			updated = this.template.update(sql, args, argTypes);
		} catch (DataAccessException e) {
			log.error("Persistence error on update group authorization", e);
			return OTHER_ERROR;
		}
		
		//return success for one update, error otherwise
		return updated == 1 ? SUCCESS : OTHER_ERROR;
	}
	
	@Transactional(readOnly = false)
	@Override
	public int removeGroupAuthorization(int groupId, int sessionId) {
		if (groupId < 0 || sessionId <= 0) {
			return INVALID_PARAMETERS;
		}
		
		String sql; Object[] args; int[] argTypes;
		if (groupId == 0) {
			//implicit all users group
			sql = "DELETE FROM USER_SESSION " +
					"WHERE GROUP_ID IS NULL AND USER_ID IS NULL " +
					"AND SESSION_ID = ?";
			args = new Object[] {sessionId};
			argTypes = new int[] {Types.INTEGER};
		} else {
			//explicit group ID
			sql = "DELETE FROM USER_SESSION " +
					"WHERE GROUP_ID = ? AND SESSION_ID = ?";
			args = new Object[] {groupId, sessionId};
			argTypes = new int[] {Types.INTEGER, Types.INTEGER};
		}
		
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
}
