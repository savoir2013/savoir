// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.test;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import ca.gc.iit.nrc.savoir.domain.Connection;
import ca.gc.iit.nrc.savoir.domain.Session;
import ca.gc.nrc.iit.savoir.dao.ISessionDAO;

public class TestSessionDAO extends BaseTestCase {

	private ISessionDAO sessionDAO;

	public void setUp() {
		super.setUp();
		sessionDAO = (ISessionDAO) super.getBeanManger().getContext().getBean(
				"sessionDAO");
	}

	public void testGetSessionById() {
		Session r = this.sessionDAO.getSessionById(5);
		assertNotNull(r);
		System.out.println(r.getTimeSlot().getStartTime().getTime());
		System.out.println(r.getTimeSlot().getEndTime().getTime());
		assertTrue(r.getConnections().size() == 2);
		assertTrue(r.getRequestedBy().getDName().equals("hickeyj"));
		for (Connection c : r.getConnections()) {

			System.out.print(c.getSourceEndPoint().getResource()
					.getDescription()
					+ " + "
					+ c.getSourceEndPoint().getPerson()
					+ " #CONNECT TO# ");
			System.out.println(c.getTargetEndPoint().getResource()
					.getDescription()
					+ " + " + c.getTargetEndPoint().getPerson());

		}
	}

	public void testGetRelevantSessions() {
		String[] availableTimezones = TimeZone.getAvailableIDs();

		for (String timezone : availableTimezones) {
			System.out.println("Timezone ID = " + timezone);
		}

		Calendar calStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		Calendar calEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		System.out.println(calStart.getTimeZone().getDisplayName());

		calStart.set(2009, 2, 15, 10, 30);
		calEnd.set(2009, 2, 15, 10, 45);
		
		System.out.println(calStart.getTime());
		
		DateFormat dateFormatter = DateFormat.getDateTimeInstance(
				DateFormat.SHORT, DateFormat.LONG);
		dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		System.out.println(dateFormatter.format(calStart.getTime()));
		
		List<Session> r = this.sessionDAO.getRelevantSessionsInTimeInterval(
				calStart, calEnd);
		assertNotNull(r);
		assertTrue(r.size() == 1);
	}
}
