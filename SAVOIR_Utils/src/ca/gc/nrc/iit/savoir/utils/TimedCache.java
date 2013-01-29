// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.utils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A map that includes a timeout, so that elements may be automatically removed 
 * when they have expired. Runs in its own thread, sleeping for 
 * {@code granularity} milliseconds before waking up and collecting any objects 
 * older than {@code maxAge} milliseconds. The overdue objects are stored in a 
 * combined linked list and hashtable map - the linked list is ordered by 
 * insertion/last accessed time, and traversed to remove overdue objects, while 
 * the hashtable is indexed by object, and used to remove objects that have 
 * been freed normally.
 * 
 * @author Aaron Moss
 *
 * @param <K>	The key type of the cache
 * @param <V>	The value type of the cache
 */
public class TimedCache<K, V> {

	/**
	 * An instantiable map entry
	 *
	 * @param <K>		The key type of the entry
	 * @param <V>		The value type of the entry
	 */
	private static class MapEntry<K, V> implements Map.Entry<K, V> {

		private K key;
		private V value;
		
		public MapEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}
		
		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			V oldValue = this.value;
			this.value = value;
			return oldValue;			
		}
		
	}
	
	/**
	 * Value with associated timestamp.
	 *
	 * @param <V>	Value type
	 */
	private static class TimedValue<V> {
		public V value;
		public long timestamp;
		
		public TimedValue(V value, long timestamp) {
			this.value = value;
			this.timestamp = timestamp;
		}
	}
	
	private static final long DEFAULT_MAX_AGE  = 30000; /* 30s */
	private static final long DEFAULT_GRANULARITY = 10000; /* 10s */
	
	/** Reference to the caller of this collector */
	private OverdueListener<Map.Entry<K, V>> listener;
	
	/** Maps objects to submission time (ms since epoch) */
	private LinkedHashMap<K, TimedValue<V>> queue;
	
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
	public TimedCache(OverdueListener<Map.Entry<K, V>> listener) {
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
	public TimedCache(OverdueListener<Map.Entry<K, V>> listener, 
			String name) {
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
	public TimedCache(OverdueListener<Map.Entry<K, V>> listener, 
			long maxAge, long granularity) {
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
	public TimedCache(OverdueListener<Map.Entry<K, V>> listener, 
			long maxAge, long granularity, String name) {
		
		this.listener = listener;
		
		//initialize local variables
		this.queue = new LinkedHashMap<K, TimedValue<V>>();
		this.maxAge = maxAge;
		this.granularity = granularity;
		this.running = true;
		
		//start collection thread running
		Thread t = new Thread(){
			public void run() {
				while (TimedCache.this.running) {
					//collect overdue messages
					collectOverdue();
					
					//sleep and wait for next time to check
					try {
						Thread.sleep(TimedCache.this.granularity);
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
	 * @param key		The key to find this object by
	 * @param obj		The object submitted for collection
	 */
	public synchronized void submit(K key, V obj) {
		queue.put(key, 
				new TimedValue<V>(obj, System.currentTimeMillis() + maxAge));
	}
	
	/**
	 * Renews an objects lifetime to {@code maxAge} ms, if it is in the 
	 * collection queue.
	 * 
	 * @param key	The object's key
	 * 
	 * @return The object's current value, null for none such
	 */
	public synchronized V touch(K key) {
		TimedValue<V> entry = queue.remove(key);
		if (entry != null) {
			entry.timestamp = System.currentTimeMillis() + maxAge;
			queue.put(key, entry);
			return entry.value;
		} else {
			return null;
		}
	}
	
	/**
	 * Withdraws an object from the collection queue
	 * 
	 * @param key	The object's key
	 * 
	 * @return The object's current value, null for none such
	 */
	public synchronized V withdraw(K key) {
		TimedValue<V> entry = queue.remove(key);
		return (entry == null) ? null : entry.value;
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
		Iterator<Map.Entry<K, TimedValue<V>>> iter = 
			queue.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<K, TimedValue<V>> entry = iter.next();
			TimedValue<V> value = entry.getValue();
			long then = value.timestamp;
			
			if (now /* is after */ > then) {
				//remove object
				iter.remove();
				
				//notify overdue object
				listener.notifyOverdue(
						new MapEntry<K, V>(entry.getKey(), value.value));
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
