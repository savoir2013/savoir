// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.userMgmt;

import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

import ca.gc.iit.nrc.savoir.domain.User;
import ca.gc.nrc.iit.savoir.dao.impl.DAOFactory;

/**
 * SAVOIR implementation of Spring Security UserDetailsService.
 * Looks up user in SAVOIR database, returns wrapped in 
 * {@link SavoirUserDetails} object.
 * 
 * @author Aaron Moss
 *
 * @see <a href="http://static.springsource.org/spring-security/site/docs/2.0.x/apidocs/org/springframework/security/userdetails/UserDetailsService.html">Spring Security API</a>
 * @see <a href="http://static.springsource.org/spring-security/site/docs/2.0.x/reference/springsecurity.html">Spring Security Manual</a>
 */
public class SavoirUserDetailServiceImpl implements UserDetailsService {

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		//load user from database
		User user = 
			DAOFactory.getDAOFactoryInstance().getUserDAO()
				.getUserByDN(username);
		
		if (user == null) {
			//no such user if user object returned from DB is null 
			throw new UsernameNotFoundException(username);
		}
		
		//wrap SAVOIR user in Spring object and return
		return new SavoirUserDetails(user);
	}

}
