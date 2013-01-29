// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.gc.nrc.iit.savoir.model.session.BoolParameter;
import ca.gc.nrc.iit.savoir.model.session.DateParameter;
import ca.gc.nrc.iit.savoir.model.session.Parameter;
import ca.gc.nrc.iit.savoir.model.session.Service;
import ca.gc.nrc.iit.savoir.scenarioMgmt.ScenarioMgrImpl;

/**
 * Represents the "when" condition of a rule.
 * Triggers when a given parameter on a given service compares to a given value 
 * in a certain way.
 * 
 * @author Aaron Moss
 */
public class WhenCondition extends Condition {

	/** The service this parameter tests for */
	private Service service;
	/** The parameter on the service that is of interest 
	 * (also includes the value to compare to, properly typed) */
	private Parameter param;
	/** The operator we are using to compare service.parameter to value */
	private Comparison comp;	
	
	public WhenCondition() {};
	
	public WhenCondition(Service service, Parameter param, Comparison comp) {
		this.service = service;
		this.param = param;
		this.comp = comp;
	}
	
	
	public Service getService() {
		return service;
	}

	public Parameter getParam() {
		return param;
	}
	
	public String getParamId() {
		return null == param ? null : param.getId();
	}

	public Comparison getComp() {
		return comp;
	}

	public String getValue() {
		return null == param ? null : param.getValue();
	}

	public void setService(Service service) {
		this.service = service;
	}

	public void setParam(Parameter param) {
		this.param = param;
	}

	public void setComp(Comparison comp) {
		this.comp = comp;
	}
	
	
	public List<Service> requiredServices() {
		List<Service> req = new ArrayList<Service>(1);
		if (null != service) req.add(service);
		return req;
	}
	
	private static DateFormat ruleDateFormat = 
		new SimpleDateFormat(ScenarioMgrImpl.DROOLS_DATE_FORMAT); 

	public String toString() {
		if (null == param) return null;

		StringBuilder sb = new StringBuilder();
		
		String serviceVar = ServiceBinding.serviceVarName(service);
		
		if (ca.gc.nrc.iit.savoir.model.session.Parameter.class == param.getClass()) {
			//string format parameter
			
			//Parameter(service == <serviceVar>, id == "<id>", 
			//	value <comp> "<value>")
			sb.append("\t\tParameter(service == ").append(serviceVar)
				.append(", id == \"").append(param.getId()).append("\", value ")
				.append(comp.toString()).append(" \"").append(param.getValue())
				.append("\")\n");
		
		} else if (ca.gc.nrc.iit.savoir.model.session.IntParameter.class 
				== param.getClass()) {
			//integer format parameter
			
			//IntParameter(service == <serviceVar>, id == "<id>", 
			//	intValue <comp> <value>)
			sb.append("\t\tIntParameter(service == ").append(serviceVar)
					.append(", id == \"").append(param.getId())
					.append("\", intValue ").append(comp.toString()).append(" ")
					.append(param.getValue()).append(")\n");
		
		} else if (ca.gc.nrc.iit.savoir.model.session.FloatParameter.class 
				== param.getClass()) {
			//floating-point format parameter
			
			//FloatParameter(service == <serviceVar>, id == "<id>", 
			//	floatValue <comp> <value>)
			sb.append("\t\tFloatParameter(service == ").append(serviceVar)
					.append(", id == \"").append(param.getId())
					.append("\", floatValue ").append(comp.toString())
					.append(" ").append(param.getValue()).append(")\n");
		
		} else if (ca.gc.nrc.iit.savoir.model.session.BoolParameter.class 
				== param.getClass()) {
			//boolean format parameter
			Boolean boolValue = ((BoolParameter)param).getBoolValue();
			if (boolValue == null) return null;
			String value = boolValue ? "true" : "false";
			
			//BoolParameter(service == <serviceVar>, id == "<id>", 
			//	boolValue <comp> <value>)
			sb.append("\t\tBoolParameter(service == ").append(serviceVar)
			.append(", id == \"").append(param.getId()).append("\", boolValue ")
			.append(comp.toString()).append(" ").append(value).append(")\n");
		
		} else if (ca.gc.nrc.iit.savoir.model.session.DateParameter.class
				 == param.getClass()) {
			//date/time format parameter
			Date dateValue = ((DateParameter)param).getDateValue();
			if (dateValue == null) return null;
			String value = ruleDateFormat.format(dateValue);			
			
			//DateParameter(service == <serviceVar>, id == "<id>", 
			//	dateValue <comp> "<value>")
			sb.append("\t\tDateParameter(service == ").append(serviceVar)
			.append(", id == \"").append(param.getId()).append("\", dateValue ")
			.append(comp.toString()).append(" \"").append(value)
			.append("\")\n");
		
		} else {
			//assume string format
			
			//Parameter(service == <serviceVar>, id == "<id>", 
			//	value <comp> "<value>")
			sb.append("\t\tParameter(service == ").append(serviceVar)
				.append(", id == \"").append(param.getId()).append("\", value ")
				.append(comp.toString()).append(" \"").append(param.getValue())
				.append("\")\n");
		}
		
		return sb.toString();
	}
	
}
