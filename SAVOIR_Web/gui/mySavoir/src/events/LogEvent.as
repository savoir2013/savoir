// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package events
{
	import flash.events.Event;
	import messages.LogMessage;
	public class LogEvent extends Event
	{
		public static const LOG:String = "logEvent";
		
		public var message:LogMessage;
		public function LogEvent(type:String, message:LogMessage)
		{
			super(type);
			
			this.message = message;
		}
		
		override public function clone():Event {
			return new LogEvent(this.type, this.message) as Event;
		}
		
		override public function toString():String {
			return "[ LogEvent type="+this.type+" ]";
		}

	}
}
