// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.utils;

/**
 * Callback interface for {@link OverdueCollector<O>}, called when an object 
 * is collected. When the {@code OverdueCollector} collects an object, it will 
 * be passed as a parameter to {@link #notifyOverdue(Object)}. 
 * 
 * @author Aaron Moss
 *
 * @param <O>	The type of objects this {@code OverdueListener} cares about
 */
public interface OverdueListener<O> {

	/**
	 * Called when an object is overdue.
	 * 
	 * @param obj		The object that is overdue.
	 */
	public void notifyOverdue(O obj);
}
