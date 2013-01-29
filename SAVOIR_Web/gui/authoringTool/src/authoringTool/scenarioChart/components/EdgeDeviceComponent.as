// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package authoringTool.scenarioChart.components
{
	import authoringTool.scenarioChart.ComponentGroup;
	import authoringTool.scenarioChart.LinkPoint;
	
	import flash.display.Bitmap;
	import flash.display.BitmapData;
	import flash.display.DisplayObject;
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Image;
	import mx.core.Application;

	public class EdgeDeviceComponent extends ComponentGroup
	{
		
		public var edgeDeviceName:String;
		
		public var edgeDeviceImageURI:String;
		
		private var resImage:Image;
		
		public function EdgeDeviceComponent(resProfile:XML){
			super();
			
			_resourceProfile = resProfile;
			resImage = Application.application.edgeDeviceImageCache.getImageDataForED(_resourceProfile.@name,_resourceProfile.@imageURI);
			resImage.addEventListener(Event.COMPLETE,handleImageLoadComplete);
			//resImage.source = _resourceProfile.@imageURI;
			//resImage.load(_resourceProfile.@imageURI);
			//var watcherSetter:ChangeWatcher = BindingUtils.bindSetter(updateImageData,resImage,"content");
			if(resImage.content != null){
				resImage.dispatchEvent(new Event(Event.COMPLETE));
			}
			activity.name = "unknown";
			activity.id = 0;
		}
		public function handleImageLoadComplete(event:Event):void{
			updateImageData(resImage.content);
		}
		public function updateImageData(val:DisplayObject):void{
			_rectEDBMPData = Bitmap(val).bitmapData;
		}
		
		public function get resourceID():String{
			return resourceProfile.@id;
		}
		private var _nodeID:Number = -1; 
		public function set nodeID(value:Number):void{
		
			_nodeID = value;
			
			if(Application.application.maxNodeID < value){
				Application.application.maxNodeID = value;
				
			}
			trace(Application.application.maxNodeID);
		    
		}
		public function get nodeID():Number{
			return _nodeID;
		}
		public var variables:ArrayCollection = null;
		//public var parameters:ArrayCollection = null;
		public var activity:Object = new Object();
		
		public function get activityID():String{
			return activity.id;
		}
		
		protected var _rectEDBMPData:BitmapData;
		
		private var _resourceProfile:XML = <resource id="0" name="UnKnown" imageURI="http://somewhere">
			                                  <activity id="0" name="UnKnown" paramValue="unknown">
				                                            <activityParameters>
					                                            <activityParameter id = "0" name="UnKnown" dataType="xs:integer"/>
					                                           
				                                            </activityParameters>
				                               </activity>
			                              </resource>;
		
		
		public function get resourceProfile():XML {
			return _resourceProfile;
		} 
		
		public function set resourceProfile(value:XML):void{
			_resourceProfile = value;
			
		}
			                              
		public function get resourceNodeXML():XML{
			var resID:String = resourceProfile.@id;
			var resName:String = resourceProfile.@name;
			var actID:String = activity.id;
			var actName:String = activity.name;
			
			var resNodeStr:String = "<resourceNode nodeId=\"" + nodeID + "\" x=\"" + Math.round(this.x) + "\"" + " y=\"" + Math.round(this.y) + "\">"
			                + "<resource id=\"" + resID + "\"" + " name=\"" + resName + "\">"
			                + "<activity id=\"" + actID + "\"" + " name=\"" + actName +  "\"" + "/>"
			                + "</resource>"
			                + "</resourceNode>";
			var resNode:XML = XML(resNodeStr);
			var resXMLList:XMLList = resNode.resource;
			var resXML:XML = XML(resXMLList[0]);
			for each(var variable:Object in variables){
				if(variable.name != "" && variable.parameter != ""){
					var variableStr:String = "<variable name=\"" + variable.name + "\"" 
					                       + " parameter=\"" + variable.parameter + "\"" 
					                       + " keepUpdate=\"" + variable.keepUpdate + "\"/>";
					var variableXML:XML = XML(variableStr);
					
					resXML.appendChild(variableXML);
				}                       
			}
			resNode.setChildren(resXML);                
			return resNode;                
		}	                              
		
//		public function EdgeDeviceComponent()
//		{
//			super();
//			activity.name = "unknown";
//			activity.id = 0;
//		}
		
		public function getParameters():XMLList{
			var actName:String = activity.name;
			var paraXMLList:XMLList = resourceProfile.activity.(@name==actName).activityParameters.activityParameter;
			return paraXMLList;
		}
		
		public function getActivities():ArrayCollection{
			var actArryCol:ArrayCollection = new ArrayCollection();
			if(resourceProfile != null){
				for each(var actXML:XML in resourceProfile.activity){
					var obj:Object = new Object();
					obj.name = actXML.@name;
					obj.id = actXML.@ID
					actArryCol.addItem(obj);
				}
			}
			return actArryCol;
		}
		//return XMLID like <resource id="70" activityID="354">
		public function getXMLIDOfNode():String{
			var nodeXMLID:String = null;
			if(resourceProfile != null && activity.hasOwnProperty("id")){
				var resourceID:String = resourceProfile.@id;
				var activityID:String = activity.id;
				nodeXMLID = "<resource id=\"" + resourceID + "\"" + " activityID=\"" + activityID + "\">" ;
			}
			return nodeXMLID;
		}
		
		public function get resourceIntanceName():String{
			var resName:String = resourceProfile.@name;
			var actName:String = activity.name;
			return resName + " activity " + actName;
		}
		
		public function get linkPointUp():LinkPoint{
			return LinkPoint(this.linkPoints.getItemAt(0));
		}
		
		public function get linkPointDown():LinkPoint{
			return LinkPoint(this.linkPoints.getItemAt(1));
		}
		
	}
}
