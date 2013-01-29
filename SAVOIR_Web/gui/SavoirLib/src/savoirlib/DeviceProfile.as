// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package savoirlib
{
	// ================================== External Modules =======================================

   	import mx.rpc.events.FaultEvent;
   	import mx.rpc.events.ResultEvent;
   	import mx.rpc.http.HTTPService;
   	import flash.events.EventDispatcher;
   	import flash.net.SharedObject;

	//////////////////////////////////////////////////////////////////////////////////////////////
	// Class:			DeviceProfile
	// Author:			Justin Hickey
	// Purpose:			This class is designed to read and parse a device profile XML file. The
	//					constructor is called without any parameters because this class is
	//					usually used in conjuntion with a ChangeWatcher (see below), and in order
	//					for the ChangeWatcher to work properly, an instance of this class must be
	//					instantiated first. After the class is instantiated, the ChangeWatcher is
	//					set to watch the initDone variable, then the class is initialized by
	//					individually setting the resourceId and resourceName variables. The
	//					processDeviceProfile function must then be called to initiate the parsing
	//					of the device profile XML file. An example for using this class is as
	//					follows:
	//
	//   					devProfile = new DeviceProfile();
	//						thisWatcher = ChangeWatcher.watch(devProfile, "initDone", handleProfile);
	//
	//						devProfile.resourceId = resourceId;
	//						devProfile.resourceName = resourceName;
	//						devProfile.processDeviceProfile();
	//
	//					Note that handleProfile is the event handler that is executed when the
	//					initDone variable is changed.
	//
	//					Also note that the functionality of this class is highly dependent on the
	//					schema of the XML. Any changes to the schema will probably require changes
	//					to this class. An example of the XML that this class parses is as follows:
	//
	//						<profileMessage action="reportProfile">
	//
	//							<service ID="14"
	//								name="Device"
	//								description="Description of Device"
	//								maxSimultaneousUsers="1"
	//								ipAddress="255.255.255.255"
	//								portNumber="80"
	//								contactName="John Doe"
	//								contactEmail="jdoe@company.com"
	//								timestamp="date:date-time()" >
	//								
	//								<widget title="Some Device">
	//									<description>Description of device</description>
	//									<icon name="picture" format="png"/>
	//									<choices baseUri="savoir://device">
	//										<choice ID="activityList"
	//											label="Dataset"
	//											type="activity"
	//											paramId="inputData"/>
	//									</choices>
	//								</widget>
	//
	//								<activities>
	//									<activity ID="1"
	//										name="Some activity 1"
	//										paramValue="value1"/>
	//									<activity ID="2"
	//										name="Some activity 2"
	//										paramValue="value2"/>
	//								</activities>
	//
	//								<globalParameters>
	//									<globalParameter ID="1"
	//										name="mapid"
	//										dataType="xs:integer"/>
	//									<globalParameter ID="2"
	//										name="mnodeid"
	//										dataType="xs:integer"/>
	//								</globalParameters>
	//							</service>
	//						</profileMessage>
	//
	//					Note that this is an example. Please see the appropriate documentation for
	//					a complete description of the XML schema since there are many options to
	//					this schema.
	//
	//					Since this class uses an HTTP service to load the XML file, the caller
	//					needs to wait for the XML file to be parsed. Therefore, using a
	//					ChangeWatcher as described above is an efficient way to detect when the
	//					parsing has finished. Since this class does not guarantee that a profile
	//					will always be loaded, any instantiation of this class should check the
	//					the various flags before accessing any part of the profile information.
	//					Valid flags are as follows:
	//
	//						haveWidget 		- indicates there is information for a graphical widget
	//						haveBaseRef 	- indicates the base URI is a reference (see schema
	//									  		documentation)
	//						haveChoices		- indicates one or more choices are present
	//						haveActivities	- indicates that activities are present
	//						haveGlobals		- indicates global parameters are present
	//						haveError		- indicates that the profile parsing caused an error
	//
	//					Note that the haveError flag should ALWAYS be checked first before any
	//					other processing in the handler function that is triggered by the
	//					initDone flag.
	//
	//					This class only defines setter functions for the resourceId and
	//					resourceName variables. All other variables are derived from the device
	//					profile and thus, should not be set directly. Therefore, only getter
	//					functions are defined for these variables. The only exception to this is
	//					the initDone variable which is required to have both a setter and getter
	//					in order for the proper events to be generated when the class is
	//					utilized. Thus, the initDone variable should NEVER be set outside of this
	//					class.
	//
	//					This class must generate events in order for a change in the initDone
	//					flag to be detected. Therefore, this class extends the EventDispatcher
	//					class, which is required for any class that generates events.
	//
	// Date Created:	Nov 24, 2010
	//////////////////////////////////////////////////////////////////////////////////////////////
	public class DeviceProfile extends EventDispatcher
	{
		// ================================ Class Constants ======================================
		
		// none
		
		// ============================= Class Public Members ====================================

		public var serverName:SharedObject;			// Name of SAVOIR server shared object 
		
		// ============================= Class Private Members ===================================
		
		private var serverNameStr:String = new String();	// SAVOIR server name
		private var profileUrl:String = new String();		// URL of profile XML file

		// ============================ Class Private Properties =================================

		private var _resourceId:String;				// Resource ID of device
		private var _resourceName:String;			// Resource name of device
		private var _errorMsg:String;				// Error message text if any

		[Bindable]
		private var _initDone:Boolean;				// Flag to indicate if class initialization done
		private var _haveError:Boolean;				// Flag to indicate if there was an error
		private var _haveWidget:Boolean;			// Flag to indicate if device has a widget
		private var _haveBaseRef:Boolean;			// Flag to indicate if base URI is a reference
		private var _haveChoices:Boolean;			// Flag to indicate if device has choices
		private var _haveActivities:Boolean;		// Flag to indicate if device has activities
		private var _haveGlobals:Boolean;			// Flag to indicate if device has global parameters
		
		private var _devProfileXml:XML;				// Complete XML structure of device profile
		private var _serviceXmlList:XMLList;		// Complete XML structure of service
		private var _widgetXmlList:XMLList;			// Complete XML structure of widget description
		private var _fullChoicesXmlList:XMLList;	// Complete XML structure of widget choices
		private var _choicesXmlList:XMLList;		// XML list of each choice
		private var _fullActivityXmlList:XMLList;	// Complete XML structure of activities
		private var _activityXmlList:XMLList;		// XML list of each activity
		private var _fullGlobalXmlList:XMLList;		// Complete XML structure of global parameters
		private var _globalXmlList:XMLList;			// XML list of each global parameter
		private var _widgetTitle:String;			// Title bar text
		private var _widgetDesc:String;				// Description of widget
		private var _widgetIconMed:String;			// URL of medium sized icon
		private var _widgetIconSmall:String;		// URL of small sized icon
		private var _baseUri:String;				// Base URI for launch command - may be reference
		private var _baseUriRef:String;				// Reference to option that has base URI
		
		// ================================= Constructor =========================================

		//////////////////////////////////////////////////////////////////////////////////////////
		// Constructor:    	DeviceProfile
		// Author:       	Justin Hickey
		// Purpose:      	To initialize the instance of the class. This sets some of the values
		//					of the data members. No parameters are passed in, since typically, the
		//					initDone flag needs to be watched by a ChangeWatcher before the
		//					device profile XML file can be parsed. Therefore, the constructor
		//					simply sets some values.
		// Input Vars:   	none
		// Return Vars:  	none
		// Date Created: 	Nov 24, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function DeviceProfile():void
		{
			// Get the server name
			this.serverName = SharedObject.getLocal("serverName","/");
			serverNameStr = this.serverName.data.serverName;
			
			// Set the member values
			_resourceId = null;
			_resourceName = null;
			_errorMsg = new String("No errors");
			
			// Set the flags to false
			_initDone = false;
			_haveError = false;
			_haveWidget = false;
			_haveBaseRef = false;
			_haveChoices = false;
			_haveActivities = false;
			_haveGlobals = false;
		}

		// =============================== Setter Functions ======================================

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set resourceId
		// Author:       	Justin Hickey
		// Purpose:      	To set the resourceId member of the class
		// Input Vars:   	value - value of the new resourceId
		// Return Vars:  	none
		// Date Created: 	Nov 24, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set resourceId(value:String):void
		{
			if (_resourceId == null)
			{
				_resourceId = new String(value);
			}
			else
			{
				_resourceId = value;
			}
   		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set resourceName
		// Author:       	Justin Hickey
		// Purpose:      	To set the resourceName member of the class
		// Input Vars:   	value - value of the new resourceName
		// Return Vars:  	none
		// Date Created: 	Nov 24, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set resourceName(value:String):void
		{
			if (_resourceName == null)
			{
				_resourceName = new String(value);
			}
			else
			{
				_resourceName = value;
			}
   		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set initDone
		// Author:       	Justin Hickey
		// Purpose:      	To set the initDone member of the class. Note that this function
		//					should NEVER be called outside of this class. This member is meant to
		//					be read only but for the event mechanism to work properly, it needs
		//					to have a setter function defined. 
		// Input Vars:   	value - value of the new initDone
		// Return Vars:  	none
		// Date Created: 	Nov 26, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set initDone(value:Boolean):void
		{
			_initDone = value;
		}

		// =============================== Getter Functions ======================================
		
		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get resourceId
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the resourceId member of the class
		// Input Vars:   	none
		// Return Vars:  	_resourceId
		// Date Created: 	Nov 24, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get resourceId():String
		{
			return _resourceId;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get resourceName
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the resourceName member of the class
		// Input Vars:   	none
		// Return Vars:  	_resourceName
		// Date Created: 	Nov 24, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get resourceName():String
		{
			return _resourceName;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get errorMsg
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the errorMsg member of the class
		// Input Vars:   	none
		// Return Vars:  	_errorMsg
		// Date Created: 	Dec 09, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get errorMsg():String
		{
			return _errorMsg;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get initDone
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the initDone member of the class
		// Input Vars:   	none
		// Return Vars:  	_initDone
		// Date Created: 	Nov 24, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		[Bindable]
		public function get initDone():Boolean
		{
			return _initDone;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get haveError
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the haveError member of the class
		// Input Vars:   	none
		// Return Vars:  	_haveError
		// Date Created: 	Dec 09, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get haveError():Boolean
		{
			return _haveError;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get haveWidget
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the haveWidget member of the class
		// Input Vars:   	none
		// Return Vars:  	_haveWidget
		// Date Created: 	Dec 08, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get haveWidget():Boolean
		{
			return _haveWidget;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get haveBaseRef
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the haveBaseRef member of the class
		// Input Vars:   	none
		// Return Vars:  	_haveBaseRef
		// Date Created: 	Dec 15, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get haveBaseRef():Boolean
		{
			return _haveBaseRef;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get haveChoices
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the haveChoices member of the class
		// Input Vars:   	none
		// Return Vars:  	_haveChoices
		// Date Created: 	Dec 15, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get haveChoices():Boolean
		{
			return _haveChoices;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get haveActivities
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the haveActivities member of the class
		// Input Vars:   	none
		// Return Vars:  	_haveActivities
		// Date Created: 	Dec 09, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get haveActivities():Boolean
		{
			return _haveActivities;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get haveGlobals
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the haveGlobals member of the class
		// Input Vars:   	none
		// Return Vars:  	_haveGlobals
		// Date Created: 	Dec 15, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get haveGlobals():Boolean
		{
			return _haveGlobals;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get devProfileXml
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the devProfileXml member of the class
		// Input Vars:   	none
		// Return Vars:  	_devProfileXml
		// Date Created: 	Nov 24, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get devProfileXml():XML
		{
			return _devProfileXml;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get serviceXmlList
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the serviceXmlList member of the class
		// Input Vars:   	none
		// Return Vars:  	_serviceXmlList
		// Date Created: 	Dec 20, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get serviceXmlList():XMLList
		{
			return _serviceXmlList;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get widgetXmlList
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the widgetXmlList member of the class
		// Input Vars:   	none
		// Return Vars:  	_widgetXmlList
		// Date Created: 	Nov 29, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get widgetXmlList():XMLList
		{
			return _widgetXmlList;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get fullChoicesXmlList
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the fullChoicesXmlList member of the class
		// Input Vars:   	none
		// Return Vars:  	_fullChoicesXmlList
		// Date Created: 	Dec 09, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get fullChoicesXmlList():XMLList
		{
			return _fullChoicesXmlList;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get choicesXmlList
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the choicesXmlList member of the class
		// Input Vars:   	none
		// Return Vars:  	_choicesXmlList
		// Date Created: 	Dec 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get choicesXmlList():XMLList
		{
			return _choicesXmlList;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get fullActivityXmlList
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the fullActivityXmlList member of the class
		// Input Vars:   	none
		// Return Vars:  	_fullActivityXmlList
		// Date Created: 	Dec 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get fullActivityXmlList():XMLList
		{
			return _fullActivityXmlList;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get activityXmlList
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the activityXmlList member of the class
		// Input Vars:   	none
		// Return Vars:  	_activityXmlList
		// Date Created: 	Dec 09, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get activityXmlList():XMLList
		{
			return _activityXmlList;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get fullGlobalXmlList
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the fullGlobalXmlList member of the class
		// Input Vars:   	none
		// Return Vars:  	_fullGlobalXmlList
		// Date Created: 	Dec 15, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get fullGlobalXmlList():XMLList
		{
			return _fullGlobalXmlList;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get globalXmlList
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the globalXmlList member of the class
		// Input Vars:   	none
		// Return Vars:  	_globalXmlList
		// Date Created: 	Dec 15, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get globalXmlList():XMLList
		{
			return _globalXmlList;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get widgetTitle
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the widgetTitle member of the class
		// Input Vars:   	none
		// Return Vars:  	_widgetTitle
		// Date Created: 	Nov 29, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get widgetTitle():String
		{
			return _widgetTitle;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get widgetDesc
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the widgetDesc member of the class
		// Input Vars:   	none
		// Return Vars:  	_widgetDesc
		// Date Created: 	Nov 24, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get widgetDesc():String
		{
			return _widgetDesc;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get widgetIconMed
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the widgetIconMed member of the class
		// Input Vars:   	none
		// Return Vars:  	_widgetIconMed
		// Date Created: 	Nov 24, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get widgetIconMed():String
		{
			return _widgetIconMed;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get widgetIconSmall
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the widgetIconSmall member of the class
		// Input Vars:   	none
		// Return Vars:  	_widgetIconSmall
		// Date Created: 	Nov 29, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get widgetIconSmall():String
		{
			return _widgetIconSmall;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get baseUri
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the baseUri member of the class
		// Input Vars:   	none
		// Return Vars:  	_baseUri
		// Date Created: 	Dec 09, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get baseUri():String
		{
			return _baseUri;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get baseUriRef
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the baseUriRef member of the class
		// Input Vars:   	none
		// Return Vars:  	_baseUriRef
		// Date Created: 	Dec 15, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get baseUriRef():String
		{
			return _baseUriRef;
		}

		// =========================== Interaction Event Handlers ================================

		// none
			
		// =========================== Application Event Handlers ================================

		// none

		// =============================== Public Functions ======================================
		
		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	processDeviceProfile
		// Author:       	Justin Hickey
		// Purpose:      	This function is required to be able to process the device profile
		//					XML file. The caller needs to call this function after setting the
		//					resourceId, and resourceName variables. It simply sets the initDone
		//					variable and then calls the processProfile function.
		// Input Vars:   	none
		// Return Vars:  	none
		// Date Created: 	Nov 26, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function processDeviceProfile():void
		{
			// Make sure the initDone flag is false - don't use the "this" object or it will
			// trigger a property change event
			_initDone = false;
			
			// Process the device profile XML file
			processProfile();
		}

		// =============================== Private Functions =====================================

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	processProfile
		// Author:       	Justin Hickey
		// Purpose:      	To determine the filename of the device profile XML file and then call
		//					the loadDeviceProfile function.
		// Input Vars:   	none
		// Return Vars:  	none
		// Date Created: 	Nov 25, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		private function processProfile():void
		{
			// Check if the resource values were initialized
			if (this.resourceId == null || this.resourceName == null)
			{
				// Set an error message and the error flag
				_errorMsg = "ERROR: Resource information for device profile XML filename " 
					+ "not defined.\n"
					+ "Generated while processing the device profile.\n\n"
					+ "Please contact your Savoir Admin regarding this issue and send "
   		    		+ "him/her the above error\n";
				
				_haveError = true;
				
				// Set remaining variables to null
				_devProfileXml = null;
				_serviceXmlList = null;
				_widgetXmlList = null;
				_fullChoicesXmlList = null;
				_choicesXmlList = null;
				_fullActivityXmlList = null;
				_activityXmlList = null;
				_fullGlobalXmlList = null;
				_globalXmlList = null;
				_widgetDesc = null;
				_widgetTitle = null;
				_widgetIconMed = null;
				_widgetIconSmall = null;
				_baseUri = null;
				_baseUriRef = null;
				
				// Set the initDone flag to trigger the change event
				this.initDone = true;
				
				// Return since we don't have the name of the profile
				return;
			}
			
			// Form the profile XML file URL
			profileUrl = "http://" + serverNameStr + "/profileRepos/" + this.resourceId
				+ "_" + this.resourceName + ".xml";
				
			// Load the profile
			loadDeviceProfile();
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	loadDeviceProfile
		// Author:       	Justin Hickey
		// Purpose:      	To load the device profile XML file if it exists. The function uses
		//					the HTTP Service class to load the file
		// Input Vars:   	none
		// Return Vars:  	none
		// Date Created: 	Nov 29, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		private function loadDeviceProfile():void
		{
			var httpService:HTTPService = new HTTPService();	// HTTP service for loading file
			
			// Fill in the HTTP service details
			httpService.resultFormat = "text";
			httpService.url = profileUrl;
			
			// Add event listeners for both success and failure
			httpService.addEventListener(ResultEvent.RESULT, handleHttpReqSuccess);
			httpService.addEventListener(FaultEvent.FAULT, handleHttpReqFailure);
			
			// Send the request
			httpService.send();
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	handleHttpReqSuccess
		// Author:       	Justin Hickey
		// Purpose:      	To handle a suuccessful transfer of the device profile XML file. This
		//					is the function that parses the XML file. The function creates several
		//					XML subsets of the profile which can then be used for further
		//					processing.
		// Input Vars:   	event - event triggered by the successful transfer of the XML file
		// Return Vars:  	none
		// Date Created: 	Nov 29, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		private function handleHttpReqSuccess(event:ResultEvent):void
		{
			var bracePtrn:RegExp;		// Regular expression to detect base URI reference
			var refPtrn:RegExp;			// Regular expression for value of base URI reference
			
			// Get the complete profile XML
			_devProfileXml = new XML(event.result);
			
			// Get the service XML list
			_serviceXmlList = this.devProfileXml.service;
			
			// Check if we have a widget
			if (this.serviceXmlList.hasOwnProperty("widget"))
			{
				_widgetXmlList = this.serviceXmlList.widget;
			
				// Get the widget details
				_widgetDesc = new String(this.widgetXmlList.description);
				_widgetTitle = new String(this.widgetXmlList.@title);
				_widgetIconMed = new String("http://" + serverNameStr + "/images/medium_"
					+ this.widgetXmlList.icon.@name + "." + this.widgetXmlList.icon.@format);
				_widgetIconSmall = new String("http://" + serverNameStr + "/images/small_"
					+ this.widgetXmlList.icon.@name + "." + this.widgetXmlList.icon.@format);
				_fullChoicesXmlList = this.widgetXmlList.choices;
				
				// Get the base URI for the launch command - it may be a reference to an option
				_baseUri = new String(this.fullChoicesXmlList.@baseUri);
				
				// Set the regular expression patterns for finding base URI references - a base
				// URI reference will start with a brace bracket '{' - the reference is between
				// brace brackets, for example '{baseReference}' 
				bracePtrn = /^{/;
				refPtrn = /^{(\w+)}$/;
				
				// Check if the baseUri is a reference to an option
				if (bracePtrn.test(this.baseUri))
				{
					_baseUriRef = new String(this.baseUri.replace(refPtrn, "$1"));
					_haveBaseRef = true;
				}
				
				// Check if there are any choices
				if (this.fullChoicesXmlList.hasOwnProperty("choice"))
				{
					_choicesXmlList = this.fullChoicesXmlList.choice;
					
					// Set the choices flag
					_haveChoices = true;
				}

				// Set the widget flag
				_haveWidget = true;
			}
			
			// Check if we have any activities
			if (this.serviceXmlList.hasOwnProperty("activities"))
			{
				_fullActivityXmlList = this.serviceXmlList.activities;
				
				if (this.fullActivityXmlList.hasOwnProperty("activity"))
				{
					_activityXmlList = this.fullActivityXmlList.activity;
					
					// Set the activities flag
					_haveActivities = true;
					
				}
			}
			
			// Check if we have any global parameters
			if (this.serviceXmlList.hasOwnProperty("globalParameters"))
			{
				_fullGlobalXmlList = this.serviceXmlList.globalParameters;
				
				if (this.fullGlobalXmlList.hasOwnProperty("globalParameter"))
				{
					_globalXmlList = this.fullGlobalXmlList.globalParameter;
					
					// Set the global parameters flag
					_haveGlobals = true;
					
				}
			}
			
			// Set the initDone flag to trigger the change event
			this.initDone = true;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	handleHttpReqFailure
		// Author:       	Justin Hickey
		// Purpose:      	To handle a failed transfer of the device profile XML file.
		// Input Vars:   	event - event triggered by an error in transferring the XML file
		// Return Vars:  	none
		// Date Created: 	Nov 29, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		private function handleHttpReqFailure(event:FaultEvent):void
		{
			// Set an error message and the error flag
			_errorMsg = "ERROR: Device profile XML file failed to load for "
				+ this.resourceName + "\nPossible reasons could include: file not found, invalid file "
				+ "name, corrupt file, and data transfer error.\n"
				+ "Generated while processing the device profile.\n\n"
				+ "Please contact your Savoir Admin regarding this issue and send "
   		    	+ "him/her the above error\n";
				
			_haveError = true;
				
			// Set remaining variables to null
			_devProfileXml = null;
			_serviceXmlList = null;
			_widgetXmlList = null;
			_fullChoicesXmlList = null;
			_choicesXmlList = null;
			_fullActivityXmlList = null;
			_activityXmlList = null;
			_fullGlobalXmlList = null;
			_globalXmlList = null;
			_widgetDesc = null;
			_widgetTitle = null;
			_widgetIconMed = null;
			_widgetIconSmall = null;
			_baseUri = null;
			_baseUriRef = null;
				
			// Set the initDone flag to trigger the change event
			this.initDone = true;
		}
	}
}
