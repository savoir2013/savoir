// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao;


import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * BaseDAO is the base class for all SAVOIR DAOs.
 * BaseDAO has the following functionality :
 * 
 * <ul>
 * <li> It sets a the reference to the Logger.
 * <li> It has a setter method for the JdbcTemplate reference which is used for 
 * 		setter injection in the Spring container.
 * </ul>
 * 
 * @author      Rene Richard
 */

public class BaseDAO {
    
	protected JdbcTemplate template;
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * datasource setter method for setter injection.
     * 
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }
}
