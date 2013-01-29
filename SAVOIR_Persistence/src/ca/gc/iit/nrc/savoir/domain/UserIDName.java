// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain;

import javax.xml.bind.annotation.XmlType;

/**
 * An object to hold a user ID and two name fields together
 * @author Aaron Moss
 */
@XmlType(name="UserIDName")
public class UserIDName implements Comparable<UserIDName> {
	/** The ID # of the user */
	public int userID;
	/** The login ID of the user */
	public String userName;
	/** The given name of the user */
	public String givenName;
	/** The surname of the user */
	public String surname;
	
	/**
	 * No-arg constructor
	 */
	public UserIDName() {
		this.userID = 0;
		this.userName = null;
		this.givenName = null;
		this.surname = null;
	}
	
	/**
	 * Default constructor
	 * @param userIdIn		The input user ID
	 * @param userNameIn	The input user name
	 * @param givenNameIn	The input given name
	 * @param surnameIn		The input surname
	 */
	public UserIDName(int userIdIn, String userNameIn, String givenNameIn, 
			String surnameIn) {
		this.userID = userIdIn;
		this.userName = userNameIn;
		this.givenName = givenNameIn;
		this.surname = surnameIn;
	}

	/**
	 * Implements Comparable
	 * @return	the string comparison of the two objects, first by last name, 
	 * 			then first, then login name (all of which should be equal for 
	 * 			the same user)
	 */
	public int compareTo(UserIDName o) {
		int compare = this.surname.compareTo(o.surname);
		if (compare == 0) {
			compare = this.givenName.compareTo(o.givenName);
			if (compare == 0) {
				compare = this.userName.compareTo(o.userName);
			}
		}
		return compare;
	}
	
	/**
	 * Returns this user's name in the format &lt;surname&gt;[, givenName]
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.surname);
		if (this.givenName != null && this.givenName.length() > 0) {
			sb.append(", " + this.givenName);
		}
		return sb.toString();
	}
}
