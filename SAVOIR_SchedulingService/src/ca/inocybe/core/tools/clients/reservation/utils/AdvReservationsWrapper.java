// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.inocybe.core.tools.clients.reservation.utils;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Stub;
import org.globus.axis.util.Util;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.oasis.wsrf.faults.BaseFaultTypeDescription;

import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.nrc.iit.savoir.dao.impl.DAOFactory;
import ca.inocybe.argia.stubs.reservation.Activate;
import ca.inocybe.argia.stubs.reservation.ActivateResponse;
import ca.inocybe.argia.stubs.reservation.Bind;
import ca.inocybe.argia.stubs.reservation.BindResponse;
import ca.inocybe.argia.stubs.reservation.CancelReservation;
import ca.inocybe.argia.stubs.reservation.CancelReservationResponse;
import ca.inocybe.argia.stubs.reservation.CompleteJob;
import ca.inocybe.argia.stubs.reservation.CompleteJobResponse;
import ca.inocybe.argia.stubs.reservation.CreateDraftReservation;
import ca.inocybe.argia.stubs.reservation.CreateReservation;
import ca.inocybe.argia.stubs.reservation.CreateReservationResponse;
import ca.inocybe.argia.stubs.reservation.DeleteReservation;
import ca.inocybe.argia.stubs.reservation.DeleteReservationResponse;
import ca.inocybe.argia.stubs.reservation.EndpointNotFoundFault;
import ca.inocybe.argia.stubs.reservation.GetEndpoints;
import ca.inocybe.argia.stubs.reservation.GetEndpointsLocation;
import ca.inocybe.argia.stubs.reservation.GetEndpointsLocationResponse;
import ca.inocybe.argia.stubs.reservation.GetEndpointsResponse;
import ca.inocybe.argia.stubs.reservation.GetFeatures;
import ca.inocybe.argia.stubs.reservation.GetFeaturesResponse;
import ca.inocybe.argia.stubs.reservation.GetFullReservations;
import ca.inocybe.argia.stubs.reservation.GetFullReservationsResponse;
import ca.inocybe.argia.stubs.reservation.GetStatus;
import ca.inocybe.argia.stubs.reservation.GetStatusResponse;
import ca.inocybe.argia.stubs.reservation.InvalidRequestFault;
import ca.inocybe.argia.stubs.reservation.InvalidReservationIDFault;
import ca.inocybe.argia.stubs.reservation.IsAvailable;
import ca.inocybe.argia.stubs.reservation.IsAvailableResponse;
import ca.inocybe.argia.stubs.reservation.ReservationPortType;
import ca.inocybe.argia.stubs.reservation.UnexpectedFault;
import ca.inocybe.argia.stubs.reservation.UpdateDraftReservation;
import ca.inocybe.argia.stubs.reservation.service.ReservationServiceAddressingLocator;

/**
 * This class calls the web service.
 * 
 * @author Laia
 */
public class AdvReservationsWrapper {

	private static Resource chronos = DAOFactory.getDAOFactoryInstance()
			.getResourceDAO().getResourcesByType("CHRONOS").get(0);

	static {
		Util.registerTransport();
	}

	/**
	 * This method is used to actually reserve resources. A pre-reservation can
	 * be used by a Middleware entity to block resources for a certain time
	 * while performing several operations to reserve Grid and network
	 * resources. In case of a failure, resources already blocked do not have to
	 * be cancelled manually but are freed after a certain timeout.
	 * 
	 * It should be noted that the blocking of pre-reserved services might not
	 * be definite. While blocked resources surely will not be available in
	 * forthcoming requests from the same user, the system may choose to make
	 * these resources available to other users, e.g. if they have a higher
	 * priority level.
	 * 
	 * @param reservationReq
	 *            contains the constraints of the reservation requested
	 * @return If the reservation is possible, the identifier of the reservation
	 *         created is returned; if the reservation was a pre-reservation,
	 *         also the identifier of the job is returned.
	 * @throws InvalidRequestFault
	 * @throws EndpointNotFoundFault
	 * @throws UnexpectedFault
	 */
	public static CreateReservationResponse createReservation(
			CreateReservation reservationReq) throws InvalidRequestFault,
			EndpointNotFoundFault, UnexpectedFault {
		ReservationPortType portType = null;

		try {
			portType = createPort();
			reservationReq.setOrganization(chronos
					.getParameterValue("CHRONOS_ORGANIZATION"));
			return portType.createReservation(reservationReq);
		} catch (InvalidRequestFault ex) {
			ex.printStackTrace();
			throw ex;
		} catch (EndpointNotFoundFault ex) {
			ex.printStackTrace();
			throw ex;
		} catch (UnexpectedFault ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			UnexpectedFault ef = new UnexpectedFault();
			BaseFaultTypeDescription b = new BaseFaultTypeDescription();
			b.set_value(ex.getMessage());
			BaseFaultTypeDescription[] bs = new BaseFaultTypeDescription[1];
			bs[0] = b;
			ef.setDescription(bs);
			throw ef;
		}
	}

	/**
	 * This method allows canceling a reservation done by the user giving its
	 * identifier.
	 * 
	 * @param cancelReservationReq
	 *            Contains the identifier of the reservation.
	 * @return The success of the operation.
	 * @throws UnexpectedFault
	 * @throws InvalidReservationIDFault
	 */
	public static CancelReservationResponse cancelReservation(
			CancelReservation cancelReservationReq) throws UnexpectedFault,
			InvalidReservationIDFault {
		ReservationPortType portType = null;

		try {
			portType = createPort();
			cancelReservationReq.setOrganization(chronos
					.getParameterValue("CHRONOS_ORGANIZATION"));
			return portType.cancelReservation(cancelReservationReq);
		} catch (InvalidReservationIDFault ex) {
			ex.printStackTrace();
			throw ex;
		} catch (UnexpectedFault ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			UnexpectedFault ef = new UnexpectedFault();
			BaseFaultTypeDescription b = new BaseFaultTypeDescription();
			b.set_value(ex.getMessage());
			BaseFaultTypeDescription[] bs = new BaseFaultTypeDescription[1];
			bs[0] = b;
			ef.setDescription(bs);
			throw ef;
		}
	}
	
	/**
	 * This method allows deleting a reservation done by the user giving its
	 * identifier.
	 * 
	 * @param deleteReservationReq
	 *            Contains the identifier of the reservation.
	 * @return The success of the operation.
	 * @throws UnexpectedFault
	 * @throws InvalidReservationIDFault
	 */
	public static DeleteReservationResponse deleteReservation(
			DeleteReservation deleteReservationReq) throws UnexpectedFault,
			InvalidReservationIDFault {
		ReservationPortType portType = null;

		try {
			portType = createPort();
			deleteReservationReq.setOrganization(chronos
					.getParameterValue("CHRONOS_ORGANIZATION"));
			return portType.deleteReservation(deleteReservationReq);
		} catch (InvalidReservationIDFault ex) {
			ex.printStackTrace();
			throw ex;
		} catch (UnexpectedFault ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			UnexpectedFault ef = new UnexpectedFault();
			BaseFaultTypeDescription b = new BaseFaultTypeDescription();
			b.set_value(ex.getMessage());
			BaseFaultTypeDescription[] bs = new BaseFaultTypeDescription[1];
			bs[0] = b;
			ef.setDescription(bs);
			throw ef;
		}
	}
	

	/**
	 * This method provides information about all the reservations done by the
	 * user from a given date. (Used by ARGIA)
	 * 
	 * @param g
	 *            Contains the date requested.
	 * @return A list that contains the reservations that are made from the
	 *         given date
	 * @throws UnexpectedFault
	 */
	public static GetFullReservationsResponse getFullReservations(
			GetFullReservations g) throws UnexpectedFault {
		ReservationPortType portType = null;

		try {
			portType = createPort();
			g
					.setOrganization(chronos
							.getParameterValue("CHRONOS_ORGANIZATION"));
			return portType.getFullReservations(g);
		} catch (UnexpectedFault ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			UnexpectedFault ef = new UnexpectedFault();
			BaseFaultTypeDescription b = new BaseFaultTypeDescription();
			b.set_value(ex.getMessage());
			BaseFaultTypeDescription[] bs = new BaseFaultTypeDescription[1];
			bs[0] = b;
			ef.setDescription(bs);
			throw ef;
		}
	}

	/**
	 * This method provides information about all the reservations done by the
	 * user from a given date. (Used by ARGIA)
	 * 
	 * @param g
	 *            Contains the identifiers of the reservation and the service to
	 *            activate.
	 * @return The success of the operation.
	 * @throws UnexpectedFault
	 */
	public static ActivateResponse activate(Activate g) throws UnexpectedFault {
		ReservationPortType portType = null;

		try {
			portType = createPort();
			g
					.setOrganization(chronos
							.getParameterValue("CHRONOS_ORGANIZATION"));
			return portType.activate(g);
		} catch (UnexpectedFault ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			UnexpectedFault ef = new UnexpectedFault();
			BaseFaultTypeDescription b = new BaseFaultTypeDescription();
			b.set_value(ex.getMessage());
			BaseFaultTypeDescription[] bs = new BaseFaultTypeDescription[1];
			bs[0] = b;
			ef.setDescription(bs);
			throw ef;
		}
	}

	public static IsAvailableResponse isAvailable(IsAvailable isAv)
			throws UnexpectedFault {
		ReservationPortType portType = null;

		try {
			portType = createPort();
			isAv.setOrganization(chronos
					.getParameterValue("CHRONOS_ORGANIZATION"));
			return portType.isAvailable(isAv);
		} catch (UnexpectedFault ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			UnexpectedFault ef = new UnexpectedFault();
			BaseFaultTypeDescription b = new BaseFaultTypeDescription();
			b.set_value(ex.getMessage());
			BaseFaultTypeDescription[] bs = new BaseFaultTypeDescription[1];
			bs[0] = b;
			ef.setDescription(bs);
			throw ef;
		}
	}

	public static GetStatusResponse getStatus(GetStatus g)
			throws UnexpectedFault {
		ReservationPortType portType = null;

		try {
			portType = createPort();
			g
					.setOrganization(chronos
							.getParameterValue("CHRONOS_ORGANIZATION"));
			return portType.getStatus(g);
		} catch (UnexpectedFault ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			UnexpectedFault ef = new UnexpectedFault();
			BaseFaultTypeDescription b = new BaseFaultTypeDescription();
			b.set_value(ex.getMessage());
			BaseFaultTypeDescription[] bs = new BaseFaultTypeDescription[1];
			bs[0] = b;
			ef.setDescription(bs);
			throw ef;
		}
	}

	public static GetFeaturesResponse getFeatures(GetFeatures g)
			throws UnexpectedFault {
		ReservationPortType portType = null;

		try {
			portType = createPort();
			g
					.setOrganization(chronos
							.getParameterValue("CHRONOS_ORGANIZATION"));
			return portType.getFeatures(g);
		} catch (UnexpectedFault ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			UnexpectedFault ef = new UnexpectedFault();
			BaseFaultTypeDescription b = new BaseFaultTypeDescription();
			b.set_value(ex.getMessage());
			BaseFaultTypeDescription[] bs = new BaseFaultTypeDescription[1];
			bs[0] = b;
			ef.setDescription(bs);
			throw ef;
		}
	}

	public static CompleteJobResponse completeJob(CompleteJob g)
			throws UnexpectedFault {
		ReservationPortType portType = null;

		try {
			portType = createPort();
			g
					.setOrganization(chronos
							.getParameterValue("CHRONOS_ORGANIZATION"));
			return portType.completeJob(g);
		} catch (UnexpectedFault ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			UnexpectedFault ef = new UnexpectedFault();
			BaseFaultTypeDescription b = new BaseFaultTypeDescription();
			b.set_value(ex.getMessage());
			BaseFaultTypeDescription[] bs = new BaseFaultTypeDescription[1];
			bs[0] = b;
			ef.setDescription(bs);
			throw ef;
		}
	}

	public static GetEndpointsResponse getEndPoints(GetEndpoints g)
			throws UnexpectedFault {
		ReservationPortType portType = null;

		try {
			portType = createPort();
			g
					.setOrganization(chronos
							.getParameterValue("CHRONOS_ORGANIZATION"));
			return portType.getEndpoints(g);
		} catch (UnexpectedFault ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			UnexpectedFault ef = new UnexpectedFault();
			BaseFaultTypeDescription b = new BaseFaultTypeDescription();
			b.set_value(ex.getMessage());
			BaseFaultTypeDescription[] bs = new BaseFaultTypeDescription[1];
			bs[0] = b;
			ef.setDescription(bs);
			throw ef;
		}
	}

	public static GetEndpointsLocationResponse getEndPointsLocation(
			GetEndpointsLocation g) throws UnexpectedFault {
		ReservationPortType portType = null;

		try {
			portType = createPort();
			g
					.setOrganization(chronos
							.getParameterValue("CHRONOS_ORGANIZATION"));
			return portType.getEndpointsLocation(g);
		} catch (UnexpectedFault ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			UnexpectedFault ef = new UnexpectedFault();
			BaseFaultTypeDescription b = new BaseFaultTypeDescription();
			b.set_value(ex.getMessage());
			BaseFaultTypeDescription[] bs = new BaseFaultTypeDescription[1];
			bs[0] = b;
			ef.setDescription(bs);
			throw ef;
		}
	}

	public static BindResponse bind(Bind g) throws UnexpectedFault {
		ReservationPortType portType = null;

		try {
			portType = createPort();
			g
					.setOrganization(chronos
							.getParameterValue("CHRONOS_ORGANIZATION"));
			return portType.bind(g);
		} catch (UnexpectedFault ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			UnexpectedFault ef = new UnexpectedFault();
			BaseFaultTypeDescription b = new BaseFaultTypeDescription();
			b.set_value(ex.getMessage());
			BaseFaultTypeDescription[] bs = new BaseFaultTypeDescription[1];
			bs[0] = b;
			ef.setDescription(bs);
			throw ef;
		}
	}

	public static CreateReservationResponse updateDraftReservation(
			UpdateDraftReservation g) throws UnexpectedFault {
		ReservationPortType portType = null;

		try {
			portType = createPort();
			g
					.setOrganization(chronos
							.getParameterValue("CHRONOS_ORGANIZATION"));
			return portType.updateDraftReservation(g);
		} catch (UnexpectedFault ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			UnexpectedFault ef = new UnexpectedFault();
			BaseFaultTypeDescription b = new BaseFaultTypeDescription();
			b.set_value(ex.getMessage());
			BaseFaultTypeDescription[] bs = new BaseFaultTypeDescription[1];
			bs[0] = b;
			ef.setDescription(bs);
			throw ef;
		}
	}

	public static CreateReservationResponse createDraftReservation(
			CreateDraftReservation g) throws UnexpectedFault {
		ReservationPortType portType = null;

		try {
			portType = createPort();
			g
					.setOrganization(chronos
							.getParameterValue("CHRONOS_ORGANIZATION"));
			return portType.createDraftReservation(g);
		} catch (UnexpectedFault ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			UnexpectedFault ef = new UnexpectedFault();
			BaseFaultTypeDescription b = new BaseFaultTypeDescription();
			b.set_value(ex.getMessage());
			BaseFaultTypeDescription[] bs = new BaseFaultTypeDescription[1];
			bs[0] = b;
			ef.setDescription(bs);
			throw ef;
		}
	}

	/**
	 * Given the host and port of a service, this method creates the connection
	 * to this service by making the URL.
	 * 
	 * @param host
	 *            Of the service requested
	 * @param port
	 *            Of the service requested
	 * @return
	 * @throws MalformedURLException
	 * @throws ServiceException
	 */
	private static ReservationPortType createPort()
			throws MalformedURLException, ServiceException {
		// loadProperties();

		String url = "https://" + chronos.getParameterValue("CHRONOS_HOST")
				+ ":8443" + "/wsrf/services/argia/ReservationService";

		ReservationPortType portType = null;

		ReservationServiceAddressingLocator service = new ReservationServiceAddressingLocator();
		portType = service.getReservationPortTypePort(new URL(url));

		setSecurity((Stub) portType);
		return portType;
	}

	private static void setSecurity(Stub stub) {
		stub._setProperty(Constants.GSI_ANONYMOUS, Boolean.TRUE);
	}
}
