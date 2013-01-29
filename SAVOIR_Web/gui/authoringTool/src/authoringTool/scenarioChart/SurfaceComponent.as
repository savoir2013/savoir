// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package authoringTool.scenarioChart{
	
	import authoringTool.scenarioChart.components.BooleanComponent;
	import authoringTool.scenarioChart.components.CircleComponent;
	import authoringTool.scenarioChart.components.ComponentEvent;
	import authoringTool.scenarioChart.components.EdgeDeviceComponent;
	import authoringTool.scenarioChart.components.EndComponent;
	import authoringTool.scenarioChart.components.LinkCurveEvent;
	import authoringTool.scenarioChart.components.RectangleComponent;
	import authoringTool.scenarioChart.components.RectangleEdgeDeviceComponent;
	import authoringTool.scenarioChart.components.StartComponent;
	
	import caurina.transitions.*;
	
	import com.degrafa.GeometryGroup;
	import com.degrafa.Surface;
	import com.degrafa.geometry.repeaters.HorizontalLineRepeater;
	import com.degrafa.geometry.repeaters.VerticalLineRepeater;
	import com.degrafa.paint.SolidFill;
	import com.degrafa.paint.SolidStroke;
	
	import flash.display.DisplayObject;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	import flash.utils.Dictionary;
	
	import mx.collections.ArrayCollection;
	import mx.core.Application;
	import mx.events.FlexEvent;
	import mx.events.ResizeEvent;
	
	[Event(name="componentSelected", type="erno.components.ComponentEvent")]
	[Event(name="linkMenuShow", type="erno.components.LinkCurveEvent")]
	[Event(name="linkMenuHide", type="erno.components.LinkCurveEvent")]
	
	/**
	 * the root of whole thing
	 * 
	 * in comments component/object/element all means possible same thing :D
	 * 
	 * */
	public class SurfaceComponent extends Surface{
		
		// private fields
        private var _backgroundGroup:GeometryGroup;
        private var _linkArrowGroup:GeometryGroup;
        private var _lineGroup:GeometryGroup;
        private var _lines:HorizontalLineRepeater;
        private var _verticalLines:VerticalLineRepeater;
        
        private var _blackStroke:SolidStroke;
        private var _whiteStroke:SolidStroke;
        private var _greyStroke:SolidStroke;
        
        private var _whiteFill:SolidFill;
        private var _closeRedFill:SolidFill;
                
        private var _linking:Boolean = false;
        private var _fromLinkPoint:LinkPoint = null;
        
        private var _links:ArrayCollection = new ArrayCollection();
        private var _components:ArrayCollection = new ArrayCollection();
        
        private var _dragging:Boolean = false;
        private var _draggingTarget:ComponentGroup = null;
        
        public var snapToGrid:Boolean = false;
        
        //public fields
        public var selectedObject:ComponentGroup = null;
        
        public var lastAddedObject:ComponentGroup = null;
		
		
		public function SurfaceComponent(){
			addEventListener(FlexEvent.INITIALIZE, setup);
			
		}
		
        /**
        * SETUP
        * creates all default stuff
        * - _backgroundGroup is for link curves
        * - _linkArrowGroup is for link arrow when creating link
        * */
		private function setup(event:FlexEvent):void
        {                                                     	
              	_linkArrowGroup = new GeometryGroup();
	        	_linkArrowGroup.target = this;
	        	graphicsCollection.addItem(_linkArrowGroup);
	        	_linkArrowGroup.addEventListener(MouseEvent.CLICK, removeLinkArrow);
        	    
        	    //couple default strokes. maybe not used
                _greyStroke = new SolidStroke();
                _greyStroke.color = 0x4b4e50;
                _greyStroke.alpha = 0.7;
                _greyStroke.weight = 2;
                
                var stroke:SolidStroke = new SolidStroke();
				stroke.color = 0xFFFFFF;
				stroke.alpha = 0.05;
				stroke.weight = 3;
				
				_lineGroup = new GeometryGroup();
				_lineGroup.target = this;
				this.graphicsCollection.addItem(_lineGroup);
				
				_lines = new HorizontalLineRepeater();
				_lines.stroke=_greyStroke;
				_lines.count= this.parent.height/30;
	            _lines.x=0;
	            _lines.y=0;
	            _lines.x1=this.parent.width;
	            _lines.moveOffsetX=0;
	            _lines.moveOffsetY=30;
	            
	            _verticalLines = new VerticalLineRepeater();
	            _verticalLines.stroke = _greyStroke;
	            _verticalLines.count = this.parent.width/30;
	            _verticalLines.x = 0;
	            _verticalLines.y = 0;
	            _verticalLines.y1 = this.parent.height;
	            _verticalLines.moveOffsetX = 30;
	            _verticalLines.moveOffsetY = 0;
				
				_lineGroup.geometryCollection.addItem(_lines);
				_lineGroup.geometryCollection.addItem(_verticalLines);
				_lineGroup.draw(null,null);
        	    //Application.application.addEventListener(ResizeEvent.RESIZE, windowResizeHandler);
        	    this.addEventListener(KeyboardEvent.KEY_UP, deleteKeyPressHandler);
        	    
        }
        
        private function windowResizeHandler(event:ResizeEvent):void{
        	updateLines();
        }
        
        private function deleteKeyPressHandler(event:KeyboardEvent):void{
        	if(event.keyCode == 46){
        	   this.removeSelectedComponent();
        	}
        	this.setFocus();
        }
        
        public function scale(value:Number):void{
        	if(this.scaleX != value){
        		Tweener.addTween(this,{tweenerScale:value,time:0.5,transition:"easeInOutCirc"});
        		
        	}
        	trace("surface scale!");
        }
        
        public function set tweenerScale(value:Number):void{
        	
        	this.scaleX = value;
        	this.scaleY = value;
        	updateLines();
        }
        
        public function get tweenerScale():Number{
        	return this.scaleX;
        }
        
        public function updateLines():void{
        	_lines.x = -this.x/this.scaleX;
        	_lines.y = (-(this.y-(this.y-(int(this.y/(30*this.scaleY))*(30*this.scaleY)))))/this.scaleY;
        	_lines.x1 = _lines.x+(this.parent.width/this.scaleX);
        	_lines.count = (this.parent.height/this.scaleX)/30;
        	trace("_verticalLines.x = " + _verticalLines.x);
        	_verticalLines.x = (-(this.x-(this.x-(int(this.x/(30*this.scaleX))*(30*this.scaleX)))))/this.scaleX;
        	_verticalLines.y = -this.y/this.scaleY;
        	_verticalLines.y1 = _verticalLines.y + (this.parent.height/this.scaleY);
        	//_verticalLines.x = 0;
        	//_verticalLines.y = 0;
        	//_verticalLines.y1 = this.parent.height;
        	_verticalLines.count = (this.parent.width/this.scaleY)/30;
        	_lineGroup.draw(null,null);
        }
 //original one       
//        public function updateLines():void{
//        	_lines.x = -this.x/this.scaleX;
//        	_lines.y = (-(this.y-(this.y-(int(this.y/(30*this.scaleY))*(30*this.scaleY)))))/this.scaleY;
//        	_lines.x1 = _lines.x+(Application.application.width/this.scaleX);
//        	_lines.count = (Application.application.width/this.scaleX)/30;
//        	_lineGroup.draw(null,null);
//        }
        
//        public function updateLines():void{
//        	trace("surface parent width " + this.parent.width);
//        	trace("surface parent height " + this.parent.height);
//        	trace("surface x" + this.x + " and surface y" + this.y + "surface width " + this.width + "surface height " + this.height);
//        	_lines.x = -this.x/this.scaleX;
//        	_lines.y = (-(this.y-(this.y-(int(this.y/(30*this.scaleY))*(30*this.scaleY)))))/this.scaleY;
//        	_lines.x1 = _lines.x+(this.parent.width/this.scaleX);
//        	_lines.count = (this.parent.width/this.scaleX)/30;
//        	//_lines.moveOffsetY = this.parent.width/_lines.count; 
//        	_verticalLines.x = (-(this.x-(this.x-(int(this.x/(30*this.scaleX))*(30*this.scaleX)))))/this.scaleX;
//        	_verticalLines.y = -this.y/this.scaleY;
//        	_verticalLines.y1 = _verticalLines.y + (this.parent.height/this.scaleY);
//        	//_verticalLines.x = 0;
//        	//_verticalLines.y = 0;
//        	//_verticalLines.y1 = this.parent.height;
//        	_verticalLines.count = (this.parent.height/this.scaleY)/30;
//        	//_verticalLines.moveOffsetX = this.parent.height/_verticalLines.count;
//        	_lineGroup.draw(null,null);
//        }
       
        
        public function get lines():GeometryGroup{
        	return _lineGroup;
        }
        
        public function set showGrid(value:Boolean):void{
        	if(value == true){
        		_lineGroup.alpha = 1;
        		updateLines();
        	}else{
        		_lineGroup.alpha = 0;
        		updateLines();
        	}
        }
     
       
        /**
        * components have all components in arraycollection
        * */
        public function get components():ArrayCollection{
        	
        	return _components;
        	
        }
        public function set components(data:ArrayCollection):void {
        	_components = data;
        	setup(new FlexEvent("initialize"));
        }
        
        private var _compDict:Dictionary = new Dictionary();
        
        public function get componentDictionary():Dictionary{
        	return this._compDict;
        }
        
        public function set componentDictionary(value:Dictionary):void{
        	this._compDict = value;
        }
        
        
        
        /**
        * addRectangle -function
        * creates rectangle 
        * 	x: X-position of new rectangle 
        * 	y: Y-position of new rectangle
        *   stargDrag: auto starts dragging when rectangle is created. (used in create buttons)
        * */
 		public function addRectangle(x:int=0,y:int=0,startDrag:Boolean=false):RectangleComponent
        {   
        	//create new rectangle component
            var rectangleComponent:RectangleComponent = new RectangleComponent();
            
            //setup
            rectangleComponent.target = this;
            rectangleComponent.x = x;
            rectangleComponent.y = y;
            
            //add it to surface
            graphicsCollection.addItem(rectangleComponent); 	
          	
            //add eventListeners
            rectangleComponent.addEventListener(MouseEvent.MOUSE_DOWN, componentMouseDownHandler);
            rectangleComponent.addEventListener(MouseEvent.MOUSE_UP, componentMouseUpHandler);
            rectangleComponent.addEventListener(MouseEvent.CLICK, componentMouseClickHandler);
            
          	
          	//if startdrag is true start dragging
          	if(startDrag == true){
          		rectangleComponent.startDrag();
          		_dragging = true;
          		_draggingTarget = rectangleComponent;
          	}
          	
          	//add component to arraycollection
          	_components.addItem(rectangleComponent);
          	
          	//return created group
          	return rectangleComponent;
        }
        
        public function addStart(x:int=0,y:int=0,startDrag:Boolean=false):StartComponent
        {   
        	//create new rectangle component
            var startComponent:StartComponent = new StartComponent();
            
            //setup
            startComponent.target = this;
            startComponent.x = x;
            startComponent.y = y;
            
            //add it to surface
            graphicsCollection.addItem(startComponent); 	
          	
            //add eventListeners
            startComponent.addEventListener(MouseEvent.MOUSE_DOWN, componentMouseDownHandler);
            startComponent.addEventListener(MouseEvent.MOUSE_UP, componentMouseUpHandler);
            startComponent.addEventListener(MouseEvent.CLICK, componentMouseClickHandler);
            
          	
          	//if startdrag is true start dragging
//          	if(startDrag == true){
//          		startComponent.startDrag();
//          		_dragging = true;
//          		_draggingTarget = startComponent;
//          	}
          	
          	//add component to arraycollection
          	_components.addItem(startComponent);
          	this.dispatchEvent(new ComponentEvent(ComponentEvent.ADD_COMPONENT,startComponent));
          	lastAddedObject = startComponent;
          	//return created group
          	return startComponent;
        }
        
        
        public function addEnd(x:int=0,y:int=0,startDrag:Boolean=false):EndComponent
        {   
        	//create new rectangle component
            var endComponent:EndComponent = new EndComponent();
            
            //setup
            endComponent.target = this;
            endComponent.x = x;
            endComponent.y = y;
            
            //add it to surface
            graphicsCollection.addItem(endComponent); 	
          	
            //add eventListeners
            endComponent.addEventListener(MouseEvent.MOUSE_DOWN, componentMouseDownHandler);
            endComponent.addEventListener(MouseEvent.MOUSE_UP, componentMouseUpHandler);
            endComponent.addEventListener(MouseEvent.CLICK, componentMouseClickHandler);
            
          	
          	//if startdrag is true start dragging
//          	if(startDrag == true){
//          		endComponent.startDrag();
//          		_dragging = true;
//          		_draggingTarget = endComponent;
//          	}
          	
          	//add component to arraycollection
          	_components.addItem(endComponent);
          	this.dispatchEvent(new ComponentEvent(ComponentEvent.ADD_COMPONENT,endComponent));
          	lastAddedObject = endComponent;
          	//return created group
          	return endComponent;
        }
        
        
        public function addRectEdgeDeviceComponent(x:int=0,y:int=0,resProfile:XML=null, startDrag:Boolean=false):RectangleEdgeDeviceComponent
        {   
        	//create new rectangle component
            var component:RectangleEdgeDeviceComponent = new RectangleEdgeDeviceComponent(resProfile);
            
            //setup
            component.target = this;
            component.x = x;
            component.y = y;
            
            //add it to surface
            graphicsCollection.addItem(component); 	
          	
            //add eventListeners
            component.addEventListener(MouseEvent.MOUSE_DOWN, componentMouseDownHandler);
            component.addEventListener(MouseEvent.MOUSE_UP, componentMouseUpHandler);
            component.addEventListener(MouseEvent.CLICK, componentMouseClickHandler);
            
          	
          	//if startdrag is true start dragging
//          	if(startDrag == true){
//          		component.startDrag();
//          		_dragging = true;
//          		_draggingTarget = component;
//          	}
          	
          	//add component to arraycollection
          	_components.addItem(component);
          	this.dispatchEvent(new ComponentEvent(ComponentEvent.ADD_COMPONENT,component));
          	lastAddedObject = component;
          	//return created group
          	return component;
        }
        /**
        * !!maybe trueFalse would be better name!!
        * 
        * addBoolean -function
        * creates boolean 
        *  
        * 	x: X-position of new rectangle 
        * 	y: Y-position of new rectangle
        *   stargDrag: auto starts dragging when boolean is created. (used in create buttons)
        * */
        public function addBoolean(x:int=0,y:int=0,startDrag:Boolean=false):BooleanComponent
        {       
        	//create new rectangle component
           var booleanComponent:BooleanComponent = new BooleanComponent();
//            var booleanContextObj:ComponentGroupContextMenu = new ComponentGroupContextMenu();
//            var booleanContextMenu:ContextMenu = booleanContextObj.compConextMenu;
//            var menuItem:ContextMenuItem = new ContextMenuItem("Delete");
//			//menuItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT,changeColor);
//			
//			var customContextMenu:ContextMenu = new ContextMenu();
//			
//			//hide the Flash menu
//			customContextMenu.hideBuiltInItems();
//			customContextMenu.customItems.push(menuItem);
//			booleanComponent.contextMenu = customContextMenu;
            //setup
            booleanComponent.target = this;
            booleanComponent.x = x;
            booleanComponent.y = y;
            
            //add it to surface
            graphicsCollection.addItem(booleanComponent); 	
          	
            //add eventListeners
            booleanComponent.addEventListener(MouseEvent.MOUSE_DOWN, componentMouseDownHandler);
            booleanComponent.addEventListener(MouseEvent.MOUSE_UP, componentMouseUpHandler);
            booleanComponent.addEventListener(MouseEvent.CLICK, componentMouseClickHandler);
//            booleanComponent.contextMenu = booleanContextMenu;
          	
          	//if startdrag is true start dragging
//          	if(startDrag == true){
//          		booleanComponent.startDrag();
//          		_dragging = true;
//          		_draggingTarget = booleanComponent;
//          	}
          	
          	
          	//add component to arraycollection
          	_components.addItem(booleanComponent);
          	this.dispatchEvent(new ComponentEvent(ComponentEvent.ADD_COMPONENT,booleanComponent));
          	lastAddedObject = booleanComponent;
          	//return created group
          	return booleanComponent;
        }
        
        /**
        * addCircle -function
        * creates circle 
        * 	x: X-position of new rectangle 
        * 	y: Y-position of new rectangle
        *   stargDrag: auto starts dragging when circle is created. (used in create buttons)
        * */
        public function addCircle(x:int=0,y:int=0,startDrag:Boolean=false):CircleComponent
        {       
        	//create new circle component
            var circleComponent:CircleComponent = new CircleComponent();
            
            //setup
            circleComponent.target = this;
            circleComponent.x = x;
            circleComponent.y = y;
            
            //add it to surface
            graphicsCollection.addItem(circleComponent); 	
          	
            //add eventListeners
            circleComponent.addEventListener(MouseEvent.MOUSE_DOWN, componentMouseDownHandler);
            circleComponent.addEventListener(MouseEvent.MOUSE_UP, componentMouseUpHandler);
            circleComponent.addEventListener(MouseEvent.CLICK, componentMouseClickHandler);
            
          	
          	//if startdrag is true start dragging
          	if(startDrag == true){
          		circleComponent.startDrag();
          		_dragging = true;
          		_draggingTarget = circleComponent;
          	}
          	
          	//add component to arraycollection
          	_components.addItem(circleComponent);
          		
          	//return created group
          	return circleComponent;
        }
        
        /**
        * MouseDownHandler 
        * dispatch when user click element
        * 
        *    used in all elements
        * */
        private function componentMouseDownHandler(event:MouseEvent):void{
        	
        	//if is not linking start dragging
        	if(_linking == false){
        		//event.currentTarget.startDrag();
        		_draggingTarget = event.currentTarget as ComponentGroup;
        		//add a scrollable rectangle for the component by yyh
        		//_draggingTarget.scrollRect = new Rectangle(0,0,this.parent.width,this.parent.height);
        		_draggingTarget.startDrag(false,new Rectangle(0,0,this.parent.width,this.parent.height));
        		trace("_draggingTarget parent is: " + this.parent.name);
        		trace("_draggingTarget parent width and height: " + this.parent.width + " : " + this.parent.height);
        		//end add 
        		_draggingTarget.addEventListener(MouseEvent.MOUSE_MOVE, updateLinkCurves);
        		
				setChildIndex(_draggingTarget as DisplayObject, numChildren-1);
								
        	}
        	this.setFocus();
        }
        
        /**
        * allmost same as mouseDownHandler 
        * used allso in all elements
        * */
        private var noClick:Boolean = false;
        private function componentMouseUpHandler(event:MouseEvent):void{
        	
        	//if draging stop it
        	if(_dragging == true){
        		trace("dragging noclick = true");
        		_draggingTarget.stopDrag();
        		_draggingTarget.removeEventListener(MouseEvent.MOUSE_MOVE, updateLinkCurves);
        		_dragging = false;
        		_draggingTarget = null;
        		noClick = true;
        		//this.dispatchEvent(new ComponentEvent(ComponentEvent.COMPONENT_MOVE,event.currentTarget as ComponentGroup));
        	}
        }
        
        /**
        * mouseClickHandler
        * when user clicks element
        * 
        * Three main situations
        *   -just click
        * 	 	not linking or not dragging element
        * 
        * 	-linking
        * 		user have allready started linking and now is selecting second point
        * 
        * 	-dragging
        * 		user is dragging
        * 
        * 
        * */        
        private function componentMouseClickHandler(event:MouseEvent):void{
        	
	        if(noClick == false){	
	        	trace("click no click = false");
	        	//linking and dragging not started
	        	if(_linking == false && _dragging == false){
	        		
	        		var fromPosition:Point = new Point(event.localX, event.localY);
	        		
	        		var fromPositionIsLink:Boolean = event.currentTarget.isLinkPoint(fromPosition);
	        		
	        		//if click position is link
	        		if(fromPositionIsLink == true){
	        			
	        			//start linking
		        		_linking = true;
		        		_fromLinkPoint = event.currentTarget.getLinkPoint(fromPosition);
			        	
		        		//create linking arrow
		        		createLinkArrow(_fromLinkPoint.stageX, _fromLinkPoint.stageY);
	        		
	        		//if is not link => select it 
	        		}else{
	        			
	        			//if selected -> unselect
	        			if(selectedObject != null){
	        				selectedObject.selected = false;
	        				selectedObject = null;
	        				
	        				unFocus(event.currentTarget as ComponentGroup);
	        				this.dispatchEvent(new ComponentEvent(ComponentEvent.COMPONENT_UNSELECTED));
	        				
	        			//its unselected -> select it
	        			}else{
	        				if(selectedObject != null){
	        					selectedObject.selected = false;
	        					selectedObject = null;
	        					this.dispatchEvent(new ComponentEvent(ComponentEvent.COMPONENT_UNSELECTED));
	        				}
	        				selectedObject = event.currentTarget as ComponentGroup;
	        				selectedObject.selected = true;
	        				
	        				focus(event.currentTarget as ComponentGroup);
	        				this.dispatchEvent(new ComponentEvent(ComponentEvent.COMPONENT_SELECTED, event.currentTarget as ComponentGroup));
	        			}
	        		}
		        	
		        //if user is linking
	        	}else if(_linking == true){
	        		
					//if click position is link
					var toPosition:Point = new Point(event.localX, event.localY);
	        		
	        		var toPositionIsLink:Boolean = event.currentTarget.isLinkPoint(toPosition);
	        		
	        		if(toPositionIsLink == true){
	        			
	        			var _toLinkPoint:LinkPoint = event.currentTarget.getLinkPoint(toPosition);
			        	
			        	//make sure its not same linkPoint
			        	if(_toLinkPoint != _fromLinkPoint){
			        		
			        		//make sure link is not allready created between linkpoints
			        		var checkPoints:Boolean = event.currentTarget.checkLinkPoints(_toLinkPoint,_fromLinkPoint);
			        		if(checkPoints){
								drawLink(_fromLinkPoint, _toLinkPoint);
			        		
								removeLinkArrow(new MouseEvent("click"));
			        		}
			        	}
	        			
	        		}
				
				//if user is dragging
	         	}else if(_dragging == true){
	         		
	         		_draggingTarget.stopDrag();
	         		_dragging = false;
	         		_draggingTarget = null;
	         		
	         	}else{
	         		
	         		removeLinkArrow(new MouseEvent("click"));
	         		
	         	}
	       }else{
	       	trace("click noclick true set to false");
	       	 noClick = false;
	       }
	       
        }
        
        /**
        * createLinkArrow
        * when user choose first link point this create link arrow on it        * 
        * */
        private function createLinkArrow(x:int, y:int):void{
        	
        	var linkArrow:LinkArrow = new LinkArrow();
          	_linkArrowGroup.geometryCollection.addItem(linkArrow);
          	_linkArrowGroup.draw(null, null);
	        
	        _linkArrowGroup.x = x;
        	_linkArrowGroup.y = y;
			setChildIndex(_linkArrowGroup, numChildren-1);
			
			Application.application.addEventListener(MouseEvent.MOUSE_MOVE, rotateLinkArrow);
			
        }
        
        //fix: change this so giving event is not required if its possible
        //remember change allso all places where removeLinkArrow is used
        private function removeLinkArrow(event:MouseEvent):void{
        	_linkArrowGroup.geometry = [];
        	_linkArrowGroup.draw(null, null);
        	Application.application.removeEventListener(MouseEvent.MOUSE_MOVE, rotateLinkArrow);
        	
     		_linking = false;
     		_fromLinkPoint = null;
        }
        
        /**
        * rotateLinkArrow
        * when user moves mouse this rotate arrow so its pointing to mouse
        * */
        private function rotateLinkArrow(event:MouseEvent):void{
        	var dx:int = event.stageX/this.scaleX - (_linkArrowGroup.x + this.x/this.scaleX);
        	var dy:int = event.stageY/this.scaleY - (_linkArrowGroup.y + this.y/this.scaleY);
        	
        	_linkArrowGroup.rotation = Math.atan2(dy, dx) * 180 / Math.PI;
        }
        
        /**
        * drawLink
        * when user select second linkPoint
        * */
        public function drawLink(fromPoint:LinkPoint, toPoint:LinkPoint):void{
        	        	
        	var linkFound:Boolean = false;
        	
        	//check that link is not created allready between these two elements
        	for(var i:int=0;i<_links.length;i++){
        		
        		if(_links.getItemAt(i).linkTo == toPoint && _links.getItemAt(i).linkFrom == fromPoint){
        			linkFound = true;
        		}
        		if(_links.getItemAt(i).linkTo == fromPoint && _links.getItemAt(i).linkFrom == toPoint){
        			linkFound = true;
        		}
        		
        	}
        	
        	//if link is not created between linkpoints
        	if(linkFound == false){
        		
        		//new linkCurve
	        	var linkCurve:LinkComponent = new LinkComponent(fromPoint,toPoint);
	  			linkCurve.target = this;
	  			//linkCurve.target = this;
	  			
	        	//add it to surface
	        	graphicsCollection.addItem(linkCurve);
	        	this.addChild(linkCurve);
	        	this.setChildIndex(linkCurve, 0);
	        	
	        	linkCurve.addEventListener(LinkCurveEvent.MOUSE_OVER_LINK,mouseOverLinkHandler);
	        	linkCurve.addEventListener(LinkCurveEvent.MOUSE_OUT_LINK,mouseOutLinkHandler);
	        	
	        	//update linkpoints
	        	fromPoint.setLink(toPoint.parentComponent, linkCurve);
	        	toPoint.setLink(fromPoint.parentComponent, linkCurve);
	        	       	
	        }
	        
        }
        
        private function mouseOverLinkHandler(event:LinkCurveEvent):void{
        	this.dispatchEvent(new LinkCurveEvent(LinkCurveEvent.LINK_MENU_SHOW,event.target as LinkComponent));
        }
        private function mouseOutLinkHandler(event:LinkCurveEvent):void{
        	this.dispatchEvent(new LinkCurveEvent(LinkCurveEvent.LINK_MENU_HIDE,event.target as LinkComponent));
        }
        
        /**
        * updateLinkCurves
        * when user drag element this dispatch event inside component so 
        * component realise to redraw link curves
        * */
        private function updateLinkCurves(event:MouseEvent):void{
        	 
        		_dragging = true;
        	//update link curve
        	var componentToUpdate:ComponentGroup = event.currentTarget as ComponentGroup;
        	
        	//dispatch event
        	componentToUpdate.dispatchMoveEvent();
			
        	if(snapToGrid == true){		
        		//var test:Number = (event.stageY-this.y) - int((event.stageY-this.y)/30)*30;
        		
        		var test:Number = ((event.stageY/this.scaleY)-(this.y/this.scaleY)) - int(((event.stageY/this.scaleY)-(this.y/this.scaleY))/30)*30;
        		
        		
        		
        		if(test < 0){
        			test = -test;
        			if(test >= 0 && test <= 14){
	        			componentToUpdate.y = ((event.stageY/this.scaleY)-(this.y/this.scaleY))+test;
	        		}else if(test >= 15 && test <= 30){
	        			componentToUpdate.y =((event.stageY/this.scaleY)-(this.y/this.scaleY))-(30-test);
	        		}
        		}else{
        			if(test >= 0 && test <= 14){
	        			componentToUpdate.y = ((event.stageY/this.scaleY)-(this.y/this.scaleY))-test;
	        		}else if(test >= 15 && test <= 30){
	        			componentToUpdate.y =((event.stageY/this.scaleY)-(this.y/this.scaleY))+(30-test);
	        		}
        		}
        	}
        }
		
        
        /**
        * focus
        * when user just click element this center clicked element and zoom to it
        * */ 
        private var _scaleValueBeforeFocus:Number = 1;
        public function focus(object:ComponentGroup):void{
    		
    		_scaleValueBeforeFocus = this.scaleX;
    		
        	var zoom:Number = 1.5;
			this.scale(zoom);
						
			var newX:int = ((this.parent.width/2)-object.x)-(object.x*(zoom-1));
			var newY:int = ((this.parent.height/2)-object.y)-(object.y*(zoom-1));
			//changed by yyh unfocus would not change the x and y except scale
			//Tweener.addTween(this,{x:newX,y:newY,time:0.5,transition:"easeInOutCirc"});
			
        }
        
        /**
        * unFocus
        * when element is zoomed this zoom back to normal 
        * */
        public function unFocus(object:ComponentGroup):void{
        	
        	if(object == null){
	        		
				this.scale(_scaleValueBeforeFocus);
				
        	}else{
	        		
				this.scale(_scaleValueBeforeFocus);
				var newX:int = ((this.parent.width/2)-object.x);
				var newY:int = ((this.parent.height/2)-object.y);
				//changed by yyh unfocus would not change the x and y except scale
				//Tweener.addTween(this,{x:newX,y:newY,time:0.5,transition:"easeInOutCirc"});
	    		
        	}
    	
    		if(this.selectedObject != null){
    			this.selectedObject.selected = false;
    			this.selectedObject = null;
    			this.dispatchEvent(new ComponentEvent(ComponentEvent.COMPONENT_UNSELECTED));
    		}
        }
        
        public function removeSelectedComponent():void{
        	if(this.selectedObject != null){
        	 this.selectedObject.remove();
        	}
        	
        }
        
        public function removeComponent(comp:ComponentGroup):void{
        	var index:int = this._components.getItemIndex(comp);
        	this._components.removeItemAt(index);
        }
        
        public function findOutDevComp(nodeId:Number):ComponentGroup{
        	var theComp:ComponentGroup = null;
        	for each(var comp:ComponentGroup in this.components){
        		if(comp is EdgeDeviceComponent){
        			var theNodeId:Number = (EdgeDeviceComponent(comp)).nodeID;
        			if(theNodeId == nodeId){
        				theComp = comp;
        			}
        		}
        	}
        	return theComp;
        }
        
        public function cleanAllComponents():void{
        	if(this._components.length == 0){
        		return;
        	}
        	this._components.removeAll();
        	graphicsCollection.pop();
        	//this.cleanAllComponents();
        	
        }
		
	}
}
