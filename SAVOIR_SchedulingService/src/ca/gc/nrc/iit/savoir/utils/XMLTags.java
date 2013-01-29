// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.utils;

public class XMLTags {

	public static final String ERROR_TYPE = "error";

	public static final String RESULT_TYPE = "result";

	public static final String BOOLEAN_TYPE = "boolean";

	public static final String CONFIG_RESPONSE_TYPE = "configResponse";
	
	public static final String LIST_TYPE = "list";

	public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

	public static final String XML_ROOT = "xml-response";
	
	public static final String STATUS_SET = "set";
	
	public static final String STATUS_UNSET = "unset";

	private static String ls = null;

	static {
		ls = System.getProperty("line.separator");
	}

	public static String getError(String fault, Exception ex) {
		String faultTrace = "";
		String faultString = fault;
		if (ex != null) {
			faultTrace = ClientException.getFaultTrace(ex);
			faultTrace = faultTrace.replaceAll("<", "&lt;");
			faultTrace = faultTrace.replaceAll(">", "&gt;");
		}
		String response = XML_HEADER + ls + "<" + XML_ROOT + " type=\"" + ERROR_TYPE + "\">" + ls;
		if (faultString != null) {
			faultString = faultString.replaceAll("<", "&lt;");
			faultString = faultString.replaceAll(">", "&gt;");
			response += "	<faultString>" + faultString + "</faultString>" + ls + "	<faultTrace>" + faultTrace + "</faultTrace>" + ls;
		}
		response += "</" + XML_ROOT + ">";
		return response;
	}

	public static String getError(Exception ex) {
		return getError(ex.getMessage(), ex);
	}

	public static String getResult(String result) {
		String response = XML_HEADER + ls + "<" + XML_ROOT + " type=\"" + RESULT_TYPE + "\">" + result + "</" + XML_ROOT + ">";
		return response;
	}

	public static String getXMLBoolean(Boolean b) {
		String response = XML_HEADER + ls + "<" + XML_ROOT + " type=\"" + BOOLEAN_TYPE + "\">" + b.toString() + "</" + XML_ROOT + ">";
		return response;
	}

	public static String getXMLScenariosList(String metaInfo, String[] scenarios, String configured, String key) {
		String response = XML_HEADER + ls + "<" + XML_ROOT + " type=\"" + LIST_TYPE + "\">" + ls + "<resource>" + key + "</resource>" + ls;

		for (String sc : scenarios) {
			String status = configured.equals("N/A") ? STATUS_UNSET : STATUS_SET ;
			response += "<scenario id=\"" + sc + "\" status=\"" + status + "\">" + ls;
			for (String meta : metaInfo.split("\n")) {
				if (meta.startsWith("identifier=" + sc)) {
					response += "<meta>" + meta + "</meta>" + ls;
				}
			}
			response += "</scenario>";
		}
		response += "</" + XML_ROOT + ">";
		return response;
	}

}
