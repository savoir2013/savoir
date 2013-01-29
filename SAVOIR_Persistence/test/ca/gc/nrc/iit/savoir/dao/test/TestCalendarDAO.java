// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.iit.nrc.savoir.domain.Session;
import ca.gc.iit.nrc.savoir.domain.TimeSlot;
import ca.gc.nrc.iit.savoir.dao.ICalendarDAO;
import ca.gc.nrc.iit.savoir.dao.ISessionDAO;

public class TestCalendarDAO extends BaseTestCase {

	private ICalendarDAO calendarDAO;

	public void setUp() {
		super.setUp();
		calendarDAO = (ICalendarDAO) super.getBeanManger().getContext()
				.getBean("calendarDAO");
	}

//	public void testCalendar() {
//
//		Calendar startTime = Calendar.getInstance();
//		Calendar endTime = Calendar.getInstance();
//
//		endTime.set(2009, 2, 10, 10, 0, 0);
//
//		this.calendarDAO.addEntry(Resource.class, 10, new TimeSlot(startTime,
//				endTime));
//
//		List<TimeSlot> list = this.calendarDAO.getCalendar(Resource.class, 10);
//
//		assertNotNull(list);
//		assertTrue(list.size() > 0);
//
//		endTime.setTimeInMillis(System.currentTimeMillis());
//
//		this.calendarDAO.updateEntry(list.get(0).getId(), new TimeSlot(
//				startTime, endTime));
//
//		for (TimeSlot ts : list)
//			this.calendarDAO.removeEntry(ts.getId());
//	}

	public void testFreeCalendar() {

		Calendar startTime = Calendar.getInstance();
		Calendar endTime = Calendar.getInstance();

		endTime.set(2009, 2, 10, 10, 0, 0);

		this.calendarDAO.addEntry(Resource.class, 10, new TimeSlot(startTime,
				endTime));

		startTime.set(2009,3,10,10,0,0);
		endTime.set(2009, 3, 11, 10, 0, 0);

		this.calendarDAO.addEntry(Resource.class, 10, new TimeSlot(startTime,
				endTime));
		
		List<TimeSlot> list = this.calendarDAO.getFreeCalendar(Resource.class, 10);

		assertNotNull(list);
		assertTrue(list.size() > 0);


		for (TimeSlot ts : list)
			this.calendarDAO.removeEntry(ts.getId());
	}
	
}
