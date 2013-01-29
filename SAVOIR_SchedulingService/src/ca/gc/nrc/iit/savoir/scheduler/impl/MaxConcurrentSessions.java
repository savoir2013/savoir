// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.impl;

import java.util.List;

import ca.gc.iit.nrc.savoir.domain.Connection;
import ca.gc.iit.nrc.savoir.domain.Constraint;
import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.iit.nrc.savoir.domain.Session;
import ca.gc.nrc.iit.savoir.dao.impl.DAOFactory;
import ca.gc.nrc.iit.savoir.scheduler.RequirementChecker;
import ca.gc.nrc.iit.savoir.scheduler.types.ResourceUnavailable;
import ca.gc.nrc.iit.savoir.scheduler.types.SchedulingConflict;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CheckResponse;

/**
 * @author Yosri Harzallah
 * 
 */
public class MaxConcurrentSessions implements RequirementChecker {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.gc.nrc.iit.savoir.scheduler.RequirementChecker#check(ca.gc.iit.nrc
	 * .savoir.domain.Session, ca.gc.iit.nrc.savoir.domain.Resource,
	 * java.lang.String)
	 */
	/**
	 * This class checks the requirement that a resource can only be used by a
	 * maximum number of sessions at a time
	 */
	@Override
	public CheckResponse check(Session s, long offset, Resource r, Constraint c) {
		// c.getConfigArgs() = arg1;arg2;arg3....

		CheckResponse response = new CheckResponse();
		int max = Integer.parseInt(c.getConfigArgs().split(";")[0]);
		int counter = 0;

		// first we need to get a list of concurrent
		// non-pending/cancelled/finished sessions, based on the
		// latest end time and earliest start time
		// then check the ones that have the resource r involved, their sum
		// should not be greater than arg1 - 1

		s.getRequestedEndTime().setTimeInMillis(
				s.getRequestedEndTime().getTimeInMillis() + offset);
		s.getRequestedStartTime().setTimeInMillis(
				s.getRequestedStartTime().getTimeInMillis() + offset);

		List<Session> concurrentSessions = DAOFactory.getDAOFactoryInstance()
				.getSessionDAO().getRelevantSessionsInTimeInterval(
						s.getRequestedStartTime(), s.getRequestedEndTime());
		for (Session session : concurrentSessions) {
			counter += countOccurences(session, r);
		}
		if (counter <= (max - 1)) {
			response.setSuccessful(true);
		} else {
			response.setSuccessful(false);
			response.setTimeOffset(findTimeOffset(s, concurrentSessions));
			SchedulingConflict[] sc = { new ResourceUnavailable(r) };
			response.setSchedulingConflicts(sc);
		}

		return response;
	}

	// Keep in mind that a session is a set of subsessions
	private int findTimeOffset(Session s, List<Session> concurrentSessions) {
		long earliestEndTime = Long.MAX_VALUE;
		long latestStartTime = Long.MIN_VALUE;
		long timeOffset = 0;
		if (s.getRequestedStartTime().getTimeInMillis() > latestStartTime) {
			for (Session cs : concurrentSessions) {
				if (cs.getSubSessions() != null) {
					for (Session subS : cs.getSubSessions()) {
						if (!subS.getStatus().equals(Session.PENDING)
								&& !subS.getStatus().equals(Session.FINISHED)
								&& !subS.getStatus().equals(Session.CANCELLED)
								&& subS.getTimeSlot().getEndTime()
										.getTimeInMillis() < earliestEndTime
								&& subS.getTimeSlot().getEndTime()
										.getTimeInMillis() > s
										.getRequestedStartTime()
										.getTimeInMillis()) {
							earliestEndTime = subS.getTimeSlot().getEndTime()
									.getTimeInMillis();
							latestStartTime = s.getRequestedStartTime()
									.getTimeInMillis();
							timeOffset = earliestEndTime - latestStartTime;
						}
					}
				}

			}
		}

		return (int) timeOffset;
	}

	private int countOccurences(Session s, Resource r) {
		int result = 0;
		if (s.getConnections() != null && s.getConnections().size() > 0) {
			for (Connection c : s.getConnections()) {
				if ((c.getSourceEndPoint().getResource() != null && c
						.getSourceEndPoint().getResource().getResourceID() == r
						.getResourceID())
						|| (c.getTargetEndPoint().getResource() != null && c
								.getTargetEndPoint().getResource()
								.getResourceID() == r.getResourceID())) {
					result++;
					break;
				}
			}
		}

		if (s.getSubSessions() != null && s.getSubSessions().size() > 0) {
			for (Session ss : s.getSubSessions()) {
				result = result + countOccurences(ss, r);
			}
		}

		return result;
	}

}
