// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.resourceMgmt;

/**
 * Uniquely identifies an edge-service instance
 * 
 * @author Aaron Moss
 */
public class InstanceId {

	/* Session this instance is being run for - implicit in context */
//	private int sessionId;
	/** Resource ID of this edge service */
	private int resourceId;
	/** Running activity ID */
	private String activityId;
	
	
	public InstanceId() {}
	
	public InstanceId(/*int sessionId, */int resourceId, String activityId) {
//		this.sessionId = sessionId;
		this.resourceId = resourceId;
		this.activityId = activityId;
	}

	
//	public int getSessionId() {
//		return sessionId;
//	}
//
	public int getResourceId() {
		return resourceId;
	}

	public String getActivityId() {
		return activityId;
	}

//	public void setSessionId(int sessionId) {
//		this.sessionId = sessionId;
//	}
//
	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}
	
	
	@Override
	/**
	 * Checks equality of all fields on obj (assuming obj is a InstanceId)
	 */
	public boolean equals(Object obj) {
		if (InstanceId.class.equals(obj.getClass())) {
			InstanceId that = (InstanceId)obj;
			return 
				/*(this.sessionId == that.sessionId) 
				&& */(this.resourceId == that.resourceId) 
				&& (this.activityId == null ?
						that.activityId == null 
						: this.activityId.equals(that.activityId));
		} else return false;
	}

	@Override
	/**
	 * XOR's hashes of all fields
	 */
	public int hashCode() {
		return 
			/*(sessionId)
			^ */(resourceId/* << 16 | resourceId >>> 16*/) 
			^ (activityId == null ? 0 : activityId.hashCode()) ; 
	}
}
