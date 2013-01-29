// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.test;

import ca.gc.nrc.iit.savoir.dao.IParametersDAO;

public class TestParametersDAO extends BaseTestCase {

	private IParametersDAO dao; 
	
	public void setUp(){
		super.setUp();
		dao = (IParametersDAO) super.getBeanManger().getContext().getBean("parametersDAO");
	}
	
	public void testGetDefaultValueByResourceAndParameter(){
		String r = this.dao.getDefaultValueByResourceAndParameter(2, "SITE_LOCATION");		
		assertNotNull(r);						
		System.out.println(r);
				
	}
}
