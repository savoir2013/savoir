// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package handlers
{
	import flash.events.Event;
	import flash.events.EventDispatcher;
	import flash.events.IEventDispatcher;
	import mx.controls.Alert;
	import mx.core.Application;
	
	import merapi.handlers.MessageHandler;
	import merapi.messages.IMessage;
	
	import messages.InitListenerMessage;
	import events.InitListenerEvent;
	public class InitListenerMessageHandler extends MessageHandler implements IEventDispatcher
	{
		public var initResp:InitListenerMessage = null;
		protected var dispatcher:EventDispatcher; 
		public function InitListenerMessageHandler()
		{
			super(InitListenerMessage.INITLISTENER);
			dispatcher = new EventDispatcher(this);
		}
		
		override public function handleMessage(message:IMessage):void {
			initResp = message as InitListenerMessage;
//			if(initResp.success ==  true){
//			    Application.application.firstInitListenerResult = true;
//			}else{
//				Application.application.firstInitListenerResult = false;
//			}
//			Application.application.initListenerReturnProcess();
			//Alert.show("received init resp msg res = " + initResp.success.toString());
			dispatchEvent(new InitListenerEvent(InitListenerEvent.INITLISTENER, initResp) );
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
