// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;

import org.apache.log4j.Logger;

import ca.gc.iit.nrc.savoir.domain.Connection;
import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.iit.nrc.savoir.domain.ResourceParameter;
import ca.gc.iit.nrc.savoir.domain.Session;
import ca.gc.iit.nrc.savoir.domain.TimeSlot;
import ca.gc.nrc.iit.savoir.dao.impl.DAOFactory;
import ca.gc.nrc.iit.savoir.scheduler.impl.ChronosNRManager;
import ca.gc.nrc.iit.savoir.scheduler.impl.HarmonyNRManager;
import ca.gc.nrc.iit.savoir.scheduler.impl.ScenariosNRManager;
import ca.gc.nrc.iit.savoir.scheduler.types.NoScenarioFoundException;
import ca.gc.nrc.iit.savoir.scheduler.types.SchedulingConflict;
import ca.gc.nrc.iit.savoir.scheduler.types.SessionReservationType;
import ca.gc.nrc.iit.savoir.scheduler.types.UnscheduledSubSession;
import ca.gc.nrc.iit.savoir.scheduler.types.domain.Reservation;
import ca.gc.nrc.iit.savoir.scheduler.types.domain.ReservationService;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.ActivateNetworkReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.ActivateNetworkReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.ActivateSessionReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.ActivateSessionReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CancelNetworkReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CancelNetworkReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CancelResourceReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CancelResourceReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CancelSessionReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CancelSessionReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CreateNetworkReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CreateNetworkReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CreateResourceReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CreateResourceReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CreateSessionReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CreateSessionReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.DeleteSessionReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.DeleteSessionReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.GetNetworkReservationStatus;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.GetReservationStatus;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.GetReservationStatusResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.IsNetworkAvailable;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.IsNetworkAvailableResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.IsResourceAvailable;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.IsResourceAvailableResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.IsSessionReservationAvailable;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.IsSessionReservationAvailableResponse;

@WebService(serviceName = "schedulerService", endpointInterface = "ca.gc.nrc.iit.savoir.scheduler.SavoirScheduler")
public class SavoirSchedulerImpl implements SavoirScheduler {

	private static final Logger logger = Logger
			.getLogger(SavoirSchedulerImpl.class);

	private IOnlineProblem onlineProblem;

	private IOfflineProblem offlineProblem;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.gc.nrc.iit.savoir.scheduler.SavoirScheduler#requestReservation(ca.
	 * gc.nrc.iit.savoir.scheduler.types.msg.CreateSessionReservation)
	 */
	public CreateSessionReservationResponse requestReservation(
			CreateSessionReservation request) {
		CreateSessionReservationResponse response = new CreateSessionReservationResponse();

		if (request.getReservationType().equals(
				SessionReservationType.advance_online_reservation)
				|| request.getReservationType().equals(
						SessionReservationType.immediate_reservation)) {

			Reservation reservation = null;
			try {
//				reservation = onlineProblem.getNetworkResourcesManager()
//						.getReservationObject(request.getNewSession());
				//changed by YYH for debugging purpose
				Session session = DAOFactory.getDAOFactoryInstance().getSessionDAO().getSessionById(request.getNewSession().getSessionID());
				for (Session sub : session.getSubSessions()){
					for (Connection c : sub.getConnections()) {
						logger.info("Scheduler: The master session ID = " + session.getSessionID());
						logger.info("Scheduler: The sub session ID = " + sub.getSessionID());
						logger.info("Scheduler: The connection ID = " + c.getConnectionID());
						logger.info("Scheduler: The connection source end point ID = " + c.getSourceEndPoint().getEndPointID());
//						logger.info("Scheduler: The connection source network point resource ID = " + c.getSourceEndPoint().getNetworkEndPoint().getResourceID());
//						logger.info("Scheduler: The start vertex = " + c.getSourceEndPoint().getNetworkEndPoint()
//								.getParameterValue("LP_END_POINT_SWITCH_ID"));
						logger.info("Scheduler: The connection target end point ID = " + c.getTargetEndPoint().getEndPointID());
//						logger.info("Scheduler: The connection target network point resource ID = " + c.getTargetEndPoint().getNetworkEndPoint().getResourceID());
//						logger.info("Scheduler: The end vertex = " + c.getTargetEndPoint().getNetworkEndPoint()
//								.getParameterValue("LP_END_POINT_SWITCH_ID"));
					}
				}
				//end debug information
				reservation = onlineProblem.getNetworkManagers().get(request.getResType())
				.getReservationObject(session);
				//reservation = onlineProblem.getNetworkManagers().get(request.getResType())
				//.getReservationObject(request.getNewSession());
				//end changed
			} catch (SchedulingConflict e) {
				if (e instanceof NoScenarioFoundException) {
					logger.info("reservation impossible");
					response.setSuccessful(false);
					SchedulingConflict[] sc = { e };
					response.setConflicts(sc);
					return response;
				}
			}

			// nothing PENDING for scheduling
			if (reservation == null) {
				response.setSuccessful(true);
				response.setSuggestedTimeOffset(0);
				return response;
			}

			CreateResourceReservation resReq = new CreateResourceReservation();
			resReq.setReservation(reservation);
			resReq.setAutomaticActivation(request.isAutomaticActivation());

			CreateNetworkReservation netReq = new CreateNetworkReservation();
			netReq.setReservation(reservation);
			netReq.setAutomaticActivation(request.isAutomaticActivation());

			// check if edge devices are available
			IsResourceAvailable isResAvReq = new IsResourceAvailable();
			isResAvReq.setSession(request.getNewSession());
			IsResourceAvailableResponse isResAvResp = onlineProblem
					.getEdgeDevicesManager().isAvailable(isResAvReq);

			// check if network is available
			IsNetworkAvailable isNetAvReq = new IsNetworkAvailable(netReq);
//			IsNetworkAvailableResponse isNetAvResp = onlineProblem
//					.getNetworkResourcesManager().isAvailable(isNetAvReq);
			IsNetworkAvailableResponse isNetAvResp = onlineProblem.getNetworkManagers().get(request.getResType()).isAvailable(isNetAvReq);

			logger.info("all the subsessions of session "
					+ request.getNewSession().getSessionID()
					+ " not scheduled yet");
 
			if (isResAvResp.isSuccessful() && isNetAvResp.isSuccessful()) {
				logger.info("resources available. requesting reservation.");
				// reserve edge devices
				CreateResourceReservationResponse resRes = onlineProblem
						.getEdgeDevicesManager().createReservation(resReq);
				if (resRes.isSuccessful()) {

					// edge devices reserved => reserve network
					//changed by yyh 12-05-10
					//if (onlineProblem.getNetworkResourcesManager() instanceof ScenariosNRManager) {
					if ( request.getResType().equalsIgnoreCase("SCENARIO_RESERVATION")) {
						logger.info("ScenarioNRManager used");

						CreateNetworkReservation netR = new CreateNetworkReservation();
						if (reservation != null) {
							netR.setReservation(reservation);
							netR.setAutomaticActivation(resReq
									.isAutomaticActivation());

							// ...then try to reserve the network
							// resources...
//							CreateNetworkReservationResponse netRes = onlineProblem
//									.getNetworkResourcesManager()
//									.createReservation(netR);
							CreateNetworkReservationResponse netRes = onlineProblem
							.getNetworkManagers().get("SCENARIO_RESERVATION")
							.createReservation(netR);

							if (netRes.isSuccessful()) {

								for (ReservationService s : reservation
										.getServicesList()) {
									Session sess = DAOFactory
											.getDAOFactoryInstance()
											.getSessionDAO().getSessionById(
													s.getSessionID());
									DAOFactory.getDAOFactoryInstance()
											.getCalendarDAO().addEntry(
													Session.class,
													sess.getSessionID(),
													new TimeSlot(s
															.getStartTime(), s
															.getEndTime()));
									sess.setStatus(Session.SCHEDULED);
									DAOFactory.getDAOFactoryInstance()
											.getSessionDAO()
											.updateSession(sess);

								}

								response.setSuccessful(true);
								response.setSuggestedTimeOffset(0);
								return response;
							}
						}
					} else if (request.getResType().equalsIgnoreCase("CHRONOS_RESERVATION")
							||request.getResType().equalsIgnoreCase("HARMONY_RESERVATION")) {
						logger
								.info("ChronosNRManager or HarmonyNRManager used");
						String reservationType = "";
//						if (onlineProblem.getNetworkResourcesManager() instanceof ChronosNRManager)
//							reservationType = "CHRONOS_RESERVATION";
//						else
//							reservationType = "HARMONY_RESERVATION";
						reservationType = request.getResType();
												

						// there is currently a bug in Harmony: when you create
						// a pre-reservation in a new job by setting the job ID
						// to 0, no new job is created

						Long jobID = null;
						boolean allSuccessful = true;
						List<Session> sessions = new ArrayList<Session>();
						List<Integer> failures = new ArrayList<Integer>();
						// for every service (sub-session)...
						for (ReservationService resSS : reservation
								.getServicesList()) {

							// ...get the session object...
							Session ss = DAOFactory.getDAOFactoryInstance()
									.getSessionDAO().getSessionById(
											resSS.getSessionID());

							// ...and create the corresponding reservation
							// request...
							Reservation res = null;
							try {
//								res = onlineProblem
//										.getNetworkResourcesManager()
//										.getReservationObject(ss);
								res = onlineProblem
								.getNetworkManagers().get(request.getResType())
								.getReservationObject(ss);
							} catch (SchedulingConflict e) {
								e.printStackTrace();
							}
							CreateNetworkReservation netR = new CreateNetworkReservation();
							if (res != null) {
								netR.setReservation(res);
								netR.setAutomaticActivation(resReq
										.isAutomaticActivation());
								netR.setJobID(jobID);

								// ...then try to reserve the network
								// resources...
//								CreateNetworkReservationResponse netRes = onlineProblem
//										.getNetworkResourcesManager()
//										.createReservation(netR);
								CreateNetworkReservationResponse netRes = onlineProblem
								.getNetworkManagers().get(request.getResType())
								.createReservation(netR);

								// if everything ok...
								if (netRes.isSuccessful()) {
									// ...create a new reservation resource...
									Resource reservationResource = createNewReservationResource(
											netRes, reservationType);

									// ...and assign it to every connection in
									// the subsession.
									for (Connection c : ss.getConnections()) {
										c
												.setNetworkResource(reservationResource);
									}
									ss.setStatus(Session.SCHEDULED);
									sessions.add(ss);
									jobID = netRes.getJobID();
								} else {
									allSuccessful = false;
									failures.add(ss.getSessionID());
								}
							}

						}
						if (allSuccessful) {
							response.setSuccessful(true);
							for (Session ss : sessions)
								DAOFactory.getDAOFactoryInstance()
										.getSessionDAO().updateSession(ss);
							logger.info("subsessions scheduled.");
							response.setSuggestedTimeOffset(0);
							return response;
						} else {
							logger.info("some sessions were not scheduled");
							List<SchedulingConflict> scs = new ArrayList<SchedulingConflict>();
							for (Integer i : failures) {
								scs.add(new UnscheduledSubSession(i));
							}
							SchedulingConflict[] sc = scs
									.toArray(new SchedulingConflict[0]);
							response.setConflicts(sc);
							response.setSuccessful(false);
							return response;
						}
					}
				} else {
					logger.info("resources reservation failed");
					response.setSuccessful(false);
					response.setSuggestedTimeOffset(resRes.getTimeOffset());
					response.setConflicts(resRes.getSchedulingConflicts());
					return response;
				}

			} else {
				// resources/network not available
				List<SchedulingConflict> list = new ArrayList<SchedulingConflict>();

				long resOffset = 0;
				long netOffset = 0;

				// find when resources will be available
				if (!isResAvResp.isSuccessful()) {
					if (isResAvResp.getSchedulingConflicts() != null) {
						for (SchedulingConflict sc : isResAvResp
								.getSchedulingConflicts()) {
							list.add(sc);
						}
					}
					resOffset = isResAvResp.getTimeOffset();
					// System.out.println(resOffset);
				}

				// find when network will be available
				if (!isNetAvResp.isSuccessful()) {
					if (isNetAvResp.getSchedulingConflicts() != null) {
						for (SchedulingConflict sc : isNetAvResp
								.getSchedulingConflicts()) {
							list.add(sc);
						}
					}
					netOffset = isNetAvResp.getTimeOffset();
					// System.out.println(netOffset);
				}
				response.setConflicts(list.toArray(new SchedulingConflict[0]));
				response.setSuccessful(false);
				// find when both network and resources will be available
				long timeOffset = findTimeOffsetToSuggest(request
						.getNewSession(), request.getResType(), reservation, Math.max(resOffset,
						netOffset));
				response.setSuggestedTimeOffset(timeOffset);
				logger.info("suggested time offset " + timeOffset);
			}

		} else if (request.getReservationType().equals(
				SessionReservationType.advance_offline_reservation)) {
			// TODO: add reservation to the queue of reservations
		}
		
		return response;
	}

	private Resource createNewReservationResource(
			CreateNetworkReservationResponse netRes, String resourceType) {
		Resource reservationResource = new Resource();
		reservationResource.setDescription("network reservation");
		reservationResource.setResourceType(DAOFactory.getDAOFactoryInstance()
				.getTypesDAO().getResourceTypeById(resourceType));

		List<ResourceParameter> params = new ArrayList<ResourceParameter>();
		ResourceParameter reservationID = new ResourceParameter();
		reservationID.setParameter(DAOFactory.getDAOFactoryInstance()
				.getTypesDAO().getParameterTypeById(resourceType + "_ID"));
		reservationID.setValue(netRes.getReservationID());
		params.add(reservationID);
		reservationResource.setParameters(params);
		reservationResource.setResourceID(DAOFactory.getDAOFactoryInstance()
				.getResourceDAO().addResource(reservationResource));
		return reservationResource;
	}

	/**
	 * @param reservation
	 * @param offset
	 * @return
	 */
	private long findTimeOffsetToSuggest(Session session, String resType,
			Reservation reservation, long offset) {

		CreateResourceReservation resReq = new CreateResourceReservation();
		CreateNetworkReservation netReq = new CreateNetworkReservation();

		IsResourceAvailable isResAvReq;
		IsResourceAvailableResponse isResAvResp;
		IsNetworkAvailable isNetAvReq;
		IsNetworkAvailableResponse isNetAvResp;

		long timeOffset = offset;

		//add an exit condition after a certain time to avoid a dead loop.
		
		do {
			long resOffset = 0;
			long netOffset = 0;

			reservation.applySlidingVector(offset);
			resReq.setReservation(reservation);
			netReq.setReservation(reservation);

			isResAvReq = new IsResourceAvailable();
			isResAvReq.setSession(session);
			isResAvReq.setOffset(timeOffset);
			isNetAvReq = new IsNetworkAvailable(netReq);
			isResAvResp = onlineProblem.getEdgeDevicesManager().isAvailable(
					isResAvReq);
//			isNetAvResp = onlineProblem.getNetworkResourcesManager()
//					.isAvailable(isNetAvReq);
			isNetAvResp = onlineProblem.getNetworkManagers().get(resType)
			.isAvailable(isNetAvReq);

			if (!isResAvResp.isSuccessful() && isResAvResp.getTimeOffset() > 0) {
				resOffset = isResAvResp.getTimeOffset();
			}
			if (!isNetAvResp.isSuccessful() && isResAvResp.getTimeOffset() > 0) {
				netOffset = isResAvResp.getTimeOffset();
			}

			offset = Math.max(resOffset, netOffset);

			timeOffset += offset;

			logger.info("TimeOffset: " + timeOffset);

		} while ((isResAvResp.isSuccessful() == false
				|| isNetAvResp.isSuccessful() == false) && offset != 0);

		return timeOffset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.gc.nrc.iit.savoir.scheduler.SavoirScheduler#cancelReservation(ca.gc
	 * .nrc.iit.savoir.scheduler.types.msg.CancelSessionReservation)
	 */
	@Override
	public CancelSessionReservationResponse cancelReservation(
			CancelSessionReservation request) {
		CancelSessionReservationResponse response = new CancelSessionReservationResponse();

		CancelResourceReservation crReq = new CancelResourceReservation();
		CancelResourceReservationResponse crResp = onlineProblem
				.getEdgeDevicesManager().cancelReservation(crReq);

		if (crResp.isSuccessful()) {

			CancelNetworkReservation cnReq = new CancelNetworkReservation();
			CancelNetworkReservationResponse cnResp = new CancelNetworkReservationResponse();

			List<Integer> processed = new ArrayList<Integer>();

			// either it's a session with a set of subsessions
			//changed by YYH 10-06-10
			Session session = DAOFactory.getDAOFactoryInstance().getSessionDAO().getSessionById(request.getSession().getSessionID());
//			if (request.getSession().getSubSessions() != null
//					&& request.getSession().getSubSessions().size() > 0) {
			if (session.getSubSessions() != null
					&& session.getSubSessions().size() > 0) {
				// for every subsession
				for (Session ss : session.getSubSessions()) {
					if (ss.getConnections() != null
							&& ss.getConnections().size() > 0) {
						// for every connection
						for (Connection c : ss.getConnections()) {
							// if it has a network resource assigned to it that
							// it has not been already cancelled
							if (c.getNetworkResource() != null
									&& !processed.contains(c
											.getNetworkResource()
											.getResourceID())) {

								// if we have a network scheduler that knows how
								// to handle this resource
								if (onlineProblem.getNetworkManagers()
										.containsKey(
												c.getNetworkResource()
														.getResourceType()
														.getId())) {

									// then call the appropriate network
									// scheduler to cancel the reservation
									cnReq.setResource(c.getNetworkResource());
									cnResp = onlineProblem.getNetworkManagers()
											.get(
													c.getNetworkResource()
															.getResourceType()
															.getId())
											.cancelReservation(cnReq);

									processed.add(c.getNetworkResource()
											.getResourceID());

									if (!cnResp.isSuccessful()) {
										response.getFailingSessions().add(
												ss.getSessionID());
										response.setSuccessful(false);
									}
								} else {
									// throw exception unknown network resource,
									// no handler available
								}
							}
						}
						if (ss.getTimeSlot() != null) {
							logger.info("The Canceled session Entry ID in Calendar table is " + ss.getTimeSlot()
													.getId());
							DAOFactory.getDAOFactoryInstance().getCalendarDAO()
									.removeEntry(
											ss.getTimeSlot()
													.getId());
						}
					}
				}
				// or it's a subsession of a session
			} else {
				if (session.getConnections() != null
						&& session.getConnections().size() > 0) {
					for (Connection c : session.getConnections()) {
						// if it has a network resource assigned to it that it
						// has not been already cancelled
						if (c.getNetworkResource() != null
								&& !processed.contains(c.getNetworkResource()
										.getResourceID())) {

							// if we have a network scheduler that knows how to
							// handle this resource
							if (onlineProblem.getNetworkManagers().containsKey(
									c.getNetworkResource().getResourceType()
											.getId())) {

								// then call the appropriate network scheduler
								// to cancel the reservation
								cnReq.setResource(c.getNetworkResource());
								cnResp = onlineProblem.getNetworkManagers()
										.get(
												c.getNetworkResource()
														.getResourceType()
														.getId())
										.cancelReservation(cnReq);

								processed.add(c.getNetworkResource()
										.getResourceID());

								if (!cnResp.isSuccessful()) {
									response.getFailingSessions()
											.add(
													request.getSession()
															.getSessionID());
									response.setSuccessful(false);
								}
							} else {
								// throw exception unknown network resource,
								// no handler available
							}
						}
					}
				}
			}
		}
		response.setSuccessful(true);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.gc.nrc.iit.savoir.scheduler.SavoirScheduler#getReservationStatus(ca
	 * .gc.nrc.iit.savoir.scheduler.types.msg.DeleteSessionReservation)
	 */
	//changed by yyh 12-05-10
	//public String getReservationStatus(DeleteSessionReservation request) {
	public String getReservationStatus(GetReservationStatus request) {
		GetReservationStatusResponse response = new GetReservationStatusResponse();
		GetNetworkReservationStatus gsReq = new GetNetworkReservationStatus();
//modified at 09-23-09
//		if (onlineProblem.getNetworkResourcesManager() instanceof ChronosNRManager
//				|| onlineProblem.getNetworkResourcesManager() instanceof HarmonyNRManager) {
//			String reservationID = request.getSession().getConnections().get(0)
//					.getNetworkResource().getParameterValue(
//							"CHRONOS_RESERVATION_ID");
		String reservationID;
		//changed again by yyh 12-05-10 
		
//		if (onlineProblem.getNetworkResourcesManager() instanceof ChronosNRManager) {
//			reservationID = request.getSession().getConnections().get(0)
//					.getNetworkResource().getParameterValue(
//							"CHRONOS_RESERVATION_ID");
//			gsReq.setReservationID(reservationID);
//			response.setStatus(onlineProblem.getNetworkResourcesManager()
//					.getNetworkStatus(gsReq).getStatus());
//			
//		} else if (onlineProblem.getNetworkResourcesManager() instanceof HarmonyNRManager){
//			reservationID = request.getSession().getConnections().get(0)
//			.getNetworkResource().getParameterValue(
//					"HARMONY_RESERVATION_ID");
//			gsReq.setReservationID(reservationID);
//			response.setStatus(onlineProblem.getNetworkResourcesManager()
//					.getNetworkStatus(gsReq).getStatus());
//		}else{
//			response.setStatus("Unknown");
//		}
		//new
		String resType = request.getSession().getConnections().get(0)
				.getNetworkResource().getResourceType().getId();
		if (resType.equalsIgnoreCase("CHRONOS_RESERVATION")) {
			reservationID = request.getSession().getConnections().get(0)
					.getNetworkResource().getParameterValue(
							"CHRONOS_RESERVATION_ID");
			gsReq.setReservationID(reservationID);
			response.setStatus(onlineProblem.getNetworkManagers().get("CHRONOS_RESERVATION")
					.getNetworkStatus(gsReq).getStatus());
		} else if (resType.equalsIgnoreCase("HARMONY_RESERVATION")) {
			reservationID = request.getSession().getConnections().get(0)
					.getNetworkResource().getParameterValue(
							"HARMONY_RESERVATION_ID");
			gsReq.setReservationID(reservationID);
			response.setStatus(onlineProblem.getNetworkManagers().get("HARMONY_RESERVATION")
					.getNetworkStatus(gsReq).getStatus());
		} else {
			response.setStatus("Unknown");
		}
		//end change 12-05-10
		
		return response.getStatus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.gc.nrc.iit.savoir.scheduler.SavoirScheduler#isReservationAvailable
	 * (ca.gc.nrc.iit.savoir.scheduler.types.msg.CreateSessionReservation)
	 */
	// changed by yyh 13-05-10   
	@Override
	public IsSessionReservationAvailableResponse isReservationAvailable(
			IsSessionReservationAvailable request) {
		IsSessionReservationAvailableResponse response = new IsSessionReservationAvailableResponse();

		if (request.getReservationType().equals(
				SessionReservationType.advance_online_reservation)
				|| request.getReservationType().equals(
						SessionReservationType.immediate_reservation)) {

			Reservation reservation = null;
			try {
//				reservation = onlineProblem.getNetworkResourcesManager()
//						.getReservationObject(request.getNewSession());
				reservation = onlineProblem.getNetworkManagers().get(request.getResType())
				.getReservationObject(request.getNewSession());
			} catch (SchedulingConflict e) {
				if (e instanceof NoScenarioFoundException) {
					logger.info("reservation impossible");
					response.setSuccessful(false);
					SchedulingConflict[] sc = { e };
					response.setConflicts(sc);
					return response;
				}
			}

			CreateResourceReservation resReq = new CreateResourceReservation();
			resReq.setReservation(reservation);
			resReq.setAutomaticActivation(request.isAutomaticActivation());

			CreateNetworkReservation netReq = new CreateNetworkReservation();
			netReq.setReservation(reservation);
			netReq.setAutomaticActivation(request.isAutomaticActivation());

			IsResourceAvailable isResAvReq = new IsResourceAvailable();
			isResAvReq.setSession(request.getNewSession());
			IsResourceAvailableResponse isResAvResp = onlineProblem
					.getEdgeDevicesManager().isAvailable(isResAvReq);

			IsNetworkAvailable isNetAvReq = new IsNetworkAvailable(netReq);
//			IsNetworkAvailableResponse isNetAvResp = onlineProblem
//					.getNetworkResourcesManager().isAvailable(isNetAvReq);
			IsNetworkAvailableResponse isNetAvResp = onlineProblem
			.getNetworkManagers().get(request.getResType()).isAvailable(isNetAvReq);

			// would be good to have a rollback for the resources in case an
			// exception is raised...
			if (isResAvResp.isSuccessful() && isNetAvResp.isSuccessful()) {
				response.setSuccessful(true);
				response.setSuggestedTimeOffset(0);

			} else {
				List<SchedulingConflict> list = new ArrayList<SchedulingConflict>();

				long resOffset = 0;
				long netOffset = 0;

				if (!isResAvResp.isSuccessful()) {
					if (isResAvResp.getSchedulingConflicts() != null) {
						for (SchedulingConflict sc : isResAvResp
								.getSchedulingConflicts()) {
							list.add(sc);
						}
					}
					resOffset = isResAvResp.getTimeOffset();
					// System.out.println(resOffset);
				}
				if (!isNetAvResp.isSuccessful()) {
					if (isNetAvResp.getSchedulingConflicts() != null) {
						for (SchedulingConflict sc : isNetAvResp
								.getSchedulingConflicts()) {
							list.add(sc);
						}
					}
					netOffset = isNetAvResp.getTimeOffset();
					// System.out.println(netOffset);
				}

				response.setConflicts(list.toArray(new SchedulingConflict[0]));
				response.setSuccessful(false);
				// System.out.println(Math.max(resOffset, netOffset));
				response.setSuggestedTimeOffset(findTimeOffsetToSuggest(request
						.getNewSession(), request.getResType(), reservation, Math.max(resOffset,
						netOffset)));
			}

		} else if (request.getReservationType().equals(
				SessionReservationType.advance_offline_reservation)) {
			// TODO: add reservation to the queue of reservations
		}

		return response;
	}

	@Override
	public ActivateSessionReservationResponse activateSessionReservation(
			ActivateSessionReservation request) {

		ActivateSessionReservationResponse response = new ActivateSessionReservationResponse();
		response.setSuccessful(true);
		
		//change the list type to integer to just save resource ID
		//List<Resource> activated = new ArrayList<Resource>();
		List<Integer> activated = new ArrayList<Integer>();
		//changed by YYH
		Session session = DAOFactory.getDAOFactoryInstance().getSessionDAO().getSessionById(request.getSession().getSessionID());
		request.setSession(session);
		//end change
		if (request.getSession().getSubSessions() != null
				&& request.getSession().getSubSessions().size() > 0) {
			for (Session ss : request.getSession().getSubSessions()) {
				boolean failingSession = false;
				if (ss.getStatus().equals(Session.SCHEDULED)) {
					if (ss.getConnections() != null
							&& ss.getConnections().size() > 0) {
						for (Connection c : ss.getConnections()) {
							if (c.isLpNeeded()
									&& c.getNetworkResource() != null
									&& !activated.contains(c
											.getNetworkResource().getResourceID())) {
								ActivateNetworkReservation rq = new ActivateNetworkReservation();
								rq.setNetworkResource(c.getNetworkResource());
								//added debugging information by yyh
								logger.info("Schduler: The activated network resource ID = " + c.getNetworkResource().getResourceID());
								logger.info("Schduler: The activated Entry_ID = " + rq.getNetworkResource()
										.getParameterValue("ENTRY_ID"));
								//end add
								ActivateNetworkReservationResponse r = onlineProblem
										.getNetworkManagers().get(
												c.getNetworkResource()
														.getResourceType()
														.getId()).activate(rq);
								if (r.isSuccessful())
									activated.add(c.getNetworkResource().getResourceID());
								else {
									response.getFailingSessions().add(
											ss.getSessionID());
									failingSession = true;
									break;
								}
							}
						}
						if (failingSession) {
							response.setSuccessful(false);
							break;
						} else {
							ss.setStatus(Session.LOADING);
							DAOFactory.getDAOFactoryInstance().getSessionDAO()
									.updateSession(ss);
						}
					}
				}
			}
		} else if (request.getSession().getConnections() != null
				&& request.getSession().getConnections().size() > 0) {
			boolean failingSession = false;
			if (request.getSession().getStatus().equals(Session.SCHEDULED)) {
				if (request.getSession().getConnections() != null
						&& request.getSession().getConnections().size() > 0) {
					for (Connection c : request.getSession().getConnections()) {
						if (c.isLpNeeded() && c.getNetworkResource() != null
								&& !activated.contains(c.getNetworkResource().getResourceID())) {
							ActivateNetworkReservation rq = new ActivateNetworkReservation();
							rq.setNetworkResource(c.getNetworkResource());
							ActivateNetworkReservationResponse r = onlineProblem
									.getNetworkManagers().get(
											c.getNetworkResource()
													.getResourceType().getId())
									.activate(rq);
							if (r.isSuccessful())
								activated.add(c.getNetworkResource().getResourceID());
							else {
								response.getFailingSessions().add(
										request.getSession().getSessionID());
								failingSession = true;
								break;
							}
						}
					}
					if (failingSession) {
						response.setSuccessful(false);
					} else {
						request.getSession().setStatus(Session.LOADING);
						DAOFactory.getDAOFactoryInstance().getSessionDAO()
								.updateSession(request.getSession());
					}
				}
			}
		}
		
		return response;
	}

	@Override
	public DeleteSessionReservationResponse deleteReservation(
			DeleteSessionReservation request) {
		DeleteSessionReservationResponse response = new DeleteSessionReservationResponse();
		response.setSuccessful(true);
		return response;
	}

	public IOnlineProblem getOnlineProblem() {
		return onlineProblem;
	}

	public void setOnlineProblem(IOnlineProblem onlineProblem) {
		this.onlineProblem = onlineProblem;
	}

	public IOfflineProblem getOfflineProblem() {
		return offlineProblem;
	}

	public void setOfflineProblem(IOfflineProblem offlineProblem) {
		this.offlineProblem = offlineProblem;
	}

}
