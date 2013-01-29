// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.impl;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.log4j.Logger;
import org.mortbay.log.Log;
import org.xbill.DNS.TextParseException;

import ca.gc.iit.nrc.savoir.domain.Connection;
import ca.gc.iit.nrc.savoir.domain.Session;
import ca.gc.nrc.iit.savoir.scheduler.INRManager;
import ca.gc.nrc.iit.savoir.scheduler.types.ExceptionCaught;
import ca.gc.nrc.iit.savoir.scheduler.types.NoScenarioFoundException;
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
import eu.ist_phosphorus.harmony.client.HarmonyServletWrapper;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.Activate;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.ActivateResponseType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.ActivateType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.CancelReservationResponseType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.CancelReservationType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.ConnectionConstraintType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.CreateReservationResponseType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.CreateReservationType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.EndpointType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.FixedReservationConstraintType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.GetStatusResponseType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.GetStatusType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.IsAvailableResponseType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.IsAvailableType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.ReservationType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.ServiceConstraintType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.StatusType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.GetStatusResponseType.ServiceStatus;
import eu.ist_phosphorus.harmony.common.utils.TNAHelper;

public class HarmonyNRManager implements INRManager {

	private static final Logger logger = Logger
			.getLogger(HarmonyNRManager.class);

	@Override
	public CancelNetworkReservationResponse cancelReservation(
			CancelNetworkReservation req) {
		CancelNetworkReservationResponse response = new CancelNetworkReservationResponse();
		CancelReservationType crReqType = new CancelReservationType();
		CancelReservationResponseType crReqResp = new CancelReservationResponseType();

		crReqType.setReservationID(req.getResource().getParameterValue(
				"HARMONY_RESERVATION_ID"));
		try {
			crReqResp = HarmonyServletWrapper.cancelReservation(crReqType);

			if (crReqResp == null) {
				response.setSuccessful(false);
				return response;
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setSuccessful(false);
			return response;
		}
		response.setSuccessful(crReqResp.isSuccess());

		return response;
	}
	
	@Override
	public DeleteNetworkReservationResponse deleteReservation(
			DeleteNetworkReservation req) {
		DeleteNetworkReservationResponse response = new DeleteNetworkReservationResponse();

		CancelReservationType crReqType = new CancelReservationType();
		CancelReservationResponseType crReqResp = new CancelReservationResponseType();

		crReqType.setReservationID(req.getResource().getParameterValue(
				"HARMONY_RESERVATION_ID"));

		try {
			crReqResp = HarmonyServletWrapper.deleteReservation(crReqType);

			if (crReqResp == null) {
				response.setSuccessful(false);
				return response;
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setSuccessful(false);
			return response;
		}
		response.setSuccessful(crReqResp.isSuccess());

		return response;
	}

	@Override
	public CreateNetworkReservationResponse createReservation(
			CreateNetworkReservation req) {

		IsNetworkAvailable isAv = new IsNetworkAvailable(req);
		IsNetworkAvailableResponse isAvResp = isAvailable(isAv);

		CreateNetworkReservationResponse response = new CreateNetworkReservationResponse();

		if (isAvResp.isSuccessful()) {
			CreateReservationResponseType crResp = null;
			try {
				CreateReservationType crReq = getReservationObject(req);

				logger.info("about to create reservation");

				crResp = HarmonyServletWrapper.createReservation(crReq);

				if (crResp == null) {
					throw new Exception("Network manager error");
				}

				logger.info("reservation created");

				String rid = crResp.getReservationID();
				logger
						.info("     -> Reservation created successfully! Identifier: "
								+ rid);
				response.setSuccessful(true);
				response.setReservationID(rid);
				return response;
			} catch (Exception e) {

				e.printStackTrace();

				logger.info("     -> Reservation NOT possible.");

				SchedulingConflict[] sc = new SchedulingConflict[1];

				ExceptionCaught ec = new ExceptionCaught();
				ec.setExceptionMessage(e.getMessage());
				ec.setMessage("Reservation not possible.");
				sc[0] = ec;

				response.setSchedulingConflicts(sc);
				response.setSuccessful(false);

				return response;
			}
		} else {
			response.setSuccessful(false);
			response.setSchedulingConflicts(isAvResp.getSchedulingConflicts());
			response.setTimeOffset(isAvResp.getTimeOffset());
		}
		return response;
	}

	@Override
	public GetNetworkReservationsResponse getReservations(
			GetNetworkReservations req) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IsNetworkAvailableResponse isAvailable(IsNetworkAvailable req) {

		logger.info("Checking network availability");

		IsNetworkAvailableResponse response = new IsNetworkAvailableResponse();
		response.setSuccessful(true);

		IsAvailableType isAvailable = new IsAvailableType();

		for (ServiceConstraintType sc : getServiceConstraintTypeArray(req
				.getReservation(), req.isAutomaticActivation())) {
			isAvailable.getService().add(sc);
		}
		isAvailable.setJobID(null);

		IsAvailableResponseType isAvResp = null;

		// logger.info("Prepare to call harmony web service");
		//
		// try {
		// isAvResp = HarmonyServletWrapper.isAvailable(isAvailable);
		// logger.info(isAvResp);
		// } catch (SoapFault e) {
		// e.printStackTrace();
		// response.setSuccessful(false);
		// return response;
		// } catch (IOException e) {
		// e.printStackTrace();
		// response.setSuccessful(false);
		// return response;
		// } catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// response.setSuccessful(false);
		// return response;
		// }
		//
		// if (isAvResp.isSetAlternativeStartTimeOffset()
		// && isAvResp.getAlternativeStartTimeOffset() != 0) {
		//
		// logger.info("network unavailable. Time offset suggested: "
		// + isAvResp.getAlternativeStartTimeOffset());
		// response.setSuccessful(false);
		//
		// if (isAvResp.isSetDetailedResult()) {
		// int maxI = isAvResp.getDetailedResult().size();
		// SchedulingConflict[] conflicts = new SchedulingConflict[maxI];
		// for (int i = 0; i < maxI; i++) {
		// ConnectionAvailabilityType caT = isAvResp
		// .getDetailedResult().get(i);
		//
		// String message = "";
		// StringTokenizer st = new StringTokenizer(caT
		// .getAvailability().value().replaceAll("_", " "),
		// " ");
		// while (st.hasMoreTokens()) {
		// String s = st.nextToken();
		// message += s.substring(0, 1).toUpperCase() + " "
		// + s.substring(1).toLowerCase();
		// }
		//
		// NetworkConnectionConflict ncConf = new NetworkConnectionConflict(
		// caT.getServiceID(), caT.getConnectionID(), message,
		// caT.getMaxBW() != null ? caT.getMaxBW() : 0);
		// conflicts[i] = ncConf;
		// }
		// response.setSchedulingConflicts(conflicts);
		// }
		// } else {
		// logger.info("network available");
		// response.setSuccessful(true);
		// }

		return response;

	}

	private ServiceConstraintType[] getServiceConstraintTypeArray(
			Reservation s, boolean autoActiv) {
		int maxJ = s.getServicesList().size();
		ServiceConstraintType[] vs = new ServiceConstraintType[maxJ];
        //added for test
		logger.info("The default time zone is " + TimeZone.getDefault().getID() + "and maxj = "  + maxJ);
		//end add
		// for each subsession(service)
		for (int j = 0; j < maxJ; j++) {

			// define a service constraint: start and end times
			ServiceConstraintType serviceConstraint = new ServiceConstraintType();

			serviceConstraint.setServiceID(j + 1);
			serviceConstraint.setTypeOfReservation(ReservationType.FIXED);

			FixedReservationConstraintType fixedResv = new FixedReservationConstraintType();

			GregorianCalendar cST = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			//added for test 09-22-09
			logger.info("The time zone is " + cST.getTimeZone().getID() + "America/Halifax is " + TimeZone.getTimeZone("America/Halifax").getID());
			//end test			
			cST.setTime(s.getServicesList().get(j).getStartTime().getTime());
            //added for test 09-22-09
			logger.info("The start time is" + s.getServicesList().get(j).getStartTime().getTime().toString());
			//end
			GregorianCalendar cET = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			cET.setTime(s.getServicesList().get(j).getEndTime().getTime());

			try {
				fixedResv.setStartTime(DatatypeFactory.newInstance()
						.newXMLGregorianCalendar(cST));
			} catch (DatatypeConfigurationException e) {
				e.printStackTrace();
				throw new RuntimeException(
						"A serious configuration error was detected ...", e);
			}
			fixedResv.setDuration((int) ((cET.getTimeInMillis() - cST
					.getTimeInMillis()) / 1000));
			serviceConstraint.setFixedReservationConstraints(fixedResv);

			// automaticActivation is true if LP is activated automatically when
			// the connection time comes
			serviceConstraint.setAutomaticActivation(autoActiv);

			int maxK = s.getServicesList().get(j).getResourcesList().size();

			// for each connection
			for (int k = 0; k < maxK; k++) {

				// define connection constraints: sourceIP, destIP, bandwidth
				// requirement and delay
				ConnectionConstraintType connectionContraint = new ConnectionConstraintType();
				connectionContraint.setConnectionID(k + 1);
				connectionContraint.setDirectionality(0);
				EndpointType source = new EndpointType();
				EndpointType target = new EndpointType();
				try {
					source.setEndpointId(TNAHelper.resolve(s.getServicesList()
							.get(j).getResourcesList().get(k).getSourceIP()));

					connectionContraint.setSource(source);

					target.setEndpointId(TNAHelper.resolve(s.getServicesList()
							.get(j).getResourcesList().get(k)
							.getDestinationIP()));
					connectionContraint.getTarget().add(target);
				} catch (TextParseException e) {
					logger.error("Wrong endpoint ID format");
					e.printStackTrace();
				} catch (UnknownHostException e) {
					logger.error("Host unknown");
					e.printStackTrace();
				}

				logger.info("connection requested between "
						+ source.getEndpointId() + " and "
						+ target.getEndpointId());

				// TODO delay need to be changed
				connectionContraint.setMaxDelay(100);
				connectionContraint.setMaxBW(s.getServicesList().get(j)
						.getResourcesList().get(k).getBandwidth());
				connectionContraint.setMinBW(s.getServicesList().get(j)
						.getResourcesList().get(k).getBandwidth());
				serviceConstraint.getConnections().add(connectionContraint);
			}

			vs[j] = serviceConstraint;
		}
		return vs;
	}

	private CreateReservationType getReservationObject(
			CreateNetworkReservation r) {
		CreateReservationType crReq = new CreateReservationType();
		crReq.setJobID(r.getJobID());
		for (ServiceConstraintType sc : getServiceConstraintTypeArray(r
				.getReservation(), r.isAutomaticActivation())) {
			crReq.getService().add(sc);
		}
		return crReq;
	}

	@Override
	public GetNetworkReservationStatusResponse getNetworkStatus(
			GetNetworkReservationStatus req) {
		GetNetworkReservationStatusResponse response = new GetNetworkReservationStatusResponse();

		GetStatusType gsReq = new GetStatusType();
		gsReq.setReservationID(req.getReservationID());

		GetStatusResponseType gsRep = null;
		try {
			gsRep = HarmonyServletWrapper.getReservationStatus(gsReq);
			if (gsRep == null) {
				response.setStatus(convertStatus(StatusType.UNKNOWN));
				return response;
			}
			for (ServiceStatus st : gsRep.getServiceStatus()) {
				if (!st.getStatus().equals(StatusType.ACTIVE)) {
					response.setStatus(convertStatus(st.getStatus()));
					return response;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(convertStatus(StatusType.UNKNOWN));
			return response;
		}
		response.setStatus(convertStatus(StatusType.ACTIVE));
		return response;
	}

	private static String convertStatus(StatusType status) {
		if (status.equals(StatusType.ACTIVE))
			return Session.RUNNING;
		if (status.equals(StatusType.CANCELLED_BY_SYSTEM)
				|| status.equals(StatusType.CANCELLED_BY_USER))
			return Session.CANCELLED;
		if (status.equals(StatusType.PENDING))
			return Session.SCHEDULED;
		if (status.equals(StatusType.SETUP_IN_PROGRESS)
				|| status.equals(StatusType.TEARDOWN_IN_PROGRESS))
			return Session.LOADING;
		if (status.equals(StatusType.COMPLETED))
			return Session.FINISHED;
		if (status.equals(StatusType.UNKNOWN))
			return Session.PENDING;
		return Session.PENDING;
	}

//	@Override
//	public CompleteNetworkReservationResponse completeReservation(
//			CompleteNetworkReservation req) {
//		CompleteNetworkReservationResponse resp = new CompleteNetworkReservationResponse();
//		CompleteJob completeJob = new CompleteJob();
//		CompleteJobType completeJobType = new CompleteJobType();
//		completeJobType.setJobID(req.getJobID());
//		completeJob.setCompleteJob(completeJobType);
//		CompleteJobResponseType response = null;
//		try {
//			response = HarmonyServletWrapper.completeJob(completeJob);
//		} catch (Exception e) {
//			e.printStackTrace();
//			resp.setSuccessful(false);
//			return resp;
//		}
//		resp.setSuccessful(response.isSuccess());
//		return resp;
//	}

	@Override
	public ActivateNetworkReservationResponse activate(
			ActivateNetworkReservation rq) {
		ActivateNetworkReservationResponse response = new ActivateNetworkReservationResponse();
		Activate activate = new Activate();
		ActivateType type = new ActivateType();
		type.setReservationID(rq.getNetworkResource().getParameterValue(
				"HARMONY_RESERVATION_ID"));
		type.setServiceID(1);
		activate.setActivate(type);

		ActivateResponseType resp = null;

		try {
			resp = HarmonyServletWrapper.activate(activate);
			if (resp != null)
				response.setSuccessful(resp.isSuccess());
			else
				response.setSuccessful(false);
		} catch (IOException e) {
			e.printStackTrace();
			response.setSuccessful(false);
			return response;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			response.setSuccessful(false);
			return response;
		}

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
		
		if (s.getStatus().equals(Session.PENDING)) {
			if (s.getConnections().size() != 0) {
				ReservationService service = null;
				service = getService(s);
				if (service != null)
					reservation.getServicesList().add(service);
				return reservation;
			}
			if (s.getSubSessions() != null && !s.getSubSessions().isEmpty()) {
				for (ca.gc.iit.nrc.savoir.domain.Session ss : s
						.getSubSessions()) {
					if (ss.getStatus().equals(Session.PENDING)) {
						if (ss.getConnections().size() != 0) {
							ReservationService service = null;
							service = getService(ss);
							if (service != null)
								reservation.getServicesList().add(service);
						}
					}
				}
			}
		} else {
			return null;
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
	private ReservationService getService(ca.gc.iit.nrc.savoir.domain.Session ss)
			throws NoScenarioFoundException {
		ReservationService service = new ReservationService();

		service.setStartTime(ss.getRequestedStartTime().getTimeInMillis());
		//added for test 09-22-09
		logger.info("The session start time zone is in session object " + ss.getRequestedStartTime().getTimeZone().getID());
		logger.info("The session start time is in session object" + ss.getRequestedStartTime().getTime().toString());
		logger.info("The session start time is in service" + service.getStartTime().toString());
		
		//end add
		service.setEndTime(ss.getRequestedEndTime().getTimeInMillis());
		service.setSessionID(ss.getSessionID());

		for (Connection c : ss.getConnections()) {
			if (c.isLpNeeded()) {
				if (c.getSourceEndPoint().getNetworkEndPoint() != null
						&& c.getSourceEndPoint().getNetworkEndPoint()
								.getResourceType().getId().equals(
										"LP_END_POINT")
						&& c.getTargetEndPoint().getNetworkEndPoint() != null
						&& c.getTargetEndPoint().getNetworkEndPoint()
								.getResourceType().getId().equals(
										"LP_END_POINT")) {
					ReservationResource res = new ReservationResource();
					res.setSourceIP(c.getSourceEndPoint().getNetworkEndPoint()
							.getParameterValue("IP_ADDRESS"));
					res.setDestinationIP(c.getTargetEndPoint()
							.getNetworkEndPoint().getParameterValue(
									"IP_ADDRESS"));
					res.setBandwidth((int) c.getBwRequirement());
					service.getResourcesList().add(res);
					continue;
				}
			}
		}

		return service;

	}
}
