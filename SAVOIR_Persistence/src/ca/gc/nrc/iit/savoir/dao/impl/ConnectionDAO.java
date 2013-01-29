// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.iit.nrc.savoir.domain.Connection;
import ca.gc.nrc.iit.savoir.dao.BaseDAO;
import ca.gc.nrc.iit.savoir.dao.IConnectionDAO;
import ca.gc.nrc.iit.savoir.dao.IEndPointDAO;
import ca.gc.nrc.iit.savoir.dao.IResourceDAO;

@Transactional(readOnly = true, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
@SuppressWarnings("unchecked")
public class ConnectionDAO extends BaseDAO implements IConnectionDAO {

	private IEndPointDAO endPointDAO;

	private IResourceDAO resourceDAO;

	class ConnectionRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			Connection conn = new Connection();
			conn.setConnectionID(rs.getInt("CONNECTION_ID"));
			conn.setSourceEndPoint(endPointDAO.getEndPointById(rs
					.getInt("SOURCE_END_POINT")));
			conn.setTargetEndPoint(endPointDAO.getEndPointById(rs
					.getInt("TARGET_END_POINT")));
			conn.setDirectionality(rs.getString("DIRECTIONALITY"));
			conn.setBwRequirement(rs.getInt("BW_REQUIREMENT"));
			conn.setMinBwRequirement(rs.getInt("MIN_BW_REQUIREMENT"));
			conn.setLpNeeded(rs.getBoolean("LP_FLAG"));
			if (rs.getInt("NETWORK_RESOURCE") != 0) {
				conn.setNetworkResource(resourceDAO.getResourceById(rs
						.getInt("NETWORK_RESOURCE")));
			}
			return conn;
		}
	}

	@Transactional(readOnly = false)
	@Override
	public int updateConnection(Connection c) {

		endPointDAO.updateEndPoint(c.getSourceEndPoint());
		endPointDAO.updateEndPoint(c.getTargetEndPoint());

		String sql = "UPDATE SAVOIR.CONNECTION SET "
				+ "SOURCE_END_POINT = ?, "
				+ "TARGET_END_POINT = ?, DIRECTIONALITY = ?, BW_REQUIREMENT = ?, "
				+ "MIN_BW_REQUIREMENT = ?, LP_FLAG = ?, NETWORK_RESOURCE = ? "
				+ " WHERE CONNECTION_ID = ?";

		Object[] args = new Object[] {
				c.getSourceEndPoint().getEndPointID(),
				c.getTargetEndPoint().getEndPointID(),
				c.getDirectionality(),
				c.getBwRequirement() + 0.0,
				c.getMinBwRequirement() + 0.0,
				c.isLpNeeded(),
				c.getNetworkResource() != null ? c.getNetworkResource()
						.getResourceID() : null, c.getConnectionID() };

		int[] argTypes = new int[] { Types.INTEGER, Types.INTEGER,
				Types.VARCHAR, Types.DOUBLE, Types.DOUBLE, Types.BOOLEAN,
				Types.INTEGER, Types.INTEGER };

		return this.template.update(sql, args, argTypes);
	}

	@Override
	public Connection getConnectionById(int connectionId) {
		String sql = "SELECT * FROM SAVOIR.CONNECTION WHERE CONNECTION_ID = ?";
		Object[] args = new Object[] { connectionId };
		try {
			Connection conn;
			conn = (Connection) this.template.queryForObject(sql, args,
					new ConnectionRowMapper());
			return conn;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public List<Connection> getConnectionsBySessionID(int sessionID) {
		String sql = "SELECT * FROM SAVOIR.CONNECTION WHERE SESSION_ID = ? ";
		log.info(sql);
		Object[] args = new Object[] { sessionID };
		try {
			List<Connection> list;
			list = (List<Connection>) this.template.query(sql, args,
					new ConnectionRowMapper());
			log.info(list);
			return list;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Transactional(readOnly = false)
	@Override
	public int removeConnection(int connectionId) {
		endPointDAO.removeEndPointsByConnectionID(connectionId);
		String sql = "DELETE FROM SAVOIR.CONNECTION WHERE CONNECTION_ID = ?";
		Object[] args = new Object[] { connectionId };
		return this.template.update(sql, args);
	}

	@Transactional(readOnly = false)
	@Override
	public int addConnection(Connection c, int sessionID) {

		endPointDAO.addEndPoint(c.getSourceEndPoint());
		c.getSourceEndPoint().setEndPointID(
				this.template.queryForInt("SELECT LAST_INSERT_ID()"));

		endPointDAO.addEndPoint(c.getTargetEndPoint());
		c.getTargetEndPoint().setEndPointID(
				this.template.queryForInt("SELECT LAST_INSERT_ID()"));

		String sql = "INSERT INTO SAVOIR.CONNECTION ( SESSION_ID , SOURCE_END_POINT, "
				+ "TARGET_END_POINT, DIRECTIONALITY, BW_REQUIREMENT, "
				+ "MIN_BW_REQUIREMENT, LP_FLAG, NETWORK_RESOURCE)"
				+ " values (?,?,?,?,?,?,?,?)";

		Object[] args = new Object[] { sessionID,
				c.getSourceEndPoint().getEndPointID(),
				c.getTargetEndPoint().getEndPointID(), c.getDirectionality(),
				c.getBwRequirement(), c.getMinBwRequirement(), c.isLpNeeded(),
				c.getNetworkResource() };

		int[] argTypes = new int[] { Types.INTEGER, Types.INTEGER,
				Types.INTEGER, Types.VARCHAR, Types.INTEGER, Types.INTEGER,
				Types.BOOLEAN, Types.INTEGER };

		return this.template.update(sql, args, argTypes);
	}

	public IEndPointDAO getEndPointDAO() {
		return endPointDAO;
	}

	public void setEndPointDAO(IEndPointDAO endPointDAO) {
		this.endPointDAO = endPointDAO;
	}

	public IResourceDAO getResourceDAO() {
		return resourceDAO;
	}

	public void setResourceDAO(IResourceDAO resourceDAO) {
		this.resourceDAO = resourceDAO;
	}

}
