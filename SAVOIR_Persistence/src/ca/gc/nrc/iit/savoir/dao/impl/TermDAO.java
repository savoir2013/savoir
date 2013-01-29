// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.iit.nrc.savoir.domain.Term;
import ca.gc.nrc.iit.savoir.dao.BaseDAO;
import ca.gc.nrc.iit.savoir.dao.ITermDAO;

@Transactional(readOnly = true, propagation = Propagation.REQUIRED,	isolation = Isolation.READ_COMMITTED)
public class TermDAO extends BaseDAO implements ITermDAO {
	
	//annonymous inner class to map recordset rows to Site objects
    class TermRowMapper implements RowMapper {
    	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
    		Term term = new Term();    		
    		term.setTermID(String.valueOf(rs.getInt("TERM_ID")));
    		term.setTermTitle(rs.getString("TITLE"));
    		String termImage = rs.getString("TERM_IMAGE");
    		term.setTermImage(termImage);
    		String termDesc = rs.getString("DESCRIPTION");
    		term.setTermDesc(termDesc);
    		String termLanguageLocale = rs.getString("LANGUAGE_LOCALE");
    		term.setTermLanguageLocale(termLanguageLocale);
    		
    		return term;
    	}
    }
	
    //Create
    @Transactional(readOnly = false)
    @Override
	public void addTerm(Term t) {
		// TODO Auto-generated method stub
		System.out.println("INSERT INTO TERM VALUES (?, ?, ?, ?, ?)");
	}

    //Read
    @Override
	public Term getTermById(int termId) {
    	String sql = "SELECT * FROM TERM WHERE TERM_ID = ?";
    	Object[] args = new Object[]{termId};
    	try {
    		Term s;
    		s = (Term) this.template.queryForObject(sql, args, new TermRowMapper());
    		return s;
    	}catch (EmptyResultDataAccessException e){
    		return null;
    	}
	}


	@Override
	public Term getTermByName(String termName) {
		String sql = "SELECT * FROM TERM WHERE TITLE = ?";
    	Object[] args = new Object[]{termName};
    	try {
    		Term t;
    		t = (Term) this.template.queryForObject(sql, args, new TermRowMapper());
    		return t;
    	}catch (EmptyResultDataAccessException e){
    		return null;
    	}
	}
	
	
    //Update
    @Transactional(readOnly = false)
    @Override
	public void updateTerm(int termId) {
		// TODO Auto-generated method stub
		System.out.println("UPDATE TERM SET TERM_ID = 0, TITLE = ?, IMAGE = ?, DESCRIPTION = ?, LANAGUAGE_LOCALE = ? WHERE TERM_ID = ?");
	}

    //Delete
    @Transactional(readOnly = false)
    @Override
	public void removeTerm(int termId) {
		// TODO Auto-generated method stub
		System.out.println("DELETE FROM TERM WHERE TERM_ID = ?");
	}
	
}
