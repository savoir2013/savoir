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


import ca.gc.iit.nrc.savoir.domain.Constraint;
import ca.gc.nrc.iit.savoir.dao.BaseDAO;
import ca.gc.nrc.iit.savoir.dao.IConstraintDAO;


@Transactional(readOnly = true, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
@SuppressWarnings("unchecked")
public class ConstraintDAO extends BaseDAO implements IConstraintDAO {

	class ConstraintRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			Constraint constraint = new Constraint();
			constraint.setId(rs.getString("RESOURCE_CONSTRAINT_ID"));
			constraint.setResourceId(rs.getString("RESOURCE_ID"));
			constraint.setParameterId(rs.getString("PARAMETER_ID"));
			constraint.setConfigArgs(rs.getString("CONFIG_ARGS"));
			return constraint;
		}
	}

	@Transactional(readOnly = false)
	@Override
	public int addConstraint(Constraint c) {
		// add at 09-05-09
		String sql = "INSERT INTO SAVOIR.RESOURCE_CONSTRAINT " +
				"(RESOURCE_CONSTRAINT_ID, RESOURCE_ID, PARAMETER_ID, " +
				"CONFIG_ARGS) VALUES (?,?,?,?)";
		Object[] args = new Object[] { c.getId(), c.getResourceId(), 
				c.getParameterId(), c.getConfigArgs()};
		int[] argTypes = new int[]{ Types.CHAR, Types.INTEGER,
				Types.CHAR, Types.VARCHAR};
		
		return this.template.update(sql, args, argTypes);
	}

	@Override
	public List<Constraint> getConstraintsByResourceID(int resourceID) {
		String sql = "SELECT * FROM SAVOIR.RESOURCE_CONSTRAINT " +
				"WHERE RESOURCE_ID = ? ";
		log.info(sql);
		Object[] args = new Object[] { resourceID };
		int[] argTypes = new int[] {Types.INTEGER};
		
		try {
			List<Constraint> list;
			list = (List<Constraint>) this.template.query(sql, args, argTypes,
					new ConstraintRowMapper());
			log.info(list);
			return list;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Transactional(readOnly = false)
	@Override
	public int removeConstraintsByResourceID(int resourceID) {
		String sql = "DELETE FROM SAVOIR.RESOURCE_CONSTRAINT " +
				"WHERE RESOURCE_ID = ?";
		log.info(sql);
		Object[] args = new Object[] {resourceID};
		int[] argTypes = new int[] {Types.INTEGER};
		
		return this.template.update(sql, args, argTypes);
	}
}
