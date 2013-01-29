// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;

import java.util.Iterator;
import java.util.List;

import ca.gc.nrc.iit.savoir.model.session.Action;
import ca.gc.nrc.iit.savoir.model.session.Parameter;
import ca.gc.nrc.iit.savoir.model.session.Service;

/**
 * Sends a message to a resource.
 * 
 * @author Aaron Moss
 */
public class UpdateConsequence extends Consequence {

	/**	The service this action updates */
	private Service service;
	/** The action this update performs */
	private Action action;
	/** The parameters for this action (if applicable) */
	private List<Parameter> params;
	
	public UpdateConsequence() {}
	
	public UpdateConsequence(Service service, Action action, 
			List<Parameter> params) {
		this.service = service;
		this.action = action;
		this.params = params;
	}

	public Service getService() {
		return service;
	}

	public Action getAction() {
		return action;
	}

	public List<Parameter> getParams() {
		return params;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public void setParams(List<Parameter> params) {
		this.params = params;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		//mgmtProxy.controlDevice(sessionId, "<serviceId>", "<activityId>", 
		//		"<action>", <params>, userId);
		sb.append("mgmtProxy.controlDevice(sessionId, \"")
			.append(service.getId()).append("\", \"")
			.append(service.getActivityId()).append("\", \"")
			.append(action.toString()).append("\", ");
		//generate parameters
		if (null == params) {
			sb.append("null");
		} else {
			//Arrays.asList(new Parameter[]{[[<param>]{,<param>}]})
			sb.append("Arrays.asList(new Parameter[]{");
			
			Iterator<Parameter> iter = params.iterator();
			//check for first parameter
			if (iter.hasNext()) {
				Parameter p = iter.next();	//first parameter
				//ensure non-null
				while(null == p && iter.hasNext()) p = iter.next();
				if (null != p) {
					//there exists a parameter
					sb.append(paramGen(p));
					
					//get the rest of the parameters
					while (iter.hasNext()) {
						p = iter.next();	//next parameter
						//ensure non-null
						while(null == p && iter.hasNext()) p = iter.next();
						if (null != p) {
							//there exists another parameter
							sb.append(",").append(paramGen(p));
						}
					}
				}
			}
			
			sb.append("})");
		}
		sb.append(", userId);\n");
		
		return sb.toString();
	}
	
	private String paramGen(Parameter p) {
		StringBuilder sb = new StringBuilder();
		
		//new Parameter().withId("<id>").withValue(<value>)
		sb.append("new Parameter().withId(\"").append(p.getId()).append("\")")
				.append(".withValue(").append(p.getValue()).append(")");
		
		return sb.toString();
	}
}
