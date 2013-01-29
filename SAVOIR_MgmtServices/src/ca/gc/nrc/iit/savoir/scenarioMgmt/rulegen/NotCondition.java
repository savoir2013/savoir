// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import ca.gc.nrc.iit.savoir.model.session.Parameter;

/**
 * Negating condition.
 * True precisely when none of its subconditions are true.
 * 
 * @author Aaron Moss
 */
public class NotCondition extends CompoundCondition {

	public NotCondition() {}
	
	public NotCondition(Condition condition) {
		this.conditions = Arrays.asList(condition);
	}
	
	public NotCondition(List<Condition> conditions) {
		this.conditions = conditions;
	}
	
	/**
	 * Negates its conditions before printing out the negated version.
	 * 
	 * Not is somewhat more difficult to implement in rules then AND or OR, 
	 * so this printing method uses DeMorgan's Law, double-negation, and 
	 * inversion of the comparison on when conditions to eliminate all 
	 * NotConditions before printing. 
	 */
	public String toString() {
		if (null == conditions || conditions.isEmpty()) {
			//don't print anything for no conditions
			return "";
		} else if (1 == conditions.size()) {
			//print the negation of the single condition
			return not(conditions.get(0)).toString();
		} else {
			//a NotCondition is logically a Not-Or, so print the negation of 
			// the OR of multiple conditions
			return not(new OrCondition(conditions)).toString();
		}
	}
	
	/* inverse comparions of all comparisons */
	private static final Map<Comparison, Comparison> opp;
	static {
		Map<Comparison, Comparison> tmp = 
			new EnumMap<Comparison, Comparison>(Comparison.class);
		
		tmp.put(Comparison.EQ, Comparison.NE);	// == : !=
		tmp.put(Comparison.GE, Comparison.LT);	// >= : <
		tmp.put(Comparison.GT, Comparison.LE);	// >  : <=
		tmp.put(Comparison.LE, Comparison.GT);	// <= : >
		tmp.put(Comparison.LT, Comparison.GE);	// <  : >=
		tmp.put(Comparison.NE, Comparison.EQ);	// != : ==
		
		opp = Collections.unmodifiableMap(tmp);
	}
	
	/**
	 * Negates a condition.
	 * When conditions are negated by inverting their comparison
	 * And and Or conditions are negated by DeMorgan's Law
	 * Not conditions are negated by double-negation (note a Not is actually a 
	 * 	Not-Or, so the Not is replaced by an Or if it has multiple conditions)
	 * 
	 * @param oldC	The condition to negate
	 * @return the negation of the condition, null for error.
	 */
	private Condition not(Condition oldC) {
		if (oldC instanceof WhenCondition) {
			WhenCondition wc = (WhenCondition)oldC;
			
			Parameter oldParam = wc.getParam();
			//make new parameter with same runtime class as old one
			Parameter newParam;
			try { newParam = oldParam.getClass().newInstance();
			} catch (Exception e) { newParam = new Parameter(); }
			//set same parameter ID and value (only important considerations)
			newParam.setId(oldParam.getId());
			newParam.setValue(oldParam.getValue());
			
			//make new WhenCondition with same service, new copy of parameter, 
			// and inverse comparison
			return new WhenCondition(wc.getService(), newParam, 
					opp.get(wc.getComp()));
		
		} else if (oldC instanceof NotCondition) {
			NotCondition nc = (NotCondition)oldC;
			
			if (null == nc.conditions) {
				//no conditions on old, no conditions on new
				return null;
			} else if (1 == nc.conditions.size()) {
				//double-negation eliminates the not
				return nc.conditions.get(0);
			} else {
				//none-of the conditions becomes any-of the conditions
				return new OrCondition(nc.conditions);
			}
		
		} else if (oldC instanceof AndCondition) {
			AndCondition ac = (AndCondition)oldC;
			//no conditions on old, none on new
			if (null == ac.conditions) return null;
			
			//DeMorgan's Law
			// NOT (a AND b AND ... ) == (NOT a) OR (NOT b) OR ...
			
			//negate all conditions of the AND
			List<Condition> cs = new ArrayList<Condition>(ac.conditions.size());
			for (Condition c : ac.conditions) {
				cs.add(not(c));
			}
			
			//OR the resulting negated conditions
			return new OrCondition(cs);
		
		} else if (oldC instanceof OrCondition) {
			OrCondition oc = (OrCondition)oldC;
			//no conditions on old, none on new
			if (null == oc.conditions) return null;
			
			//DeMorgan's Law
			// NOT (a OR b OR ... ) == (NOT a) AND (NOT b) AND ...
			
			//negate all conditions of the OR
			List<Condition> cs = new ArrayList<Condition>(oc.conditions.size());
			for (Condition c : oc.conditions) {
				cs.add(not(c));
			}
			
			//AND the resulting negated conditions
			return new AndCondition(cs);
		
		} else /* if (oldC instanceof unknown condition type) */ {
			return null;
		}
	}
}
