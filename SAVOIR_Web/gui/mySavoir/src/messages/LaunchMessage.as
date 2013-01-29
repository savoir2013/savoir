// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package messages
{
	import merapi.messages.Message;
	[RemoteClass( alias="socketproxy.messages.LaunchMessage" ) ]
	public class LaunchMessage extends Message
	{
		public static const LAUNCH:String = "launch";
		public var launchURI:String;
//		public var program:String;
//		public var launchArgs:String;
		
		public function LaunchMessage(type:String = null)
		{
			super(type);
		}

	}
}
