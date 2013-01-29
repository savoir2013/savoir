// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler;

import javax.jws.WebService;

import ca.gc.nrc.iit.savoir.scheduler.types.msg.ActivateSessionReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.ActivateSessionReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CancelSessionReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CancelSessionReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CreateSessionReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CreateSessionReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.DeleteSessionReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.DeleteSessionReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.GetReservationStatus;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.IsSessionReservationAvailable;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.IsSessionReservationAvailableResponse;

@WebService
public interface SavoirScheduler {

	/**
	 * This method reserves the resources needed by the requested session
	 * 
	 * @param request
	 *            The requested session plus the type of reservation
	 * @return When the reservation is not possible, returns a suggested time
	 *         offset and what causes the conflict
	 */
	public CreateSessionReservationResponse requestReservation(
			CreateSessionReservation request);

	/**
	 * This method checks if a session is possible by checking the availability
	 * of the resources.
	 * 
	 * @param request
	 *            Holds the session and the reservation type
	 * @return When the reservation is not possible, returns a suggested time
	 *         offset and what causes the conflict
	 */
	public IsSessionReservationAvailableResponse isReservationAvailable(
			IsSessionReservationAvailable request);

	/**
	 * This method deletes a reservation.
	 * 
	 * @param request
	 *            Session to delete the reservation for
	 * @return Action status and reason in case of failure
	 */
	public DeleteSessionReservationResponse deleteReservation(
			DeleteSessionReservation request);

	/**
	 * This method cancels a reservation. If it's a scenario and it is running
	 * then it should be torn down. If it's a chronos/harmony reservation then
	 * it should be deactivated.
	 * 
	 * @param request
	 *            Session to cancel the reservation for
	 * @return Action status and reason in case of failure
	 */
	public CancelSessionReservationResponse cancelReservation(
			CancelSessionReservation request);

	/**
	 * This method activates a reservation. If it's a scenario, it is set up. If
	 * it's a chronos/harmony reservation then it is activated.
	 * 
	 * @param request
	 *            Session to cancel the reservation for
	 * @return Action status and reason in case of failure
	 */
	public ActivateSessionReservationResponse activateSessionReservation(
			ActivateSessionReservation request);

	/**
	 * This method returns a status of the reservation. It is useful especially
	 * when using CHRONOS to reserve the network.
	 * 
	 * @param request
	 * @return Status of the reservation
	 */
	//changed by yyh 12-05-10
	public String getReservationStatus(GetReservationStatus request);

}
