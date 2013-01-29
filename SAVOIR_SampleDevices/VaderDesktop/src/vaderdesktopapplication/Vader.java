// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package vaderdesktopapplication;
import java.util.Observable;


/**
 * Vader
 *
 * The model class of our little project.  It starts a thread that
 * simulates a heart beat.  The model presents a method that other
 * objects (controllers) can use to alter the rate of the heart beat.
 *
 *@author <a href="http://www.cs.indiana.edu/~cbaray/">cristobal baray</a> cbaray@cs.indiana.edu 
 */
public class Vader
	extends Observable 
	implements Runnable
	{
		

	/** The heartbeat unit duration */
	private static final long DURATION = 60;
        
        private long sleepSpan = 100;

	
	/** The number of beats counter */
	private long numberOfBeats = 0;
	
	/** The state of the model */
	private boolean running = false;

        private Thread heartBeatThread;

        private long heartbeatRate;
	
	/** An empty constructor */
	public Vader() {
//		running = true;
		heartBeatThread = new Thread(this);
	}


	
	/**
	 * The "heart" of the Vader model.
	 * The loop increments the beat counter, then sends updates
	 * to whoever subscribed, then goes to sleep for a 
	 * certain amount of time. The amount of time depends 
	 * on the current excitementLevel of the model.
 	 *
 	 */
	public void run() {
		while (running) {
			numberOfBeats++;
			updateObservers();
			try {
				Thread.sleep(sleepSpan);
                                System.out.println("SleepSpan is " + sleepSpan);
			} catch (InterruptedException e) {
			}
			
		}
	}
	
	/**
	 * Set the changed flag in the Observable object
	 * and then notify the observers with the current
	 * beat count.
	 *
	 * Each model might have it's own process for updating
	 * observers - for instance, they might need to build new
	 * data objects before sending the update - this is a good
	 * reason for separating the update method in the model.
	 * Also, if there is more than one way to trigger the update,
	 * a single update method is the smart way to go.
	 * Also, if there is more than one update in one model, 
	 * it might benefit from having separate methods as well.
	 */
	private void updateObservers() {
		setChanged();
		notifyObservers(new Long(numberOfBeats));
	}
	
	

        public void adjustHeartbeatRate(long rate){
            heartbeatRate = rate;
            long span = (DURATION *1000)/rate;
            sleepSpan = span;
        }
	
	/**
	 * Halts the heart...with no way to start it up again...
	 */
	public void stopHeartBeat() {
		running = false;
	}

        public void startHeartBeat(){
            if(heartBeatThread != null ){
               running = true;
               this.heartBeatThread.start();
               
            }
        }

        public boolean isRunning(){
            return running;
        }

        public long rate(){
            return this.heartbeatRate;
        }
}
