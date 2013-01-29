// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package messages
{
	import merapi.messages.Message;
	[RemoteClass( alias="socketproxy.messages.InitListenerMessage" ) ]
	public class InitListenerMessage extends Message
	{
		public static const INITLISTENER:String = "initListener";
		
		public var server:String;
		public var sessionID:int;
		public var userSessionID:int;
		public var url:String;
		public var isListenLogInfo:Boolean;
		public var success:Boolean;
		public function InitListenerMessage(type:String = null)
		{
			super(type);
		}

	}
}
