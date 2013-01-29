// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Java representation of a service element of a SAVOIR message 
 */
public class Service implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/** id attribute of service */
	private String id;
	/** name attribute of service */
	private String name;
	/** activityID attribute of service - identifies activity */
	private String activityId;
	/** activityName attribute of service */
	private String activityName;
	/** user accessing service */
	private String serviceUserId;
	/** user's password for service */
	private String servicePassword;
	/** path to launch on user's system */
	private String path;
	/** all parameter elements of activity */
	private Map<String, Parameter> parameters;
	/** all notification elements of service */
	private Notification notification;
	
	public Service() {
	}
	
	//Java bean API for Service construction		
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public String getActivityId() {
		return activityId;
	}
	
	public String getActivityName() {
		return activityName;
	}
	
	public String getServiceUserId() {
		return serviceUserId;
	}
	
	public String getServicePassword() {
		return servicePassword;
	}
	
	public String getPath() {
		return path;
	}

	public List<Parameter> getParameters() {
		if (parameters == null) return null;
		return new ArrayList<Parameter>(parameters.values());
	}
	
	public Parameter getParameter(String id) {
		if (this.parameters == null) {
			return null;
		}
		return this.parameters.get(id);
	}
	
	public Parameter removeParameter(String id) {
		if (this.parameters == null) return null;
		return this.parameters.remove(id);
	}

	public Notification getNotification() {
		return notification;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public void setServiceUserId(String serviceUserId) {
		this.serviceUserId = serviceUserId;
	}

	public void setServicePassword(String servicePassword) {
		this.servicePassword = servicePassword;
	}

	public void setPath(String path) {
		this.path = path;
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
	
	public void setNotification(Notification notification) {
		this.notification = notification;
	}

	//Fluent API for Service construction
	public Service withId(String id) {
		this.id = id;
		return this;
	}
	
	public Service withName(String name) {
		this.name = name;
		return this;
	}

	public Service withActivityId(String activityId) {
		this.activityId = activityId;
		return this;
	}

	public Service withActivityName(String activityName) {
		this.activityName = activityName;
		return this;
	}

	public Service withServiceUserId(String serviceUserId) {
		this.serviceUserId = serviceUserId;
		return this;
	}

	public Service withServicePassword(String servicePassword) {
		this.servicePassword = servicePassword;
		return this;
	}

	public Service withPath(String path) {
		this.path = path;
		return this;
	}
	
	public Service addParameter(Parameter parameter) {
		if (this.parameters == null) {
			this.parameters = new LinkedHashMap<String, Parameter>();
		}
		this.parameters.put(parameter.getId(), parameter);
		parameter.setService(this);
		return this;
	}
	
	public Service withNotification(Notification notification) {
		this.notification = notification;
		this.notification.setService(this);
		return this;
	}
}
