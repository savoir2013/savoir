// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package authoringTool.scenarioChart{
	import authoringTool.scenarioChart.components.ComponentEvent;
	
	import caurina.transitions.Tweener;
	
	import com.degrafa.GeometryGroup;
	
	import flash.geom.Point;
	
	import mx.collections.ArrayCollection;
	
	[Event(name="componentMove", type="erno.components.ComponentEvent")]
	
	/**
	 * Main class what all components(elements) extends
	 * 
	 * */
	public class ComponentGroup extends GeometryGroup{
		
		[ArrayElementType("erno.LinkPoint")]
		public var linkPoints:ArrayCollection = new ArrayCollection();
		
		public var componentType:String = null;
		
		[ArrayElementType("erno.SettingsObject")]
		public var settingsData:ArrayCollection = new ArrayCollection();
		
		public var dbID:int = undefined;
		
		public var _selected:Boolean = false;
		
				
		public function ComponentGroup(){
			super();
		}
		
		//refresh geometryGroup
		public function refresh():void{
			draw(null, null);
		}
		
		/**
		 * isLinkPoint 
		 * returns true if clicked point(point) is over linkpoint
		 * 
		 * */
		public function isLinkPoint(point:Point):Boolean{
			
			var isLink:Boolean = false;
			//check all links
			for(var i:int=0;i<linkPoints.length;i++){
				
				//ask from each linkPoint is it over
				if(linkPoints.getItemAt(i).isLinkPoint(point) == true){
					isLink = true;
				}
				
			}
			
			return isLink;
		}
		
		/**
		 * getLinkPoint
		 * return linkpoint from point (point).
		 * */
		public function getLinkPoint(point:Point):LinkPoint{
			
			var returnLinkPoint:LinkPoint;
			//check all links
			for(var i:int=0;i<linkPoints.length;i++){
				
				if(linkPoints.getItemAt(i).isLinkPoint(point) == true){
					returnLinkPoint = linkPoints.getItemAt(i) as LinkPoint;
				}
				
			}
			return returnLinkPoint;
			
		}
		
		/**
		 * checkLinkPoints
		 * checks that there is not allready link between point1 and point2
		 * */
		public function checkLinkPoints(point1:LinkPoint,point2:LinkPoint):Boolean{
			
			var _link1found:Boolean = false;
			var _link2found:Boolean = false;
			
			for(var i:int=0;i<linkPoints.length;i++){
				
				if(linkPoints.getItemAt(i) == point1){
					_link1found = true;
				}	
				
				if(linkPoints.getItemAt(i) == point2){
					_link2found = true;
				}
			}
			
			if(_link1found == true && _link2found == true){
				//both found
				return false;
			}else{
				//everything ok
				return true;
			}
			
		}
		
		public function dispatchMoveEvent():void{
			
			dispatchEvent(new ComponentEvent("componentMove",this));
			
		}
		
		public function get selected():Boolean{
			return _selected;
		}
		
		public function set selected(value:Boolean):void{
			if(value == true){
				_selected = true;
				refresh();
			}else{
				_selected = false;
				refresh();
			}
		}
		
		/**
		 * remove
		 * this removes component and all linkCurves to it
		 * */
		public function remove():void{
			
			//loop trough all linkpoints
			for(var i:int=0;i<linkPoints.length;i++){
				var linkPoint:LinkPoint = linkPoints.getItemAt(i) as LinkPoint;
				
				//if linkpoint is linked
				if(linkPoint.linked == true){
					
					var allTargetLinks:ArrayCollection = linkPoint.targetLinks;
					for(var j:int=0;j<allTargetLinks.length;j++){
						allTargetLinks.getItemAt(j).linkCurve.remove();
					}
				}
			}
			removeComp();
			//remove (this)component
			//Tweener.addTween(this,{scaleX:0,scaleY:0,time:0.5,onComplete:testremove,transition:"linear"});
		}
		
		private function removeComp():void {
			var surface:SurfaceComponent = this.parent as SurfaceComponent;
			
			surface.unFocus(this);
			this.parent.removeChild(this);
			
		}
		
	}
}
