// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.session;

/**
 * Java representation of an activityParameter element of a SAVOIR message.
 * {@code Parameter} has various typed subclasses, for the purpose of accurate 
 * comparisons in the rule engine (as an example of why string comparison is 
 * not always sufficient, {@code "042" != "42"} when compared as strings, but 
 * {@code 042 == 42} when compared as integers. For serialization and 
 * deserialization, these subclasses are not required to be used, and have been 
 * coded to behave exactly like {@code Parameter} (though they do provide 
 * possibly useful datatype-specific setters for {@code value}). For purposes 
 * of deciding correct datatype (such as is used in the scenario-running rule 
 * engine), the typed subclasses should be used.
 * 
 * @see ParameterFactory
 */
public class Parameter implements Cloneable {
	
	/** associated service */
	private Service service;
	
	/** id attribute of parameter */
	private String id;
	/** value attribute of parameter */
	protected String value;
	/** name attribute of parameter */
	private String name;
	/** dataType of this parameter */
	protected String dataType;
	
	public Parameter() {
		this.dataType = "xs:string";
	}
	
	/**
	 * Constructor that also sets {@code service}
	 * @param service	The new value for the service
	 */
	public Parameter(Service service) {
		this();
		this.setService(service);
	}
	

	//Java bean API for Parameter construction
	public Service getService() {
		return service;
	}
	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
	
	public String getDataType() {
		return dataType;
	}
	

	public void setService(Service service) {
		if (this.service == service) {
			//break infinite loop
			return;
		}
		
		if (this.service != null) {
			this.service.removeParameter(this.id);
		}
		this.service = service;
		if (service != null && service.getParameter(this.id) == null) {
			service.addParameter(this);
		}
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}	
	
	//Fluent API for Parameter construction
	public Parameter withId(String id) {
		this.id = id;
		return this;
	}

	public Parameter withName(String name) {
		this.name = name;
		return this;
	}

	public Parameter withValue(String value) {
		setValue(value);
		return this;
	}
	
	public Parameter clone() {
		Parameter p = new Parameter();
		p.id = id;
		p.value = value;
		p.name = name;
		p.dataType = dataType;
		
		return p;
	}
}
