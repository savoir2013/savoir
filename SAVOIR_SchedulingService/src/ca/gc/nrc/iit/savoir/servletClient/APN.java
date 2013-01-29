// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.servletClient;


import org.example.apnSchema.Scenario;
import org.example.apnSchema.XmlResponseDocument.XmlResponse;

public class APN
{
	XmlResponse xmlResponse;
	String configured = "N/A";
	
	
	public APN(XmlResponse xmlResponse)
	{
		for(Scenario sc : xmlResponse.getScenarioArray()){
			if(sc.getStatus().equals("set")){
				configured = sc.getId();
			}
		}
		
		this.xmlResponse = xmlResponse;
	}

	public String getKey()
	{
		return xmlResponse.getResource();
	}

	public String getConfigured() {
		return configured;
	}

	public void setConfigured(String configured) {
		this.configured = configured;
	}

	public XmlResponse getXmlResponse() {
		return xmlResponse;
	}
		

	
}
