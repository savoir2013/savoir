// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package events
{
	import flash.events.Event;
	
	/**
	 * Event fired on erroneous API requests
	 * 
	 *
	 * 
	 * @author Dorian Roy
	 * http://dasflash.com
	 */
	public class FaultEvent extends Event
	{
		
		public static const FAULT:String = "fault";
		
		/**
		 * contains a text message describing the error 
		 */
		public var message:String;
		
		/**
		 * contains the HTTP code in case of an HTTP response 
		 */
		public var errorCode:int;
		
		
		public function FaultEvent(type:String, message:String, errorCode:int=0, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			
			this.message = message;
			
			this.errorCode = errorCode;
		}
		
		override public function clone():Event
		{
			return new FaultEvent(type, message, errorCode, bubbles, cancelable);
		}
	}
}
