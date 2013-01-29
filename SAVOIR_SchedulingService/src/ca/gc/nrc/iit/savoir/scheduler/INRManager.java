// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler;

import ca.gc.iit.nrc.savoir.domain.Session;
import ca.gc.nrc.iit.savoir.scheduler.types.SchedulingConflict;
import ca.gc.nrc.iit.savoir.scheduler.types.domain.Reservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.ActivateNetworkReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.ActivateNetworkReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CancelNetworkReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CancelNetworkReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CompleteNetworkReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CompleteNetworkReservationResponse;
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

public interface INRManager {

	public IsNetworkAvailableResponse isAvailable(IsNetworkAvailable req);

	public CreateNetworkReservationResponse createReservation(CreateNetworkReservation req);

	public CancelNetworkReservationResponse cancelReservation(CancelNetworkReservation req);

	public GetNetworkReservationsResponse getReservations(GetNetworkReservations req);
	
	public GetNetworkReservationStatusResponse getNetworkStatus(GetNetworkReservationStatus req);
	
	//public CompleteNetworkReservationResponse completeReservation(CompleteNetworkReservation req);

	public ActivateNetworkReservationResponse activate(ActivateNetworkReservation rq);
	
	public Reservation getReservationObject(Session session) throws SchedulingConflict;

	public DeleteNetworkReservationResponse deleteReservation(
			DeleteNetworkReservation req); 
}
