// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : SAVOIR
//  @ File Name : SAVOIR_Resource.java
//  @ Date : 21/10/2008
//  @ Author : Yosri Harzallah
//
//

package ca.gc.iit.nrc.savoir.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlType;

import ca.gc.iit.nrc.savoir.domain.types.ResourceType;

@XmlType(name="Resource")
public class Resource {
	
	/** ID of the resource**/
	private int resourceID;	
	/** Resource name */
	private String resourceName;
	/** Description of the resource**/
	private String description;	
	/** Type of the resource**/
	private ResourceType resourceType;
	/** Contact person for this resource (optional) */
	private Person contact;
	/** Session-specific parameters and their values**/
	private Map<String, ResourceParameter> parameters;
	/** Default parameters and their values */
	private Map<String, ResourceParameter> defaultParameters;
	/** Time slots where the resource is unavailable**/
	private List<TimeSlot> usedTimeSlots;
	/** constraints linked to the resource **/
	private List<Constraint> constraints;
	/** Endpoint of this resource */
	private String endpoint = null;
	
	
	public Resource(){
	}
	
	
	public int getResourceID() {
		return resourceID;
	}
	
	public String getResourceName() {
		return resourceName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public ResourceType getResourceType() {
		return resourceType;
	}
	
	public Person getContact() {
		return contact;
	}
	
	public List<ResourceParameter> getParameters() {
		if (parameters != null) 
			return new ArrayList<ResourceParameter>(parameters.values());
		else return null;
	}
	
	public List<ResourceParameter> getDefaultParameters() {
		if (defaultParameters != null)
			return new ArrayList<ResourceParameter>(defaultParameters.values());
		else return null;
	}
	
	public String getParameterValue(String param){
		ResourceParameter p = null;
		if (parameters == null) {
			if (defaultParameters == null) return null;
			else p = defaultParameters.get(param);
		} else {
			p = parameters.get(param);
			if (p == null && defaultParameters != null) 
				p = defaultParameters.get(param); 
		}
		return (p == null) ? null : p.getValue();
	}
	
	public List<TimeSlot> getUsedTimeSlots() {
		return usedTimeSlots;
	}
	
	public List<Constraint> getConstraints() {
		return constraints;
	}
	
	/**
	 * Dynamically generated from resource parameters.
	 * If an "ENDPOINT" parameter is present, will be used.
	 * Otherwise, will be generated based on "PROTOCOL" parameter, if present.
	 * If PROTOCOL = TCP, returns "tcp://<SERVICE_IP_ADDRESS>:<SERVICE_PORT_NUMBER>".
	 * If PROTOCOL = JMS, returns "jms://topic:<SERVICE_TOPIC>"
	 * 
	 * @return URI that this resource can be reached at.
	 */
	public String getEndpoint() {
		if (endpoint == null) {
			//generate endpoint from parameters if needed
			endpoint = getParameterValue("SERVICE_URI");
			
			if (endpoint == null) {
				String proto = getParameterValue("PROTOCOL");
				if ("TCP".equals(proto)) {
					String ip = getParameterValue("SERVICE_IP_ADDRESS"),
						port = getParameterValue("SERVICE_PORT_NUMBER");
					if (port == null) port = "80";
					
					if (ip != null && port != null) {
						endpoint = "tcp://" + ip + ":" + port;
					}
				} else if ("JMS".equals(proto)) {
					String topic = getParameterValue("SERVICE_TOPIC");
					
					if (topic != null) {
						endpoint = "jms://topic:" + topic;
					}
				}
			}
		}
		
		return endpoint;
	}
	
	
	public void setResourceID(int resourceID) {
		this.resourceID = resourceID;
	}
	
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}
	
	public void setContact(Person contact) {
		this.contact = contact;
	}
	
	public void setParameters(List<ResourceParameter> parameters) {
		if (parameters != null) {
			this.parameters = new HashMap<String, ResourceParameter>();
			for (ResourceParameter parameter : parameters) {
				this.parameters.put(
						parameter.getParameter().getId(), parameter);
			}
		} else {
			this.parameters = null;
		}
	}
	
	public void setDefaultParameters(List<ResourceParameter> parameters) {
		if (parameters != null) {
			this.defaultParameters = new HashMap<String, ResourceParameter>();
			for (ResourceParameter parameter : parameters) {
				this.defaultParameters.put(
						parameter.getParameter().getId(), parameter);
			}
		} else {
			this.defaultParameters = null;
		}
	}
	
	public void setUsedTimeSlots(List<TimeSlot> usedTimeSlots) {
		this.usedTimeSlots = usedTimeSlots;
	}
	
	public void setConstraints(List<Constraint> constraints) {
		this.constraints = constraints;
	}
}
