// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="EndPoint")
public class EndPoint {

	public static final String CLIENT = "CLIENT";
	
	public static final String SERVER = "SERVER";
	
	private int endPointID;
	
	private Resource resource;
	
	private Person person;
	
	private String endPointType;
	
	private Resource networkEndPoint;

	public EndPoint(){
		
	}
	
	public int getEndPointID() {
		return endPointID;
	}

	public void setEndPointID(int endPointID) {
		this.endPointID = endPointID;
	}

	public Resource getResource() {
		return resource;
	}

	public Person getPerson() {
		return person;
	}

	public String getEndPointType() {
		return endPointType;
	}

	public Resource getNetworkEndPoint() {
		return networkEndPoint;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public void setEndPointType(String endPointType) {
		this.endPointType = endPointType;
	}

	public void setNetworkEndPoint(Resource networkEndPoint) {
		this.networkEndPoint = networkEndPoint;
	}
	
}
