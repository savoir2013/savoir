// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;

import java.util.regex.Pattern;

import ca.gc.nrc.iit.savoir.model.session.Service;

/**
 * Drools parameter binding for a Service.
 * 
 * @author Aaron Moss
 */
public class ServiceBinding {

	/** service to bind to */
	private Service service;
	
	
	public ServiceBinding() {}
	
	public ServiceBinding(Service service) {
		this.service = service;
	}
	
	
	public Service getService() {
		return service;
	}
	
	public void setService(Service service) {
		this.service = service;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ServiceBinding) {
			ServiceBinding sb = (ServiceBinding)obj;
			
			return (null == service) ? 
					null == sb.service  
					: service.equals(sb.service);
		} else return false;
	}

	@Override
	public int hashCode() {
		return (null == service) ? 0 : service.hashCode();
	}

	@Override
	public String toString() {
		if (null == service) return "";
		
		StringBuilder sb = new StringBuilder();
		
		//service selector
		//<serviceVarName> : Service(id == "<id>", activityId == "<activityId>")
		sb.append("\t\t").append(serviceVarName()).append(" : Service(id == \"")
				.append(service.getId()).append("\", activityId == \"")
				.append(service.getActivityId()).append("\")\n");
		
		return sb.toString();
	}
	
	/**
	 * @return the name of the variable this service is bound to,
	 * 			null for service unset
	 */
	public String serviceVarName() {
		return serviceVarName(service);
	}
	
	/* Characters to be stripped in variable names */
	private static Pattern unsafeChars = Pattern.compile("[^0-9A-Za-z]");
	
	/**
	 * Gets the variable name for a Service object, derived from the service ID 
	 * and activity ID, normalized if neccessary.
	 * @param service	The service to consider
	 * @return the name of the variable this service is bound to,
	 * 			null null service
	 */
	public static String serviceVarName(Service service) {
		if (null == service) return null;
		
		String serviceId = 
			(null == service.getId()) ? 
					"" : 
					unsafeChars.matcher(service.getId()).replaceAll("");
		String activityId = 
			(null == service.getActivityId()) ? 
					"" :
					unsafeChars.matcher(service.getActivityId()).replaceAll("");
		return "s_" + serviceId + "_" + activityId;
	}
}
