// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package events
{
	import flash.events.Event;

	public class JoinSessionEvent extends Event
	{
		public var joinRespXml:XML;
		public static const JOIN_SESSION:String = "join_session";
		public function JoinSessionEvent(type:String, respXml:XML, bubbles:Boolean=true, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			joinRespXml = respXml;
		}
		
		override public function clone():Event {
            return new JoinSessionEvent(type,joinRespXml, bubbles, cancelable);
        }
		
	}
}
