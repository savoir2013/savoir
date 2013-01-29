// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ca.gc.nrc.iit.savoir.model.SavoirXml;
import ca.gc.nrc.iit.savoir.model.session.Parameter;

/**
 * Represents an XML profile message, as defined in the SAVOIR message spec
 * 
 * @author Aaron Moss
 */
public class ServiceProfile extends SavoirXml {

	/** Service ID */
	private String id;
	/** Human-readable name for service (optional) */
	private String name;
	/** Long-format human-readable description of service */
	private String description;
	/** Time this profile message was sent */
	private Date timestamp;
	/** Widget details */
	private Widget widget;
	/** Activities defined on this service, indexed by name */
	private Map<String, Activity> activities;
	/** Global parameters on this service */
	private List<Parameter> globals;

	
	public ServiceProfile() {}
	
	
	//Java bean API for ServiceProfile
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Date getTimestamp() {
		return timestamp;
	}
	
	public Widget getWidget() {
		return widget;
	}
	
	public List<Activity> getActivities() {
		if (activities == null) return null;
		return new ArrayList<Activity>(activities.values());
	}
	
	public Activity getActivity(String id) {
		if (this.activities == null) return null;
		return this.activities.get(id);
	}
	
	public Activity removeActivity(String id) {
		if (this.activities == null) return null;
		return this.activities.remove(id);
	}
	
	public List<Parameter> getGlobalParameters() {
		return globals;
	}
	

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setWidget(Widget widget) {
		this.widget = widget;
	}
	
	public void setActivities(Collection<Activity> activities) {
		if (activities == null) {
			this.activities = null;
		} else {
			this.activities = new LinkedHashMap<String, Activity>();
			for (Activity activity : activities) {
				this.activities.put(activity.getId(), activity);
			}
		}
	}
	
	public void setGlobalParameters(Collection<Parameter> parameters) {
		//set parameters
		if (parameters == null) {
			globals = null;
		} else {
			globals = new ArrayList<Parameter>(parameters);
		}
		//set globals for all activities
		if (activities != null) for (Activity a : activities.values()) {
			a.setGlobalParameters(globals);
		}
	}

	//Fluent API for ServiceProfile
	public ServiceProfile withId(String id) {
		this.id = id;
		return this;
	}

	public ServiceProfile withName(String name) {
		this.name = name;
		return this;
	}

	public ServiceProfile withDescription(String description) {
		this.description = description;
		return this;
	}

	public ServiceProfile withTimestamp(Date timestamp) {
		this.timestamp = timestamp;
		return this;
	}
	
	public ServiceProfile withWidget(Widget widget) {
		this.widget = widget;
		return this;
	}
	
	public ServiceProfile addActivity(Activity activity) {
		if (this.activities == null) {
			this.activities = new LinkedHashMap<String, Activity>();
		}
		this.activities.put(activity.getId(), activity);
		return this;
	}
}
