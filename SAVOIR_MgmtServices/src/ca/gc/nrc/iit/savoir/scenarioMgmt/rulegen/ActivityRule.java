// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import ca.gc.nrc.iit.savoir.model.session.Service;

/**
 * Represents a rule on an activity instance.
 * This rule will be activated when the activity has control of the scenario.
 * 
 * @author Aaron Moss
 */
public abstract class ActivityRule extends Rule {

	/** The name of this activity node */
	protected String activity;
	/** The rule index of this activity node */
	protected int index;
	
	/** services this rule uses in its conditions */
	protected LinkedHashSet<ServiceBinding> services;
	/** variables this rule uses in its consequences */
	protected LinkedHashSet<VarBinding> vars;
	/** The condition for this rule to trigger */
	protected List<Condition> conditions;
	/** any consequences of this rule triggering */
	protected List<Consequence> consequences;
	
	public String getActivity() {
		return activity;
	}
	public int getIndex() {
		return index;
	}
	public void setActivity(String activity) {
		this.activity = activity;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	
	public List<ServiceBinding> getServices() {
		return new ArrayList<ServiceBinding>(services);
	}
	
	public List<VarBinding> getVars() {
		return new ArrayList<VarBinding>(vars);
	}
	
	public List<Condition> getConditions() {
		return conditions;
	}

	public List<Consequence> getConsequences() {
		return consequences;
	}
	
	public void setVars(List<VarBinding> vars) {
		this.vars = (null == vars) ? null : new LinkedHashSet<VarBinding>(vars);
		
		//reparse service bindings
		genServiceBindings();
	}
	
	public void addVar(VarBinding var) {
		if (null == this.vars)
			this.vars = new LinkedHashSet<VarBinding>();
		
		this.vars.add(var);
	}

	public void setCondition(Condition condition) {
		this.conditions = new ArrayList<Condition>(Arrays.asList(condition));

		//reparse service bindings
		genServiceBindings();
	}
	
	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;

		//reparse service bindings
		genServiceBindings();
	}
	
	public void addCondition(Condition condition) {
		if (null == this.conditions)
			this.conditions = new ArrayList<Condition>();
		
		this.conditions.add(condition);
		
		//reparse service bindings
		genServiceBindings();
	}

	public void setConsequences(List<Consequence> consequences) {
		this.consequences = consequences;
	}
	
	public void addConsequence(Consequence consequence) {
		if (null == this.consequences)
			this.consequences = new ArrayList<Consequence>();
		
		this.consequences.add(consequence);
	}
	
	private void genServiceBindings() {
		services = new LinkedHashSet<ServiceBinding>();
		
		if (null != vars) for (VarBinding v : vars) {
			services.add(new ServiceBinding(v.getService()));
		}
		
		if (null != conditions) for (Condition c : conditions) {
			for (Service s : c.requiredServices()) {
				services.add(new ServiceBinding(s));
			}
		}
	}
}
