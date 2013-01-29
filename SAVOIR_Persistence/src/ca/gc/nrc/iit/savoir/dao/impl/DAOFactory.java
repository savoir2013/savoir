// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ca.gc.nrc.iit.savoir.dao.ICalendarDAO;
import ca.gc.nrc.iit.savoir.dao.IConnectionDAO;
import ca.gc.nrc.iit.savoir.dao.IConstraintDAO;
import ca.gc.nrc.iit.savoir.dao.ICredentialDAO;
import ca.gc.nrc.iit.savoir.dao.IDAOFactory;
import ca.gc.nrc.iit.savoir.dao.IGroupDAO;
import ca.gc.nrc.iit.savoir.dao.IParametersDAO;
import ca.gc.nrc.iit.savoir.dao.IPersonDAO;
import ca.gc.nrc.iit.savoir.dao.IResourceDAO;
import ca.gc.nrc.iit.savoir.dao.IRoleDAO;
import ca.gc.nrc.iit.savoir.dao.IScenarioDAO;
import ca.gc.nrc.iit.savoir.dao.ISessionDAO;
import ca.gc.nrc.iit.savoir.dao.ISiteDAO;
import ca.gc.nrc.iit.savoir.dao.ITypesDAO;
import ca.gc.nrc.iit.savoir.dao.IUserDAO;
import ca.gc.nrc.iit.savoir.spring.BeanManager;

public class DAOFactory implements IDAOFactory {	

	private static DAOFactory factory;

	private IParametersDAO parametersDAO;

	private IResourceDAO resourceDAO;

	private ISiteDAO siteDAO;
	
	private ISessionDAO sessionDAO;
	
	private IScenarioDAO scenarioDAO;
	
	private ICalendarDAO calendarDAO;
	
	private ITypesDAO typesDAO;
	
	private IConnectionDAO connectionDAO;
	
	private IUserDAO userDAO;
	
	private IPersonDAO personDAO;
	
	private IGroupDAO groupDAO;
	
	private IRoleDAO roleDAO;
	
	private ICredentialDAO credentialDAO;
	
	private IConstraintDAO constraintDAO; 
	
	protected final static Log log = LogFactory.getLog(DAOFactory.class);
	
	public static synchronized DAOFactory getDAOFactoryInstance() {
		if (factory == null) {
			log.info("Initializing the DAOFactory...");
			factory = (DAOFactory) BeanManager.getBeanManager().getContext().getBean("daoFactory");
			log.info("DAOFactory initialized");
		}
		return factory;
	}

	@Override
	public IParametersDAO getParametersDAO() {
		return parametersDAO;
	}

	@Override
	public IResourceDAO getResourceDAO() {
		return resourceDAO;
	}

	@Override
	public ISiteDAO getSiteDAO() {
		return siteDAO;
	}

	public void setParametersDAO(IParametersDAO parametersDAO) {
		this.parametersDAO = parametersDAO;
	}

	public void setResourceDAO(IResourceDAO resourceDAO) {
		this.resourceDAO = resourceDAO;
	}

	public void setSiteDAO(ISiteDAO siteDAO) {
		this.siteDAO = siteDAO;
	}

	@Override
	public ISessionDAO getSessionDAO() {		
		return sessionDAO;
	}

	public void setSessionDAO(ISessionDAO sessionDAO) {
		this.sessionDAO = sessionDAO;
	}
	
	@Override
	public IScenarioDAO getScenarioDAO() {		
		return scenarioDAO;
	}

	public void setScenarioDAO(IScenarioDAO scenarioDAO) {
		this.scenarioDAO = scenarioDAO;
	}

	@Override
	public ICalendarDAO getCalendarDAO() {		
		return calendarDAO;
	}
	
	public void setCalendarDAO(ICalendarDAO calendarDAO) {
		this.calendarDAO = calendarDAO;
	}
	
	@Override
	public ITypesDAO getTypesDAO() {
		return typesDAO;
	}

	public void setTypesDAO(ITypesDAO typesDAO) {
		this.typesDAO = typesDAO;
	}

	@Override
	public IConnectionDAO getConnectionDAO() {
		return connectionDAO;
	}

	public void setConnectionDAO(IConnectionDAO connectionDAO) {
		this.connectionDAO = connectionDAO;
	}

	@Override
	public IUserDAO getUserDAO() {
		return userDAO;
	}

	public void setUserDAO(IUserDAO userDAO) {
		this.userDAO = userDAO;
	}
	
	@Override
	public IPersonDAO getPersonDAO() {
		return personDAO;
	}

	public void setPersonDAO(IPersonDAO personDAO) {
		this.personDAO = personDAO;
	}
	
	@Override
	public IGroupDAO getGroupDAO() {
		return groupDAO;
	}
	
	public void setGroupDAO(IGroupDAO groupDAO) {
		this.groupDAO = groupDAO;
	}

	@Override
	public IRoleDAO getRoleDAO() {
		return roleDAO;
	}
	
	public void setRoleDAO(IRoleDAO roleDAO) {
		this.roleDAO = roleDAO;
	}
	
	@Override
	public ICredentialDAO getCredentialDAO() {
		return credentialDAO;
	}
	
	public void setCredentialDAO(ICredentialDAO credentialDAO) {
		this.credentialDAO = credentialDAO;
	}
	
	public IConstraintDAO getConstraintDAO() {
		return constraintDAO;
	}

	public void setConstraintDAO(IConstraintDAO constraintDAO) {
		this.constraintDAO = constraintDAO;
	}

}
