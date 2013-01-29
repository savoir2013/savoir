// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao;

import ca.gc.iit.nrc.savoir.domain.EndPoint;

public interface IEndPointDAO {
	
	public int addEndPoint(EndPoint e);

	public int removeEndPoint(int endPointId);
	
	public int removeEndPointsByConnectionID(int connectionId);

	public EndPoint getEndPointById(int endPointId);
	
	public int updateEndPoint(EndPoint e);

}
