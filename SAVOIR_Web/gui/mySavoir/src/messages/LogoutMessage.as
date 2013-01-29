// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package messages
{
	import merapi.messages.Message;
	[RemoteClass( alias="socketproxy.messages.LogoutMessage" ) ]
	public class LogoutMessage extends Message
	{
		public static const LOGOUT:String = "logout";
		public function LogoutMessage(type:String = null)
		{
			super(type);
		}

	}
}
