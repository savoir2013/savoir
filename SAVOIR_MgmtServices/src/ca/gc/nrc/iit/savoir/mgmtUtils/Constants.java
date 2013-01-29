// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.mgmtUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * Defines constants for use in the SAVOIR project.
 * These are mainly integer values and enumerations that are used in multiple 
 * subprojects, and thus fit in none. It also includes some utility methods 
 * that don't comprise their own class, but have similar scope. Recommended 
 * usage of this class is a static import, 
 * "{@code import static ca.gc.nrc.iit.savoir.mgmtUtils.Constants.*;}".
 * 
 * @author Aaron Moss
 */
public abstract class Constants {
	
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
	/** if this is returned, there was some error with the preconditions for 
	 * the method */
	public static final int PRECONDITION_ERROR = -5;
	/** if this is returned, the provided data failed to validate against some 
	 * stored schema */
	public static final int SCHEMA_VALIDATION_FAILS = -6;
	/** Some error in file I/O */
	public static final int FILE_IO_ERROR = -7;
//	/** if this is returned, we have unspecified error in MyProxy */
//	public static final int MYPROXY_ERROR = -10;
	/** if this is returned, the caller failed authentication */
	public static final int INVALID_CALLER = -20;
	/** if this is returned, the caller lacked authorization to perform the 
	 * desired action */
	public static final int UNAUTHORIZED = -21;
	/** if this is returned, the subject lacked sufficient authorization for 
	 * the action to be performed on them */
	public static final int SUBJECT_UNAUTHORIZED = -22;
	
	//------------------------------------------------
	// Field Enumerations
	//------------------------------------------------
	
	/**
	 * Defines valid field names for UserInfo types.			
	 * 		<ul>
	 * 		<li><b>{@code MID_NAME}</b>
	 * 			Middle initials
	 * 		<li><b>{@code HONORIFIC}</b>
	 * 			honorific (Mr., Ms., Dr., etc.)
	 * 		<li><b>{@code EMAIL1}</b>
	 * 			primary email address
	 * 		<li><b>{@code EMAIL2}</b>
	 * 			secondary email address
	 * 		<li><b>{@code WORK_PHONE}</b>
	 * 			work phone number
	 * 		<li><b>{@code CELL_PHONE}</b>
	 * 			mobile phone number
	 * 		<li><b>{@code HOME_PHONE}</b>
	 * 			home phone number
	 * 		<li><b>{@code STREET_ADDRESS}</b>
	 * 			street address
	 * 		<li><b>{@code CITY}</b>
	 * 			city of street address
	 * 		<li><b>{@code REGION}</b>
	 * 			province or state of street address
	 * 		<li><b>{@code COUNTRY}</b>
	 * 			country of street address
	 * 		<li><b>{@code POSTAL}</b>
	 * 			postal or ZIP code of street address
	 * 		</ul>
	 */
	public enum UserInfoFields {MID_NAME, HONORIFIC, EMAIL1, EMAIL2, 
			WORK_PHONE, CELL_PHONE, HOME_PHONE, ORGANIZATION, STREET_ADDRESS, 
			CITY, REGION, COUNTRY, POSTAL};
	
	//------------------------------------------------
	// Authorization data
	//------------------------------------------------
						 
	public static final boolean ALLOW = true, DENY = false;
	public static final int NO_USER = 0, NO_GROUP = 0, EVERYONE = 0;
	
	public enum UserMgmtAction {
		VIEW_MEMBERS(0), UPDATE_CREDENTIALS(1), UPDATE_MEMBERS(2), 
		MANAGE_GROUP(3), CREATE_USER(4), UPDATE_USER(5), DELETE_USER(6), 
		CREATE_GROUP(7);
		
		public int code;
		
		UserMgmtAction(int codeIn) {
			this.code = codeIn;
		}
	}
	
	//------------------------------------------------
	// Utility methods
	//------------------------------------------------
	
	/**
	 * Checks if the given begin and end times are sane
	 * @param begin	the begin time	(may be null)
	 * @param end	the end time	(may be null)
	 * @return begin is before end AND end is after now
	 */
	public static boolean timesSane(Date begin, Date end) {
		Date now = Calendar.getInstance().getTime();
		
		if (end != null) {		//if we have an end value
			if (end.after(now)) {	//if the end time is after the present
										//return begin is before end (or null)
				return (begin == null || begin.before(end));
			} else {
				return false;			//return false for end time before now
			}
		} else {
			return true;		//if there is no end, the time is always sane
		}
	}
}
