// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.iit.nrc.savoir.domain.PersonInfo;
import ca.gc.iit.nrc.savoir.domain.Scenario;
import ca.gc.iit.nrc.savoir.domain.User;
import ca.gc.nrc.iit.savoir.dao.BaseDAO;
import ca.gc.nrc.iit.savoir.dao.IScenarioDAO;
import ca.gc.nrc.iit.savoir.dao.IUserDAO;

import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.addInts;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.getNullableDate;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.MAX_NAME_LENGTH;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.MAX_DESC_LENGTH;
import static ca.gc.nrc.iit.savoir.dao.impl.DAOUtils.truncate;

@Transactional(readOnly = true, propagation = Propagation.REQUIRED,	
		isolation = Isolation.READ_COMMITTED)
public class ScenarioDAO extends BaseDAO implements IScenarioDAO {
	
	private IUserDAO userDAO;
	
	public void setUserDAO(IUserDAO userDAO) {
		this.userDAO = userDAO;
	}
	
	class ScenarioRowMapper implements RowMapper {
		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			Scenario scn = new Scenario();
			
			scn.setScenarioId(rs.getInt("SCENARIO_ID"));
			scn.setXmlUri(rs.getString("SCENARIO_XML_URI"));
			scn.setRuleUri(rs.getString("SCENARIO_RULE_URI"));
			scn.setScenarioName(rs.getString("SCENARIO_NAME"));
			
			User u = userDAO.getUserById(rs.getInt("AUTHOR_ID"));
			if (u != null) {
				//set author ID to username
				scn.setAuthorId(u.getDName());
				PersonInfo pi = u.getPerson().getPersonInfo();
				//set author name to author's name
				String authorName = 
					((pi.getFName() == null) ? "" : pi.getFName() + " ") +
					pi.getLName();
				scn.setAuthorName(authorName);
			}
			
			scn.setLastModified(getNullableDate(rs, "LAST_MODIFIED"));
			scn.setDescription(rs.getString("DESCRIPTION"));
			scn.setApnParameters(rs.getString("APN_PARAMETERS"));
			scn.setDeviceNames(rs.getString("DEVICE_NAMES"));
			
			return scn;
		}
	}

	@Override
	public Scenario getScenarioById(int scenarioId) {
		String sql = "SELECT * FROM SCENARIO WHERE SCENARIO_ID = ?";
		log.info(sql);
		Object[] args = new Object[]{scenarioId};
		
		try {
			return (Scenario)this.template.queryForObject(sql, args, 
					new ScenarioRowMapper());
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public Scenario getScenarioBySessionId(int sessionId) {
		String sql = "SELECT SN.* FROM SCENARIO AS SN, SESSION AS S " +
				"WHERE S.SCENARIO_ID = SN.SCENARIO_ID AND S.SESSION_ID = ?";
		log.info(sql);
		Object[] args = new Object[] { sessionId };
		
		try {
			return (Scenario)this.template.queryForObject(sql, args, 
					new ScenarioRowMapper());
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Scenario> getAllScenarios() {
		String sql = "SELECT * FROM SCENARIO";
		
		return new ArrayList<Scenario>(
				this.template.query(sql, new ScenarioRowMapper()));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Scenario> getScenariosByIds(List<Integer> scenarioIds) {
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT * FROM SCENARIO WHERE SCENARIO_ID IN ");
		addInts(sqlBuilder, new LinkedHashSet<Integer>(scenarioIds));
		
		return new ArrayList<Scenario>(
				this.template.query(sqlBuilder.toString(), 
						new ScenarioRowMapper()));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Scenario> getRemovableScenarios() {
		String sql = "SELECT * FROM SCENARIO WHERE SCENARIO_ID NOT IN " +
				"(SELECT DISTINCT SCENARIO_ID FROM SAVOIR.SESSION " +
				"WHERE SCENARIO_ID IS NOT NULL)";
		
		try {
			return new ArrayList<Scenario>(this.template.query(
					sql, new ScenarioRowMapper()));
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<Scenario>();
		} catch (DataAccessException e) {
			log.error("Error on removable scenario lookup", e);
			return null;
		}
	}

	@Transactional(readOnly = false)
	@Override
	public void newScenario(Scenario scn) {
		//get author ID
		User author = userDAO.getUserByDN(scn.getAuthorId());
		
		String sql;
		Object[] args;
		int[] argTypes;
		if (author == null) {
			sql = 
				"INSERT INTO SAVOIR.SCENARIO (SCENARIO_ID, " +
				"SCENARIO_XML_URI, SCENARIO_RULE_URI, SCENARIO_NAME, " +
				"LAST_MODIFIED, DESCRIPTION, APN_PARAMETERS, DEVICE_NAMES) " +
				"VALUES (?,?,?,?,?,?,?,?)";
			log.info(sql);
			
			args = new Object[]{scn.getScenarioId(), scn.getXmlUri(), 
					scn.getRuleUri(), 
					truncate(scn.getScenarioName(), MAX_NAME_LENGTH), 
					scn.getLastModified(), 
					truncate(scn.getDescription(), MAX_DESC_LENGTH), 
					scn.getApnParameters(), scn.getDeviceNames()};
			argTypes = new int[]{Types.INTEGER, Types.VARCHAR, Types.VARCHAR, 
					Types.VARCHAR, Types.TIMESTAMP, Types.VARCHAR, 
					Types.VARCHAR, Types.VARCHAR};
		} else {
			sql = 
				"INSERT INTO SAVOIR.SCENARIO (SCENARIO_ID, " +
				"SCENARIO_XML_URI, SCENARIO_RULE_URI, SCENARIO_NAME, " +
				"AUTHOR_ID, LAST_MODIFIED, DESCRIPTION, APN_PARAMETERS, " +
				"DEVICE_NAMES) VALUES (?,?,?,?,?,?,?,?,?)";
			log.info(sql);
			
			args = new Object[]{scn.getScenarioId(), scn.getXmlUri(), 
					scn.getRuleUri(), 
					truncate(scn.getScenarioName(), MAX_NAME_LENGTH), 
					author.getUserID(), scn.getLastModified(), 
					truncate(scn.getDescription(), MAX_DESC_LENGTH), 
					scn.getApnParameters(), scn.getDeviceNames()};
			argTypes = new int[]{Types.INTEGER, Types.VARCHAR, Types.VARCHAR, 
					Types.VARCHAR, Types.INTEGER, Types.TIMESTAMP, 
					Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
		}
		
		this.template.update(sql, args, argTypes);
	}
	
	@Transactional(readOnly = false)
	@Override
	public void removeScenario(int scenarioId) {
		String sql = "DELETE FROM SCENARIO WHERE SCENARIO_ID = ?";
		log.info(sql);
		Object[] args = new Object[]{scenarioId};
		
		this.template.update(sql, args);
	}

	@Transactional(readOnly = false)
	@Override
	public void updateScenario(Scenario newScn) {
		int scenarioId = newScn.getScenarioId();
		
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE SCENARIO SET ");
		Vector<Object> args = new Vector<Object>();
		Vector<Integer> argTypes = new Vector<Integer>();
		
		String newXmlUri = newScn.getXmlUri();
		if (newXmlUri != null) {
			sql.append("SCENARIO_XML_URI = ?,");
			args.add(newXmlUri);
			argTypes.add(Types.VARCHAR);
		}
		
		String newRuleUri = newScn.getRuleUri();
		if (newRuleUri != null) {
			sql.append("SCENARIO_RULE_URI = ?,");
			args.add(newRuleUri);
			argTypes.add(Types.VARCHAR);
		}
		
		String newName = newScn.getScenarioName();
		if (newName != null) {
			sql.append("SCENARIO_NAME = ?,");
			args.add(truncate(newName, MAX_NAME_LENGTH));
			argTypes.add(Types.VARCHAR);
		}
		
		String newAuthorId = newScn.getAuthorId();
		if (newAuthorId != null) {
			if (newAuthorId.isEmpty()) {
				sql.append("AUTHOR_ID = NULL,");
			} else {
				User u = userDAO.getUserByDN(newAuthorId);
				if (u != null) {
					sql.append("AUTHOR_ID = ?,");
					args.add(u.getUserID());
					argTypes.add(Types.INTEGER);
				}
			}
		}
		
		Date newLastModified = newScn.getLastModified();
		if (newLastModified != null) {
			sql.append("LAST_MODIFIED = ?,");
			args.add(newLastModified);
			argTypes.add(Types.TIMESTAMP);
		}
		
		String newDescription = newScn.getDescription();
		if (newDescription != null) {
			sql.append("DESCRIPTION = ?,");
			args.add(truncate(newDescription, MAX_DESC_LENGTH));
			argTypes.add(Types.VARCHAR);
		}
		
		String newApnParams = newScn.getApnParameters();
		if (newApnParams != null) {
			sql.append("APN_PARAMETERS = ?,");
			args.add(newApnParams);
			argTypes.add(Types.VARCHAR);
		}
		
		String newDeviceNames = newScn.getDeviceNames();
		if (newDeviceNames != null) {
			sql.append("DEVICE_NAMES = ?,");
			args.add(newDeviceNames);
			argTypes.add(Types.VARCHAR);
		}
		
		//trim final comma, (if none, no change, return invalid parameters)
		int lastIndex = sql.length() - 1;
		if (sql.charAt(lastIndex) == ',') {
			sql.deleteCharAt(lastIndex);
		} else {
			return;
		}
		
		//finalize query and argument list
		sql.append(" WHERE SCENARIO_ID = ?");
		args.addElement(scenarioId);
		argTypes.add(Types.INTEGER);
		
		int[] argT = new int[argTypes.size()];
		for (int i = 0; i < argT.length; i++) {
			argT[i] = argTypes.get(i);
		}
		
		template.update(sql.toString(), args.toArray(new Object[args.size()]), 
				argT);
	}
	
	@Override
	public int maxScenarioId() {
		try {
			return template.queryForInt(
				"SELECT MAX(SCENARIO_ID) FROM SAVOIR.SCENARIO");
		} catch (EmptyResultDataAccessException e) {
			return 0;
		}
	}
}
