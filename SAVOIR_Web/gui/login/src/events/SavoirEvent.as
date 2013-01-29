// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package events
{
	import flash.events.Event;
	
	/**
	 * Basic event fired on successful requests to the savoir
	 * 
	 * 
	 * 
	 * @author Dorian Roy
	 * http://dasflash.com
	 */
	public class SavoirEvent extends Event
	{
		
		public static const REQUEST_COMPLETE:String = "requestComplete";
		
		
		/**
		 * contains the parsed response of an API call if it returns
		 * XML or JSON format
		 */
		public var data:Object;
		
		/**
		 * contains the raw response of an API call 
		 */
		public var rawData:Object;
		
		
		public function SavoirEvent(type:String, data:Object, rawData:Object, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			
			this.data = data;
			
			this.rawData = rawData;
		}
		
		override public function clone():Event
		{
			return new SavoirEvent(type, data, rawData, bubbles, cancelable);
		}
	}
}
