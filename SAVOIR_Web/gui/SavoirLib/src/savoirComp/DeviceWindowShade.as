// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package savoirComp
{
	// ================================== External Modules =======================================

	import flash.events.IOErrorEvent;
	import flash.net.SharedObject;
	
	import flexlib.containers.WindowShade;
	
	import mx.collections.ArrayCollection;
	import mx.containers.Form;
	import mx.containers.FormItem;
	import mx.containers.HBox;
	import mx.containers.Tile;
	import mx.containers.VBox;
	import mx.controls.Button;
	import mx.controls.CheckBox;
	import mx.controls.ComboBox;
	import mx.controls.HRule;
	import mx.controls.Image;
	import mx.controls.Label;
	import mx.controls.Text;
	import mx.controls.TextInput;
	import mx.core.ClassFactory;
	import mx.managers.PopUpManager;
	
	import renderers.DeviceWinShadeHdrRndr;
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	// Class:			DeviceWindowShade
	// Author:			Justin Hickey
	// Purpose:			This class is a component containing a Flexlib window shade with the base
	//					implementation for a SAVOIR device widget. Each device will use one of
	//					these window shades and then fill in the components - such as the icon, 
	//					the description, and any options - specific to the device.
	//
	//					Since this component is a generic component that can have various
	//					configurations, the layout of the device widget is crucial to the software
	//					using this component. In order to access various display objects in the
	//					component, the calling software must rely on the current relationships
	//					between the display objects from a parent - child point of view.
	//					Therefore, changes to this layout should only be done after careful
	//					consideration of the effects the change will have for the calling
	//					software. Any change to this component may break existing code that uses
	//					this component.
	//
	//					The constructor of this class will create the basic objects that are not
	//					dependent on options and are usually static. If the appropriate
	//					information is not passed to the constructor, then default values are
	//					assigned. Once the component is instantiated, then the view state, the
	//					base URI, and option objects can be added.
	//
	//					The view state indicates how the window shade is displayed on the
	//					Tools and Devices page. It is saved as a user preference in the SAVOIR
	//					database. There are three valid values:
	//
	//						MAXIMIZED - the full device widget is visible
	//						MINIMIZED - the device widget is not visible 
	//						SHADED - the window shade is closed leaving only the top border visible
	//
	//					The base URI can either be directly read from the profile or it is a
	//					reference to an option that contains the base URI. The information in the
	//					profile should be used to set the base URI and base reference properties
	//					of this class.
	//
	//					If there are options to be displayed, the first function that should be
	//					called is the addOptionContainer() function which creates the "Options"
	//					label and a container object for the various options. Next should be a
	//					loop to go through all the options. For each option the following tasks
	//					should be performed:
	//
	//						- Check if this option is a reference for the base URI - checkBaseRef()
	//						- Check if this option has a parameter ID - addParamId() - pass in
	//							null if there is no parameter ID
	//						- Determine the option type and add the appropriate display object
	//							according to the following table:
	//
	//							Option Type | Selection 	| Component		| Class Method
	//							==================================================================
	//							single 		| one of many	| Combo Box		| addOptionComboBox()
	//							multiple 	| many of many	| Check Boxes	| addOptionChkTile()
	//							activity 	| one of many	| Combo Box		| addOptionComboBox()
	//							userEntered	| text entry	| Text Input	| addOptionTextInput()
	//
	//							Typically, the Option Type is used in a case statement, data for
	//							the particular option is collected from the profile, and then the
	//							Class Method is called to create the given option display object
	//						- Add event listeners to handle various behaviour - typical behaviour
	//							includes drag and drop capability, launching the device, and
	//							various cosmetic behaviours to improve the look and feel of the
	//							device widget.
	//
	//					Various arrays are used to track the option display objects, their
	//					names, and other properties. These are required so that the different
	//					options and data can be found during interaction with these device widgets.
	//					These arrays are described below.
	//
	//					The optionWidgets array is actually an array of the containers of each
	//					option component for the given device. By using the base container of the
	//					option as the entry point into the display object tree, we can easily
	//					access the individual option display object by using getChild() type
	//					functions.
	//
	//					The optionNames array is an array of the names of the individual option
	//					display objects. These are used in the getChildByName() function to access
	//					the individual option display objects.
	//
	//					The optionParamIds array is an array of parameter Id's for each option.
	//					Some options may not have a parameter ID, in which case the value of null
	//					is used as the value.
	//
	//					The multiOptionData	array is an array of array collections for data used
	//					with the multiple option type. It contains the data associated with the
	//					check boxes and is passed in to the addOptionChkTile() function. This is
	//					required since unlike combo boxes and text input components where pertinent
	//					data are stored with the component, the check box only has the label and
	//					whether or not the option is selected. Therefore, we need to maintain the
	//					pertinent data within the device widget itself.
	//
	//					For a complete example of using this class see the
	//					handleDevProfileComplete() function in the DevicesView.mxml file.
	//
	// Date Created:	Nov 03, 2010
	//////////////////////////////////////////////////////////////////////////////////////////////
	public class DeviceWindowShade extends WindowShade
	{
		// ================================ Class Constants ======================================
		
		// none
		
		// ============================= Class Public Members ====================================

		public var serverName:SharedObject;				// SAVOIR server name
		           
		public var allContentBox:VBox;					// VBox to contain all children
		public var topHRule:HRule;						// Horizontal rule to seperate options
		public var botHRule:HRule;						// Horizontal rule to seperate the launch button
		public var butBox:HBox;							// HBox for the launch button
		public var launchBut:Button;					// Launch button
		public var chkTileItemHolder:FormItem;			// Place holder for new checkbox tile form item
		public var chkTileHolder:Tile;					// Place holder for new checkbox tile
		public var comboItemHolder:FormItem;			// Place holder for new combo box form item
		public var comboHolder:ComboBox;				// Place holder for new combo box
		public var textInputItemHolder:FormItem;		// Place holder for new text input form item
		public var textInputHolder:TextInput;			// Place holder for new text input
		public var textInputMaxLabel:Label;				// Label for the maximum characters
		
		public var optionWidgets:Array = new Array();	// Array of option components
		public var optionNames:Array = new Array();		// Array of option component names
		public var optionParamIds:Array = new Array();	// Array of option parameter id's
		public var multiOptionData:Array = new Array();	// Array of data for multiple options

		// ============================= Class Private Members ===================================
		
		private var serverNameStr:String = new String();	// SAVOIR server name
		private var initialIconUrl:String = new String();	// Initial icon image URL for error message
		private var initialErrText:String = new String();	// Initial error text for error message
		private var imgErrorCount:Number;					// Number of times icon image failed to load
		private var popupWindow:InfoPopupWindow;			// Error message popup window
		
		private var defaultContentBox:HBox;		// HBox to contain icon and description
		private var iconImg:Image;				// Device icon image
		private var descriptionTxt:Text;		// Text describing device
		private var optionLbl:Label;			// Label for the option section
		private var allOptionsForm:Form;		// Form to contain the options
		
		// ============================ Class Private Properties =================================

		private var _winTitle:String;			// Text of window title
		private var _iconUrl:String;			// URL of the image for the device icon
		private var _description:String;		// Text of the device description
		private var _viewState:String;			// View state of window shade
		private var _resourceId:String;			// Resource ID of device - for reference
		
		private var _optionCount:Number;		// Number of options for this widget
		private var _baseIndex:Number;			// Index of option containing base URI if reference
		private var _baseUri:String;			// Base URI for launch command - may be reference
		private var _baseUriRef:String;			// Reference to option that has base URI
		private var _haveBaseRef:Boolean;		// Flag to indicate if base URI is a reference

		// ================================= Constructor =========================================

		//////////////////////////////////////////////////////////////////////////////////////////
		// Constructor:    	DeviceWindowShade
		// Author:       	Justin Hickey
		// Purpose:      	To initialize the instance of the class. The input variables are set
		//					to null to allow a constructor to be called without any arguments.
		//					The flex components are generated here instead of in the
		//					createChildren function since more components are added dynamically
		//					before the complete component is instantiated. That is, access to the
		//					components is required before the createChildren function is triggered.
		//					Thus, the components defined in this class need to be generated in the
		//					constructor.
		// Input Vars:   	winTitle - title of the window shade
		//					iconUrl - icon for the widget
		//					description - description of the widget
		//					resourceId - resource Id of device
		// Return Vars:  	none
		// Date Created: 	Nov 03, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function DeviceWindowShade(winTitle:String = null, iconUrl:String = null,
			description:String = null, resourceId:String = null):void
		{
			var now:Date = new Date();		// Date object to obtain timestamp
			
			// Call the constructor of the parent
			super();
			
			// Get the server name
			this.serverName = SharedObject.getLocal("serverName","/");
			serverNameStr = this.serverName.data.serverName;
			
			// Set the member values
			if (winTitle == null)
			{
				_winTitle = new String("Device");
			}
			else
			{
				_winTitle = new String(winTitle);
			}
			
			if (iconUrl == null)
			{
				_iconUrl = new String("http://" + serverNameStr + "/images/medium_noIcon.png");
			}
			else
			{
				_iconUrl = new String(iconUrl);
			}
			
			if (description == null)
			{
				_description = new String("No description provided");
			}
			else
			{
				_description = new String(description);
			}
			
			if (resourceId == null)
			{
				_resourceId = null;
			}
			else
			{
				_resourceId = new String(resourceId);
			}

			_baseUri = null;
			_baseUriRef = null;
			_haveBaseRef = false;
			_viewState = null;
			_optionCount = 0;
			_baseIndex = -1;
			
			imgErrorCount = 0;
			
			// Set the name here since minimized devices have not called createChildren() yet
			// Also use a timestamp in the ID to relatively ensure that the ID is unique
			this.name = this.winTitle;
			this.id = this.name + now.getTime().toString();

			// Set properties of the base window shade 
			this.headerRenderer = new ClassFactory(DeviceWinShadeHdrRndr);
			this.width = 400;
			this.setStyle("borderStyle", "solid");
			this.setStyle("paddingTop", 10);
			this.setStyle("paddingRight", 10);
			this.setStyle("paddingBottom", 10);
			this.setStyle("paddingLeft", 10);
			
			// Create the two main containers for the window shade
			allContentBox = new VBox();
			allContentBox.id = "allContent";
			allContentBox.percentWidth = 100;
			allContentBox.setStyle("horizontalAlign", "left");

			defaultContentBox = new HBox();
			defaultContentBox.percentWidth = 100;
			
			// Create the icon image and add a listener for I/O errors
			iconImg = new Image();
			iconImg.load(this.iconUrl);
			iconImg.addEventListener(IOErrorEvent.IO_ERROR, handleFileError);
			
			// Create the description text component
			descriptionTxt = new Text();
			descriptionTxt.maxWidth = 290;
			descriptionTxt.selectable = false;
			descriptionTxt.text = this.description;
			
			// Add the icon and description to the HBox
			defaultContentBox.addChild(iconImg);
			defaultContentBox.addChild(descriptionTxt);
			
			// Create the top and bottom horizontal rule - don't add the bottom rule
			topHRule = new HRule();
			topHRule.percentWidth = 100;
			botHRule = new HRule();
			botHRule.percentWidth = 100;
			 
			// Create the launch button - but don't add it since options should be first
			butBox = new HBox();
			butBox.percentWidth = 100;
			butBox.setStyle("horizontalAlign", "center");
			
			launchBut = new Button();
			launchBut.id = "launchBut";
			launchBut.label = "Launch";
			
			butBox.addChild(launchBut);
			
			// Add the components to the all contnent box
			allContentBox.addChild(defaultContentBox);
			allContentBox.addChild(topHRule);
		}

		// ============================== Override Functions =====================================
		
		//////////////////////////////////////////////////////////////////////////////////////////
		// Override Func:	createChildren
		// Author:       	Justin Hickey
		// Purpose:      	This function overrides the Flex function and is required to create
		//					the componet. It simply calls the parent createChildren function and
		//					then adds the completed components of this class.
		// Input Vars:   	none
		// Return Vars:  	none
		// Date Created: 	Nov 03, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		override protected function createChildren():void
		{
			// Call the createChildren function of the parent
			super.createChildren();
			
			// Add all the content to the device window shade
			addChild(allContentBox);
		}

		// =============================== Setter Functions ======================================

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set winTitle
		// Author:       	Justin Hickey
		// Purpose:      	To set the winTitle member of the class
		// Input Vars:   	value - value of the new winTitle
		// Return Vars:  	none
		// Date Created: 	Nov 29, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set winTitle(value:String):void
		{
			_winTitle = value;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set iconUrl
		// Author:       	Justin Hickey
		// Purpose:      	To set the iconUrl member of the class
		// Input Vars:   	value - value of the new iconUrl
		// Return Vars:  	none
		// Date Created: 	Nov 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set iconUrl(value:String):void
		{
			_iconUrl = value;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set description
		// Author:       	Justin Hickey
		// Purpose:      	To set the description member of the class
		// Input Vars:   	value - value of the new description
		// Return Vars:  	none
		// Date Created: 	Nov 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set description(value:String):void
		{
			_description = value;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set viewState
		// Author:       	Justin Hickey
		// Purpose:      	To set the viewState member of the class
		// Input Vars:   	value - value of the new viewState - one of MAXIMIZED, MINIMIZED, or
		//					SHADED
		// Return Vars:  	none
		// Date Created: 	Dec 01, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set viewState(value:String):void
		{
			if (_viewState == null)
			{
				_viewState = new String(value);
			}
			else
			{
				_viewState = value;
			}
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set resourceId
		// Author:       	Justin Hickey
		// Purpose:      	To set the resourceId member of the class
		// Input Vars:   	value - value of the new resourceId
		// Return Vars:  	none
		// Date Created: 	Dec 06, 2010
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
		// Function:     	set baseUri
		// Author:       	Justin Hickey
		// Purpose:      	To set the baseUri member of the class
		// Input Vars:   	value - value of the new baseUri
		// Return Vars:  	none
		// Date Created: 	Dec 21, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set baseUri(value:String):void
		{
			if (_baseUri == null)
			{
				_baseUri = new String(value);
			}
			else
			{
				_baseUri = value;
			}
   		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set baseUriRef
		// Author:       	Justin Hickey
		// Purpose:      	To set the baseUriRef member of the class
		// Input Vars:   	value - value of the new baseUriRef
		// Return Vars:  	none
		// Date Created: 	Dec 21, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set baseUriRef(value:String):void
		{
			if (_baseUriRef == null)
			{
				_baseUriRef = new String(value);
			}
			else
			{
				_baseUriRef = value;
			}
   		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set haveBaseRef
		// Author:       	Justin Hickey
		// Purpose:      	To set the haveBaseRef member of the class
		// Input Vars:   	value - value of the new haveBaseRef
		// Return Vars:  	none
		// Date Created: 	Dec 21, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set haveBaseRef(value:Boolean):void
		{
			_haveBaseRef = value;
   		}

		// =============================== Getter Functions ======================================
		
		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get winTitle
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the winTitle member of the class
		// Input Vars:   	none
		// Return Vars:  	_winTitle
		// Date Created: 	Nov 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get winTitle():String
		{
			return _winTitle;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get iconUrl
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the iconUrl member of the class
		// Input Vars:   	none
		// Return Vars:  	_iconUrl
		// Date Created: 	Nov 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get iconUrl():String
		{
			return _iconUrl;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get description
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the description member of the class
		// Input Vars:   	none
		// Return Vars:  	_description
		// Date Created: 	Nov 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get description():String
		{
			return _description;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get viewState
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the viewState member of the class
		// Input Vars:   	none
		// Return Vars:  	_viewState
		// Date Created:	Dec 01, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get viewState():String
		{
			return _viewState;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get resourceId
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the resourceId member of the class
		// Input Vars:   	none
		// Return Vars:  	_resourceId
		// Date Created: 	Dec 06, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get resourceId():String
		{
			return _resourceId;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get optionCount
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the optionCount member of the class
		// Input Vars:   	none
		// Return Vars:  	_optionCount
		// Date Created: 	Dec 21, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get optionCount():Number
		{
			return _optionCount;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get baseIndex
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the baseIndex member of the class
		// Input Vars:   	none
		// Return Vars:  	_baseIndex
		// Date Created: 	Dec 21, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get baseIndex():Number
		{
			return _baseIndex;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get baseUri
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the baseUri member of the class
		// Input Vars:   	none
		// Return Vars:  	_baseUri
		// Date Created: 	Dec 21, 2010
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
		// Date Created: 	Dec 21, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get baseUriRef():String
		{
			return _baseUriRef;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get haveBaseRef
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the haveBaseRef member of the class
		// Input Vars:   	none
		// Return Vars:  	_haveBaseRef
		// Date Created: 	Dec 21, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get haveBaseRef():Boolean
		{
			return _haveBaseRef;
		}

		// =========================== Interaction Event Handlers ================================

		// none
			
		// =========================== Application Event Handlers ================================

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	handleFileError
		// Author:       	Justin Hickey
		// Purpose:      	To set the icon image to a default image if there is an error loading
		//					the initial image. If the default image also generates an error then
		//					the function will pop up an error message.
		// Input Vars:   	event - event triggered by an I/O error while trying to load the icon
		//					image
		// Return Vars:  	none
		// Date Created: 	Feb 01, 2011
		//////////////////////////////////////////////////////////////////////////////////////////
		private function handleFileError(event:IOErrorEvent):void
		{
			// If this is the first time called try to load the default icon
			if (imgErrorCount == 0)
			{
				// Save the initial icon URL and error message text
				initialIconUrl = this.iconUrl;
				initialErrText = event.text;
				
				// Set the icon image to the default icon
				this.iconUrl = "http://" + serverNameStr + "/images/medium_noIcon.png";
				this.iconImg.load(this.iconUrl);

				// Increment the error count
				imgErrorCount++;
			}
			else
			{
  		    	// Add a message to a popup window
   		    	popupWindow = InfoPopupWindow(PopUpManager.createPopUp(this, InfoPopupWindow, true));
   		    	popupWindow.title = "ERROR: File Load Error";
   		    	popupWindow.setMessage("ERROR: The icon image " + initialIconUrl + " and the\n"
   		    		+ "default image both failed to load from the server. The error returned was\n"
   		    		+ initialErrText + "\n"
   		    		+ "Generated while initializing the device " + this.winTitle + ".\n\n"
					+ "Please contact your Savoir Admin regarding this issue "
   		    		+ "and send him/her the above error\n");
   		    	
   		    	// Change the popup into an info only dialog box
				popupWindow.formatInfoBox();
				
				// Popup the message
   		    	PopUpManager.centerPopUp(popupWindow);
			}
		}
			
		// =============================== Public Functions ======================================

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	checkBaseRef
		// Author:       	Justin Hickey
		// Purpose:      	To check if the current option contains a base reference. If it does,
		//					save the optionCount as the index into the option components array
		//					and the option names array that can be used to access the base
		//					reference at a future time
		// Input Vars:   	choiceId - id of the current option
		// Return Vars:  	none
		// Date Created: 	Dec 21, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function checkBaseRef(choiceId:String):void
		{
			// Check if this option contains the base URI
			if (this.haveBaseRef)
			{
				if (this.baseUriRef == choiceId)
				{
					// Set the base index for this option
					_baseIndex = this.optionCount;
				}
			}
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	addParamId
		// Author:       	Justin Hickey
		// Purpose:      	To add the current parameter id to the option parameter Id's array
		// Input Vars:   	paramId - id of the current parameter
		// Return Vars:  	none
		// Date Created: 	Dec 22, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function addParamId(paramId:String):void
		{
			optionParamIds[this.optionCount] = paramId;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	addOptionContainer
		// Author:       	Justin Hickey
		// Purpose:      	To add the option label and the container for the option components
		// Input Vars:   	none
		// Return Vars:  	none
		// Date Created: 	Dec 15, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function addOptionContainer():void
		{
			// Create the option label
			optionLbl = new Label();
			optionLbl.text = "Options";
			optionLbl.setStyle("styleName", "headingLabel");
			
			// Create the options main container
			allOptionsForm = new Form();
			allOptionsForm.percentWidth = 100;
			
			// Add the components to the widget
			allContentBox.addChild(optionLbl);
			allContentBox.addChild(allOptionsForm);
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	addOptionComboBox
		// Author:       	Justin Hickey
		// Purpose:      	To add a combo box for a list of options. The index is returned so
		//					that this combo box can be easily accessed to set the data provider. 
		// Input Vars:   	serviceId - SAVOIR resource id of device
		//					labelStr - label for the combo box
		// Return Vars:  	returnVal - index of the combo box
		// Date Created: 	Dec 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function addOptionComboBox(serviceId:String, labelStr:String):Number
		{
			var returnVal:Number;
			
			// Form the option name
			optionNames[this.optionCount] = new String(serviceId + "option" + this.optionCount);
			
			// Create the components for the combo box
			comboItemHolder = new FormItem();
			comboHolder = new ComboBox();
			
			// Initialize the option components and add them to the form item
			comboHolder.name = optionNames[this.optionCount];
			comboHolder.prompt = "Select one...";
			comboItemHolder.label = labelStr + ":";
			comboItemHolder.addChild(comboHolder);
			
			// Add it to the option array
			optionWidgets[this.optionCount] = comboItemHolder;
			returnVal = this.optionCount;
			_optionCount++;
			
			// Add it to the device widget
			allOptionsForm.addChild(comboItemHolder);
			
			// Return the index
			return(returnVal);
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	addOptionTextInput
		// Author:       	Justin Hickey
		// Purpose:      	To add a text input box to the device widget. A maximum character
		//					length for the input field can be specified, with the default value
		//					being 100 if a number < 1 is passed in.
		// Input Vars:   	serviceId - SAVOIR resource id of device
		//					labelStr - label for the combo box
		//					maxChar - maximum number of characters for the text input
		// Return Vars:  	none
		// Date Created: 	Jan 06, 2011
		//////////////////////////////////////////////////////////////////////////////////////////
		public function addOptionTextInput(serviceId:String, labelStr:String, maxChar:Number):void
		{
			// Check the maximum character value
			if (maxChar < 1)
			{
				// Set a default of 100
				maxChar = 100;
			}
			
			// Form the option name
			optionNames[this.optionCount] = new String(serviceId + "option" + this.optionCount);
			
			// Create the components for the text input
			textInputItemHolder = new FormItem();
			textInputHolder = new TextInput();
			textInputMaxLabel = new Label();
			
			// Initialize the option components and add them to the form item
			textInputHolder.name = optionNames[this.optionCount];
			textInputHolder.maxChars = maxChar;
			textInputItemHolder.label = labelStr + ":";
			textInputMaxLabel.text = "(Max characters: " + maxChar.toString() + ")";
			textInputItemHolder.addChild(textInputHolder);
			textInputItemHolder.addChild(textInputMaxLabel);
			
			// Add it to the option array
			optionWidgets[this.optionCount] = textInputItemHolder;
			_optionCount++;
			
			// Add it to the device widget
			allOptionsForm.addChild(textInputItemHolder);
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	addOptionChkTile
		// Author:       	Justin Hickey
		// Purpose:      	To add a tile box with checkboxes to the device widget. After 3 rows,
		//					a scroll bar is produced automatically so that the user can scroll
		//					down to further options. An array collection is passed into this
		//					function so that the data associated with the check boxes can be
		//					accessed as part of the device widget.
		// Input Vars:   	serviceId - SAVOIR resource id of device
		//					labelStr - label for the tile box
		//					optionData - array collection containing information for the options
		// Return Vars:  	none
		// Date Created: 	Jan 07, 2011
		//////////////////////////////////////////////////////////////////////////////////////////
		public function addOptionChkTile(serviceId:String, labelStr:String,
			optionData:ArrayCollection):void
		{
			var opt:Object;
			var chkBox:CheckBox;
			
			// Form the option name
			optionNames[this.optionCount] = new String(serviceId + "option" + this.optionCount);
			
			// Copy the option data
			multiOptionData[optionNames[this.optionCount]] = optionData;
			
			// Create the components for the tile box of checkboxes
			chkTileItemHolder = new FormItem();
			chkTileHolder = new Tile();
			
			// Initialize the upper level components
			chkTileItemHolder.percentWidth = 100;
			chkTileItemHolder.label = labelStr + ":";
			chkTileHolder.name = optionNames[this.optionCount];
			chkTileHolder.percentWidth = 100;
			chkTileHolder.maxHeight = 90;
			
			// Go through the option data
			for each (opt in multiOptionData[optionNames[this.optionCount]])
			{
				// Create the check box
				chkBox = new CheckBox();
				chkBox.name = opt.label;
				chkBox.label = opt.label;
				
				// Add it to the tile box
				chkTileHolder.addChild(chkBox);
			}
			
			// Add the compoents to the form item
			chkTileItemHolder.addChild(chkTileHolder);
			
			// Add it to the option array
			optionWidgets[this.optionCount] = chkTileItemHolder;
			_optionCount++;
			
			// Add it to the device widget
			allOptionsForm.addChild(chkTileItemHolder);
		}

		// =============================== Private Functions =====================================

		// none
	}
}
