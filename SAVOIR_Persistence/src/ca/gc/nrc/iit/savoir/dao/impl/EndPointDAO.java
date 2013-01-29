// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.iit.nrc.savoir.domain.EndPoint;
import ca.gc.nrc.iit.savoir.dao.BaseDAO;
import ca.gc.nrc.iit.savoir.dao.IEndPointDAO;
import ca.gc.nrc.iit.savoir.dao.IPersonDAO;
import ca.gc.nrc.iit.savoir.dao.IResourceDAO;

@Transactional(readOnly = true, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class EndPointDAO extends BaseDAO implements IEndPointDAO {

	private IResourceDAO resourceDAO;

	private IPersonDAO personDAO;

	class EndPointRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			EndPoint ep = new EndPoint();
			ep.setEndPointID(rs.getInt("END_POINT_ID"));
			ep.setResource(resourceDAO
					.getResourceById(rs.getInt("RESOURCE_ID")));
			ep.setPerson(personDAO.getPersonById(rs.getInt("PERSON_ID")));
			ep.setEndPointType(rs.getString("END_POINT_TYPE"));
			ep.setNetworkEndPoint(resourceDAO.getResourceById(rs
					.getInt("NETWORK_END_POINT")));
			return ep;
		}
	}

	@Transactional(readOnly = false)
	@Override
	public int updateEndPoint(EndPoint e) {
		String sql = "UPDATE END_POINT SET " + "RESOURCE_ID = ? ,"
				+ " PERSON_ID = ?," + " END_POINT_TYPE = ?,"
				+ " NETWORK_END_POINT = ?" + " WHERE END_POINT_ID = ?";

		Object[] args = new Object[] {
				e.getResource() != null ? e.getResource().getResourceID()
						: null,
				e.getPerson() != null ? e.getPerson().getPersonId() : null,
				e.getEndPointType(), e.getNetworkEndPoint() != null ? e.getNetworkEndPoint()
						.getResourceID() : null,
				e.getEndPointID() };

		int[] argTypes = new int[] { Types.INTEGER, Types.INTEGER,
				Types.VARCHAR, Types.INTEGER, Types.INTEGER };

		return this.template.update(sql, args, argTypes);
	}

	@Transactional(readOnly = false)
	@Override
	public int addEndPoint(EndPoint e) {
		String sql = "INSERT INTO END_POINT ( RESOURCE_ID , PERSON_ID, "
				+ "END_POINT_TYPE, NETWORK_END_POINT )" + " values (?,?,?,?)";

		Object[] args = new Object[] {
				e.getResource() != null ? e.getResource().getResourceID()
						: null,
				e.getPerson() != null ? e.getPerson().getPersonId() : null,
				e.getEndPointType(),
				e.getNetworkEndPoint() != null ? e.getNetworkEndPoint()
						.getResourceID() : null };

		int[] argTypes = new int[] { Types.INTEGER, Types.INTEGER,
				Types.VARCHAR, Types.INTEGER };

		return this.template.update(sql, args, argTypes);
	}

	@Override
	public EndPoint getEndPointById(int endPointId) {
		String sql = "SELECT * FROM END_POINT WHERE END_POINT_ID = ?";
		Object[] args = new Object[] { endPointId };
		try {
			EndPoint ep;
			ep = (EndPoint) this.template.queryForObject(sql, args,
					new EndPointRowMapper());
			return ep;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Transactional(readOnly = false)
	@Override
	public int removeEndPoint(int endPointId) {
		String sql = "DELETE FROM END_POINT WHERE END_POINT_ID = ?";
		Object[] args = new Object[] { endPointId };
		return this.template.update(sql, args);
	}

	@Transactional(readOnly = false)
	@Override
	public int removeEndPointsByConnectionID(int connectionId) {
		String sql = "DELETE FROM END_POINT AS E, SAVOIR.CONNECTION AS C WHERE "
				+ "(E.END_POINT_ID = C.SOURCE_END_POINT OR E.END_POINT_ID = C.TARGET_END_POINT)"
				+ " AND C.CONNECTION_ID = ?";
		Object[] args = new Object[] { connectionId };
		return this.template.update(sql, args);
	}

	public IResourceDAO getResourceDAO() {
		return resourceDAO;
	}

	public IPersonDAO getPersonDAO() {
		return personDAO;
	}

	public void setResourceDAO(IResourceDAO resourceDAO) {
		this.resourceDAO = resourceDAO;
	}

	public void setPersonDAO(IPersonDAO personDAO) {
		this.personDAO = personDAO;
	}

}
