// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package savoirlib
{
	// ================================== External Modules =======================================

   	import mx.controls.Alert;
	import mx.collections.ArrayCollection;
	import mx.formatters.DateFormatter;
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	// Class:			Scenario
	// Author:			Justin Hickey
	// Purpose:			This class is meant to store the pertinent information for a SAVOIR
	//					session.
	// Date Created:	Jun 17, 2010
	//////////////////////////////////////////////////////////////////////////////////////////////
	public class Session
	{
		// ================================ Class Constants ======================================
		
		// none
		
		// ============================= Class Public Members ====================================

		// none          
		
		// ============================= Class Private Members ===================================
		
		private var _sessionID:Number;				// Session number for this session
		private var _scenarioID:Number;				// Scenario number pointing to the scenario data
		private var _title:String;					// Title of the session
		private var _author:String;					// Author of the session
		private var _status:String;					// State the session is in
		private var _description:String;			// Description of session
		private var _scenarioData:Scenario;			// Reference to the scenario data 
		private var _authUsers:ArrayCollection;		// List of authorized users who can run the session
		private var _authGroups:ArrayCollection;	// List of authorized groups that can run the session
		private var _submitted:Date;				// Date the session was submitted - time not needed
		private var _start:Date;					// Date of start time for this session - time needed
		private var _end:Date;						// Date of end time for this session - time needed

		// ================================= Constructor =========================================

		//////////////////////////////////////////////////////////////////////////////////////////
		// Constructor:    	Session
		// Author:       	Justin Hickey
		// Purpose:      	To initialize the instance of the class. This sets the values of the
		//					data members. The input variables are set to null to allow a constructor
		//					to be called without any arguments.
		// Input Vars:   	sessionID - session ID from the database
		//					scenarioID - scenario ID pointing to the scenario data
		//					title - title of the session
		//					author - author of the session
		//					status - current state of the session
		//					description - description of the session
		//					scenarioData - reference to the scenario data for this session
		//					authUsers - list of users authorized to run the session
		//					authGroups - list of groups authorized to run the session
		//					submitted - date and time when the session was submitted as a string
		//						examples: "Feb 1 2010 12:00:00 AM" "Feb 1 00:00:00 UTC-0400 2010"
		//					start - date and time of the start of next session as a string
		//						examples: "Feb 1 2010 12:00:00 AM" "Feb 1 00:00:00 UTC-0400 2010"
		//					end - date and time of the end of next session as a string
		//						examples: "Feb 1 2010 12:00:00 AM" "Feb 1 00:00:00 UTC-0400 2010"
		// Return Vars:  	none
		// Date Created: 	Jun 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function Session(sessionID:Number = NaN, scenarioID:Number = NaN, title:String = null,
			author:String = null, status:String = null, description:String = null,
			scenarioData:Scenario = null, authUsers:ArrayCollection = null,
			authGroups:ArrayCollection = null, submitted:String = null, start:String = null,
			end:String = null):void
		{
			// Set the member values
			_sessionID = sessionID;
			_scenarioID = scenarioID;
			_title = new String(title);
			_author = new String(author);
			_status = new String(status);
			_description = new String(description);
			_scenarioData = scenarioData;
			_authUsers = authUsers;
			_authGroups = authGroups;
			_submitted = new Date(submitted);
			_start = new Date(start);
			_end = new Date(end);
		}

		// =============================== Setter Functions ======================================

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set sessionID
		// Author:       	Justin Hickey
		// Purpose:      	To set the sessionID member of the class
		// Input Vars:   	value - value of the new sessionID
		// Return Vars:  	none
		// Date Created: 	Jun 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set sessionID(value:Number):void
		{
			_sessionID = value;
   		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set scenarioID
		// Author:       	Justin Hickey
		// Purpose:      	To set the scenarioID member of the class
		// Input Vars:   	value - value of the new scenarioID
		// Return Vars:  	none
		// Date Created: 	Jun 17, 2010
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
		// Date Created: 	Aug 19, 2010
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
		// Date Created: 	Aug 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set author(value:String):void
		{
			_author = value;
   		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set status
		// Author:       	Justin Hickey
		// Purpose:      	To set the status member of the class
		// Input Vars:   	value - value of the new status
		// Return Vars:  	none
		// Date Created: 	Jul 26, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set status(value:String):void
		{
			_status = value;
   		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set description
		// Author:       	Justin Hickey
		// Purpose:      	To set the description member of the class
		// Input Vars:   	value - value of the new description
		// Return Vars:  	none
		// Date Created: 	Aug 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set description(value:String):void
		{
			_description = value;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set scenarioData
		// Author:       	Justin Hickey
		// Purpose:      	To set the scenarioData member of the class
		// Input Vars:   	value - value of the new scenario data
		// Return Vars:  	none
		// Date Created: 	Jun 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set scenarioData(value:Scenario):void
		{
			_scenarioData = value;
   		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set authUsers
		// Author:       	Justin Hickey
		// Purpose:      	To set the authUsers member of the class
		// Input Vars:   	value - value of the new authorized users
		// Return Vars:  	none
		// Date Created: 	Jun 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set authUsers(value:ArrayCollection):void
		{
			// Copy using values
			_authUsers = value;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set authGroups
		// Author:       	Justin Hickey
		// Purpose:      	To set the authGroups member of the class
		// Input Vars:   	value - value of the new authorized groups
		// Return Vars:  	none
		// Date Created: 	Aug 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set authGroups(value:ArrayCollection):void
		{
			// Copy using values
			_authGroups = value;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set submitted
		// Author:       	Justin Hickey
		// Purpose:      	To set the submitted date member of the class
		// Input Vars:   	value - value of the new submitted date
		// Return Vars:  	none
		// Date Created: 	Aug 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set submitted(value:Date):void
		{
			_submitted = value;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	setSubmittedStr
		// Author:       	Justin Hickey
		// Purpose:      	To set the submitted date member of the class using a string
		// Input Vars:   	value - value of the new submitted date as a string
		// Return Vars:  	none
		// Date Created: 	Aug 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function setSunmittedStr(value:String):void
		{
			_submitted.setTime(Date.parse(value));
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set start
		// Author:       	Justin Hickey
		// Purpose:      	To set the start date member of the class
		// Input Vars:   	value - value of the new start date
		// Return Vars:  	none
		// Date Created: 	Jun 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set start(value:Date):void
		{
			_start = value;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	setStartStr
		// Author:       	Justin Hickey
		// Purpose:      	To set the start date member of the class using a string
		// Input Vars:   	value - value of the new start date as a string
		// Return Vars:  	none
		// Date Created: 	Jun 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function setStartStr(value:String):void
		{
			_start.setTime(Date.parse(value));
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	set end
		// Author:       	Justin Hickey
		// Purpose:      	To set the end date member of the class
		// Input Vars:   	value - value of the new end date
		// Return Vars:  	none
		// Date Created: 	Jun 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function set end(value:Date):void
		{
			_end = value;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	setEndStr
		// Author:       	Justin Hickey
		// Purpose:      	To set the end date member of the class using a string
		// Input Vars:   	value - value of the new end date as a string
		// Return Vars:  	none
		// Date Created: 	Jun 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function setEndStr(value:String):void
		{
			_end.setTime(Date.parse(value));
		}

		// =============================== Getter Functions ======================================
		
		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get sessionID
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the sessionID member of the class
		// Input Vars:   	none
		// Return Vars:  	_sessionID
		// Date Created: 	Jun 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get sessionID():Number
		{
			return _sessionID;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get scenarioID
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the scenarioID member of the class
		// Input Vars:   	none
		// Return Vars:  	_scenarioID
		// Date Created: 	Jun 17, 2010
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
		// Date Created: 	Aug 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get title():String
		{
			// Check for a null or empty value title
			if (_title == null || _title == "" || _title == "null")
			{
				_title = "Session title not specified";
			}
			
			return _title;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get author
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the author member of the class
		// Input Vars:   	none
		// Return Vars:  	_author
		// Date Created: 	Aug 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get author():String
		{
			return _author;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get status
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the status member of the class
		// Input Vars:   	none
		// Return Vars:  	_status
		// Date Created: 	Jul 26, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get status():String
		{
			return _status;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get description
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the description member of the class
		// Input Vars:   	none
		// Return Vars:  	_description
		// Date Created: 	Aug 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get description():String
		{
			return _description;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get scenarioData
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the scenarioData member of the class
		// Input Vars:   	none
		// Return Vars:  	_scenarioData
		// Date Created: 	Jun 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get scenarioData():Scenario
		{
			return _scenarioData;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get authUsers
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the authUsers member of the class
		// Input Vars:   	none
		// Return Vars:  	_authUsers
		// Date Created: 	Jun 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get authUsers():ArrayCollection
		{
			return _authUsers;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get authGroups
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the authGroups member of the class
		// Input Vars:   	none
		// Return Vars:  	_authGroups
		// Date Created: 	Aug 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get authGroups():ArrayCollection
		{
			return _authGroups;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get submitted
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the submitted date member of the class
		// Input Vars:   	none
		// Return Vars:  	_submitted
		// Date Created: 	Aug 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get submitted():Date
		{
			return _submitted;
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	getSubmittedStr
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the submitted date member of the class as a
		//					properly formatted date string
		// Input Vars:   	none
		// Return Vars:  	_submitted - formatted to a proper date string
		// Date Created: 	Aug 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function getSubmittedStr():String
		{
			var dateFormatter:DateFormatter;
			
			// Create a format to print the start date and return the string
			dateFormatter = new DateFormatter();
			dateFormatter.formatString = "MMMM DD, YYYY @ L:NN A";
			
			// Check if the time has been defined
			if (_submitted.time == 0)
			{
				return "No submission date defined";
			}
			else
			{
				return dateFormatter.format(_submitted);
			}
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get start
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the start date member of the class
		// Input Vars:   	none
		// Return Vars:  	_start
		// Date Created: 	Jun 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get start():Date
		{
			return _start;
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	getStartStr
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the start date member of the class as a
		//					properly formatted date string
		// Input Vars:   	none
		// Return Vars:  	_start - formatted to a proper date string
		// Date Created: 	Jun 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function getStartStr():String
		{
			var dateFormatter:DateFormatter;
			
			// Create a format to print the start date and return the string
			dateFormatter = new DateFormatter();
			dateFormatter.formatString = "MMMM DD, YYYY @ L:NN A";
			
			// Check if the time has been defined
			if (_start.time == 0)
			{
				return "Not defined - Session can be started anytime";
			}
			else
			{
				return dateFormatter.format(_start);
			}
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	get end
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the end date member of the class
		// Input Vars:   	none
		// Return Vars:  	_end
		// Date Created: 	Jun 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function get end():Date
		{
			return _end;
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	getEndStr
		// Author:       	Justin Hickey
		// Purpose:      	To get the current value of the end date member of the class as a
		//					properly formatted date string 
		// Input Vars:   	none
		// Return Vars:  	_end - formatted to a proper date string
		// Date Created: 	Jun 17, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		public function getEndStr():String
		{
			var dateFormatter:DateFormatter;
			
			// Create a format to print the next session and retrun the string
			dateFormatter = new DateFormatter();
			dateFormatter.formatString = "MMMM DD, YYYY @ L:NN A";
			
			// Check if the time has been defined
			if (_end.time == 0)
			{
				return "Not defined - Session will always be available";
			}
			else
			{
				return dateFormatter.format(_end);
			}
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
			var users:String;
			var groups:String;
			
			// Check for null values
			checkNullValues();
			scenarioData.checkNullValues();
			
			// Format the user and group lists
			users = formatUserString();
			groups = formatGroupString();
			
			// Form the HTML string
			displayStr = description + "<br><br>Tools & Devices: " + scenarioData.devices + "<br>APN Connections: " +
				scenarioData.apnConnections + "<br>Session Author: " + author + "<br>Session Submitted: " +
				getSubmittedStr() + "<br><br>Authorized Groups: " + groups + "<br>Authorized Users: " +
				users + "<br><br>Start Time: <b>" +	getStartStr() + "</b><br>End Time: <b>" + getEndStr() + "</b>";
					
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
			// Check for a null or empty value author
			if (author == null || author == "" || author == "null")
			{
				author = "No session author specified";
			}
			
			// Check for a null or empty value description
			if (description == null || description == "" || description == "null")
			{
				description = "No session description provided";
			}
		}

		// =============================== Private Functions =====================================

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	formatUserString
		// Author:       	Justin Hickey
		// Purpose:      	To format the user list into a printable form
		// Input Vars:   	none
		// Return Vars:  	userStr
		// Date Created: 	Aug 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		private function formatUserString():String
		{
			var userData:Object;
			var userStr:String = new String();
			var i:int;

			// Get the user data from the authorized users list
			if (authUsers != null)
			{
   				for (i = 0; i < authUsers.length; i++)
   				{
   					userData = authUsers.getItemAt(i);
   					
   					// Add a comma and space after the user name
					userStr = userStr + userData.username + ", ";
   				}
   				
   				// Strip off the last comma and space
   				userStr = userStr.substring(0, userStr.length - 2);
			}
			else
			{
				// Indicate no authorized users were listed
				userStr = "No authorized users listed";
			}
			
			return userStr;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// Function:     	formatGroupString
		// Author:       	Justin Hickey
		// Purpose:      	To format the group list into a printable form
		// Input Vars:   	none
		// Return Vars:  	groupStr
		// Date Created: 	Aug 19, 2010
		//////////////////////////////////////////////////////////////////////////////////////////
		private function formatGroupString():String
		{
			var groupData:Object;
			var groupStr:String = new String();
			var i:int;

			// Get the group data from the authorized groups list
			if (authGroups != null)
			{
   				for (i = 0; i < authGroups.length; i++)
   				{
   					groupData = authGroups.getItemAt(i);
   					
   					// Add a comma and space after the group name
					groupStr = groupStr + groupData.groupName + ", ";
   				}
   				
   				// Strip off the last comma and space
   				groupStr = groupStr.substring(0, groupStr.length - 2);
			}
			else
			{
				// Indicate no authorized groups were listed
				groupStr = "No authorized groups listed";
			}
			
			return groupStr;
		}
	}
}
