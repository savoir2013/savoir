// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler;

import ca.gc.iit.nrc.savoir.domain.Constraint;
import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.iit.nrc.savoir.domain.Session;
import ca.gc.nrc.iit.savoir.scheduler.types.msg.CheckResponse;

/**
 * @author harzallahy
 *
 */
public interface RequirementChecker {
	
	
	
	/**
	 * @param s
	 * @param r
	 * @param config
	 * @return
	 */
	public CheckResponse check(Session s, long offset, Resource r, Constraint c); 
}
