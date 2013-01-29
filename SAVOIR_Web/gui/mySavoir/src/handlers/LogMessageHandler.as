// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package handlers
{
	import flash.events.Event;
	import flash.events.EventDispatcher;
	import flash.events.IEventDispatcher;
	
	import merapi.handlers.MessageHandler;
	import merapi.messages.IMessage;
	import messages.LogMessage;
	import events.LogEvent;
	public class LogMessageHandler extends MessageHandler implements IEventDispatcher
	{
		public var logMsg:LogMessage = null;
		protected var dispatcher:EventDispatcher;
		public function LogMessageHandler()
		{
			super(LogMessage.LOGINFO);
			dispatcher = new EventDispatcher(this);
		}
		
		override public function handleMessage(message:IMessage):void {
			logMsg = message as LogMessage;
			
			dispatchEvent(new LogEvent(LogEvent.LOG, logMsg) );
		}
		
		public function addEventListener(type:String, listener:Function, useCapture:Boolean=false, priority:int=0, useWeakReference:Boolean=false):void {
			dispatcher.addEventListener(type, listener, useCapture, priority, useWeakReference);
		}
		
		public function removeEventListener(type:String, listener:Function, useCapture:Boolean=false):void {
			dispatcher.removeEventListener(type, listener, useCapture);
		}
		
		public function dispatchEvent(event:Event):Boolean {
			return dispatcher.dispatchEvent(event);
		}
		
		public function hasEventListener(type:String):Boolean {
			return dispatcher.hasEventListener(type);
		}
		
		public function willTrigger(type:String):Boolean {
			return dispatcher.willTrigger(type);
		}

	}
}
