// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.test;

import java.util.ArrayList;
import java.util.List;

import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.iit.nrc.savoir.domain.ResourceParameter;
import ca.gc.nrc.iit.savoir.dao.IResourceDAO;
import ca.gc.nrc.iit.savoir.dao.ITypesDAO;

public class TestResourceDAO extends BaseTestCase {

	private IResourceDAO resourceDAO;
	
	private ITypesDAO typesDAO;

	public void setUp() {
		super.setUp();
		resourceDAO = (IResourceDAO) super.getBeanManger().getContext()
				.getBean("resourceDAO");
		typesDAO = (ITypesDAO) super.getBeanManger().getContext()
		.getBean("typesDAO");

	}
	
	public void testAddResource(){
		Resource reservationResource = new Resource();
		reservationResource
				.setDescription("chronos reservation");
		reservationResource
				.setResourceType(typesDAO.getResourceTypeById(
								"CHRONOS_RESERVATION"));

		List<ResourceParameter> params = new ArrayList<ResourceParameter>();
		ResourceParameter reservationID = new ResourceParameter();
		reservationID.setParameter(typesDAO
				.getParameterTypeById(
						"CHRONOS_RESERVATION_ID"));
		reservationID.setValue("1254486");
		params.add(reservationID);
		reservationResource.setParameters(params);

		System.out.println(resourceDAO
				.addResource(reservationResource));
	}

	public void testGetResourceById() {
		Resource r = this.resourceDAO.getResourceById(1);
		assertNotNull(r);
		assertTrue(r.getResourceType().getId().equals("LP_END_POINT"));
	}

	public void testGetResourceByType() {
		List<Resource> r = this.resourceDAO
				.getResourcesByType("LP_END_POINT");
		assertNotNull(r);
		assertTrue(r.size() == 8);
		for (Resource res : r) {
			System.out.println(res.getDescription());
		}
	}

	public void testGetResourceByTypeAndParamValue() {
		List<Resource> r = this.resourceDAO
				.getResourcesByTypeAndParameterValue("LP_END_POINT",
						"SITE_LOCATION", "OTTAWA");
		assertNotNull(r);
		for (Resource res : r) {
			System.out.println(res.getDescription());
		}
		assertTrue(r.size() == 4);
	}

	public void testGetDefaultValueBySwitchID() {
		List<Resource> r = this.resourceDAO
				.getResourcesByTypeAndParameterValue("LP_END_POINT",
						"LP_END_POINT_SWITCH_ID", "GLIF-HDXC-SEA01");
		assertNotNull(r.get(0));
		String location = r.get(0).getParameterValue("SITE_LOCATION");
		System.out.println(location);
	}
}
