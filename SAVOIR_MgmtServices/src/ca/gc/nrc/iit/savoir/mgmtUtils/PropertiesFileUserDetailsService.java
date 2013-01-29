// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.mgmtUtils;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.NonTransientDataAccessResourceException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

public class PropertiesFileUserDetailsService implements UserDetailsService {
	
	private Map<String, User> users;
	
	/**
	 * Default constructor.
	 * 
	 * @param propertiesFilename	The base of the properties file name
	 * @param usersProperty			The name of the property storing the list 
	 * 								of valid users.
	 */
	public PropertiesFileUserDetailsService(String propertiesFilename, 
			String usersProperty) {
		
		try {
			ResourceBundle properties = 
				ResourceBundle.getBundle(propertiesFilename, 
						Locale.getDefault());
			
			String validUsers = properties.getString(usersProperty);
			
			String[] userList = validUsers.split("\\s*,\\s*");
			
			users = new LinkedHashMap<String, User>();
			
			for (String userPrefix : userList) {
				String username = null, password = null, roles = null, 
						enabled = null, expired = null, locked = null;
				//mandatory properties
				try {
					username = properties.getString(userPrefix + ".username");
					password = properties.getString(userPrefix + ".password");
					roles = properties.getString(userPrefix + ".roles");
				} catch (MissingResourceException e) {
					//skip user on username, password, or roles undefined
					continue;
				}
				
				//optional properties
				try {
					enabled = properties.getString(userPrefix + ".enabled");
				} catch (MissingResourceException ignored) {}
				
				try {
					expired = properties.getString(userPrefix + ".expired");
				} catch (MissingResourceException ignored) {}
				
				try {
					locked = properties.getString(userPrefix + ".locked");
				} catch (MissingResourceException ignored) {}
				
				//parse values
				boolean isEnabled = true;
				if (enabled != null && "false".equalsIgnoreCase(enabled)) 
					isEnabled = false;
				
				boolean notExpired = true;
				if (expired != null && "true".equalsIgnoreCase(expired))
					notExpired = false;
				
				boolean credentialsNotExpired = true;
				
				boolean notLocked = true;
				if (locked != null && "true".equalsIgnoreCase(locked))
					notLocked = false;
				
				String[] allRoles = roles.split("\\s*,\\s*");
				GrantedAuthority[] authorities = 
					new GrantedAuthority[allRoles.length];
				for (int i = 0; i < allRoles.length; i++) {
					authorities[i] = new GrantedAuthorityImpl(allRoles[i]);
				}
				
				//add user to map
				users.put(username, 
						new User(username, password, isEnabled, notExpired, 
								credentialsNotExpired, notLocked, authorities));				
			}
			
		//exceptions just mean misconfiguration
		} catch (NullPointerException e) {
		} catch (MissingResourceException e) {
		}
		
		
	}
	
	@Override
	public UserDetails loadUserByUsername(String userName)
			throws UsernameNotFoundException, DataAccessException {
		try {
			User u = users.get(userName);
			if (u == null) {
				throw new UsernameNotFoundException(
						"No such user \"" + userName + "\"");
			} else {
				return u;
			}
		} catch (NullPointerException e) {
			throw new NonTransientDataAccessResourceException(
					"User properties file failed to load");
		}
		
	}

	
}
