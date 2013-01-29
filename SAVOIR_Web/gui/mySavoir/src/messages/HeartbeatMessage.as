// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package messages
{
	import merapi.messages.Message;
	[RemoteClass( alias="socketproxy.messages.HeartbeatMessage" ) ]
	public class HeartbeatMessage extends Message{
		public static const HEARTBEAT:String = "heartbeat";
		public var message:String;
		public function HeartbeatMessage(type:String = null)
		{
			super(type);
		}
		
	}
}
