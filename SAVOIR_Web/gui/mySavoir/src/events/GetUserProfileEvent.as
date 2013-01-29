// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package events
{
	import flash.events.Event;

	public class GetUserProfileEvent extends Event
	{
		public var getUserProfileXml:XML;
		public static const GET_USER_PROFILE:String = "get_user_profile";
		public function GetUserProfileEvent(type:String, respXml:XML, bubbles:Boolean=true, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			getUserProfileXml = respXml;
		}
		override public function clone():Event {
            return new GetUserProfileEvent(type, getUserProfileXml, bubbles, cancelable);
        }
	}
}
