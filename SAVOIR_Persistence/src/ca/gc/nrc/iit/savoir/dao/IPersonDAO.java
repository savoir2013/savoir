// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao;

import java.util.List;

import ca.gc.iit.nrc.savoir.domain.Person;
import ca.gc.iit.nrc.savoir.domain.PersonInfo;

public interface IPersonDAO{
	public void addPerson(Person p);
	public void removePerson (int personId);
	public Person getPersonById(int personId);
	public List<Person> getSessionParticipants(int sessionID);
	
	/**
	 * @return the next person ID in the series (the current max + 1) (0 for error)	
	 */
	public int getNextPersonId();
	
	/**
	 * Updates a person's information in the database
	 * @param personId		The person's ID
	 * @param personInfo	The information to update (all null fields will be left as is)
	 * @return	0 for success
	 * 			-1 for no such person
	 * 			-2 for invalid personId parameter, null personInfo parameter, 
	 * 				or all personInfo fields null
	 * 			!=0 for other error
	 */
	public int updatePerson(int personId, PersonInfo personInfo);
}
