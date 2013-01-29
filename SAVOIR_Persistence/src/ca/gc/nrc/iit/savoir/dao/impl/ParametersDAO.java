// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.iit.nrc.savoir.domain.ResourceParameter;
import ca.gc.nrc.iit.savoir.dao.BaseDAO;
import ca.gc.nrc.iit.savoir.dao.IParametersDAO;
import ca.gc.nrc.iit.savoir.dao.ISiteDAO;
import ca.gc.nrc.iit.savoir.dao.ITypesDAO;

@Transactional(readOnly = true, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class ParametersDAO extends BaseDAO implements IParametersDAO {

	ITypesDAO lookUpDAO;

	ISiteDAO siteDAO;
	
	// annonymous inner class to map recordset rows to ResourceParameter objects
	class ResourceParameterRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			ResourceParameter r = new ResourceParameter();
			r.setResourceParameterID(rs.getString("RESOURCE_PARAMETER_ID"));
			r.setParameter(lookUpDAO.getParameterTypeById(rs
					.getString("PARAMETER_ID")));
			if (rs.getString("PARAMETER_ID").equals("SITE_LOCATION")) {
				r.setValue(siteDAO.getSiteById(
						Integer.valueOf(rs.getString("PARAMETER_VALUE")))
						.getDescription());
			} else {
				r.setValue(rs.getString("PARAMETER_VALUE"));
			}
			return r;
		}
	}

	@Transactional(readOnly = false)
	@Override
	public void saveParameters(int resourceID, List<ResourceParameter> params,
			int sessionID) {
		if (params != null && params.size() > 0) {
			for (ResourceParameter rp : params) {
				String sql = "INSERT INTO RESOURCE_PARAMETER (RESOURCE_ID, PARAMETER_ID, PARAMETER_VALUE, SESSION_ID)"
						+ " values (?,?,?,?)";

				Object[] args = new Object[] { resourceID,
						rp.getParameter().getId(), rp.getValue(), sessionID };
				int[] argTypes = new int[] { Types.INTEGER, Types.VARCHAR,
						Types.VARCHAR, Types.INTEGER };
				this.template.update(sql, args, argTypes);
			}
		}
	}
	
	@Transactional(readOnly = false)
	@Override
	public void clearParameters(int resourceID, int sessionID) {
		
		String sql;
		Object[] args;
		int[] argTypes;
		if (sessionID < 0) { //clear all sessions
			sql = "DELETE FROM RESOURCE_PARAMETER WHERE RESOURCE_ID = ?";
			args = new Object[] {resourceID};
			argTypes = new int[] {Types.INTEGER};
		} else {			 //clear a specific session
			sql = "DELETE FROM RESOURCE_PARAMETER " +
					"WHERE RESOURCE_ID = ? AND SESSION_ID = ?";
			args = new Object[] {resourceID, sessionID};
			argTypes = new int[] {Types.INTEGER, Types.INTEGER};
		}
		
		this.template.update(sql, args, argTypes);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ResourceParameter> getDefaultParametersByResourceID(
			int resourceId) {
		String sql = "SELECT * FROM RESOURCE_PARAMETER WHERE RESOURCE_ID = ? AND SESSION_ID = 0";
		Object[] args = new Object[] { resourceId };
		try {
			List<ResourceParameter> list;
			list = (List<ResourceParameter>) this.template.query(sql, args,
					new ResourceParameterRowMapper());
			return list;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ResourceParameter> getParametersByResourceIDAndSessionID(
			int resourceId, int sessionID) {
		String sql = "SELECT * FROM RESOURCE_PARAMETER WHERE RESOURCE_ID = ? AND SESSION_ID = ?";
		Object[] args = new Object[] { resourceId, sessionID };
		try {
			List<ResourceParameter> list;
			list = (List<ResourceParameter>) this.template.query(sql, args,
					new ResourceParameterRowMapper());
			return list;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public String getDefaultValueByResourceAndParameter(int resourceID,
			String parameterID) {
		String sql = "SELECT * FROM RESOURCE_PARAMETER WHERE RESOURCE_ID = ? AND SESSION_ID = 0 AND PARAMETER_ID = ?";
		Object[] args = new Object[] { resourceID, parameterID };
		try {
			ResourceParameter rp;
			rp = (ResourceParameter) this.template.queryForObject(sql, args,
					new ResourceParameterRowMapper());
			return rp.getValue();
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public ITypesDAO getLookUpDAO() {
		return lookUpDAO;
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
}
