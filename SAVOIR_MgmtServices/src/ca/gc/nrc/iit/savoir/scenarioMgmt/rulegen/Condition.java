// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;

import java.util.List;

import ca.gc.nrc.iit.savoir.model.session.Service;

/**
 * Represents a condition of a rule.
 * 
 * @author Aaron Moss
 */
public abstract class Condition {

	/** 
	 * Service bindings required by this condition
	 * @return all Service objects used in this condition or its subconditions, 
	 * 		and thus required to be bound.
	 */
	public abstract List<Service> requiredServices();
}
