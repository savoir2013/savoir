// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.impl;

import java.rmi.RemoteException;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.TimeZone;

import ca.gc.iit.nrc.savoir.domain.Connection;
import ca.gc.iit.nrc.savoir.domain.Session;
import ca.gc.nrc.iit.savoir.scheduler.INRManager;
import ca.gc.nrc.iit.savoir.scheduler.types.ExceptionCaught;
import ca.gc.nrc.iit.savoir.scheduler.types.NetworkConnectionConflict;
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
import ca.inocybe.argia.stubs.reservation.Activate;
import ca.inocybe.argia.stubs.reservation.ActivateResponse;
import ca.inocybe.argia.stubs.reservation.ActivateType;
import ca.inocybe.argia.stubs.reservation.ConnectionAvailabilityType;
import ca.inocybe.argia.stubs.reservation.ConnectionConstraintType;
import ca.inocybe.argia.stubs.reservation.CreateReservationType;
import ca.inocybe.argia.stubs.reservation.EndpointType;
import ca.inocybe.argia.stubs.reservation.FixedReservationConstraintType;
import ca.inocybe.argia.stubs.reservation.GetStatus;
import ca.inocybe.argia.stubs.reservation.GetStatusResponse;
import ca.inocybe.argia.stubs.reservation.GetStatusResponseTypeServiceStatus;
import ca.inocybe.argia.stubs.reservation.GetStatusType;
import ca.inocybe.argia.stubs.reservation.InvalidReservationIDFault;
import ca.inocybe.argia.stubs.reservation.ReservationType;
import ca.inocybe.argia.stubs.reservation.ServiceConstraintType;
import ca.inocybe.argia.stubs.reservation.StatusType;
import ca.inocybe.argia.stubs.reservation.UnexpectedFault;
import ca.inocybe.core.tools.clients.reservation.utils.AdvReservationsWrapper;

public class ChronosNRManager implements INRManager {

	@Override
	public CancelNetworkReservationResponse cancelReservation(
			CancelNetworkReservation req) {
		CancelNetworkReservationResponse response = new CancelNetworkReservationResponse();

		ca.inocybe.argia.stubs.reservation.CancelReservation crReq = new ca.inocybe.argia.stubs.reservation.CancelReservation();
		ca.inocybe.argia.stubs.reservation.CancelReservationType crReqType = new ca.inocybe.argia.stubs.reservation.CancelReservationType();
		ca.inocybe.argia.stubs.reservation.CancelReservationResponse crReqResp = new ca.inocybe.argia.stubs.reservation.CancelReservationResponse();

		crReqType.setReservationID(Long.parseLong(req.getResource()
				.getParameterValue("CHRONOS_RESERVATION_ID")));
		crReq.setCancelReservation(crReqType);

		try {
			crReqResp = AdvReservationsWrapper.cancelReservation(crReq);
			response.setSuccessful(crReqResp.getCancelReservationResponse()
					.isSuccess());
		} catch (InvalidReservationIDFault e) {
			e.printStackTrace();
			response.setSuccessful(false);
		} catch (UnexpectedFault e) {
			e.printStackTrace();
			response.setSuccessful(false);
		}

		return response;
	}
	
	@Override
	public DeleteNetworkReservationResponse deleteReservation(
			DeleteNetworkReservation req) {
		DeleteNetworkReservationResponse response = new DeleteNetworkReservationResponse();

		ca.inocybe.argia.stubs.reservation.DeleteReservation dlReq = new ca.inocybe.argia.stubs.reservation.DeleteReservation();
		ca.inocybe.argia.stubs.reservation.DeleteReservationType dlReqType = new ca.inocybe.argia.stubs.reservation.DeleteReservationType();
		ca.inocybe.argia.stubs.reservation.DeleteReservationResponse dlReqResp = new ca.inocybe.argia.stubs.reservation.DeleteReservationResponse();

		dlReqType.setReservationID(Long.parseLong(req.getResource()
				.getParameterValue("CHRONOS_RESERVATION_ID")));
		dlReq.setDeleteReservation(dlReqType);

		try {
			dlReqResp = AdvReservationsWrapper.deleteReservation(dlReq);
			response.setSuccessful(dlReqResp.getDeleteReservationResponse()
					.isSuccess());
		} catch (InvalidReservationIDFault e) {
			e.printStackTrace();
			response.setSuccessful(false);
		} catch (UnexpectedFault e) {
			e.printStackTrace();
			response.setSuccessful(false);
		}

		return response;
	}

	@Override
	public CreateNetworkReservationResponse createReservation(
			CreateNetworkReservation req) {

		IsNetworkAvailable isAv = new IsNetworkAvailable(req);
		IsNetworkAvailableResponse isAvResp = isAvailable(isAv);

		CreateNetworkReservationResponse response = new CreateNetworkReservationResponse();

		if (isAvResp.isSuccessful()) {
			ca.inocybe.argia.stubs.reservation.CreateReservationResponse crResp = null;
			try {
				ca.inocybe.argia.stubs.reservation.CreateReservation crReq = getReservationObject(req);
				crResp = AdvReservationsWrapper.createReservation(crReq);

				Long rid = crResp.getCreateReservationResponse()
						.getReservationID();
				System.out
						.println("     -> Reservation created successfully! Identifier: "
								+ rid);
				response.setSuccessful(true);
				response.setReservationID(rid.toString());
				response.setJobID(crResp.getCreateReservationResponse()
						.getJobID());
				return response;
			} catch (RemoteException e) {
				e.printStackTrace();
				System.out.println("     -> Reservation NOT possible.");

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

//	@Override
//	public CompleteNetworkReservationResponse completeReservation(
//			CompleteNetworkReservation req) {
//
//		CompleteNetworkReservationResponse resp = new CompleteNetworkReservationResponse();
//
//		ca.inocybe.argia.stubs.reservation.CompleteJob completeJob = new ca.inocybe.argia.stubs.reservation.CompleteJob();
//		CompleteJobType completeJobType = new CompleteJobType();
//		completeJobType.setJobID(req.getJobID());
//		completeJob.setCompleteJob(completeJobType);
//		CompleteJobResponse response = null;
//		try {
//			response = AdvReservationsWrapper.completeJob(completeJob);
//		} catch (UnexpectedFault e) {
//			e.printStackTrace();
//			resp.setSuccessful(false);
//			return resp;
//		}
//
//		resp.setSuccessful(response.getCompleteJobResponse().isSuccess());
//		return resp;
//	}

	@Override
	public GetNetworkReservationsResponse getReservations(
			GetNetworkReservations req) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IsNetworkAvailableResponse isAvailable(IsNetworkAvailable req) {

		ca.inocybe.argia.stubs.reservation.IsAvailable isAv = new ca.inocybe.argia.stubs.reservation.IsAvailable();
		ca.inocybe.argia.stubs.reservation.IsAvailableType isAvailable = new ca.inocybe.argia.stubs.reservation.IsAvailableType();

		isAvailable.setService(getServiceConstraintTypeArray(req
				.getReservation(), req.isAutomaticActivation()));
		isAvailable.setJobID(null);

		isAv.setIsAvailable(isAvailable);

		ca.inocybe.argia.stubs.reservation.IsAvailableResponse isAvResp = null;
		IsNetworkAvailableResponse response = new IsNetworkAvailableResponse();
		try {
			isAvResp = AdvReservationsWrapper.isAvailable(isAv);
		} catch (UnexpectedFault e) {
			e.printStackTrace();
			SchedulingConflict[] sc = { new ExceptionCaught(e.getMessage(), e
					.getFaultString()) };
			response.setSchedulingConflicts(sc);
			response.setSuccessful(false);
			return response;
		}

		if (isAvResp.getIsAvailableResponse().getAlternativeStartTimeOffset() != null) {
			response.setSuccessful(false);

			int maxI = isAvResp.getIsAvailableResponse().getDetailedResult().length;
			SchedulingConflict[] conflicts = new SchedulingConflict[maxI];
			for (int i = 0; i < maxI; i++) {
				ConnectionAvailabilityType caT = isAvResp
						.getIsAvailableResponse().getDetailedResult(i);

				String message = "";

				StringTokenizer st = new StringTokenizer(caT.getAvailability()
						.getValue().replaceAll("_", " "), " ");
				while (st.hasMoreTokens()) {
					String s = st.nextToken();
					message += s.substring(0, 1).toUpperCase() + " "
							+ s.substring(1).toLowerCase();
				}

				NetworkConnectionConflict ncConf = new NetworkConnectionConflict(
						caT.getServiceID(), caT.getConnectionID(), message, caT
								.getMaxBW() != null ? caT.getMaxBW() : 0);
				conflicts[i] = ncConf;
			}
			response.setSchedulingConflicts(conflicts);
		} else {
			response.setSuccessful(true);
		}

		return response;

	}

	@Override
	public ActivateNetworkReservationResponse activate(
			ActivateNetworkReservation rq) {
		ActivateNetworkReservationResponse response = new ActivateNetworkReservationResponse();
		Activate activate = new Activate();
		ActivateType type = new ActivateType();
		type.setReservationID(Long.valueOf(rq.getNetworkResource()
				.getParameterValue("CHRONOS_RESERVATION_ID").split("@")[0]));
		type.setServiceID(1);
		activate.setActivate(type);

		ActivateResponse resp = null;
		try {
			resp = AdvReservationsWrapper.activate(activate);
			if (resp != null && resp.getActivateResponse() != null)
				response.setSuccessful(resp.getActivateResponse().isSuccess());
			else
				response.setSuccessful(false);
		} catch (UnexpectedFault e) {
			e.printStackTrace();
			response.setSuccessful(false);
			return response;
		}

		return response;
	}

	private ServiceConstraintType[] getServiceConstraintTypeArray(
			Reservation s, boolean autoActiv) {
		int maxJ = s.getServicesList().size();
		ServiceConstraintType[] vs = new ServiceConstraintType[maxJ];

		// for each subsession(service)
		for (int j = 0; j < maxJ; j++) {

			// define a service constraint: start and end times
			ServiceConstraintType serviceConstraint = new ServiceConstraintType();

			serviceConstraint.setServiceID(j + 1);
			serviceConstraint.setTypeOfReservation(ReservationType
					.fromValue("fixed"));

			FixedReservationConstraintType fixedResv = new FixedReservationConstraintType();

			GregorianCalendar cST = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			cST.setTime(s.getServicesList().get(j).getStartTime().getTime());

			GregorianCalendar cET = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			cET.setTime(s.getServicesList().get(j).getEndTime().getTime());

			fixedResv.setStartTime(cST);
			fixedResv.setDuration((int) ((cET.getTimeInMillis() - cST
					.getTimeInMillis()) / 1000));
			serviceConstraint.setFixedReservationConstraints(fixedResv);

			// automaticActivation is true if LP is activated automatically when
			// the connection time comes
			serviceConstraint.setAutomaticActivation(autoActiv);

			int maxK = s.getServicesList().get(j).getResourcesList().size();
			ConnectionConstraintType[] vc = new ConnectionConstraintType[maxK];

			// for each connection
			for (int k = 0; k < maxK; k++) {

				// define connection constraints: sourceIP, destIP, bandwidth
				// requirement and delay
				ConnectionConstraintType connectionContraint = new ConnectionConstraintType();
				connectionContraint.setConnectionID(k + 1);
				connectionContraint.setDirectionality(0);
				EndpointType source = new EndpointType();
				source.setEndpointId(s.getServicesList().get(j)
						.getResourcesList().get(k).getSourceIP());
				connectionContraint.setSource(source);
				EndpointType target = new EndpointType();
				target.setEndpointId(s.getServicesList().get(j)
						.getResourcesList().get(k).getDestinationIP());
				EndpointType[] targets = new EndpointType[1];
				targets[0] = target;
				connectionContraint.setTarget(targets);
				// TODO delay needs to be changed
				connectionContraint.setMaxDelay(10);
				connectionContraint.setMaxBW(s.getServicesList().get(j)
						.getResourcesList().get(k).getBandwidth());
				connectionContraint.setMinBW(s.getServicesList().get(j)
						.getResourcesList().get(k).getBandwidth());
				vc[k] = connectionContraint;
			}
			serviceConstraint.setConnections(vc);
			vs[j] = serviceConstraint;
		}
		return vs;
	}

	private ca.inocybe.argia.stubs.reservation.CreateReservation getReservationObject(
			CreateNetworkReservation r) {
		ca.inocybe.argia.stubs.reservation.CreateReservation crReq = new ca.inocybe.argia.stubs.reservation.CreateReservation();
		CreateReservationType crReqT = new CreateReservationType();
		crReqT.setJobID(r.getJobID());
		crReqT.setService(getServiceConstraintTypeArray(r.getReservation(), r
				.isAutomaticActivation()));
		crReq.setCreateReservation(crReqT);
		return crReq;
	}

	@Override
	public GetNetworkReservationStatusResponse getNetworkStatus(
			GetNetworkReservationStatus req) {
		GetNetworkReservationStatusResponse response = new GetNetworkReservationStatusResponse();

		GetStatusType gsReq = new GetStatusType();
		gsReq.setReservationID(Long.valueOf(req.getReservationID()));

		GetStatus request = new GetStatus();
		request.setGetStatus(gsReq);

		GetStatusResponse gsRep = null;
		try {
			gsRep = AdvReservationsWrapper.getStatus(request);
			if (gsRep == null) {
				response
						.setStatus(convertStatus(StatusType._unknown.toString()));
				return response;
			}
			for (GetStatusResponseTypeServiceStatus st : gsRep
					.getGetStatusResponse().getServiceStatus()) {
				if (!st.getStatus().equals(StatusType._active.toString())) {
					response
							.setStatus(convertStatus(st.getStatus().toString()));
					return response;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(convertStatus(StatusType._unknown.toString()));
			return response;
		}
		response.setStatus((StatusType._active.toString()));
		return response;
	}

	private static String convertStatus(String status) {
		if (status.equals(StatusType._active))
			return Session.RUNNING;
		if (status.equals(StatusType._cancelled_by_system)
				|| status.equals(StatusType._cancelled_by_user))
			return Session.CANCELLED;
		if (status.equals(StatusType._pending))
			return Session.SCHEDULED;
		if (status.equals(StatusType._setup_in_progress)
				|| status.equals(StatusType._teardown_in_progress))
			return Session.LOADING;
		if (status.equals(StatusType._completed))
			return Session.FINISHED;
		if (status.equals(StatusType._unknown))
			return Session.PENDING;
		return Session.PENDING;
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
