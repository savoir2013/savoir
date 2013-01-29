// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import ca.gc.iit.nrc.savoir.domain.Authorization;
import ca.gc.iit.nrc.savoir.domain.Role;
import ca.gc.nrc.iit.savoir.dao.IRoleDAO;

public class AuthorizationDAO {

	/**
	 * Maps subtypes of {@link Authorization}, providing 
	 * 
	 * @param <A>	The {@code Authorization} subtype to map 
	 */
	public static class AuthorizationRowMapper<A extends Authorization> 
			implements RowMapper {
		
		/** DAO to use for role lookup */
		protected IRoleDAO roleDAO;
		/** class that this row mapper returns */
		protected Class<A> clazz;
		
		
		/**
		 * Create a row mapper that returns objects of the specified class
		 * 
		 * @param clazz		The class to return
		 */
		public AuthorizationRowMapper(IRoleDAO roleDAO, Class<A> clazz) {
			this.roleDAO = roleDAO;
			this.clazz = clazz;
		}

		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			A a;
			
			try {
				a = clazz.newInstance();
			} catch (InstantiationException e) {
				return null;
			} catch (IllegalAccessException e) {
				return null;
			}
			
			a.setAuthorizedOn(rs.getInt(1));
			a.setAuthorizedTo(rs.getInt(2));
			a.setRole(roleDAO.getRoleById(rs.getInt(3)));
			addInfo(rs, rowNum, a);
			
			return a;
		}
		
		/**
		 * Add extra information to an {@code Authorization} subclass. It is 
		 * preferred that you use string-based column lookups, and it should be 
		 * noted that column 1 must be the {@code authorizedOn} ID, column 2 
		 * the {@code authorizedTo} ID, and column 3 the {@code role} ID, and 
		 * that all these data points will be filled in by this class. The 
		 * default implementation of {@code addInfo()} does nothing.
		 * 
		 * @param rs				The result set returned
		 * @param rowNum			The row number
		 * @param authorization		The authorization object, with its 
		 * 							super-class fields already filled in.
		 */
		protected void addInfo(ResultSet rs, int rowNum, A authorization) 
				throws SQLException {
			//do nothing
		}
	}
	
	/**
	 *	Binds an integer "id" to a Role "role" 
	 */
	public static class IntRole {
		public int id;
		public Role role;
	}

	/**
	 * Maps two columns, an integer (column 1), and a role ID (column 2) to an IntRole object
	 */
	public static class IntRoleRowMapper implements RowMapper {
		
		private IRoleDAO roleDAO;
		
		public IntRoleRowMapper(IRoleDAO roleDAO) {
			this.roleDAO = roleDAO;
		}
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			IntRole ir = new IntRole();
			
			ir.id = rs.getInt(1);		//this returns 0 for SQL NULL
			ir.role = roleDAO.getRoleById(rs.getInt(2));
			
			return ir;
		}
	}
}
