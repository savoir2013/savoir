// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao;

import java.util.List;

import ca.gc.iit.nrc.savoir.domain.Constraint;

public interface IConstraintDAO {
	
	/**
	 * Adds a new constraint.
	 * 
	 * @param c				The constraint to add
	 * 
	 * @return {@code 1} for success, {@code 0} for failure, any other number 
	 * 			for critical database failure
	 */
	public int addConstraint(Constraint c);

	/**
	 * Gets all the constraints applying to a particular resource
	 * 
	 * @param resourceID	The ID of the resource
	 * 
	 * @return a list of constraints applying to that resource, null for none 
	 * 			such
	 */
	public List<Constraint> getConstraintsByResourceID(int resourceID);
	
	/**
	 * Removes all the constraints applying to a particular resource
	 * 
	 * @param resourceID	The ID of the resource
	 * 
	 * @return the number of constraints removed
	 */
	public int removeConstraintsByResourceID(int resourceID);
	
}
