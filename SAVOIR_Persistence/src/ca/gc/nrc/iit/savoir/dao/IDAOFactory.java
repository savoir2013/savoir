// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao;

public interface IDAOFactory {
	public IResourceDAO getResourceDAO();
	public IParametersDAO getParametersDAO();
	public ISiteDAO getSiteDAO();
	public ISessionDAO getSessionDAO();
	public IScenarioDAO getScenarioDAO();
	public ICalendarDAO getCalendarDAO();
	public ITypesDAO getTypesDAO();
	public IConnectionDAO getConnectionDAO();
	public IUserDAO getUserDAO();
	public IPersonDAO getPersonDAO();
	public IGroupDAO getGroupDAO();
	public IRoleDAO getRoleDAO();
	public ICredentialDAO getCredentialDAO();
}
