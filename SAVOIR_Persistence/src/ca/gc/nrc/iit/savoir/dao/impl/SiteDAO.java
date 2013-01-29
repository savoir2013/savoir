// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.iit.nrc.savoir.domain.Site;
import ca.gc.nrc.iit.savoir.dao.BaseDAO;
import ca.gc.nrc.iit.savoir.dao.ISiteDAO;

@Transactional(readOnly = true, propagation = Propagation.REQUIRED,	isolation = Isolation.READ_COMMITTED)
public class SiteDAO extends BaseDAO implements ISiteDAO {

    //annonymous inner class to map recordset rows to Site objects
    class SiteRowMapper implements RowMapper {
    	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
    		Site s = new Site();
    		s.setDescription(rs.getString("SITE_DESCRIPTION"));
    		s.setId(rs.getInt("SITE_ID"));
    		return s;
    	}
    }
	
	public void addSite(Site s) {
		System.out.println("bla bla");// TODO Auto-generated method stub
	}

	public Site getSiteById(int siteId) {
    	String sql = "SELECT * FROM SITE WHERE SITE_ID = ?";
    	Object[] args = new Object[]{siteId};
    	try {
    		Site s;
    		s = (Site) this.template.queryForObject(sql, args, new SiteRowMapper());
    		return s;
    	}catch (EmptyResultDataAccessException e){
    		return null;
    	}
	}

	@Transactional(readOnly = false)
	public void removeSite(int siteId) {
		// TODO Auto-generated method stub
		String sql = "DELETE FROM SAVOIR.SITE WHERE SITE_ID = ?";
		Object[] args = new Object[] { siteId };
		this.template.update(sql, args);

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Site> getSitesByValue(String location) {
		String sql = "SELECT * FROM SITE WHERE SITE_DESCRIPTION LIKE ?";
		Object[] args = new Object[] { location };
		try {
			List<Site> list;
			list = (List<Site>) this.template.query(sql, args,
					new SiteRowMapper());
			return list;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

}
