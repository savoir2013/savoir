// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.resourceMgmt;

/**
 * Represents the state of a specific instance of an ED
 * 
 * @author Aaron Moss
 */
public class InstanceState {

	/** Reference to the instance this state is of */
	private InstanceId id;
	/** State of the ED instance */
	private ResourceState state;
	
	
	public InstanceState() {}
	
	public InstanceState(InstanceId id) {
		this(id, ResourceState.INACTIVE);
	}
	
	public InstanceState(InstanceId id, ResourceState state) {
		this.id = id;
		this.state = state;
	}
	

	public InstanceId getId() {
		return id;
	}

	public ResourceState getState() {
		return state;
	}

	public void setId(InstanceId id) {
		this.id = id;
	}

	public void setState(ResourceState state) {
		this.state = state;
	}
	
}
