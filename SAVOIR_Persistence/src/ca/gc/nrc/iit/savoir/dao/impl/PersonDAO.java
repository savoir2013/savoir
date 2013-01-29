// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.impl;

import static ca.gc.nrc.iit.savoir.utils.Constants.DATA_CORRUPTION;
import static ca.gc.nrc.iit.savoir.utils.Constants.INVALID_PARAMETERS;
import static ca.gc.nrc.iit.savoir.utils.Constants.NO_SUCH_ENTITY;
import static ca.gc.nrc.iit.savoir.utils.Constants.OTHER_ERROR;
import static ca.gc.nrc.iit.savoir.utils.Constants.SUCCESS;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Vector;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.iit.nrc.savoir.domain.Person;
import ca.gc.iit.nrc.savoir.domain.PersonInfo;
import ca.gc.nrc.iit.savoir.dao.BaseDAO;
import ca.gc.nrc.iit.savoir.dao.IPersonDAO;

@Transactional(readOnly = true, propagation = Propagation.REQUIRED,	isolation = Isolation.READ_COMMITTED)
@SuppressWarnings("unchecked")
public class PersonDAO extends BaseDAO implements IPersonDAO {

    //annonymous inner class to map recordset rows to Person objects
    class PersonRowMapper implements RowMapper {
    	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
    		Person p = new Person();
    		p.setPersonId(rs.getInt("PERSON_ID"));
    		
    		PersonInfo info = new PersonInfo();
    		info.setLName(rs.getString("LAST_NAME"));
    		info.setFName(rs.getString("FIRST_NAME"));
    		info.setMName(rs.getString("MIDDLE_NAME"));
    		info.setHonorific(rs.getString("TITLE"));
    		info.setEmail1(rs.getString("EMAIL_1"));
    		info.setEmail2(rs.getString("EMAIL_2"));
    		info.setWorkPhone(rs.getString("WORK_PHONE"));
    		info.setCellPhone(rs.getString("CELL_PHONE"));
    		info.setHomePhone(rs.getString("HOME_PHONE"));
    		info.setOrganization(rs.getString("ORGANIZATION"));
    		info.setStreetAddress(rs.getString("STREET_ADDRESS"));
    		info.setCity(rs.getString("CITY"));
    		info.setRegion(rs.getString("REGION"));
    		info.setCountry(rs.getString("COUNTRY"));
    		info.setPostal(rs.getString("POSTAL"));
    	
    		p.setPersonInfo(info);
    		
    		return p;
    	}
    }
	
	@Override
	public List<Person> getSessionParticipants(int sessionID) {
		// add 09-05-09 Auto-generated method stub
		String sql = "SELECT * FROM SAVOIR.PERSON WHERE PERSON_ID " 
			+ "in  SELECT PERSON_ID FROM SAVOIR.SESSION_PARTICIPANT WHERE SESSION_ID = ? ";
		Object[] args = new Object[] { sessionID };
		try {
			List<Person> list = (List<Person>) this.template.query(sql, args, new PersonRowMapper());
			log.info(list);
			return list;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
    
	@Transactional(readOnly = false)
	@Override
	public void addPerson(Person p) {
		// add 09-05-09 Auto-generated method stub
		String sql = "INSERT INTO SAVOIR.PERSON (PERSON_ID, LAST_NAME, FIRST_NAME, MIDDLE_NAME" 
			+ ", TITLE, EMAIL_1, EMAIL_2, WORK_PHONE, CELL_PHONE, HOME_PHONE"
			+", ORGANIZATION, STREET_ADDRESS, CITY, REGION, COUNTRY, POSTAL)" 
			+ " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		Object[] args = new Object[] { p.getPersonId(),p.getPersonInfo().getLName(), p.getPersonInfo().getFName()
				, p.getPersonInfo().getMName(), p.getPersonInfo().getHonorific(), p.getPersonInfo().getEmail1()
				,p.getPersonInfo().getEmail2(), p.getPersonInfo().getWorkPhone(), p.getPersonInfo().getCellPhone()
				, p.getPersonInfo().getHomePhone(), p.getPersonInfo().getOrganization(), p.getPersonInfo().getStreetAddress(), p.getPersonInfo().getCity()
				, p.getPersonInfo().getRegion(), p.getPersonInfo().getCountry(), p.getPersonInfo().getPostal()};
		int[] argTypes = new int[]{ Types.INTEGER, Types.CHAR, Types.CHAR, Types.CHAR, Types.CHAR,Types.CHAR, 
				Types.CHAR, Types.CHAR, Types.CHAR, Types.CHAR, Types.CHAR, Types.CHAR,Types.CHAR,Types.CHAR,Types.CHAR,Types.CHAR};
		this.template.update(sql, args, argTypes);
	}

	@Override
	public Person getPersonById(int personId) {
		String sql = "SELECT * FROM PERSON WHERE PERSON_ID = ?";
    	Object[] args = new Object[]{personId};
    	try {
    		Person p;
    		p = (Person) this.template.queryForObject(sql, args, new PersonRowMapper());
    		return p;
    	}catch (EmptyResultDataAccessException e){
    		return null;
    	}
	}

	@Transactional(readOnly = false)
	@Override
	public void removePerson(int personId) {
		String sql = "DELETE FROM SAVOIR.PERSON WHERE PERSON_ID = ?";
		Object[] args = new Object[] { personId };
		this.template.update(sql, args);
	}
	
	@Transactional(readOnly = false)
	@Override
	public int updatePerson(int id, PersonInfo ui) {
		if (id <= 0 || ui == null) {
			return INVALID_PARAMETERS;
		}
		
		//build SQL query
		//initialize query and argument list
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("UPDATE PERSON SET ");
		Vector<Object> argBuilder = new Vector<Object>();
		
		//for each field, check if non-null, and, if so, add to query and arg list
		String t;
		if ((t = ui.getLName()) != null) {
			sqlBuilder.append("LAST_NAME = ?,");
			argBuilder.addElement(t);
		}
		if ((t = ui.getFName()) != null) {
			sqlBuilder.append("FIRST_NAME = ?,");
			argBuilder.addElement(t);
		}
		if ((t = ui.getMName()) != null) {
			sqlBuilder.append("MIDDLE_NAME = ?,");
			argBuilder.addElement(t);
		}
		if ((t = ui.getHonorific()) != null) {
			sqlBuilder.append("TITLE = ?,");
			argBuilder.addElement(t);
		}
		if ((t = ui.getEmail1()) != null) {
			sqlBuilder.append("EMAIL_1 = ?,");
			argBuilder.addElement(t);
		}
		if ((t = ui.getEmail2()) != null) {
			sqlBuilder.append("EMAIL_2 = ?,");
			argBuilder.addElement(t);
		}
		if ((t = ui.getWorkPhone()) != null) {
			sqlBuilder.append("WORK_PHONE = ?,");
			argBuilder.addElement(t);
		}
		if ((t = ui.getCellPhone()) != null) {
			sqlBuilder.append("CELL_PHONE = ?,");
			argBuilder.addElement(t);
		}
		if ((t = ui.getHomePhone()) != null) {
			sqlBuilder.append("HOME_PHONE = ?,");
			argBuilder.addElement(t);
		}
		if ((t = ui.getOrganization()) != null) {
			sqlBuilder.append("ORGANIZATION = ?,");
			argBuilder.addElement(t);
		}
		if ((t = ui.getStreetAddress()) != null) {
			sqlBuilder.append("STREET_ADDRESS = ?,");
			argBuilder.addElement(t);
		}
		if ((t = ui.getCity()) != null) {
			sqlBuilder.append("CITY = ?,");
			argBuilder.addElement(t);
		}
		if ((t = ui.getRegion()) != null) {
			sqlBuilder.append("REGION = ?,");
			argBuilder.addElement(t);
		}
		if ((t = ui.getCountry()) != null) {
			sqlBuilder.append("COUNTRY = ?,");
			argBuilder.addElement(t);
		}
		if ((t = ui.getPostal()) != null) {
			sqlBuilder.append("POSTAL = ?,");
			argBuilder.addElement(t);
		}
		
		//trim final comma, (if none, no change, return invalid parameters)
		int lastIndex = sqlBuilder.length() - 1;
		if (sqlBuilder.charAt(lastIndex) == ',') {
			sqlBuilder.deleteCharAt(lastIndex);
		} else {
			return INVALID_PARAMETERS;
		}
		
		//finalize query and argument list
		sqlBuilder.append(" WHERE PERSON_ID = ?");
		argBuilder.addElement(id);
		
		//reformat query and arguments
		String sql = sqlBuilder.toString();
		Object[] args = argBuilder.toArray(new Object[argBuilder.size()]);
		
		try {
			int updated = template.update(sql, args);
			switch (updated) {
			case 1:		//a single record updated - the correct behaviour
				return SUCCESS;
			case 0:		//no records updated (ergo no records match user ID)
				return NO_SUCH_ENTITY;
			default:	//multiple records updated (should not happen - means data corruption)
				log.fatal(updated + " records updated on query \"" + sql + "\".\n" +
							"Data corruption likely.");
				return DATA_CORRUPTION;
			}
		} catch (DataAccessException e) {
			log.error("Persistence error on Person update", e);
			return OTHER_ERROR;
		}
	}

	@Override
	public int getNextPersonId() {
		String sql = "SELECT MAX(PERSON_ID) + 1 FROM PERSON";
		
		try {
			//get DNames matching given
			return this.template.queryForInt(sql);
		} catch (DataAccessException e) {
			log.error("Data access error on max personID lookup", e);
			return 0;
		}
	}

}
