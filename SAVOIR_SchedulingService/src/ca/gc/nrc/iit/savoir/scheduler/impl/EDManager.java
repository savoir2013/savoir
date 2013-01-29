// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.impl;

import java.util.Map;

import ca.gc.iit.nrc.savoir.domain.Constraint;
import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.nrc.iit.savoir.scheduler.IEDManager;
import ca.gc.nrc.iit.savoir.scheduler.RequirementChecker;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CancelResourceReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CancelResourceReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CheckResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CreateResourceReservation;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CreateResourceReservationResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.GetResourceReservations;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.GetResourceReservationsResponse;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.IsResourceAvailable;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.IsResourceAvailableResponse;

public class EDManager implements IEDManager {

	private Map<String, RequirementChecker> requirementCheckers;

	@Override
	public CancelResourceReservationResponse cancelReservation(
			CancelResourceReservation req) {
		// TODO Auto-generated method stub
		CancelResourceReservationResponse response = new CancelResourceReservationResponse();
		response.setSuccessful(true);
		return response;
	}

	@Override
	public CreateResourceReservationResponse createReservation(
			CreateResourceReservation req) {
		// TODO Auto-generated method stub
		CreateResourceReservationResponse response = new CreateResourceReservationResponse();
		response.setSuccessful(true);
		return response;
	}

	@Override
	public GetResourceReservationsResponse getReservations(
			GetResourceReservations req) {
		// TODO Auto-generated method stub
		GetResourceReservationsResponse response = new GetResourceReservationsResponse();
		return response;
	}

	@Override
	public IsResourceAvailableResponse isAvailable(IsResourceAvailable req) {
		// TODO Auto-generated method stub
		IsResourceAvailableResponse response = new IsResourceAvailableResponse();
		// this can be changed to keep looking for the time offset that matches
		// all the requirements
		for (Resource resource : req.getSession().getResources()) {
			for (Constraint constraint : resource.getConstraints()) {
				// check if constraint is satisfied
				CheckResponse rsp = requirementCheckers.get(constraint.getId())
						.check(req.getSession(), req.getOffset(), resource, constraint);
				if (!rsp.isSuccessful()) {
					response.setSuccessful(false);
					response.setSchedulingConflicts(rsp
							.getSchedulingConflicts());
					response.setTimeOffset(rsp.getTimeOffset());
					return response;
				}
			}
		}
		response.setTimeOffset(0);
		response.setSuccessful(true);
		return response;
	}

	public Map<String, RequirementChecker> getRequirementCheckers() {
		return requirementCheckers;
	}

	public void setRequirementCheckers(
			Map<String, RequirementChecker> requirementCheckers) {
		this.requirementCheckers = requirementCheckers;
	}

}
