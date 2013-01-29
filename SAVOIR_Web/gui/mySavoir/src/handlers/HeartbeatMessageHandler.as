// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package handlers
{
	import mx.controls.Alert;
	import merapi.handlers.MessageHandler;
	import merapi.messages.IMessage;
	
	import messages.HeartbeatMessage;
	public class HeartbeatMessageHandler extends MessageHandler
	{
		public function HeartbeatMessageHandler()
		{
			super(HeartbeatMessage.HEARTBEAT);
		}
		
		override public function handleMessage(message:IMessage):void {
			var heartbeatMsg:HeartbeatMessage = message as HeartbeatMessage;
			//Alert.show("Received heartbeat!");
			//heartbeatMsg.message = "I am still here!";
			//heartbeatMsg.send();
		
		}

	}
}
