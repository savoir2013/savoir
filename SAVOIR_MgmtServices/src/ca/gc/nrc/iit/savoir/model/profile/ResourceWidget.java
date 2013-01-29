// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.profile;

import ca.gc.iit.nrc.savoir.domain.ResourcePreference;

/**
 * Contains a widget, annotated with information necessary to make a widget out 
 * of it.
 * 
 * @author Aaron Moss
 */
public class ResourceWidget {

	/** ID of the resource */
	private int resourceId;
	/** Name of the resource */
	private String resourceName;
	/** Preferred UI state */
	private ResourcePreference state;
	/** Widget to display */
	private Widget widget;
	
	
	public ResourceWidget() {}


	//Java bean API
	public int getResourceId() {
		return resourceId;
	}

	public String getResourceName() {
		return resourceName;
	}

	public ResourcePreference getState() {
		return state;
	}

	public Widget getWidget() {
		return widget;
	}


	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public void setState(ResourcePreference state) {
		this.state = state;
	}

	public void setWidget(Widget widget) {
		this.widget = widget;
	}
	
	
	//Fluent API
	public ResourceWidget withResourceId(int resourceId) {
		this.resourceId = resourceId;
		return this;
	}

	public ResourceWidget withResourceName(String resourceName) {
		this.resourceName = resourceName;
		return this;
	}

	public ResourceWidget withState(ResourcePreference state) {
		this.state = state;
		return this;
	}

	public ResourceWidget withWidget(Widget widget) {
		this.widget = widget;
		return this;
	}
}
