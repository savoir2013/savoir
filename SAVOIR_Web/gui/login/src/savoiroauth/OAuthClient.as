// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package savoiroauth
{
	import events.AuthEvent;
	import events.FaultEvent;
	import events.SavoirEvent;
	
	import flash.events.Event;
	import flash.events.EventDispatcher;
	import flash.events.IOErrorEvent;
	import flash.net.URLLoaderDataFormat;
	import flash.net.URLRequestMethod;
	import flash.net.URLVariables;
	
	import mx.controls.Alert;
	
	import org.iotashan.oauth.OAuthConsumer;
	import org.iotashan.oauth.OAuthToken;

	/**
	 * Base class
	 * 
	 * Create an instance of this class and call its authentication methods. 
	 * 
	 * 
	 * 
	 * @author Dorian Roy
	 * http://dasflash.com
	 */
	 
	 [Event(type="events.AuthEvent", name="requestToken")]
	 
	 [Event(type="events.AuthEvent", name="accessToken")]
	 
	 [Event(type="events.AuthEvent", name="authReuqestToken")]
	 
	 [Event(type="events.FaultEvent", name="fault")]
	 
	 
	public class OAuthClient extends EventDispatcher
	{
		// SC resource locations
		protected const requestTokenResource:String		= "/SavoirRequestToken";
		protected const accessTokenResource:String		= "/SavoirAccessToken";
		protected const authorizationResource:String = "/SavoirRequestAuthorization";
		protected const logoutResource:String = "/SavoirLogout";
		
		protected var consumerKey:String;
		protected var consumerSecret:String;
		protected var _useSandBox:Boolean;	
		protected var _responseFormat:String;
		
		protected var consumer:OAuthConsumer;
		protected var requestToken:OAuthToken;
		protected var _accessToken:OAuthToken;
		
		protected var _verificationRequired:Boolean;
		protected var callbackURL:String;
		protected var _useOAuth1_0:Boolean;
		
		private var urlValues:OAuthURLs = new OAuthURLs();
		
		private var authrizeUserReqTokenDelegate:OAuthDelegate;
				
		/**
		 * create a   API client
		 * 
		 * @param consumerKey		the consumer key you receive when registering your app with  
		 * 
		 * @param consumerSecret	the consumer secret you receive when registering your app with  
		 * 
		 * @param accessToken		a previously retrieved access token for the current user (optional)
		 * 
		 * @param useSandbox		switch between the   live system (false) and the developer
		 * 							sandbox system (true, default)
		 * 
		 * @param responseFormat	"json" or "xml" (default)
		 */
		public function OAuthClient(	consumerKey:String,
											consumerSecret:String,
											accessToken:OAuthToken=null,
											useSandbox:Boolean=true,
											responseFormat:String="xml" )
		{
			this.consumerKey = consumerKey;
			this.consumerSecret = consumerSecret;
			this.accessToken = accessToken;
			this.useSandBox = useSandbox;
			this.responseFormat = responseFormat;
			
			consumer = new OAuthConsumer(consumerKey, consumerSecret);
		}
		
		
		
		/**
		 * Token key
		*/
		public function get accessToken():OAuthToken {
			return _accessToken;
		}
		
		/**
		 * @private
		*/
		public function set accessToken(val:OAuthToken):void {
			if (val != _accessToken)
				_accessToken = val;
		}
		
		/**
		 * get a request token
		 * 
		 * this token must be traded for an access token by calling
		 * getAccessToken() after user authentication
		 * 
		 * @param callbackURL (optional)
		 * 			the user will be redirected to this page after authentication.
		 * 			this url will be extended with parameters that you can use
		 * 			on the page:
		 * 			oauth_token: the request token the user authorized or denied
		 * 			oauth_verifier: the verification code (only for OAuth 1.0a)
		 * 
		 * @return 	a  Delegate instance you can attach a listener to for
		 * 			the  Event and  Fault events
		 */
		public function getRequestToken(callbackURL:String=null):OAuthDelegate
		{
			// store callback URL
			this.callbackURL = callbackURL;
			
			// create parameter object
			var requestParams:Object = {};
			
			// add OAuth version
			requestParams.oauth_version = "1.0";
			
			// add oauth_callback parameter for OAuth 1.0a authentication
			// if no callback is passed, this parameter is set to "oob" (out-of-band)
			// @see http://oauth.googlecode.com/svn/spec/core/1.0a/drafts/3/oauth-core-1_0a.html#auth_step1
			// if useOAuth1_0 is true the callback is passed later in @see authorizeUser()
			//if (!useOAuth1_0) requestParams.oauth_callback = callbackURL || "oob";
			
			// create request
			var delegate:OAuthDelegate = createDelegate(requestTokenResource,
															URLRequestMethod.GET,
															requestParams,
															"",
															URLLoaderDataFormat.VARIABLES);
			
			// send request
			delegate.execute();
			
			// listen for response
			delegate.addEventListener(SavoirEvent.REQUEST_COMPLETE, getRequestTokenCompleteHandler);
			
			return delegate;
		}
		
		/**
		 * @private
		 */
		protected function getRequestTokenCompleteHandler(event:SavoirEvent):void	
		{
			var responseVariables:URLVariables = URLVariables(event.data);
			
			requestToken = createTokenFromURLVariables(responseVariables);
			
			// check if OAuth 1.0a parameter oauth_callback_confirmed is returned
			if (responseVariables["oauth_callback_confirmed"] == "true") {
				
				// we need to submit a verification code
				_verificationRequired = true;
			}
			
			dispatchEvent( new AuthEvent(AuthEvent.REQUEST_TOKEN, requestToken) );
		}
		
		/**
		 * open authorization page to grant data access for your app
		 * 
		 * @param targetWindow (optional)
		 * 		target window name, defaults to "_self". use "_blank" to open the
		 * 		authentication page in a new window
		 */
		public function authorizeUser(targetWindow:String="_self"):void
		{
			// create url request
			//var userAuthReq:URLRequest = new URLRequest(authURL + this.authorizationResource);
			
			// add request parameters
			// create parameter object
			var requestParams:Object = {};
			//requestParams.oauth_token = requestToken.key;
			// add OAuth version
			requestParams.oauth_version = "1.0";
			
			// add callback URL if it has been set before and useOAuth1_0 is true
			if (useOAuth1_0 && callbackURL) requestParams.oauth_callback = callbackURL;
			
			// create request
			authrizeUserReqTokenDelegate = createDelegate(authorizationResource,
															URLRequestMethod.GET,
															requestParams,
															"",
															URLLoaderDataFormat.VARIABLES,
															requestToken);
			
			// send request
			authrizeUserReqTokenDelegate.isAuthRequestTokenDelegate = true;
			authrizeUserReqTokenDelegate.execute();
			
			// listen for response
			authrizeUserReqTokenDelegate.addEventListener(AuthEvent.AUTH_REQUEST_TOKEN, authrizeRequestTokenCompleteHandler);
			authrizeUserReqTokenDelegate.addEventListener(FaultEvent.FAULT, authrizeRequestTokenFailHandler);
			// assign parameters
			//userAuthReq.data = params;
			
			// open url in browser
			//navigateToURL(userAuthReq, targetWindow);
			//userAuthReq.method = URLRequestMethod.GET;
			//var userAuthLoader:URLLoader = new URLLoader();
            //userAuthLoader.addEventListener(Event.COMPLETE,userAuthResultHandler);
            //userAuthLoader.addEventListener(IOErrorEvent.IO_ERROR,errorHandler);
            //try{
            //    userAuthLoader.load(userAuthReq);
            //}catch(error:SecurityErrorEvent){
    	    //    trace(error.text);
    	    //    Alert.show(error.text);
            //}
		}
		
		private function errorHandler(event:IOErrorEvent):void{
   	        trace(event.text);
   	        Alert.show(event.text,"IOError");
        }
		
		private function authrizeRequestTokenCompleteHandler(event:Event):void{
			trace(event.toString());
			this.getAccessToken();
		}
		
		private function authrizeRequestTokenFailHandler(event:Event):void{
			Alert.show("Either your user name or password is wrong! If you believe " +
            		"that these are correct, please make sure your computer system time " +
            		"is correct, which is required for security reasons.", "Login Error");
		}
		
		/**
		 * get access token
		 * 
		 * this token will be used for all subsequent api calls. you can store it 
		 * and reuse it the next time the current user uses your app
		 * 
		 * @param verificationCode The code that is displayed on the confirmation page
		 * 			after user authorization. This parameter is optional because it 
		 * 			won't be generated when you use legacy OAuth 1.0 authentication 
		 * 
		 * @return 	a  Delegate instance you can attach a listener to for
		 * 			the  Event and  Fault events
		 */		
		public function getAccessToken(verificationCode:String=null):OAuthDelegate
		{
			// create parameter object
			var requestParams:Object = {};
			
			// add OAuth version
			requestParams.oauth_version = "1.0";
			
			// add verification code if we're using OAuth 1.0a
			if (_verificationRequired) requestParams.oauth_verifier = verificationCode;
			
			// create request
			var delegate:OAuthDelegate = createDelegate(	accessTokenResource,
																URLRequestMethod.GET,
																requestParams,
																"",
																URLLoaderDataFormat.VARIABLES,
																requestToken);
			
			// send request
			delegate.execute();
			
			// listen for response
			delegate.addEventListener(SavoirEvent.REQUEST_COMPLETE, getAccessTokenCompleteHandler);
			delegate.addEventListener(FaultEvent.FAULT, getAccessTokenFailHandler);
			return delegate;
		}
		
		protected function getAccessTokenCompleteHandler(event:SavoirEvent):void
		{
			accessToken = createTokenFromURLVariables( URLVariables(event.data) );
			
			dispatchEvent( new AuthEvent(AuthEvent.ACCESS_TOKEN, accessToken) ); 
		}
		
		private function getAccessTokenFailHandler(event:Event):void{
			Alert.show("Either your user name or password is wrong! If you believe " +
            		"that these are correct, please make sure your computer system time " +
            		"is correct, which is required for security reasons.", "Login Error");
		}
		
		/**
		 * Make a request to the API
		 * 
		 * @param resource	the resource locator, e.g. user/userid/tracks
		 * 
		 * @param method	GET, POST, PUT or DELETE. Note that FlashPlayer	only supports GET
		 * 					and POST as of this writing. AIR supports all four methods.
		 * 
		 * @param params	(optional) a generic object containing the request parameters
		 * 
		 * @return 			a  Delegate instance you can attach a listener to for
		 * 					the  Event and  Fault events
		 */
		public function sendRequest(	resource:String,
										method:String,
										data:Object=null):OAuthDelegate
		{
			var delegate:OAuthDelegate = createDelegate(	resource,
																method,
																data,
																responseFormat,
																URLLoaderDataFormat.TEXT,
																accessToken);
			
			// send request
			delegate.execute();
			
			// return delegate so you can add a listener to it
			return delegate;
		}
		
		/**
		 * Sends the actual API call
		 * 
		 * @param resource			the resource locator, e.g. user/userid/tracks
		 * 
		 * @param method			GET, POST, PUT or DELETE. Note that FlashPlayer
		 * 							only supports GET and POST as of this writing.
		 * 							AIR supports all four methods.
		 * 
		 * @param data				(optional) the data to be sent. This can be a generic object
		 * 							containing request parameters as key/value pairs or a XML object
		 * 
		 * @param responseFormat	(optional) tells Savoir whether to render response as JSON or XML.
		 * 							Value must be ResponseFormat.JSON, .XML or an empty String 
		 * 							(default) which will also return XML 
		 * 
		 * @param dataFormat		(optional) "binary", "text" (default) or "variables"
		 * 
		 * @param requestToken		(optional) overwrites the access token. Used to pass the 
		 * 							request token when requesting an access token.
		 * 
		 * @return 					a  Delegate instance you can attach a listener to
		 * 							for the  Event and  Fault events
		 */
		protected function createDelegate(	resource:String,
											method:String,
											data:Object=null,
											responseFormat:String="",
											dataFormat:String="",
											requestToken:OAuthToken=null):OAuthDelegate
		{
			// use request token if one is passed (to get an access token)
			var token:OAuthToken = requestToken || accessToken;
			
			// create delegate
			var delegate:OAuthDelegate = new OAuthDelegate(	savoirURL+resource,
																		method,
																		consumer,
																		token,
																		data,
																		responseFormat,
																		dataFormat);

			// return delegate so you can add a listener to it
			return delegate;
		}
		
		
		// GETTER / SETTER
		
		public function get useSandBox():Boolean
		{
			return _useSandBox;
		}

		public function set useSandBox(value:Boolean):void
		{
			_useSandBox = value;
		}
		
		public function get responseFormat():String
		{
			return _responseFormat;
		}

		public function set responseFormat(value:String):void
		{
			_responseFormat = value;
		}

		/**
		 * Force OAuth 1.0 authentication (not recommended) 
		 * @return 
		 */
		public function get useOAuth1_0():Boolean
		{
			return _useOAuth1_0;
		}

		public function set useOAuth1_0(value:Boolean):void
		{
			_useOAuth1_0 = value;
		}

		/**
		 * @returns true if authentication is based on OAuth 1.0a and requires
		 * 		the verification code from the authentication page
		 */
		public function get verificationRequired():Boolean
		{
			return _verificationRequired;
		}
		
		
		// HELPER METHODS

		protected function createTokenFromURLVariables(data:URLVariables):OAuthToken
		{
			return new OAuthToken(data["oauth_token"], data["oauth_token_secret"]);
		}
		
		protected function get authURL():String
		{
			return useSandBox ? urlValues.getSandboxAuthUrl() : urlValues.getLiveAuthUrl();
		}
		
		protected function get savoirURL():String
		{
			return useSandBox ? urlValues.getSandboxUrl() : urlValues.getLiveUrl();
		}

		
	}
}
