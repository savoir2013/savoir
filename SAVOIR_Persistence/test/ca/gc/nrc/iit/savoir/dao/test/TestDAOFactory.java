// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.test;

import java.util.List;

import junit.framework.TestCase;

import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.nrc.iit.savoir.dao.IDAOFactory;
import ca.gc.nrc.iit.savoir.dao.impl.DAOFactory;


public class TestDAOFactory extends TestCase{

	public void testFactory(){
		IDAOFactory factory = DAOFactory.getDAOFactoryInstance();		
		List<Resource> r = factory.getResourceDAO().getResourcesByTypeAndParameterValue("LP_END_POINT","LP_END_POINT_SWITCH_ID","GLIF-HDXC-SEA01");		
		assertNotNull(r.get(0));
		String location = r.get(0).getParameterValue("SITE_LOCATION");									
		System.out.println(location);
	}
	
}
