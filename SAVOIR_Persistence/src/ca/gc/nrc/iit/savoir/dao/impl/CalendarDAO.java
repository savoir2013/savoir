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
import java.util.List;
import java.util.TimeZone;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.iit.nrc.savoir.domain.Person;
import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.iit.nrc.savoir.domain.Session;
import ca.gc.iit.nrc.savoir.domain.TimeSlot;
import ca.gc.nrc.iit.savoir.dao.BaseDAO;
import ca.gc.nrc.iit.savoir.dao.ICalendarDAO;
import ca.gc.nrc.iit.savoir.dao.IPersonDAO;
import ca.gc.nrc.iit.savoir.dao.IResourceDAO;
import ca.gc.nrc.iit.savoir.dao.ISessionDAO;

@Transactional(readOnly = true, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
@SuppressWarnings("unchecked")
public class CalendarDAO extends BaseDAO implements ICalendarDAO {

	private IResourceDAO resourceDAO;

	private IPersonDAO personDAO;

	private ISessionDAO sessionDAO;

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

	@Transactional(readOnly = false)
	@Override
	public int addEntry(Class resourceType, int objectID, TimeSlot timeSlot) {

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		String sql = "INSERT INTO CALENDAR (";
		if (resourceType == Person.class)
			sql += "PERSON_ID";
		else if (resourceType == Session.class)
			sql += "SESSION_ID";
		else if (resourceType == Resource.class)
			sql += "RESOURCE_ID";
		sql += ", START_TIME, END_TIME) values (?,?,?)";

		Object[] args = new Object[] { objectID,
				formatter.format(timeSlot.getStartTime().getTime()),
				formatter.format(timeSlot.getEndTime().getTime()) };

		int[] argTypes = new int[] { Types.INTEGER, Types.VARCHAR,
				Types.VARCHAR };

		this.template.update(sql, args, argTypes);

		return this.template.queryForInt("SELECT LAST_INSERT_ID()");
	}

	@Override
	public List<TimeSlot> getCalendar(Class resourceType, int objectID) {
		String sql = "SELECT * FROM CALENDAR WHERE ";

		if (resourceType == Person.class)
			sql += "PERSON_ID = ?";
		else if (resourceType == Session.class)
			sql += "SESSION_ID =?";
		else if (resourceType == Resource.class)
			sql += "RESOURCE_ID =?";

		Object[] args = new Object[] { objectID };
		try {
			List<TimeSlot> ts;
			ts = (List<TimeSlot>) this.template.query(sql, args,
					new TimeSlotRowMapper());
			return ts;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public List<TimeSlot> getFreeCalendar(Class resourceType, int objectID) {
		String sql = "SELECT * FROM CALENDAR WHERE ";

		if (resourceType == Person.class)
			sql += "PERSON_ID = ?";
		else if (resourceType == Session.class)
			sql += "SESSION_ID =?";
		else if (resourceType == Resource.class)
			sql += "RESOURCE_ID =?";

		sql += " AND START_TIME >= NOW()";
		sql += " ORDER BY START_TIME ASC";

		Object[] args = new Object[] { objectID };
		try {
			List<TimeSlot> ts;
			ts = (ArrayList<TimeSlot>) this.template.query(sql, args,
					new TimeSlotRowMapper());

			int size = ts.size();

			long minusInf = System.currentTimeMillis();
			long plusInf = Long.MAX_VALUE;

			Calendar lowBoundCal = Calendar.getInstance();
			lowBoundCal.setTimeInMillis(minusInf);

			Calendar highBoundCal = Calendar.getInstance();
			highBoundCal.setTimeInMillis(plusInf);

			switch (size) {
			case 0:
				ts.add(new TimeSlot(lowBoundCal, highBoundCal));
				break;
			case 1:
				ts.add(0, new TimeSlot(lowBoundCal, ts.get(0).getStartTime()));
				ts.get(1).setStartTime(ts.get(1).getEndTime());
				ts.get(1).setEndTime(highBoundCal);
				break;
			default:
				ts.add(0, new TimeSlot(lowBoundCal, ts.get(0).getStartTime()));
				size = ts.size();
				for (int i = 1; i < size - 1; i++) {
					ts.get(i).setStartTime(ts.get(i).getEndTime());
					ts.get(i).setEndTime(ts.get(i + 1).getStartTime());
				}
				ts.get(size - 1).setStartTime(ts.get(size - 1).getEndTime());
				ts.get(size - 1).setEndTime(highBoundCal);
				break;
			}
			return ts;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public List<TimeSlot> getScenarioCalendar(int resourceID) {
		String sql = "SELECT CAL.* FROM CALENDAR AS CAL WHERE CAL.RESOURCE_ID = ?";
		Object[] args = new Object[] { resourceID };
		try {
			List<TimeSlot> ts;
			ts = (List<TimeSlot>) this.template.query(sql, args,
					new TimeSlotRowMapper());
			return ts;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public List<TimeSlot> getScenariosCalendar(Calendar minTime,
			Calendar maxTime) {

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		String sql = "SELECT CAL.* FROM CALENDAR AS CAL, RESOURCE AS R "
				+ "WHERE CAL.RESOURCE_ID = R.RESOURCE_ID "
				+ "AND R.RESOURCE_TYPE_ID = 'LP_SCENARIO' ";

		if (minTime != null)
			sql += "AND (CAL.START_TIME >= "
					+ formatter.format(minTime.getTime()) + " ";
		if (maxTime != null)
			sql += "AND " + formatter.format(maxTime.getTime())
					+ "  <= CAL.END_TIME )";

		try {
			List<TimeSlot> ts;
			ts = (List<TimeSlot>) this.template.query(sql,
					new TimeSlotRowMapper());
			return ts;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	// @Override
	// public boolean isNetworkAvailable(int resourceID, TimeSlot timeSlot) {
	// List<TimeSlot> list = getNetworkCalendar(resourceID);
	// if (list != null && !list.isEmpty()) {
	// for (TimeSlot ts : list) {
	// if (timeOverlap(ts, timeSlot) != 0) {
	// return false;
	// }
	// }
	// }
	// return true;
	// }

	@Override
	public boolean isAvailable(Class resourceType, int objectID,
			TimeSlot timeSlot) {

		if (resourceType != Session.class) {
			List<TimeSlot> list = getCalendar(resourceType, objectID);
			if (list != null && !list.isEmpty()) {
				for (TimeSlot ts : list) {
					if (timeOverlap(ts, timeSlot) != 0) {
						return false;
					}
				}
			}
		} else {

		}

		return true;
	}

	@Transactional(readOnly = false)
	@Override
	public int removeEntry(int entryID) {
		String sql = "DELETE FROM CALENDAR WHERE CALENDAR_ID = ?";
		Object[] args = new Object[] { entryID };
		return this.template.update(sql, args);
	}

	@Transactional(readOnly = false)
	@Override
	public int updateEntry(int entryID, TimeSlot timeSlot) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		String sql = "UPDATE CALENDAR SET START_TIME=?, END_TIME=? WHERE CALENDAR_ID = ?";
		Object[] args = new Object[] {
				formatter.format(timeSlot.getStartTime().getTime()),
				formatter.format(timeSlot.getEndTime().getTime()), entryID, };
		int[] argTypes = new int[] { Types.VARCHAR, Types.VARCHAR,
				Types.INTEGER };
		return this.template.update(sql, args, argTypes);
	}

	private int timeOverlap(TimeSlot tS1, TimeSlot tS2) {

		if (tS1.getStartTime().getTimeInMillis() <= tS2.getStartTime()
				.getTimeInMillis()
				&& tS2.getEndTime().getTimeInMillis() <= tS1.getEndTime()
						.getTimeInMillis())
			return 1;
		if (tS1.getStartTime().getTimeInMillis() <= tS2.getStartTime()
				.getTimeInMillis()
				&& tS2.getEndTime().getTimeInMillis() >= tS1.getEndTime()
						.getTimeInMillis()
				&& tS2.getStartTime().getTimeInMillis() < tS1.getEndTime()
						.getTimeInMillis())
			return 2;
		if (tS1.getStartTime().getTimeInMillis() >= tS2.getStartTime()
				.getTimeInMillis()
				&& tS2.getEndTime().getTimeInMillis() <= tS1.getEndTime()
						.getTimeInMillis()
				&& tS2.getEndTime().getTimeInMillis() > tS1.getStartTime()
						.getTimeInMillis())
			return 3;
		if (tS1.getStartTime().getTimeInMillis() >= tS2.getStartTime()
				.getTimeInMillis()
				&& tS2.getEndTime().getTimeInMillis() >= tS1.getEndTime()
						.getTimeInMillis())
			return 4;
		return 0;

		// sb.append(
		// "defbool active(i,j,t) := (Times[t] <= STime[i,j] and ETime[i,j] <= Times[t+1]) "
		// +
		// "or (Times[t] <= STime[i,j] and ETime[i,j] >= Times[t+1] and STime[i,j] < Times[t+1]) "
		// +
		// "or (Times[t] >= STime[i,j] and ETime[i,j] <= Times[t+1] and ETime[i,j] > Times[t]) "
		// +
		// "or (Times[t] >= STime[i,j] and ETime[i,j] >= Times[t+1]);\n\n");

		// return false;
	}

	@Override
	public Object getAssociatedObject(int entryID) {
		String sql = "SELECT CAL.RESOURCE_ID FROM CALENDAR AS CAL WHERE CAL.CALENDAR_ID = ?";
		Object[] args = new Object[] { entryID };
		Integer id = null;
		id = this.template.queryForInt(sql, args);
		if (id == null) {
			sql = "SELECT CAL.PERSON_ID FROM CALENDAR AS CAL WHERE CAL.CALENDAR_ID = ?";
			id = this.template.queryForInt(sql, args);
			if (id == null) {
				sql = "SELECT CAL.SESSION_ID FROM CALENDAR AS CAL WHERE CAL.CALENDAR_ID = ?";
				id = this.template.queryForInt(sql, args);
				if (id != null) {
					return sessionDAO.getSessionById(id);
				} else {
					return null;
				}
			} else {
				personDAO.getPersonById(id);
			}
		} else {
			return resourceDAO.getResourceById(id);
		}
		return null;
	}

	public IResourceDAO getResourceDAO() {
		return resourceDAO;
	}

	public void setResourceDAO(IResourceDAO resourceDAO) {
		this.resourceDAO = resourceDAO;
	}

	public IPersonDAO getPersonDAO() {
		return personDAO;
	}

	public ISessionDAO getSessionDAO() {
		return sessionDAO;
	}

	public void setPersonDAO(IPersonDAO personDAO) {
		this.personDAO = personDAO;
	}

	public void setSessionDAO(ISessionDAO sessionDAO) {
		this.sessionDAO = sessionDAO;
	}

}
