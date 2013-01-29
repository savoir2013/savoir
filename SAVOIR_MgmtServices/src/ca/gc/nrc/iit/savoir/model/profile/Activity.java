// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ca.gc.nrc.iit.savoir.model.session.Parameter;

/**
 * Represents the activity element of a SAVOIR service profile message.
 * 
 * @author Aaron Moss
 */
public class Activity {

	/** Service-unique activity ID */
	private String id;
	/** Human-readable activity name */
	private String name;
	/** Parameter ID if used in a widget choice-box */
	private String paramId;
	/** Parameter value if used in a widget choice-box */
	private String paramValue;
	/** parameters of this activity */
	private Map<String, Parameter> parameters;
	/** global parameters on this activity's service */
	private Map<String, Parameter> globals;
	
	public Activity() {}


	//Java bean API for Activity
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getParamId() {
		return paramId;
	}
	
	public String getParamValue() {
		return paramValue;
	}
	
	public List<Parameter> getParameters() {
		if (parameters == null) return null;
		return new ArrayList<Parameter>(parameters.values());
	}
	
	public List<Parameter> getGlobalParameters() {
		if (globals == null) return null;
		return new ArrayList<Parameter>(globals.values());
	}
	
	/**
	 * Gets all parameters
	 * @return a list of all parameters (activity-specific and service-global)
	 */
	public List<Parameter> getAllParameters() {
		List<Parameter> ps = new ArrayList<Parameter>();
		if (parameters != null) ps.addAll(parameters.values());
		if (globals != null) ps.addAll(globals.values());
		return ps;
	}
	
	/**
	 * Gets a parameter by its ID
	 * @param id	The parameter's ID
	 * @return	The parameter having that ID, first checking the local 
	 * 	parameter list then the global list
	 */
	public Parameter getParameter(String id) {
		Parameter p = null;
		if (parameters != null) p = parameters.get(id);
		if (p == null && globals != null) p = globals.get(id);
		return p;
	}
	
	/**
	 * Removes a parameter by its ID
	 * @param id	The parameter's ID
	 * @return	The parameter that had that ID, first attempting to remove it 
	 * 	from the local list, and, if that fails, from the global list
	 */
	public Parameter removeParameter(String id) {
		Parameter p = null;
		if (this.parameters != null) p = parameters.remove(id);
		if (p == null && globals != null) p = globals.remove(id);
		return p;
	}

	
	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setParamId(String paramId) {
		this.paramId = paramId;
	}
	
	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}
	
	public void setParameters(Collection<Parameter> parameters) {
		if (parameters == null) {
			this.parameters = null;
		} else {
			this.parameters = new LinkedHashMap<String, Parameter>();
			for (Parameter parameter : parameters) {
				this.parameters.put(parameter.getId(), parameter);
			}
		}
	}
	
	public void setGlobalParameters(Collection<Parameter> parameters) {
		if (parameters == null) {
			this.globals = null;
		} else {
			this.globals = new LinkedHashMap<String, Parameter>();
			for (Parameter parameter : parameters) {
				this.globals.put(parameter.getId(), parameter);
			}
		}
	}
	
	//Fluent API for Activity
	public Activity withId(String id) {
		this.id = id;
		return this;
	}

	public Activity withName(String name) {
		this.name = name;
		return this;
	}
	
	public Activity withParamId(String paramId) {
		this.paramId = paramId;
		return this;
	}
	
	public Activity withParamValue(String paramValue) {
		this.paramValue = paramValue;
		return this;
	}
	
	public Activity addParameter(Parameter parameter) {
		if (this.parameters == null) {
			this.parameters = new LinkedHashMap<String, Parameter>();
		}
		this.parameters.put(parameter.getId(), parameter);
		return this;
	}
	
	public Activity addGlobalParameter(Parameter parameter) {
		if (this.globals == null) {
			this.parameters = new LinkedHashMap<String, Parameter>();
		}
		this.globals.put(parameter.getId(), parameter);
		return this;
	}
}
