// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package savoiroauth
{
	/**
	 * Static URL constants 
	 * 
	 * 
	 * 
	 * @author Dorian Roy
	 * http://dasflash.com
	 */
	import flash.net.SharedObject;
	
	public class OAuthURLs
	{
    	public var serverName:SharedObject;           

		private var url:String = new String();
		private var liveUrl:String = new String();
		private var sandboxUrl:String = new String();
		private var liveAuthUrl:String = new String();
		private var sandboxAuthUrl:String = new String();
		
		public function OAuthURLs():void
		{
			//Get the server name and set the url
			this.serverName = SharedObject.getLocal("serverName","/");
			url = "http://" + this.serverName.data.serverName + "/SAVOIR_WebBroker";
			
			// Set the URL's
			liveUrl = url;
			sandboxUrl = url;
			liveAuthUrl = url;
			sandboxAuthUrl = url;
		}
		
		public function getLiveUrl():String
		{
			return this.liveUrl;
		}
		
		public function getSandboxUrl():String
		{
			return this.sandboxUrl;
		}
		
		public function getLiveAuthUrl():String
		{
			return this.liveAuthUrl;
		}
		
		public function getSandboxAuthUrl():String
		{
			return this.sandboxAuthUrl;
		}
	}
}
