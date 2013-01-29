// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package events
{
	import flash.events.Event;
	
	import org.iotashan.oauth.OAuthToken;
	
	/**
	 * Event fired on successful authentication requests
	 * 
	 * 
	 * 
	 * @author Dorian Roy
	 * http://dasflash.com
	 */
	public class AuthEvent extends Event
	{
		
		public static const REQUEST_TOKEN:String = "requestToken";
		
		public static const ACCESS_TOKEN:String = "accessToken";
		
		
		public var token:OAuthToken;
		
		
		public function AuthEvent(type:String, token:OAuthToken, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			
			this.token = token;
		}
		
		override public function clone():Event
		{
			return new AuthEvent(type, token, bubbles, cancelable);
		}
	}
}
