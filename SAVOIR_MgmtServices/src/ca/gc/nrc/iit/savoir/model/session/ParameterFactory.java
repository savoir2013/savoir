// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.session;

import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Factory class to build properly-typed Parameter objects from XSD type or 
 * inferred from parameter value.
 * 
 * @author Aaron Moss
 */
public class ParameterFactory {

	/** Map of XML Schema types to Parameter subclasses 
	 * (no mapping implies String format, the superclass) */
	protected static Map<String, Class<? extends Parameter>> typeClasses;
	static {
		typeClasses = new HashMap<String, Class<? extends Parameter>>();
		
		//boolean types
		typeClasses.put(
				"boolean", ca.gc.nrc.iit.savoir.model.session.BoolParameter.class);
		
		//floating-point types
		typeClasses.put(
				"decimal", ca.gc.nrc.iit.savoir.model.session.FloatParameter.class);
		typeClasses.put(
				"float", ca.gc.nrc.iit.savoir.model.session.FloatParameter.class);
		typeClasses.put(
				"double", ca.gc.nrc.iit.savoir.model.session.FloatParameter.class);
		
		//date types
		typeClasses.put(
				"duration", ca.gc.nrc.iit.savoir.model.session.DateParameter.class);
		typeClasses.put(
				"dateTime", ca.gc.nrc.iit.savoir.model.session.DateParameter.class);
		typeClasses.put(
				"time", ca.gc.nrc.iit.savoir.model.session.DateParameter.class);
		typeClasses.put(
				"date", ca.gc.nrc.iit.savoir.model.session.DateParameter.class);
		typeClasses.put(
				"gYearMonth", ca.gc.nrc.iit.savoir.model.session.DateParameter.class);
		typeClasses.put(
				"gYear", ca.gc.nrc.iit.savoir.model.session.DateParameter.class);
		typeClasses.put(
				"gMonthDay", ca.gc.nrc.iit.savoir.model.session.DateParameter.class);
		typeClasses.put(
				"gDay", ca.gc.nrc.iit.savoir.model.session.DateParameter.class);
		typeClasses.put(
				"gMonth", ca.gc.nrc.iit.savoir.model.session.DateParameter.class);
		
		//integer types
		typeClasses.put(
				"integer", ca.gc.nrc.iit.savoir.model.session.IntParameter.class);
		typeClasses.put(
				"nonPositiveInteger", 
				ca.gc.nrc.iit.savoir.model.session.IntParameter.class);
		typeClasses.put(
				"negativeInteger", 
				ca.gc.nrc.iit.savoir.model.session.IntParameter.class);
		typeClasses.put(
				"long", ca.gc.nrc.iit.savoir.model.session.IntParameter.class);
		typeClasses.put(
				"int", ca.gc.nrc.iit.savoir.model.session.IntParameter.class);
		typeClasses.put(
				"short", ca.gc.nrc.iit.savoir.model.session.IntParameter.class);
		typeClasses.put(
				"byte", ca.gc.nrc.iit.savoir.model.session.IntParameter.class);
		typeClasses.put(
				"nonNegativeInteger", 
				ca.gc.nrc.iit.savoir.model.session.IntParameter.class);
		typeClasses.put(
				"unsignedLong", ca.gc.nrc.iit.savoir.model.session.IntParameter.class);
		typeClasses.put(
				"unsignedInt", ca.gc.nrc.iit.savoir.model.session.IntParameter.class);
		typeClasses.put(
				"unsignedShort", ca.gc.nrc.iit.savoir.model.session.IntParameter.class);
		typeClasses.put(
				"unsignedByte", ca.gc.nrc.iit.savoir.model.session.IntParameter.class);
		typeClasses.put(
				"positiveInteger", 
				ca.gc.nrc.iit.savoir.model.session.IntParameter.class);
	}
	
	/**
	 * Factory method for Parameter objects (and subclasses)
	 * @param dataType	XML dataType of parameter
	 * @return a new parameter with the specified dataType, and of an 
	 * 	appropriate runtime class
	 */
	public static Parameter newParameter(String dataType) {
		if (dataType == null) return new Parameter();
		
		//strip namespace declaration
		String lookupType = dataType.substring(dataType.indexOf(':') + 1);
		
		//lookup runtime class
		Class<? extends Parameter> clazz = typeClasses.get(lookupType);
		Parameter p = null;
		if (clazz == null) {
			//unknown type, or known string type
			p = new Parameter();
		} else {
			//attempt to instantiate instance of typed parameter, fallback to 
			// superclass
			try {
				p = clazz.newInstance();
			} catch (Exception e) {
				//shouldn't happen
				p = new Parameter();
			}
		}
		
		//set the parameter's datatype, and return
		p.dataType = dataType;
		return p;
	}
	
	/**
	 * Attempts to infer parameter type from value, by trying to parse the 
	 * given value as each of the parameter subtypes.
	 * This method is not recommended for use where alternatives exist, as 
	 * it may incorrectly infer parameters, and involves somewhat expensive 
	 * attempts to parse the value as all known types.
	 * 
	 * @param value		The value to infer parameter type from
	 * @return A Parameter of the inferred runtime type, having the given value 
	 * 			as its value.
	 */
	public static Parameter inferParameter(String value) {
		if (value == null) {
			//null
			return new Parameter().withValue(null);
		} else if ("true".equals(value)) {
			//boolean true
			return new BoolParameter().withValue(value);
		} else if ("false".equals(value)) {
			//boolean false
			return new BoolParameter().withValue(value);
		}
		
		try {
			Long.parseLong(value);
			//if here is reached, value is integral
			return new IntParameter().withValue(value);
		} catch (NumberFormatException e) {/* value is not integral */}
		
		try {
			Double.parseDouble(value);
			//if here is reached, value is floating-point
			return new FloatParameter().withValue(value);			
		} catch (NumberFormatException e) {/* value is not floating-point */}
		
		try {
			XMLGregorianCalendar cal = 
				DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(value.trim());
			if (cal != null) {
				//if here is reached, successfully parsed as XML date value
				return new DateParameter().withValue(value);
			}
		} catch (Exception ignored) {}
		
		//fallback to string-format
		return new Parameter().withValue(value);
	}
}
