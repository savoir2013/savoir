// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.iit.nrc.savoir.domain.types.ParameterType;
import ca.gc.iit.nrc.savoir.domain.types.ResourceType;
import ca.gc.nrc.iit.savoir.dao.BaseDAO;
import ca.gc.nrc.iit.savoir.dao.ITypesDAO;

@Transactional(readOnly = true, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class TypesDAO extends BaseDAO implements ITypesDAO {

	class ParameterTypeMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			ParameterType s = new ParameterType(rs.getString("PARAMETER_ID"),
					rs.getString("PARAMETER_NAME"), rs
							.getString("PARAMETER_DESCRIPTION"));
			return s;
		}
	}

	class ResourceTypeMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			ResourceType s = new ResourceType(rs.getString("RESOURCE_TYPE_ID"),
					rs.getString("RESOURCE_TYPE_NAME"), rs
							.getString("RESOURCE_TYPE_DESCRIPTION"), rs
							.getString("RESOURCE_CLASS"));
			return s;
		}
	}

	@Override
	public ParameterType getParameterTypeById(String typeID) {
		log.info("Retreieving Parameter Type");
		String sql = "SELECT * FROM PARAMETER WHERE PARAMETER_ID = ?";
		Object[] args = new Object[] { typeID };
		try {
			ParameterType pt;
			pt = (ParameterType) this.template.queryForObject(sql, args,
					new ParameterTypeMapper());
			return pt;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public ResourceType getResourceTypeById(String typeID) {
		log.info("Retreieving Resource Type");
		String sql = "SELECT * FROM RESOURCE_TYPE WHERE RESOURCE_TYPE_ID = ?";
		Object[] args = new Object[] { typeID };
		try {
			ResourceType rt;
			rt = (ResourceType) this.template.queryForObject(sql, args,
					new ResourceTypeMapper());
			return rt;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ResourceType> getAllResourceTypes() {
		log.info("Retrieving resource type list");
		String sql = "SELECT * FROM RESOURCE_TYPE";
		
		return new ArrayList<ResourceType>(
				this.template.query(sql, new ResourceTypeMapper()));
	}

	@Transactional(readOnly = false)
	@Override
	public void addParameterType(ParameterType pt) {
		String sql = "INSERT INTO PARAMETER (PARAMETER_ID, PARAMETER_NAME,PARAMETER_DESCRIPTION) "
				+ "VALUES (?,?,?)";

		Object[] args = new Object[] { pt.getId(), pt.getName(),
				pt.getDescription() };

		int[] argTypes = new int[] { Types.VARCHAR, Types.VARCHAR,
				Types.VARCHAR };

		this.template.update(sql, args, argTypes);
	}

	@Transactional(readOnly = false)
	@Override
	public void addResourceType(ResourceType rt) {
		String sql = "INSERT INTO RESOURCE_TYPE (RESOURCE_TYPE_ID, RESOURCE_TYPE_NAME, RESOURCE_TYPE_DESCRIPTION) "
				+ "VALUES (?,?,?)";

		Object[] args = new Object[] { rt.getId(), rt.getName(),
				rt.getDescription() };

		int[] argTypes = new int[] { Types.VARCHAR, Types.VARCHAR,
				Types.VARCHAR };

		this.template.update(sql, args, argTypes);
	}
}
