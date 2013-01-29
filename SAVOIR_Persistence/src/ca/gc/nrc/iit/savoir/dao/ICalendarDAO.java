// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao;

import java.util.Calendar;
import java.util.List;

import ca.gc.iit.nrc.savoir.domain.TimeSlot;

public interface ICalendarDAO {
	
	public List<TimeSlot> getCalendar(Class resourceType, int objectID);
	
	public boolean isAvailable(Class resourceType, int objectID, TimeSlot timeSlot);
	
	public int removeEntry(int entryID);
	
	public Object getAssociatedObject(int entryID);
	
	public int addEntry(Class resourceType, int objectID, TimeSlot timeSlot);
	
	public int updateEntry(int entryID, TimeSlot timeSlot);
	
	public List<TimeSlot> getFreeCalendar(Class resourceType, int objectID);
	
	public List<TimeSlot> getScenarioCalendar(int resourceID);

	public List<TimeSlot> getScenariosCalendar(Calendar minTime, Calendar maxTime);

	//public boolean isNetworkAvailable(int resourceID, TimeSlot timeSlot);
}
