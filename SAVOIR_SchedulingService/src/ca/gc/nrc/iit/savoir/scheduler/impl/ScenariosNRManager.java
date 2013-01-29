// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConstrainedDijkstraShortestPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.ListenableUndirectedGraph;

import ca.gc.iit.nrc.savoir.argia.integration.ArgiaServletClient;
import ca.gc.iit.nrc.savoir.domain.Connection;
import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.iit.nrc.savoir.domain.ResourceParameter;
import ca.gc.iit.nrc.savoir.domain.Session;
import ca.gc.iit.nrc.savoir.domain.TimeSlot;
import ca.gc.nrc.iit.savoir.dao.impl.DAOFactory;
import ca.gc.nrc.iit.savoir.graph.model.ApnConnection;
import ca.gc.nrc.iit.savoir.scheduler.INRManager;
import ca.gc.nrc.iit.savoir.scheduler.types.ExceptionCaught;
import ca.gc.nrc.iit.savoir.scheduler.types.NoScenarioFoundException;
import ca.gc.nrc.iit.savoir.scheduler.types.ScenarioConflict;
import ca.gc.nrc.iit.savoir.scheduler.types.SchedulingConflict;
import ca.gc.nrc.iit.savoir.scheduler.types.domain.Reservation;
import ca.gc.nrc.iit.savoir.scheduler.types.domain.ReservationResource;
import ca.gc.nrc.iit.savoir.scheduler.types.domain.ReservationService;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.ActivateNetworkReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.ActivateNetworkReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CancelNetworkReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CancelNetworkReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CreateNetworkReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CreateNetworkReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.DeleteNetworkReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.DeleteNetworkReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.GetNetworkReservationStatus;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.GetNetworkReservationStatusResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.GetNetworkReservations;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.GetNetworkReservationsResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.IsNetworkAvailable;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.IsNetworkAvailableResponse;
import ca.gc.nrc.iit.savoir.utils.ConfigResponse;
import ca.gc.nrc.iit.savoir.utils.ResponseParser;
import ca.gc.nrc.iit.savoir.utils.Utils;

public class ScenariosNRManager implements INRManager {

	private static final Logger logger = Logger
			.getLogger(ScenariosNRManager.class);

	private Map<Integer, UndirectedGraph<String, ApnConnection>> graphs = new HashMap<Integer, UndirectedGraph<String, ApnConnection>>();

	@Override
	public CancelNetworkReservationResponse cancelReservation(
			CancelNetworkReservation req) {

		CancelNetworkReservationResponse response = new CancelNetworkReservationResponse();

		Object resource = DAOFactory.getDAOFactoryInstance().getCalendarDAO()
				.getAssociatedObject(
						Integer.parseInt(req.getResource().getParameterValue(
								"ENTRY_ID")));
		if (resource instanceof Resource) {
			try {
				//Temporally comment out send query scenario req by YYH 
				//unnessary to send query scenario req to Argia if we have no codes to update our scenario ID in our database
				//in future we may add this ability. Now we just send unsetscenario req based on ID in our database.
				
//				if ((new ArgiaServletClient()).getCurrentScenarioID()
//						.equalsIgnoreCase(
//								((Resource) resource)
//										.getParameterValue("LP_SCENARIO_ID"))) {

					ConfigResponse cfgResp = (ConfigResponse) ResponseParser
							.parse((new ArgiaServletClient())
									.unSetLightPath(((Resource) resource)
											.getParameterValue("LP_SCENARIO_ID")));

					if (cfgResp.isDone())
						response.setSuccessful(true);
					else
						response.setSuccessful(false);
//				} else {
//					response.setSuccessful(true);
//				}
			} catch (Exception e) {
				response.setSuccessful(false);
				e.printStackTrace();
				return response;
			}

		} else {
			response.setSuccessful(false);
		}
		return response;
	}

	@Override
	public DeleteNetworkReservationResponse deleteReservation(
			DeleteNetworkReservation req) {
		DeleteNetworkReservationResponse response = new DeleteNetworkReservationResponse();
		if (DAOFactory.getDAOFactoryInstance().getCalendarDAO().removeEntry(
				Integer.parseInt(req.getResource()
						.getParameterValue("ENTRY_ID"))) == 1)
			response.setSuccessful(true);
		else
			response.setSuccessful(false);

		return response;
	}

	@Override
	public ActivateNetworkReservationResponse activate(
			ActivateNetworkReservation rq) {

		ActivateNetworkReservationResponse response = new ActivateNetworkReservationResponse();
		//added debugging information by yyh
		logger.info("Scenarios NRManager: The activated Entry_ID = " + rq.getNetworkResource()
				.getParameterValue("ENTRY_ID"));
		//end add
		Object resource = DAOFactory.getDAOFactoryInstance().getCalendarDAO()
				.getAssociatedObject(
						Integer.parseInt(rq.getNetworkResource()
								.getParameterValue("ENTRY_ID")));
		if (resource instanceof Resource) {
			try {
				// note that ArgiaServlet tests if the current configured
				// scenario is the one you want to set before issuing a request
				// ;)
				ConfigResponse cfgResp = (ConfigResponse) ResponseParser
						.parse((new ArgiaServletClient())
								.setLightPath(((Resource) resource)
										.getParameterValue("LP_SCENARIO_ID")));
				if (cfgResp.isDone())
					response.setSuccessful(true);
				else
					response.setSuccessful(false);
			} catch (Exception e) {
				response.setSuccessful(false);
				e.printStackTrace();
				return response;
			}
		} else {
			response.setSuccessful(false);
		}
		return response;
	}

	@Override
	public CreateNetworkReservationResponse createReservation(
			CreateNetworkReservation req) {

		CreateNetworkReservationResponse response = new CreateNetworkReservationResponse();
		SchedulingConflict[] sc;

		if (!checkIntegrity(req.getReservation())) {
			sc = new SchedulingConflict[1];

			ExceptionCaught ec = new ExceptionCaught();
			ec
					.setExceptionMessage("Cannot reserve two different scenarios for the same time slot");
			ec.setMessage("Reservation not possible.");
			sc[0] = ec;

			response.setSchedulingConflicts(sc);
			response.setSuccessful(false);

			return response;
		}

		req.setReservation(getUpdatedReservationObject(req.getReservation()));

		IsNetworkAvailable isAv = new IsNetworkAvailable(req);
		IsNetworkAvailableResponse isAvResp = isAvailable(isAv);

		if (isAvResp.isSuccessful()) {
			for (ReservationService s : req.getReservation().getServicesList()) {

				Session sess = DAOFactory.getDAOFactoryInstance()
						.getSessionDAO().getSessionById(s.getSessionID());

				if (s.getResourcesList().size() > 0) {

					int res = DAOFactory.getDAOFactoryInstance()
							.getCalendarDAO().addEntry(
									Resource.class,
									s.getResourcesList().get(0)
											.getNetworkResourceID(),
									new TimeSlot(s.getStartTime(), s
											.getEndTime()));

					if (res == 0) {

						sc = new SchedulingConflict[1];
						ExceptionCaught ec = new ExceptionCaught();
						ec
								.setExceptionMessage("Unable to persist the reservation in the system.");
						ec.setMessage("Reservation not possible.");
						sc[0] = ec;
						response.setSchedulingConflicts(sc);
						response.setSuccessful(false);
						return response;
					} else {
						// ...assign the scenario reservation as a network
						// reservation
						// resource for the connections...
						// TODO: create a resource SCENARIO_RESERVATION with a
						// parameter ENTRY_ID == res
						if (sess.getConnections() != null
								&& sess.getConnections().size() > 0)
							for (Connection c : sess.getConnections()) {

								Resource reservation = new Resource();
								reservation.setResourceType(DAOFactory
										.getDAOFactoryInstance().getTypesDAO()
										.getResourceTypeById(
												"SCENARIO_RESERVATION"));
								reservation
										.setDescription("Scenario reservation");

								ResourceParameter param = new ResourceParameter();
								param.setParameter(DAOFactory
										.getDAOFactoryInstance().getTypesDAO()
										.getParameterTypeById("ENTRY_ID"));
								param.setValue(res + "");
								ArrayList<ResourceParameter> tempParas = new ArrayList<ResourceParameter>();
								tempParas.add(param);
								reservation.setParameters(tempParas);
								//reservation.getParameters().add(param);
								//added debugging info by YYH
								logger.info("Scenario NRManager: Reservation ID: " + reservation.getResourceID());
								logger.info("Scenario NRManager: Reservation Param Type ID: " + param.getParameter().getId());
								logger.info("Scenario NRManager: Reservation Param value: " + param.getValue());
								//end add

								int id = DAOFactory.getDAOFactoryInstance()
										.getResourceDAO().addResource(
												reservation);

								c.setNetworkResource(DAOFactory
										.getDAOFactoryInstance()
										.getResourceDAO().getResourceById(id));
							}

						// ...and update the sub-session
						DAOFactory.getDAOFactoryInstance().getSessionDAO()
								.updateSession(sess);

						logger.info("subsession scheduled successfully.");
					}
				}

			}

			response.setSuccessful(true);
			response.setTimeOffset(0);
			return response;

		} else {
			response.setSuccessful(false);
			response.setSchedulingConflicts(isAvResp.getSchedulingConflicts());
			response.setTimeOffset(isAvResp.getTimeOffset());
			return response;
		}
	}

	@Override
	public GetNetworkReservationsResponse getReservations(
			GetNetworkReservations req) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IsNetworkAvailableResponse isAvailable(IsNetworkAvailable req) {
		IsNetworkAvailableResponse response = new IsNetworkAvailableResponse();

		// Map<Integer, List<TimeSlot>> calendars = new HashMap<Integer,
		// List<TimeSlot>>();

		Reservation clone = new Reservation(req.getReservation());
		long slideVector = 0;
		long totalSlideVector = 0;
		List<SchedulingConflict> conflicts = new Vector<SchedulingConflict>();
		List<TimeSlot> conflictingTimeSlots = new Vector<TimeSlot>();
		boolean firstRun = true;

		if (req.getReservation().getServicesList() != null
				&& req.getReservation().getServicesList().size() > 0) {
			response.setSuccessful(true);
			response.setTimeOffset(0);
			return response;
		}

		Session s = DAOFactory.getDAOFactoryInstance().getSessionDAO()
				.getSessionById(req.getReservation().getSessionID());

		do {
			long latestBlockedStartTime = Long.MIN_VALUE;
			long earliestConflictingEndTime = Long.MAX_VALUE;
			slideVector = 0;

			// for every reservation service checks if all the resources are
			// available. Since we are dealing with scenarios the service can't
			// request more than one resource per service.
			int index = s.getSubSessions().size();
			for (int i = 0; i < index; i++) {
				slideVector = 0;

				// conflictingTimeSlots keeps track of the time slots that
				// conflicts with the current ReservationService. It will be
				// used to determine the sliding vector.
				conflictingTimeSlots = new Vector<TimeSlot>();

				// conflicts list should only hold the first conflicts
				// encountered
				if (firstRun) {
					conflicts.add(new ScenarioConflict(0,
							"No Scenario available", s.getSubSessions().get(i)
									.getRequestedStartTime(), s
									.getSubSessions().get(i)
									.getRequestedEndTime()));
				}
				conflictingTimeSlots.addAll(DAOFactory.getDAOFactoryInstance()
						.getCalendarDAO().getScenariosCalendar(null, null));

				if (conflictingTimeSlots.size() != 0
						&& s.getSubSessions().get(i).getRequestedStartTime()
								.getTimeInMillis() > latestBlockedStartTime) {
					for (TimeSlot ts : conflictingTimeSlots) {
						if (ts.getEndTime().getTimeInMillis() < earliestConflictingEndTime
								&& ts.getEndTime().getTimeInMillis() > s
										.getSubSessions().get(i)
										.getRequestedStartTime()
										.getTimeInMillis()) {
							latestBlockedStartTime = s.getSubSessions().get(i)
									.getRequestedStartTime().getTimeInMillis();
							earliestConflictingEndTime = ts.getEndTime()
									.getTimeInMillis();
							slideVector = earliestConflictingEndTime
									- latestBlockedStartTime;
						}
					}
				}

				if (firstRun)
					firstRun = false;

				if (slideVector != 0) {
					s.slideStartTimes(slideVector);
					// clone.applySlidingVector(slideVector);
					try {
						clone = getReservationObject(s);
					} catch (NoScenarioFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					totalSlideVector += slideVector;
					break;
				} else if (clone.getServicesList() == null
						|| clone.getServicesList().size() == 0) {
					response.setTimeOffset(0);
					response.setSuccessful(false);
					SchedulingConflict[] scConf = (SchedulingConflict[]) conflicts
							.toArray(new SchedulingConflict[0]);
					response.setSchedulingConflicts(scConf);
					return response;
				}

			}
			// repeat until no sliding is necessary and there is no conflicts
			//
		} while (slideVector != 0
				&& (clone.getServicesList() == null || clone.getServicesList()
						.size() == 0));

		if (conflicts.size() != 0) {
			response.setTimeOffset((int) totalSlideVector);
			response.setSuccessful(false);
			SchedulingConflict[] scConf = (SchedulingConflict[]) conflicts
					.toArray(new SchedulingConflict[0]);
			response.setSchedulingConflicts(scConf);
		} else {
			response.setSuccessful(true);
			response.setTimeOffset(0);
		}

		return response;

	}

	private boolean checkIntegrity(Reservation reservation) {
		// TODO: raise scheduling exception with details about the conflicting
		// time slots => useful to suggest another start time
		List<ReservationService> services = reservation.getServicesList();

		int loopIndex = services.size();

		// a service cannot request two different scenarios
		for (int i = 0; i < loopIndex; i++) {
			for (int j = 0; j < services.get(i).getResourcesList().size() - 1; j++) {
				for (int k = j + 1; k < services.get(i).getResourcesList()
						.size(); k++) {
					if (services.get(i).getResourcesList().get(j)
							.getNetworkResourceID() != services.get(i)
							.getResourcesList().get(k).getNetworkResourceID()) {
						return false;
					}
				}
			}
		}

		// two different services cannot request different scenarios if they
		// overlap in time
		for (int i = 0; i < loopIndex - 1; i++) {
			for (int j = i + 1; j < loopIndex; j++) {
				for (ReservationResource res1 : services.get(i)
						.getResourcesList()) {
					for (ReservationResource res2 : services.get(j)
							.getResourcesList()) {
						if (res1.getNetworkResourceID() != res2
								.getNetworkResourceID()
								&& timeOverlap(services.get(i), services.get(j)) != 0)
							return false;
					}
				}
			}
		}

		return true;
	}

	private Reservation getUpdatedReservationObject(Reservation res) {
		Reservation newRes = new Reservation();
		for (ReservationService s : res.getServicesList()) {
			boolean flag = false;
			for (ReservationService ns : newRes.getServicesList()) {
				switch (timeOverlap(s, ns)) {
				case 1:
					ns.setStartTime(s.getStartTime().getTimeInMillis());
					ns.setEndTime(s.getEndTime().getTimeInMillis());
					flag = true;
					break;
				case 2:
					ns.setStartTime(s.getStartTime().getTimeInMillis());
					flag = true;
					break;
				case 3:
					ns.setEndTime(s.getEndTime().getTimeInMillis());
					flag = true;
					break;
				case 4:
					flag = true;
					break;
				}
				if (flag)
					break;
			}
			if (!flag) {
				newRes.getServicesList().add(s);
			}
		}
		return res;
	}

	private int timeOverlap(ReservationService rS1, ReservationService rS2) {

		if (rS1.getStartTime().getTimeInMillis() <= rS2.getStartTime()
				.getTimeInMillis()
				&& rS2.getEndTime().getTimeInMillis() <= rS1.getEndTime()
						.getTimeInMillis())
			return 1;
		if (rS1.getStartTime().getTimeInMillis() <= rS2.getStartTime()
				.getTimeInMillis()
				&& rS2.getEndTime().getTimeInMillis() >= rS1.getEndTime()
						.getTimeInMillis()
				&& rS2.getStartTime().getTimeInMillis() < rS1.getEndTime()
						.getTimeInMillis())
			return 2;
		if (rS1.getStartTime().getTimeInMillis() >= rS2.getStartTime()
				.getTimeInMillis()
				&& rS2.getEndTime().getTimeInMillis() <= rS1.getEndTime()
						.getTimeInMillis()
				&& rS2.getEndTime().getTimeInMillis() > rS1.getStartTime()
						.getTimeInMillis())
			return 3;
		if (rS1.getStartTime().getTimeInMillis() >= rS2.getStartTime()
				.getTimeInMillis()
				&& rS2.getEndTime().getTimeInMillis() >= rS1.getEndTime()
						.getTimeInMillis())
			return 4;
		return 0;

		// sb.append(
		// "defbool active(i,j,t) := (Times[t] <= STime[i,j] and ETime[i,j] <= Times[t+1]) "
		// +
		// "or (Times[t] <= STime[i,j] and ETime[i,j] >= Times[t+1] and STime[i,j] < Times[t+1]) "
		// +
		// "or (Times[t] >= STime[i,j] and ETime[i,j] <= Times[t+1] and ETime[i,j] > Times[t]) "
		// +
		// "or (Times[t] >= STime[i,j] and ETime[i,j] >= Times[t+1]);\n\n");

		// return false;
	}

	@Override
	public GetNetworkReservationStatusResponse getNetworkStatus(
			GetNetworkReservationStatus req) {
		// TODO get the status of the scenario if possible
		GetNetworkReservationStatusResponse response = new GetNetworkReservationStatusResponse();
		response.setStatus("ACTIVE");
		return response;
	}

	/**
	 * This method goes through the Session s looking for required reservations.
	 * Only PENDING subsessions are considered.
	 * 
	 * @param s
	 *            Session
	 * @return Reservation object with a list of ReservationService
	 * @throws NoScenarioFoundException
	 */
	public Reservation getReservationObject(
			ca.gc.iit.nrc.savoir.domain.Session s)
			throws NoScenarioFoundException {

		Reservation reservation = new Reservation();
		reservation.setSessionID(s.getSessionID());

		// important assumption, only one scenario is set at a time => we look
		// for the scenarios reserved between start and end times,
		// if > 1 then fail
		// else if == 1 then check if that scenario can accommodate S
		// else if == 0 any scenario can be chosen

		List<Resource> apnScenarios = DAOFactory.getDAOFactoryInstance()
				.getResourceDAO().getAPNReservedBetween(getMinStartTime(s),
						getMaxEndTime(s));

		if (apnScenarios == null || apnScenarios.size() == 0) {
			apnScenarios = DAOFactory.getDAOFactoryInstance().getResourceDAO()
					.getResourcesByType("LP_SCENARIO");
		} else if (apnScenarios.size() > 1)
			return null;
		//added by YYH for debug log
		for(Resource tr : apnScenarios){
		logger.info("The APN resource Scenarios " + tr.getDescription() + "\n Resource ID is:" + tr.getResourceID());
		}
		
		//end debug

		// get the list of sessions that are of interest: sessions that are
		// running/loading/scheduled/ and use the lightpath
		List<Session> rSessions = DAOFactory.getDAOFactoryInstance()
				.getSessionDAO().getRelevantSessionsInTimeInterval(
						getMinStartTime(s), getMaxEndTime(s));

		// determine the list of relevant time points to consider
		List<Long> rTimes = getRTimesList(rSessions, s);

		// for every possible APN we create a corresponding graph updated with
		// the available bw
		createScenariosGraphs(rSessions, rTimes, apnScenarios, s);

		for (Integer key : graphs.keySet()) {
			System.out.println(graphs.get(key));
		}

		// for every scenario, could be one or all of them
		main_loop: for (Resource r : apnScenarios) {
			boolean possible = true;
			for (Session sub : s.getSubSessions()) {
				if (sub.getStatus().equals(Session.PENDING)
						&& sub.getConnections() != null
						&& sub.getConnections().size() > 0) {
					// for every connection
					for (Connection c : sub.getConnections()) {
						if (c.isLpNeeded()) {
							// see if the APN has enough bandwidth => look for
							// the
							// shortest path
							// for that connection
							
							//added by YYH for debug log
							logger.info("The master session ID = " + s.getSessionID());
							logger.info("The sub session ID = " + sub.getSessionID());
							logger.info("The APN Graphs resource ID = " + r.getResourceID());
							logger.info("The connection ID = " + c.getConnectionID());
							logger.info("The connection source end point ID = " + c.getSourceEndPoint().getEndPointID());
							logger.info("The connection source network point resource ID = " + c.getSourceEndPoint().getNetworkEndPoint().getResourceID());
							logger.info("The start vertex = " + c.getSourceEndPoint().getNetworkEndPoint()
									.getParameterValue("LP_END_POINT_SWITCH_ID"));
							logger.info("The connection target end point ID = " + c.getTargetEndPoint().getEndPointID());
							logger.info("The connection target network point resource ID = " + c.getTargetEndPoint().getNetworkEndPoint().getResourceID());
							logger.info("The end vertex = " + c.getTargetEndPoint().getNetworkEndPoint()
									.getParameterValue("LP_END_POINT_SWITCH_ID"));
							//end debug
							ConstrainedDijkstraShortestPath sp = new ConstrainedDijkstraShortestPath(
									graphs.get(r.getResourceID()), c
											.getSourceEndPoint()
											.getNetworkEndPoint()
											.getParameterValue(
													"LP_END_POINT_SWITCH_ID"),
									c.getTargetEndPoint().getNetworkEndPoint()
											.getParameterValue(
													"LP_END_POINT_SWITCH_ID"),
									c.getMinBwRequirement(),
									overlappingSublist(rTimes, s
											.getRequestedStartTime()
											.getTimeInMillis(), s
											.getRequestedEndTime()
											.getTimeInMillis()));

							if (sp.getPath() != null) {
								// update graph and continue
								for (Object e : sp.getPathEdgeList()) {
									System.out.println("before: "
											+ ((ApnConnection) e));
									substract_edge_time_bw((ApnConnection) e, c
											.getMinBwRequirement(), sub
											.getRequestedStartTime()
											.getTimeInMillis(), sub
											.getRequestedEndTime()
											.getTimeInMillis());
									System.out.println("after: "
											+ ((ApnConnection) e));
								}
								continue;
							} else {
								possible = false;
								if (apnScenarios.size() == 1) {
									// if no and apnScenarios.size == 1 stop
									break main_loop;
								} else if (apnScenarios.size() > 1) {
									// if no and apnScenarios.size > 1
									// cancel!!!!
									// and try
									// another APN(break twice)
									continue main_loop;
								}
							}
						}
					}
				}
			}
			if (possible) {
				// if all connections have been accepted then create the
				// reservation
				// object with that scenario
				System.out.println("Reservation possible with "
						+ r.getResourceID());
				for (Session sub : s.getSubSessions()) {
					reservation.getServicesList().add(getService(sub, r));
				}
				return reservation;
			}
		}
		return reservation;
	}

	/**
	 * This method defines what network reservation needs to be done according
	 * to the Session definition
	 * 
	 * @param ss
	 *            Session
	 * @param service
	 *            Instance of ReservationService that will hold the relevant
	 *            info and be returned by the method
	 * @return The instance of ReservationService passed as parameter.
	 * @throws NoScenarioFoundException
	 */
	private ReservationService getService(
			ca.gc.iit.nrc.savoir.domain.Session ss, Resource apnScenario)
			throws NoScenarioFoundException {
		ReservationService service = new ReservationService();
		service.setStartTime(ss.getRequestedStartTime().getTimeInMillis());
		service.setEndTime(ss.getRequestedEndTime().getTimeInMillis());
		service.setSessionID(ss.getSessionID());

		int n = 0;

		if (ss.getConnections() != null) {
			for (Connection connection : ss.getConnections()) {
				if (connection.isLpNeeded()) {
					break;
				} else {
					n++;
				}
			}
		} else {
			return service;
		}

		if (n == ss.getConnections().size())
			return service;

		ReservationResource res = new ReservationResource();
		res.setNetworkResourceID(apnScenario.getResourceID());
		service.getResourcesList().add(res);

		return service;
	}

	private static List<Long> overlappingSublist(List<Long> rtimes,
			long startTime, long endTime) {

		List<Long> result = rtimes.subList(rtimes.indexOf(startTime), rtimes
				.indexOf(endTime) + 1);
		return result;
	}

	private List<Long> populateTimes(List<Session> sessionsList) {

		List<Long> timesList = new Vector<Long>();
		int maxI = sessionsList.size();
		for (int i = 0; i < maxI; i++) {
			int maxJ = sessionsList.get(i).getSubSessions().size();
			for (int j = 0; j < maxJ; j++) {
				if (!timesList.contains((sessionsList.get(i).getSubSessions()
						.get(j).getTimeSlot().getEndTime().getTimeInMillis()))) {
					timesList.add((sessionsList.get(i).getSubSessions().get(j)
							.getTimeSlot().getEndTime().getTimeInMillis()));
				}

				if (!timesList
						.contains((sessionsList.get(i).getSubSessions().get(j)
								.getTimeSlot().getStartTime().getTimeInMillis()))) {
					timesList.add((sessionsList.get(i).getSubSessions().get(j)
							.getTimeSlot().getStartTime().getTimeInMillis()));
				}
			}
		}
		Collections.sort(timesList);
		return timesList;
	}

	private Calendar getMinStartTime(Session s) {
		Calendar cal = s.getRequestedStartTime();

		if (s.getSubSessions() != null)
			for (Session ss : s.getSubSessions()) {
				if (ss.getRequestedStartTime().before(cal)) {
					cal = ss.getRequestedStartTime();
				}
			}
		return cal;
	}

	private Calendar getMaxEndTime(Session s) {
		Calendar cal = s.getRequestedEndTime();
		if (s.getSubSessions() != null)
			for (Session ss : s.getSubSessions()) {
				if (ss.getRequestedEndTime().before(cal)) {
					cal = ss.getRequestedEndTime();
				}
			}
		return cal;
	}

	private List<Long> getRTimesList(List<Session> sessionsList, Session s) {

		List<Long> rtimes = new Vector<Long>();
		if (s.getSubSessions() != null) {
			int maxJ = s.getSubSessions().size();
			for (int j = 0; j < maxJ; j++) {
				if (!rtimes.contains((s.getSubSessions().get(j)
						.getRequestedEndTime().getTimeInMillis()))) {
					rtimes.add((s.getSubSessions().get(j).getRequestedEndTime()
							.getTimeInMillis()));
				}

				if (!rtimes.contains((s.getSubSessions().get(j)
						.getRequestedStartTime().getTimeInMillis()))) {
					rtimes.add((s.getSubSessions().get(j)
							.getRequestedStartTime().getTimeInMillis()));
				}
			}

			Collections.sort(rtimes);
			Long minTime = rtimes.get(0);
			Long maxTime = rtimes.get(rtimes.size() - 1);

			List<Long> timesList = populateTimes(sessionsList);

			for (Long t : timesList) {
				if (!rtimes.contains(t) && t > minTime && t < maxTime) {
					rtimes.add(t);
				}
			}

			Collections.sort(rtimes);
		}

		return rtimes;
	}

	public static void main(String[] args) {
		// UndirectedGraph<String, ApnConnection> scenarioOne =
		// updateScenarioGraph(
		// DAOFactory.getDAOFactoryInstance().getResourceDAO()
		// .getResourcesByTypeAndParameterValue("LP_SCENARIO",
		// "LP_SCENARIO_ID", "one").get(0), null, null);
		// System.out.println("#" + scenarioOne.toString());
		//
		// for (ApnConnection e : scenarioOne.edgeSet()) {
		// System.out.println("#" + e);
		// }
		//
		// UndirectedGraph<String, ApnConnection> scenarioTwo =
		// updateScenarioGraph(
		// DAOFactory.getDAOFactoryInstance().getResourceDAO()
		// .getResourcesByTypeAndParameterValue("LP_SCENARIO",
		// "LP_SCENARIO_ID", "two").get(0), null, null);
		// System.out.println("#" + scenarioTwo.toString());
		//
		// for (ApnConnection e : scenarioTwo.edgeSet()) {
		// System.out.println("#" + e);
		// }
		//
		// DijkstraShortestPath<String, ApnConnection> algo = new
		// DijkstraShortestPath<String, ApnConnection>(
		// scenarioOne, "OTWA3OME1", "GLIF-HDXC-SEA01", 2000);
		//
		// System.out.println(algo.getPath());
		// System.out.println(algo.getPathLength());
		//
		// for (ApnConnection e : scenarioOne.edgeSet()) {
		// System.out.println("#" + e);
		// }
		try {

			Session s = DAOFactory.getDAOFactoryInstance().getSessionDAO()
					.getSessionById(28);

			CreateNetworkReservation req = new CreateNetworkReservation();
			ScenariosNRManager manager = new ScenariosNRManager();
			req.setReservation(manager.getReservationObject(s));
			IsNetworkAvailable av = new IsNetworkAvailable(req);
			manager.isAvailable(av);
			// manager.createReservation(req);

		} catch (NoScenarioFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void createScenariosGraphs(List<Session> rSessions,
			List<Long> rTimes, List<Resource> apnScenarios, Session s) {

		// we first create the graph
		for (Resource apnScenario : apnScenarios) {
			UndirectedGraph<String, ApnConnection> g = new ListenableUndirectedGraph<String, ApnConnection>(
					ApnConnection.class);

			List<Resource> connections = DAOFactory.getDAOFactoryInstance()
					.getResourceDAO().getResourcesByTypeAndParameterValue(
							"LP_CONNECTION", "LP_CONNECTION_SC",
							apnScenario.getResourceID() + "");
			//added by YYH for debug log
			for(Resource tc : connections){
			logger.info("The APN connection resource " + tc.getDescription() + "\n Resource ID is:" + tc.getResourceID());
			}
			//end

			for (Resource conn : connections) {
				List<Resource> eps = DAOFactory.getDAOFactoryInstance()
						.getResourceDAO().getResourcesByTypeAndParameterValue(
								"LP_END_POINT", "LP_END_POINT_CONN",
								conn.getResourceID() + "");

				ApnConnection apnC = new ApnConnection();
				apnC.setConnection(conn);
				apnC.setNominalBW(Double.parseDouble(eps.get(0)
						.getParameterValue("BW_PROVIDED")));
				if (!g.containsVertex(eps.get(0).getParameterValue(
						"LP_END_POINT_SWITCH_ID"))){
					g.addVertex(eps.get(0).getParameterValue(
							"LP_END_POINT_SWITCH_ID"));
				//added by YYH for debug log
					logger.info("The APN connection resource  LP_END_POINT_SWITCH_ID" + eps.get(0).getParameterValue(
					"LP_END_POINT_SWITCH_ID"));
				
				//end
				}
				if (!g.containsVertex(eps.get(1).getParameterValue(
						"LP_END_POINT_SWITCH_ID"))){
					g.addVertex(eps.get(1).getParameterValue(
							"LP_END_POINT_SWITCH_ID"));
					//added by YYH for debug log
					logger.info("The APN connection resource  LP_END_POINT_SWITCH_ID" + eps.get(1).getParameterValue(
					"LP_END_POINT_SWITCH_ID"));
				
				    //end
				}
				apnC.setAvailableBW(new TreeMap<Long, Double>());
				for (int t = 0; t < rTimes.size(); t++) {
					apnC.getAvailableBW().put(
							rTimes.get(t),
							Double.parseDouble(eps.get(0).getParameterValue(
									"BW_PROVIDED")));
				}

				g.addEdge(eps.get(0)
						.getParameterValue("LP_END_POINT_SWITCH_ID"), eps
						.get(1).getParameterValue("LP_END_POINT_SWITCH_ID"),
						apnC);

			}
			graphs.put(apnScenario.getResourceID(), g);
		}

		Map<Integer, Integer> sessionReservationMap = DAOFactory
				.getDAOFactoryInstance().getSessionDAO()
				.getNetworkReservationPerSession();

		// then we update the edge bw according to the rSessions
		// every edge will have a map Map<Long, Double> that maps time t to bw
		// between t and t + 1

		for (Session rS : rSessions) {
			if (sessionReservationMap.containsKey(rS.getSessionID())) {
				Resource res = DAOFactory.getDAOFactoryInstance()
						.getResourceDAO().getResourceById(
								sessionReservationMap.get(rS.getSessionID()));
				if (res.getResourceType().getId()
						.equals("SCENARIO_RESERVATION")) {

					Resource resource = (Resource) DAOFactory
							.getDAOFactoryInstance().getCalendarDAO()
							.getAssociatedObject(
									Integer.parseInt(res
											.getParameterValue("ENTRY_ID")));

					for (Session rSub : rS.getSubSessions()) {
						for (Connection rC : rSub.getConnections()) {
							if (rC.isLpNeeded()) {
								String source = rC.getSourceEndPoint()
										.getNetworkEndPoint()
										.getParameterValue(
												"LP_END_POINT_SWITCH_ID");
								String target = rC.getTargetEndPoint()
										.getNetworkEndPoint()
										.getParameterValue(
												"LP_END_POINT_SWITCH_ID");

								DijkstraShortestPath sp = new DijkstraShortestPath(
										graphs.get(resource.getResourceID()),
										source, target);

								for (Object e : sp.getPathEdgeList()) {
									System.out.println(((ApnConnection) e));
									substract_edge_time_bw((ApnConnection) e,
											rC.getMinBwRequirement(), rSub
													.getTimeSlot()
													.getStartTime()
													.getTimeInMillis(), rSub
													.getTimeSlot().getEndTime()
													.getTimeInMillis());
									System.out.println(((ApnConnection) e));
								}
							}
						}
					}
				} else {
					// TODO: shouldn't happen!! raise exception
				}
			}
		}

	}

	public void substract_edge_time_bw(ApnConnection edge, double bw,
			long startTime, long endTime) {

		// System.out.print(Utils.dateToString(startTime) + "%");
		// System.out.println(Utils.dateToString(endTime) + " ");

		if (edge.getAvailableBW() == null) {
			edge.setAvailableBW(new TreeMap<Long, Double>());
			edge.getAvailableBW().put(startTime, edge.getNominalBW() - bw);
			edge.getAvailableBW().put(endTime, edge.getNominalBW());
		} else {
			Set<Long> keySet = edge.getAvailableBW().keySet();
			List<Long> keyList = new ArrayList<Long>(keySet);
			// System.out.print("OldEntry ");
			int loopMax = keyList.size();
			// System.out.print(loopMax + " ");
			for (int i = 0; i < loopMax - 1; i++) {
				// case where the interval contains both start and end times
				// System.out.print("#" + i + " ");
				// System.out.print(Utils.dateToString(keyList.get(i)) + " ");
				// System.out.print(Utils.dateToString(keyList.get(i + 1)) +
				// " ");

				if (keyList.get(i) == startTime)
					edge.getAvailableBW().put(startTime,
							edge.getAvailableBW().get(keyList.get(i)) - bw);
				else if (keyList.get(i) > startTime && keyList.get(i) < endTime)
					edge.getAvailableBW().put(keyList.get(i),
							edge.getAvailableBW().get(keyList.get(i)) - bw);
				else if (keyList.get(i) == startTime)
					continue;
			}
			// System.out.println("done");
		}
	}

	/**
	 * Craete a toy graph based on String objects.
	 * 
	 * @param times
	 * @param sessions
	 * 
	 * @return a graph based on String objects.
	 */
	private static UndirectedGraph<String, ApnConnection> updateScenarioGraph(
			Resource scenario, List<Session> sessions, List<Long> times) {
		UndirectedGraph<String, ApnConnection> g = new ListenableUndirectedGraph<String, ApnConnection>(
				ApnConnection.class);

		List<Resource> connections = DAOFactory.getDAOFactoryInstance()
				.getResourceDAO().getResourcesByTypeAndParameterValue(
						"LP_CONNECTION", "LP_CONNECTION_SC",
						scenario.getResourceID() + "");

		for (Resource conn : connections) {
			List<Resource> eps = DAOFactory.getDAOFactoryInstance()
					.getResourceDAO().getResourcesByTypeAndParameterValue(
							"LP_END_POINT", "LP_END_POINT_CONN",
							conn.getResourceID() + "");

			ApnConnection apnC = new ApnConnection();
			apnC.setConnection(conn);
			apnC.setNominalBW(Double.parseDouble(eps.get(0).getParameterValue(
					"BW_PROVIDED")));
			if (!g.containsVertex(eps.get(0).getParameterValue(
					"LP_END_POINT_SWITCH_ID")))
				g.addVertex(eps.get(0).getParameterValue(
						"LP_END_POINT_SWITCH_ID"));
			if (!g.containsVertex(eps.get(1).getParameterValue(
					"LP_END_POINT_SWITCH_ID")))
				g.addVertex(eps.get(1).getParameterValue(
						"LP_END_POINT_SWITCH_ID"));

			g.addEdge(eps.get(0).getParameterValue("LP_END_POINT_SWITCH_ID"),
					eps.get(1).getParameterValue("LP_END_POINT_SWITCH_ID"),
					apnC);

		}

		return g;
	}

	/**
	 * This method finds an APN scenario that contains all the
	 * requestedConnections and TODO: has enough bandwidth
	 * 
	 * @param endTime
	 * @param startTime
	 * 
	 * @param requestedConnections
	 *            List of Connection
	 * @return Resource that describes the APN, null if no APN is found
	 */
	private List<Resource> findAPNScenario(Calendar startTime,
			Calendar endTime, List<Connection> requestedConnections) {

		List<Resource> apnScenarios = DAOFactory.getDAOFactoryInstance()
				.getResourceDAO().getResourcesByType("LP_SCENARIO");

		for (Resource apnScenario : apnScenarios) {
			List<Session> rSessions = DAOFactory.getDAOFactoryInstance()
					.getSessionDAO().getRelevantSessionsInTimeInterval(
							apnScenario, startTime, endTime);
			// graphs.put(apnScenario.getResourceID(), updateScenarioGraph(
			// apnScenario, rSessions, rTimes));
		}

		List<Resource> foundAPNs = new ArrayList<Resource>();

		if (apnScenarios != null && apnScenarios.size() > 0) {
			for (Resource apnScenario : apnScenarios) {

				boolean allConnections = true;
				boolean lpNeeded = false;

				for (Connection reqConn : requestedConnections) {

					if (reqConn.isLpNeeded()) {

						lpNeeded = true;

						List<Resource> scenarioConnections = DAOFactory
								.getDAOFactoryInstance().getResourceDAO()
								.getResourcesByTypeAndParameterValue(
										"LP_CONNECTION", "LP_CONNECTION_SC",
										"" + apnScenario.getResourceID());
						if (scenarioConnections != null
								&& scenarioConnections.size() > 0) {

							boolean foundMatch = false;

							for (Resource c : scenarioConnections) {
								List<Resource> ept = DAOFactory
										.getDAOFactoryInstance()
										.getResourceDAO()
										.getResourcesByTypeAndParameterValue(
												"LP_END_POINT",
												"LP_END_POINT_CONN",
												"" + c.getResourceID());
								if (ept != null && ept.size() == 2) {

									Resource source = ept.get(0);
									Resource target = ept.get(1);

									if (reqConn
											.getSourceEndPoint()
											.getNetworkEndPoint()
											.getParameterValue("IP_ADDRESS")
											.equals(
													source
															.getParameterValue("IP_ADDRESS"))
											|| reqConn
													.getSourceEndPoint()
													.getNetworkEndPoint()
													.getParameterValue(
															"IP_ADDRESS")
													.equals(
															target
																	.getParameterValue("IP_ADDRESS"))) {

										if (reqConn
												.getTargetEndPoint()
												.getNetworkEndPoint()
												.getParameterValue("IP_ADDRESS")
												.equals(
														source
																.getParameterValue("IP_ADDRESS"))
												|| reqConn
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
				}
				if (allConnections && lpNeeded) {
					foundAPNs.add(apnScenario);
					continue;
					// return apnScenario;
				}
			}
		}
		return foundAPNs;
	}

}

// TODO: define the time offset suggested for the corresponding
// session

/*
 * 1: get the list of free time intervals for every resource: done => calendars
 * map
 */

/*
 * 2: transform to a set list of possible intervals for the time offset
 * according to the services start and end times
 */

/*
 * 3: find the intersection of these intervals: for every interval for resource
 * 1 check for every resource k if there is an overlapping interval
 */
