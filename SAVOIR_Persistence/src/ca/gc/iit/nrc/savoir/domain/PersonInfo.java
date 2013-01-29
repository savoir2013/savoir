// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="PersonInfo")
public class PersonInfo {

	/** surname **/
	private String lName;
	/** given name **/
	private String fName;
	/** middle initials **/
	private String mName;
	/** honorific **/
	private String honorific;
	/** primary email **/
	private String email1;
	/** secondary email **/
	private String email2;
	/** daytime phone **/
	private String workPhone;
	/** mobile phone **/
	private String cellPhone;
	/** off-hours phone **/
	private String homePhone;
	/** organizational affiliation */
	private String organization;
	/** mailing address **/
	private String streetAddress;
	/** city of residence **/
	private String city;
	/** province or state **/
	private String region;
	/** country **/
	private String country;
	/** postal or ZIP code **/
	private String postal;
		
	
	public PersonInfo() {}
	
	
	public String getLName() {
		return lName;
	}
	
	public String getFName() {
		return fName;
	}
	
	public String getMName() {
		return mName;
	}
	
	public String getHonorific() {
		return honorific;
	}
	
	public String getEmail1() {
		return email1;
	}
	
	public String getEmail2() {
		return email2;
	}
	
	public String getWorkPhone() {
		return workPhone;
	}
	
	public String getCellPhone() {
		return cellPhone;
	}
	
	public String getHomePhone() {
		return homePhone;
	}
	
	public String getOrganization() {
		return organization;
	}
	
	public String getStreetAddress() {
		return streetAddress;
	}
	
	public String getCity() {
		return city;
	}
	
	public String getRegion() {
		return region;
	}
	
	public String getCountry() {
		return country;
	}
	
	public String getPostal() {
		return postal;
	}
	
	
	public void setLName(String name) {
		lName = name;
	}
	
	public void setFName(String name) {
		fName = name;
	}
	
	public void setMName(String name) {
		mName = name;
	}
	
	public void setHonorific(String honorific) {
		this.honorific = honorific;
	}
	
	public void setEmail1(String email1) {
		this.email1 = email1;
	}
	
	public void setEmail2(String email2) {
		this.email2 = email2;
	}
	
	public void setWorkPhone(String workPhone) {
		this.workPhone = workPhone;
	}
	
	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
	}
	
	public void setHomePhone(String homePhone) {
		this.homePhone = homePhone;
	}
	
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	
	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}
	
	public void setCity(String city) {
		this.city = city;
	}
	
	public void setRegion(String region) {
		this.region = region;
	}
	
	public void setCountry(String country) {
		this.country = country;
	}
	
	public void setPostal(String postal) {
		this.postal = postal;
	}
	
}
