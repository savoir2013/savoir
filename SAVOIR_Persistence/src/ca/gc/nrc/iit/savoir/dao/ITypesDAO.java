// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao;

import java.util.List;

import ca.gc.iit.nrc.savoir.domain.types.ParameterType;
import ca.gc.iit.nrc.savoir.domain.types.ResourceType;

public interface ITypesDAO {

	public ParameterType getParameterTypeById(String typeID);

	/**
	 * Get a specific resource type, by its string-format ID
	 * 
	 * @param typeID		The ID of the resource type
	 * 
	 * @return the ResourceType object with that type, null for none such or 
	 * 			error
	 */
	public ResourceType getResourceTypeById(String typeID);
	
	/**
	 * Gets a list of all the available resource types.
	 * 
	 * @return a list of available resource types, empty for none such, null 
	 * 			for error
	 */
	public List<ResourceType> getAllResourceTypes();
	
	public void addParameterType(ParameterType pt);
	
	/**
	 * Adds a new resource type
	 * 
	 * @param rt			The type object containing ID, human-readable name, 
	 * 						description, and class
	 */
	public void addResourceType(ResourceType rt);
}
