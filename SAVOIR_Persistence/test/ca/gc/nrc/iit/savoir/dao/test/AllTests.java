// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for ca.gc.nrc.iit.savoir.dao.test");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestSiteDAO.class);
		suite.addTestSuite(TestSessionDAO.class);				
		suite.addTestSuite(TestParametersDAO.class);
		suite.addTestSuite(TestResourceDAO.class);
		suite.addTestSuite(TestCalendarDAO.class);
		suite.addTestSuite(TestDAOFactory.class);
		//$JUnit-END$
		return suite;
	}

}
