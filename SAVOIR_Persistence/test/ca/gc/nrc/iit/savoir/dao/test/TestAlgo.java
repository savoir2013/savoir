// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.test;

import java.util.List;
import java.util.Vector;

import ca.gc.iit.nrc.savoir.domain.Connection;
import ca.gc.iit.nrc.savoir.domain.EndPoint;
import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.nrc.iit.savoir.dao.IResourceDAO;
import ca.gc.nrc.iit.savoir.dao.ITypesDAO;
import ca.gc.nrc.iit.savoir.dao.impl.DAOFactory;

public class TestAlgo extends BaseTestCase {

	private IResourceDAO resourceDAO;

	private ITypesDAO typesDAO;

	public void setUp() {
		super.setUp();
		resourceDAO = (IResourceDAO) super.getBeanManger().getContext()
				.getBean("resourceDAO");
		typesDAO = (ITypesDAO) super.getBeanManger().getContext().getBean(
				"typesDAO");

	}

	public void testAddResource() {
		
		Resource sourceLP = resourceDAO.getResourceById(49);		
		Resource destinationLP = resourceDAO.getResourceById(50);
		
		Resource sourceLP1 = resourceDAO.getResourceById(3);		
		Resource destinationLP1 = resourceDAO.getResourceById(4);

		List<Connection> connections = new Vector<Connection>();

		EndPoint sourceEndPoint = new EndPoint();
		EndPoint targetEndPoint = new EndPoint();
		Connection connection = new Connection();

		sourceEndPoint.setNetworkEndPoint(sourceLP);
		sourceEndPoint.setEndPointType(EndPoint.CLIENT);
		targetEndPoint.setNetworkEndPoint(destinationLP);
		targetEndPoint.setEndPointType(EndPoint.SERVER);
		connection.setLpNeeded(true);
		connection.setBwRequirement(800);
		connection.setMinBwRequirement(800);
		connection.setSourceEndPoint(sourceEndPoint);
		connection.setTargetEndPoint(targetEndPoint);
		connections.add(connection);
					

		EndPoint sourceEndPoint1 = new EndPoint();
		EndPoint targetEndPoint1 = new EndPoint();
		Connection connection1 = new Connection();

		sourceEndPoint1.setNetworkEndPoint(sourceLP1);
		sourceEndPoint1.setEndPointType(EndPoint.CLIENT);
		targetEndPoint1.setNetworkEndPoint(destinationLP1);
		targetEndPoint1.setEndPointType(EndPoint.SERVER);
		connection1.setLpNeeded(true);
		connection1.setBwRequirement(800);
		connection1.setMinBwRequirement(800);
		connection1.setSourceEndPoint(sourceEndPoint1);
		connection1.setTargetEndPoint(targetEndPoint1);
		connections.add(connection1);
		
		assertNotNull(findAPNScenario(connections));

	}

	private Resource findAPNScenario(List<Connection> list) {
		List<Resource> apnScenarios = DAOFactory.getDAOFactoryInstance()
				.getResourceDAO().getResourcesByType("LP_SCENARIO");
		if (apnScenarios != null && apnScenarios.size() > 0) {
			for (Resource r : apnScenarios) {

				boolean allConnections = true;

				for (Connection cr : list) {

					List<Resource> connections = DAOFactory
							.getDAOFactoryInstance().getResourceDAO()
							.getResourcesByTypeAndParameterValue(
									"LP_CONNECTION", "LP_CONNECTION_SC",
									"" + r.getResourceID());
					if (connections != null && connections.size() > 0) {

						boolean foundMatch = false;

						for (Resource c : connections) {
							List<Resource> ept = DAOFactory
									.getDAOFactoryInstance().getResourceDAO()
									.getResourcesByTypeAndParameterValue(
											"LP_END_POINT",
											"LP_END_POINT_CONN",
											"" + c.getResourceID());
							if (ept != null && ept.size() == 2) {

								Resource source = ept.get(0);
								Resource target = ept.get(1);

								if (cr
										.getSourceEndPoint()
										.getNetworkEndPoint()
										.getParameterValue("IP_ADDRESS")
										.equals(
												source
														.getParameterValue("IP_ADDRESS"))
										|| cr
												.getSourceEndPoint()
												.getNetworkEndPoint()
												.getParameterValue("IP_ADDRESS")
												.equals(
														target
																.getParameterValue("IP_ADDRESS"))) {

									if (cr
											.getTargetEndPoint()
											.getNetworkEndPoint()
											.getParameterValue("IP_ADDRESS")
											.equals(
													source
															.getParameterValue("IP_ADDRESS"))
											|| cr
													.getTargetEndPoint()
													.getNetworkEndPoint()
													.getParameterValue(
															"IP_ADDRESS")
													.equals(
															target
																	.getParameterValue("IP_ADDRESS"))) {
										foundMatch = true;
										break;
									}
								}

							}
						}

						if (foundMatch)
							continue;
						else {
							allConnections = false;
							break;
						}
					}
				}
				if (allConnections)
					return r;
			}
		}
		return null;
	}
}
