// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.test;

import ca.gc.iit.nrc.savoir.domain.Site;
import ca.gc.nrc.iit.savoir.dao.ISiteDAO;

public class TestSiteDAO extends BaseTestCase {

	private ISiteDAO dao; 
	
	public void setUp(){
		super.setUp();
		dao = (ISiteDAO) super.getBeanManger().getContext().getBean("siteDAO");
	}
	
	public void testAddSite(){		
		dao.addSite(new Site ());
		assertTrue(true);
	}
	
	public void testGetSite(){
		Site s = this.dao.getSiteById(2);
		assertNotNull(s);
		assertTrue(s.getDescription().equals("OTTAWA"));
	}
}
