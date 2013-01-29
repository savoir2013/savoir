// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao;

import java.util.List;

import ca.gc.iit.nrc.savoir.domain.ResourceParameter;

public interface IParametersDAO {
	
	/**
	 * Adds a list of parameters to a resource.
	 * 
	 * @param resourceID	The ID of the resource to set the parameters for
	 * @param params		The parameters to set	
	 * @param sessionID		The ID of the session the parameters apply on 
	 * 						({@code 0} for default parameters)
	 */
	public void saveParameters(int resourceID, List<ResourceParameter> params, int sessionID );
	
	/**
	 * Clears the list of parameters on a resource.
	 * 
	 * @param resourceID	The ID of the resource to clear the parameters for
	 * @param sessionID		The ID of the session to clear parameters on 
	 * 						({@code 0} for default parameters, {@code -1} for 
	 * 						all sessions)
	 */
	public void clearParameters(int resourceID, int sessionID);

	public List<ResourceParameter> getDefaultParametersByResourceID(int resourceId);
	
	public List<ResourceParameter> getParametersByResourceIDAndSessionID(int resourceId, int sessionID);
	
	public String getDefaultValueByResourceAndParameter(int resourceID, String parameterID);
}
