// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package savoirlib
{
	// ================================== External Modules =======================================

	import mx.formatters.DateFormatter;
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	// Class:			Scenario
	// Author:			Justin Hickey
	// Purpose:			This class is meant to store the pertinent information for a SAVOIR
	//					scenario.
	// Date Created:	Mar 19, 2010
	//////////////////////////////////////////////////////////////////////////////////////////////
	public class Scenario
	{
		// ================================ Class Constants ======================================
		
		// none
		
		// ============================= Class Public Members ====================================

		// none          
		
		// ============================= Class Private Members ===================================
		
		private var _scenarioID:Number;		// Scenario number this scenario
		private var _title:String;			// Title of the scenario
		private var _author:String;			// Author of the scenario
		private var _devices:String;		// Comma separated list of devices used in scenario
		private var _description:String;	// Description of scenario
		private var _lastEdited:Date;		// Date scenario was last edited - time not needed
		private var _apnConnections:String;	// List of APN connections - optional - may be empty

		// ================================= Constructor =========================================

		//////////////////////////////////////////////////////////////////////////////////////////
		// Constructor:    	Scenario
		// Author:       	Justin Hickey
		// Purpose:      	To initialize the instance of the class. This sets the values of the
		//					data members. The input variables are set to null to allow a constructor
		//					to be called without any arguments.
		// Input Vars:   	scenarioID - scenario ID from the database
		//					title - title of scenario
		//					author - author of scenario
		//					devices - devices used in scenario
		//					description - description of scenario
		//					lastEdited - date when scenario was last edited as a string
		//						examples: "Feb 1 2010" "02/01/2005"
		// Return Vars:  	none
		// Date Created: 	Mar 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function Scenario(scenarioID:Number = NaN, title:String = null, author:String = null,
			devices:String = null, description:String = null, lastEdited:String = null,
			apnConnections:String = null):void
		{
			// Set the member values
			_scenarioID = scenarioID;
			_title = new String(title);
			_author = new String(author);
			_devices = new String(devices);
			_description = new String(description);
			_lastEdited = new Date(lastEdited);
			_apnConnections = new String(apnConnections);
			
			// Register an alias and setup the scenario shared object
			// Commented out for now until we get the shared object working
//			registerClassAlias("savoirlib.Scenario", Scenario);

		}

		// =============================== Setter Functions ======================================

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set scenarioID
		// Author:       	Justin Hickey
		// Purpose:      	To set the scenarioID member of the class
		// Input Vars:   	value - value of the new scenarioID
		// Return Vars:  	none
		// Date Created: 	Mar 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set scenarioID(value:Number):void
		{
			_scenarioID = value;
   		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set title
		// Author:       	Justin Hickey
		// Purpose:      	To set the title member of the class
		// Input Vars:   	value - value of the new title
		// Return Vars:  	none
		// Date Created: 	Mar 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set title(value:String):void
		{
			_title = value;
   		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set author
		// Author:       	Justin Hickey
		// Purpose:      	To set the author member of the class
		// Input Vars:   	value - value of the new author
		// Return Vars:  	none
		// Date Created: 	Mar 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set author(value:String):void
		{
			_author = value;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set devices
		// Author:       	Justin Hickey
		// Purpose:      	To set the devices member of the class
		// Input Vars:   	value - value of the new devices
		// Return Vars:  	none
		// Date Created: 	Mar 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set devices(value:String):void
		{
			_devices = value;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set description
		// Author:       	Justin Hickey
		// Purpose:      	To set the description member of the class
		// Input Vars:   	value - value of the new description
		// Return Vars:  	none
		// Date Created: 	Mar 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set description(value:String):void
		{
			_description = value;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set lastEdited
		// Author:       	Justin Hickey
		// Purpose:      	To set the lastEdited member of the class
		// Input Vars:   	value - value of the new last edited date
		// Return Vars:  	none
		// Date Created: 	Mar 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set lastEdited(value:Date):void
		{
			_lastEdited = value;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	setLastEditedStr
		// Author:       	Justin Hickey
		// Purpose:      	To set the lastEdited member of the class by passing in the date as
		//					a string
		// Input Vars:   	value - value of the new last edited date as a string
		// Return Vars:  	none
		// Date Created: 	Mar 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function setLastEditedStr(value:String):void
		{
			_lastEdited.setTime(Date.parse(value));
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set apnConncetions
		// Author:       	Justin Hickey
		// Purpose:      	To set the apnConnections member of the class
		// Input Vars:   	value - value of the new apnConnections
		// Return Vars:  	none
		// Date Created: 	Aug 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set apnConnections(value:String):void
		{
			_apnConnections = value;
		}

		// =============================== Getter Functions ======================================
		
		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get scenarioID
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the scenarioID member of the class
		// Input Vars:   	none
		// Return Vars:  	_scenarioID
		// Date Created: 	Mar 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get scenarioID():Number
		{
			return _scenarioID;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get title
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the title member of the class
		// Input Vars:   	none
		// Return Vars:  	_title
		// Date Created: 	Mar 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get title():String
		{
			// Check for a null or empty value title
			if (_title == null || _title == "" || _title == "null")
			{
				_title = "Scenario title not specified";
			}
			
			return _title;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get author
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the author member of the class
		// Input Vars:   	none
		// Return Vars:  	_author
		// Date Created: 	Mar 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get author():String
		{
			return _author;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get devices
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the devices member of the class
		// Input Vars:   	none
		// Return Vars:  	_devices
		// Date Created: 	Mar 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get devices():String
		{
			return _devices;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get description
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the description member of the class
		// Input Vars:   	none
		// Return Vars:  	_description
		// Date Created: 	Mar 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get description():String
		{
			return _description;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get lastEdited
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the lastEdited member of the class
		// Input Vars:   	none
		// Return Vars:  	_lastEdited
		// Date Created: 	Mar 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get lastEdited():Date
		{
			return _lastEdited;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	getLastEditedStr
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the lastEdited member of the class as a
		//					properly formatted date string 
		// Input Vars:   	none
		// Return Vars:  	_lastEdited - formatted to a proper date string
		// Date Created: 	Mar 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function getLastEditedStr():String
		{
			var dateFormatter:DateFormatter;
			
			// Create a format to print the last edited date and return the string
			dateFormatter = new DateFormatter();
			dateFormatter.formatString = "MMMM DD, YYYY";
			
			return dateFormatter.format(_lastEdited);
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get apnConnections
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the apnConncetions member of the class
		// Input Vars:   	none
		// Return Vars:  	_apnConnections
		// Date Created: 	Aug 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get apnConnections():String
		{
			return _apnConnections;
		}

		// =============================== Public Functions ======================================
		
		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	createTextString
		// Author:       	Justin Hickey
		// Purpose:      	To create the text string to be used to display in a UI text field
		// Input Vars:   	none
		// Return Vars:  	displayStr - a string of HTML formatted text 
		// Date Created: 	Jun 28, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function createTextString():String
		{
			var displayStr:String = new String();
			
			// Check for any null values
			checkNullValues();
			
			// Form the display string
			displayStr = description + "<br><br>Tools & Devices: " + devices + "<br>APN Connections: " +
				apnConnections + "<br>Scenario Author: " + author + "<br>Last Edited: " + getLastEditedStr();
					
			return displayStr;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	checkNullValues
		// Author:       	Justin Hickey
		// Purpose:      	To check for null values of some of the members of the class, then set
		//					them to appropriate text values 
		// Input Vars:   	none
		// Return Vars:  	none 
		// Date Created: 	Aug 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function checkNullValues():void
		{
			// Check for a null or empty value description
			if (description == null || description == "" || description == "null")
			{
				description = "No description provided";
			}
			
			// Check for a null or empty value APN connections
			if (apnConnections == null || apnConnections == "" || apnConnections == "null")
			{
				apnConnections = "No APN connections required";
			}
			
			// Check for a null or empty value devices
			if (devices == null || devices == "" || devices == "null")
			{
				devices = "No devices listed";
			}
			
			// Check for a null or empty value author
			if (author == null || author == "" || author == "null")
			{
				author = "No author specified";
			}
		}

		// =============================== Private Functions =====================================

		// none
	}
}
