// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.utils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Garbage collector to aid in closing memory leaks. Runs in its own thread, 
 * sleeping for {@code granularity} milliseconds before waking up and 
 * collecting any objects older than {@code maxAge} milliseconds. The overdue 
 * objects are stored in a combined linked list and hashtable map - the linked 
 * list is ordered by insertion/last accessed time, and traversed to remove 
 * overdue objects, while the hashtable is indexed by object, and used to 
 * remove objects that have been freed normally.
 * 
 * @author Aaron Moss
 *
 * @param <O>	The type of objects this {@code OverdueCollector} stores
 */
public class OverdueCollector<O> {

	private static final long DEFAULT_MAX_AGE  = 30000; /* 30s */
	private static final long DEFAULT_GRANULARITY = 10000; /* 10s */
	
	/** Reference to the caller of this collector */
	private OverdueListener<O> listener;
	
	/** Maps objects to submission time (ms since epoch) */
	private LinkedHashMap<O, Long> queue;
	
	/** Maximum age allowed for objects, in milliseconds */
	private long maxAge;
	/** Time this collector will wait between checks, in milliseconds */
	private long granularity;
	/** is this collector running? */
	private boolean running;
	
	/**
	 * Initializes this overdue collection thread, and starts it running with 
	 * the default maximum message age (30s) and collection granularity (10s), 
	 * and default name.
	 * 
	 * @param listener		The object to be notified of overdue objects
	 */
	public OverdueCollector(OverdueListener<O> listener) {
		this(listener, DEFAULT_MAX_AGE, DEFAULT_GRANULARITY, null);
	}
	
	/**
	 * Initializes this overdue collection thread, and starts it running with 
	 * the default maximum message age (30s) and collection granularity (10s)
	 * 
	 * @param listener		The object to be notified of overdue objects
	 * @param maxAge		Maximum age for objects (in milliseconds)
	 * @param granularity	Time between collection runs (in milliseconds)
	 * @param name			The name of this thread
	 */
	public OverdueCollector(OverdueListener<O> listener, String name) {
		this(listener, DEFAULT_MAX_AGE, DEFAULT_GRANULARITY, name);
	}
	
	/**
	 * Initializes this overdue collection thread, and starts it running with 
	 * the default name.
	 * 
	 * @param listener		The object to be notified of overdue objects
	 * @param maxAge		Maximum age for objects (in milliseconds)
	 * @param granularity	Time between collection runs (in milliseconds)
	 */
	public OverdueCollector(OverdueListener<O> listener, long maxAge, 
			long granularity) {
		this(listener, maxAge, granularity, null);
	}
	
	/**
	 * Initializes this overdue collection thread, and starts it running
	 * 
	 * @param listener		The object to be notified of overdue objects
	 * @param maxAge		Maximum age for objects (in milliseconds)
	 * @param granularity	Time between collection runs (in milliseconds)
	 * @param name			The name of this thread
	 */
	public OverdueCollector(OverdueListener<O> listener, long maxAge, 
			long granularity, String name) {
		this.listener = listener;
		
		//initialize local variables
		this.queue = new LinkedHashMap<O, Long>();
		this.maxAge = maxAge;
		this.granularity = granularity;
		this.running = true;
		
		//start collection thread running
		Thread t = new Thread(){
			public void run() {
				while (OverdueCollector.this.running) {
					//collect overdue messages
					collectOverdue();
					
					//sleep and wait for next time to check
					try {
						Thread.sleep(OverdueCollector.this.granularity);
					} catch (InterruptedException e) {
						continue;
					}
				}
			}
		};
		if (name != null) t.setName(name);
		t.start();
	}
	
	public void setMaxAge(long maxAge) {
		this.maxAge = maxAge;
	}

	public void setGranularity(long granularity) {
		this.granularity = granularity;
	}
		
	/**
	 * Stops this collector, terminating its collection thread.
	 */
	public void stop() {
		this.running = false;
	}
	
	/**
	 * Submits a new object for collection {@code maxAge} ms from now
	 * 
	 * @param obj	The object
	 */
	public synchronized void submit(O obj) {
		queue.put(obj, System.currentTimeMillis() + maxAge);
	}
	
	/**
	 * Renews an objects lifetime to {@code maxAge} ms, if it is in the 
	 * collection queue.
	 * 
	 * @param obj	The object
	 */
	public synchronized void touch(O obj) {
		Long l = queue.remove(obj);
		if (l != null) queue.put(obj, System.currentTimeMillis() + maxAge);
	}
	
	/**
	 * Withdraws an object from the collection queue
	 * 
	 * @param obj	The object
	 */
	public synchronized void withdraw(O obj) {
		queue.remove(obj);
	}
	
	/**
	 * Traverse the collection of objects in access order, removing 
	 * any that have not been accessed for longer than {@code maxAge}, and 
	 * notifying the listener that they are overdue. 
	 */
	public synchronized void collectOverdue() {
		//escape hatch if there's nothing to collect
		if (queue.isEmpty()) return;
		
		long now = System.currentTimeMillis();
		
		//note that the collection view DOES NOT constitute an access
		Iterator<Map.Entry<O, Long>> iter = queue.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<O, Long> entry = iter.next();
			long then = entry.getValue();
			
			if (now /* is after */ > then) {
				//remove object
				iter.remove();
				
				//notify overdue object
				listener.notifyOverdue(entry.getKey());
			} else /* if now is before then */ {
				//objects still within time limit, stop collecting
				break;
			}
		}
	}
	
	protected void finalize() throws Throwable {
		super.finalize();
		this.running = false;
	}
}
