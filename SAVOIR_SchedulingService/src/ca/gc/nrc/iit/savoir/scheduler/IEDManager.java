// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler;

import ca.gc.nrc.iit.savoir.scheduler.types.msg.CancelResourceReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CancelResourceReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CreateResourceReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CreateResourceReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.GetResourceReservations;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.GetResourceReservationsResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.IsResourceAvailable;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.IsResourceAvailableResponse;

public interface IEDManager {

	public IsResourceAvailableResponse isAvailable(IsResourceAvailable req);
	
	public CreateResourceReservationResponse createReservation(CreateResourceReservation req);
	
	public CancelResourceReservationResponse cancelReservation(CancelResourceReservation req);
	
	public GetResourceReservationsResponse getReservations(GetResourceReservations req);
	
}
