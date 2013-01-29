// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.utils;

/**
 * Defines constants for the SAVOIR project
 * @author Aaron Moss
 */
public final class Constants {
	
	private Constants() {}
	
	//------------------------------------------------
	// Method Return codes
	//------------------------------------------------
	/** successful operation */
	public static final int SUCCESS = 0;
	/** entity to be created already exists */
	public static final int ALREADY_EXISTS = -1;
	/** entity to be destroyed / edited does not exist */
	public static final int NO_SUCH_ENTITY = -1;
	/** parameters passed to method are invalid */
	public static final int INVALID_PARAMETERS = -2;
	/** undefined unsuccessful operation */
	public static final int OTHER_ERROR = -3;
	/** if this is returned, we have data corruption */
	public static final int DATA_CORRUPTION = -4;
	/** if this is returned, there was some error with the preconditions for the method */
	public static final int PRECONDITION_ERROR = -5;
	/** if this is returned, we have unspecified error in MyProxy */
	public static final int MYPROXY_ERROR = -10;
	/** if this is returned, the caller failed authentication */
	public static final int INVALID_CALLER = -20;
	/** if this is returned, the caller lacked authorization to perform the desired action */
	public static final int UNAUTHORIZED = -21;
	
	//------------------------------------------------
	// Field Enumerations
	//------------------------------------------------
	
	/**
	 * Defines valid field names for UserInfo types			
	 * 		MID_NAME		Middle initials
	 * 		HONORIFIC		honorific (Mr., Ms., Dr., etc.)
	 * 		EMAIL1			primary email address
	 * 		EMAIL2			secondary email address
	 * 		WORK_PHONE		work phone number
	 * 		CELL_PHONE		mobile phone number
	 * 		HOME_PHONE		home phone number
	 * 		STREET_ADDRESS	street address
	 * 		CITY			city of street address
	 * 		REGION			province or state of street address
	 * 		COUNTRY			country of street address
	 * 		POSTAL			postal or ZIP code of street address
	 */
	public enum UserInfoFields {MID_NAME, HONORIFIC, EMAIL1, EMAIL2, WORK_PHONE, CELL_PHONE, HOME_PHONE,
						 STREET_ADDRESS, CITY, REGION, COUNTRY, POSTAL};
	
	//------------------------------------------------
	// Authorization data
	//------------------------------------------------
						 
	public static final boolean ALLOW = true;
	public static final boolean DENY = false;
	
	public enum UserMgmtAction {
		VIEW_SUBENTITIES(0), INFO_SUBENTITIES(1), ROLE_MEMBERS(2), ADD_USER(3),
		ADD_GROUP(4), UPDATE_MEMBER(5), DELETE_USER(6), ADMINISTER(7);
		
		public int code;
		
		UserMgmtAction(int codeIn) {
			this.code = codeIn;
		}
	};
	
}
