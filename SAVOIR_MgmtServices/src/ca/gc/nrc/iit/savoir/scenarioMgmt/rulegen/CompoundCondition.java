// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.gc.nrc.iit.savoir.model.session.Service;

/**
 * A condition compounded of other subconditions.
 * 
 * @author Aaron Moss
 */
public abstract class CompoundCondition extends Condition {

	/** The conditions this condition is compounded of */
	protected List<Condition> conditions;
	
	public List<Condition> getConditions() {
		return conditions;
	}
	
	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}
	
	public void setCondition(Condition condition) {
		this.conditions = Arrays.asList(condition);
	}
	
	@Override
	public List<Service> requiredServices() {
		List<Service> req = new ArrayList<Service>();
		
		if (null != conditions) for (Condition c : conditions) {
			req.addAll(c.requiredServices());
		}
		
		return req;
	}
}
