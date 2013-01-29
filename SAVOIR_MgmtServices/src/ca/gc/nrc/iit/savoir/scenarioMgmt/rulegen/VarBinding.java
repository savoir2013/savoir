// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;

import ca.gc.nrc.iit.savoir.model.session.Parameter;
import ca.gc.nrc.iit.savoir.model.session.Service;

/**
 * Drools parameter binding for a scenario variable
 * 
 * @author Aaron Moss
 */
public class VarBinding {

	/** Name of this variable */
	private String name;
	/** Service to bind the variable on */
	private Service service;
	/** ID of parameter on service to bind to */
	private String parameterId;
	/** Runtime type of parameter */
	private Class<? extends Parameter> type;
	
	
	public VarBinding() {}
	
	public VarBinding(String name, Service serv, String paramId, 
			Class<? extends Parameter> type) {
		this.name = name;
		this.service = serv;
		this.parameterId = paramId;
		this.type = type;
	}

	
	public String getName() {
		return name;
	}

	public Service getService() {
		return service;
	}

	public String getParameterId() {
		return parameterId;
	}
	
	public Class<? extends Parameter> getType() {
		return type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public void setParameterId(String parameterId) {
		this.parameterId = parameterId;
	}
	
	public void setType(Class<? extends Parameter> type) {
		this.type = type;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VarBinding) {
			VarBinding vb = (VarBinding)obj;
			
			return (null == name) ?
					null == vb.name
					: name.equals(vb.name);
		} else return false;
	}

	@Override
	public int hashCode() {
		return (null == name) ? 0 : name.hashCode();
	}

	@Override
	public String toString() {
		if (null == name || null == service || null == parameterId)
			return null;
		if (type == null) type = Parameter.class;
		
		StringBuilder sb = new StringBuilder();
		String serviceVar = ServiceBinding.serviceVarName(service);
		
		//variable binding
		if (type == Parameter.class) {
			//string variable

			//Parameter(service == <serviceVar>, id == "<parameterId>", 
			//			value != null, <name> : value)
			sb.append("\t\tParameter(service == ").append(serviceVar)
				.append(", id == \"").append(parameterId)
				.append("\", value != null, ").append(name)
				.append(" : value)\n");			
		} else if (ca.gc.nrc.iit.savoir.model.session.IntParameter.class == type) {
			//integer variable
			
			//IntParameter(service == <serviceVar>, id == "<parameterId>", 
			//			intValue != null, <name> : intValue)
			sb.append("\t\tIntParameter(service == ").append(serviceVar)
				.append(", id == \"").append(parameterId)
				.append("\", intValue != null, ").append(name)
				.append(" : intValue)\n");
		
		} else if (ca.gc.nrc.iit.savoir.model.session.FloatParameter.class == type) { 
			//floating-point variable
			
			//FloatParameter(service == <serviceVar>, id == "<id>", 
			//			floatValue != null, <name> : floatValue)
			sb.append("\t\tFloatParameter(service == ").append(serviceVar)
				.append(", id == \"").append(parameterId)
				.append("\", floatValue != null, ").append(name)
				.append(" : floatValue)\n");
		
		} else if (ca.gc.nrc.iit.savoir.model.session.BoolParameter.class == type) { 
			//boolean variable
			
			//BoolParameter(service == <serviceVar>, id == "<id>", 
			//			boolValue != null, <name> : boolValue)
			sb.append("\t\tBoolParameter(service == ").append(serviceVar)
				.append(", id == \"").append(parameterId)
				.append("\", boolValue != null, ").append(name)
				.append(" : boolValue)\n");
		
		} else if (ca.gc.nrc.iit.savoir.model.session.DateParameter.class == type) {
			//temporal variable
			
			//DateParameter(service == <serviceVar>, id == "<id>", 
			//			dateValue != null, <name> : dateValue)
			sb.append("\t\tDateParameter(service == ").append(serviceVar)
				.append(", id == \"").append(parameterId)
				.append("\", dateValue != null, ").append(name)
				.append(" : dateValue)\n");
		
		} else {
			//assume string variable
			
			//Parameter(service == <serviceVar>, id == "<parameterId>", 
			//			value != null, <name> : value)
			sb.append("\t\tParameter(service == ").append(serviceVar)
				.append(", id == \"").append(parameterId)
				.append("\", value != null, ").append(name)
				.append(" : value)\n");
		}
		
		return sb.toString();
	}
}
