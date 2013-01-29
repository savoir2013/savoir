// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package messages
{
	import merapi.messages.Message;
	[RemoteClass( alias="socketproxy.messages.LogMessage" ) ]
	public class LogMessage extends Message
	{
		public static const LOGINFO:String = "logInfo";
		
		public var logMessage:String;
		public function LogMessage(type:String = null)
		{
			super(type);
		}

	}
}
