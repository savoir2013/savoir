// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao;

import java.util.List;

import ca.gc.iit.nrc.savoir.domain.Connection;

public interface IConnectionDAO {
	public int addConnection(Connection c, int sessionID);

	public int removeConnection(int connectionId);

	public Connection getConnectionById(int connectionId);

	public List<Connection> getConnectionsBySessionID(int sessionID);
	
	public int updateConnection(Connection c);	

}
