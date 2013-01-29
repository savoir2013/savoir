// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.springframework.jdbc.core.RowMapper;

/**
 * Generally useful DAO tools.
 * 
 * @author Aaron Moss
 */
public class DAOUtils {

	/**
	 * Maps a result set to a single integer, from the first column
	 */
	protected static class IntRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			return rs.getInt(1);
		}
	}

	//--------------------------------------------------------
	// Useful Fields
	//--------------------------------------------------------
	
	/** 
	 * Used as filter in WHERE clause of SELECT to verify correct time on 
	 * update 
	 */
	static final String NOW_IS_BETWEEN_BEGIN_AND_END_TIMES =
		"((CURRENT_TIMESTAMP - BEGIN_TIME >= 0) AND "
		+ "((END_TIME = 0) OR (CURRENT_TIMESTAMP - END_TIME < 0)))";
	
	/**
	 * Version of NOW_IS_BETWEEN_BEGIN_AND_END_TIMES where a table name can be 
	 * specified
	 * 
	 * @param name	The name of the table to check the times on
	 * 
	 * @return a version of NOW_IS_BETWEEN_BEGIN_AND_END_TIMES for that table
	 */
	static final String NOW_IS_BETWEEN_BEGIN_AND_END_TIMES(String name) {
		return 
			"((CURRENT_TIMESTAMP - " + name + ".BEGIN_TIME >= 0) AND (("  
			+ name + ".END_TIME = 0) OR (CURRENT_TIMESTAMP - " 
			+ name + ".END_TIME < 0)))";
	}
	
	/** Maximum number of characters that can be stored in the name field for a 
	 *  Session, Scenario, or Resource */
	static final int MAX_NAME_LENGTH = 128;
	/** Maximum number of characters that can be stored in the description 
	 *  field for a Session, Scenario, or Resource */
	static final int MAX_DESC_LENGTH = 1024;
	
	/**
	 * Truncates a string to length, if neccessary
	 * 
	 * @param s			The string to truncate
	 * @param len		The length to truncate to (must be at least {@code 3})
	 * 
	 * @return The original string, if the string is null or has no more then 
	 * 			{@code len} characters, or a string of length {@code len} 
	 * 			otherwise, where the first {@code len - 3} characters are the 
	 * 			prefix of {@code s}, and the remaining 3 are periods to form an 
	 * 			ellipsis
	 */
	static final String truncate(String s, int len) {
		if (s == null || s.length() <= len) return s;
		else return s.substring(0, len - 3) + "...";
	}

	/**
	 * Adds a set of integers in SQL query format to a StringBuilder.
	 * When done, sb will have "(s1,s2,s3...)" added to it (where si is the 
	 * i-th element of s). If s is empty, will return "(0)" (as "()" is not 
	 * valid SQL syntax)
	 * 
	 * @param sb	The StringBuilder
	 * @param s		The set of integers to add
	 */
	static final void addInts(StringBuilder sb, Set<Integer> s) {
		sb.append('(');
		if(!s.isEmpty()) {
			Iterator<Integer> iter = s.iterator();
			sb.append(iter.next());
			while(iter.hasNext()) {
				sb.append(',');
				sb.append(iter.next().toString());
			}
		} else {
			sb.append("0");
		}
		sb.append(')');
	}
    
    /**
     * Gets a Date from a ResultSet, converting SQL DATETIME 0 to Java null 
     * 
     * @param rs			The ResultSet
     * @param columnLabel	The label of the column to get from
     * 
     * @return the Java Date, null for SQL DATETIME 0
     * 
     * @throws SQLException
     */
    static final Date getNullableDate(ResultSet rs, String columnLabel) 
    		throws SQLException {
		try {
			return rs.getTimestamp(columnLabel);
		} catch (SQLException e) {
			// check for exception where 0 value cannot be converted to date
			if(e.getMessage().contains("0000-00-00 00:00:00")) {
				return null;
			} else {	//some other exception, pass up the stack
				throw e;
			}
		}
	}
	
    /**
	 * Gets an appropriate value to insert into a SQL DATETIME field
	 * @param cal		The date in question
	 * @return integer 0 for Java null, {@code cal.getTime()} if not null 
	 */
    static final Object nullableCalendar(Calendar cal) {
		return cal == null ? 0 : cal.getTime();
	}
	
    /**
     * Gets the type of value insert into a SQL DATETIME field, appropriate for 
     * the given {@code Calendar}
     * 
     * @param cal		The calendar to add
     * 
     * @return SQL INTEGER for Java null (will be a 0 value), SQL TIMESTAMP for 
     * 		a valid Date
     */
	static final int nullableType(Calendar cal) {
		return cal == null ? Types.INTEGER : Types.TIMESTAMP;
	}
    
    /**
	 * Gets an appropriate value to insert into a SQL DATETIME field
	 * @param date	The date in question
	 * @return integer 0 for Java null, date if not null 
	 */
    static final Object nullableDate(Date date) {
		return date == null ? 0 : date;
	}
	
    /**
     * Gets the type of value insert into a SQL DATETIME field, appropriate for 
     * the given Date
     * 
     * @param date	The date to add
     * 
     * @return SQL INTEGER for Java null (will be a 0 value), SQL TIMESTAMP for 
     * 		a valid Date
     */
	static final int nullableType(Date date) {
		return date == null ? Types.INTEGER : Types.TIMESTAMP;
	}
	
	/**
	 * @return Java null for i == 0, i otherwise 
	 */
    static final Object nullableInt(int i) {
		return i == 0 ? null : i;
	}
    
    /**
     * Gets the type of value insert into a SQL INTEGER field, appropriate for 
     * the given integer
     * 
     * @param i		the int to add
     * 
     * @return SQL NULL for {@code i = 0}, SQL INTEGER for {@code i = 0}
     */
    static final int nullableType(int i) {
    	return i == 0 ? Types.NULL : Types.INTEGER;
    }
}
