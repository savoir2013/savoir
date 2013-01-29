// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package events
{
	import flash.events.Event;
	import messages.InitListenerMessage;
	public class InitListenerEvent extends Event
	{
		public static const INITLISTENER:String = "initListenerEvent";
		
		public var message:InitListenerMessage;
		public function InitListenerEvent(type:String, message:InitListenerMessage)
		{
			super(type);
			
			this.message = message;
		}
		
		override public function clone():Event {
			return new InitListenerEvent(this.type, this.message) as Event;
		}
		
		override public function toString():String {
			return "[ InitListenerEvent type="+this.type+" ]";
		}

	}
}
