// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.test;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ca.gc.nrc.iit.savoir.spring.BeanManager;


public class BaseTestCase extends TestCase{
    protected final Log log = LogFactory.getLog(getClass());
    protected BeanManager bm;
    
	protected void setUp() {
		log.info("************ " + "BaseTestCase Setup ()" + " *******************" );
		this.bm = BeanManager.getBeanManager();
	}
	
	protected void tearDown() {
		log.info("************ " + "BaseTestCase TearDown ()" + " *******************" );
		this.bm = null;
	}	
	
	protected BeanManager getBeanManger(){
		return this.bm;
	}

}
