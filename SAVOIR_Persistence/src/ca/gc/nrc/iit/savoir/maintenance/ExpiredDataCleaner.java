// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.maintenance;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import ca.gc.nrc.iit.savoir.spring.BeanManager;

public class ExpiredDataCleaner {

	private JdbcTemplate template;
    private final Log log = LogFactory.getLog(getClass());

    private ExpiredDataCleaner(DataSource dataSource) {
    	this.template = new JdbcTemplate(dataSource);
    }
    
   	/**
	 * @param table	The table to delete the expired data from (must have an END_TIME field)
	 * @return	The SQL query string to delete expired data from the table.
	 * 			This includes records where END_TIME 
	 */
	private static final String DELETE_EXPIRED_FROM(String table) {
		return "DELETE FROM " + table + " WHERE (END_TIME != 0) AND (CURRENT_TIMESTAMP - END_TIME > 0)";
	}
	
	private void deleteExpiredFrom(String table) {
		int updated = update(DELETE_EXPIRED_FROM(table));
		if (updated >= 0) log.info(updated + " expired records cleaned from " + table);
	}
	
	private int update(String sql) {
		try {
			return this.template.update(sql);
		} catch (DataAccessException e) {
			log.error(sql, e);
			return -1;
		}
	}
	
	/**
	 * TODO add offset feature (will leave expired data for X amount of time after expiry)
	 * TODO add selection of which deletions to do (via command line flags)
	 * TODO look for other data cleanup chores to add to this
	 * @param args TODO define command line flags
	 */
	public static void main(String[] args) {
		ExpiredDataCleaner cleaner = new ExpiredDataCleaner(
				(DataSource)BeanManager.getBeanManager().getContext().getBean("dataSource"));
		
		cleaner.deleteExpiredFrom("SAVOIR.USER");
		cleaner.deleteExpiredFrom("USER_GROUP");
		cleaner.deleteExpiredFrom("USER_RESOURCE");
		//cleaner.deleteExpiredFrom("CALENDAR");
	}

}
