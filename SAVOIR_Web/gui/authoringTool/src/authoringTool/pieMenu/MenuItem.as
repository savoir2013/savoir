// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package authoringTool.pieMenu{
	import caurina.transitions.Tweener;
	
	import com.degrafa.GeometryGroup;
	import com.degrafa.GraphicImage;
	import com.degrafa.GraphicText;
	import com.degrafa.IGraphic;
	import com.degrafa.core.IGraphicsFill;
	import com.degrafa.core.IGraphicsStroke;
	
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.events.PropertyChangeEvent;
	
	[Event(name="itemClosed",type="erno.menus.PieMenuEvent")]
	
	public class MenuItem extends GeometryGroup implements IGraphic{
			
		private var _arcComponent:ArcComponent;
		public function MenuItem(){
						
			_arcComponent = new ArcComponent(0,0,0,0,0,0,0,"pie");
			
			_arcComponent.target = this;
			
			_arcComponent.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE,refreshArc);
			this.addEventListener(MouseEvent.MOUSE_OVER,mouseOverHandler);
			this.addEventListener(MouseEvent.MOUSE_DOWN,mouseDownHandler);
			this.addEventListener(MouseEvent.MOUSE_UP,mouseUpHandler);
							
			this.geometryCollection.addItem(_arcComponent);
			
			
		}
		
		
		/**
		 * to get/set this items parent item
		 * */
		private var _parentItem:Object;
		public function set parentItem(item:Object):void{
			_parentItem = item;
		}
		public function get parentItem():Object{
			return _parentItem;
		}
		
		
		/**
		 * to set/get start angle
		 * update it straight to _arcComponent
		 * */
		private var _startAngle:Number;
		public function set startAngle(value:Number):void{
			if(_startAngle != value){
				_startAngle = value;
			}
		}
		public function get startAngle():Number{
			return _startAngle;
		}
		
		/**
		 * to set/get arc
		 * update it straight to _arcComponent
		 * */
		private var _arc:Number;
		public function set arc(value:Number):void{
			if(_arc != value){
				_arc = value;
			}
		}
		public function get arc():Number{
			return _arc;
		}
		
		/**
		 * to set/get innerRadius
		 * update it straight to _arcComponent
		 * */
		private var _innerRadius:Number;
		public function set innerRadius(value:Number):void{
			if(_innerRadius != value){
				_innerRadius = value;
			}
		}
		public function get innerRadius():Number{
			if(_innerRadius >= 0){
				return _innerRadius;
			}else{
				return parentItem.innerRadius;
			}
		}
		
		/**
		 * to set/get gap
		 * */
		private var _gap:Number;
		public function set gap(value:Number):void{
			if(_gap != value){
				_gap = value;
			}
		}
		public function get gap():Number{
			if(_gap >= 0){
				return _gap;
			}else{
				return parentItem.gap;
			}
		}
		
		/**
		 * override function to get/Set stroke
		 * this is overrided because if we dont have stroke we have to get it 
		 * from parentItem
		 * */
		private var _stroke:IGraphicsStroke;
		public override function get stroke():IGraphicsStroke{
			if(_stroke){
				return _stroke;
			}else{
				return parentItem.stroke;
			}
		}
		public override function set stroke(value:IGraphicsStroke):void{
			_stroke = value;
			_arcComponent.stroke = value;
		}
		
		
		/**
		 * override function to get/Set mouse over stroke
		 * get it from parentItem if dont have own
		 * */
		private var _mouseOverStroke:IGraphicsStroke;
		public function get mouseOverStroke():IGraphicsStroke{
			if(_mouseOverStroke){
				return _mouseOverStroke;
			}else{
				return parentItem.mouseOverStroke;
			}
		}
		public function set mouseOverStroke(value:IGraphicsStroke):void{
			_mouseOverStroke = value;
		}
		
		
		/**
		 * override function to get/Set mouse down stroke
		 * get it from parentItem if dont have own
		 * */
		private var _mouseDownStroke:IGraphicsStroke;
		public function get mouseDownStroke():IGraphicsStroke{
			if(_mouseDownStroke){
				return _mouseDownStroke;
			}else{
				return parentItem.mouseDownStroke;
			}
		}
		public function set mouseDownStroke(value:IGraphicsStroke):void{
			_mouseDownStroke = value;
		}
		
		
		/**
		 * override function to get/Set fill
		 * this is overrided because if we dont have fill we have to get it 
		 * from parentItem
		 * */
		private var _fill:IGraphicsFill;
		public override function get fill():IGraphicsFill{
			if(_fill){
				return _fill;
			}else{
				return parentItem.fill;
			}
		}
		public override function set fill(value:IGraphicsFill):void{
			_fill = value;
			_arcComponent.fill = value;
		}
		
		
		/**
		 * override function to get/Set mouse over fill
		 * get it from parentItem if dont have own
		 * */
		private var _mouseOverFill:IGraphicsFill;
		public function get mouseOverFill():IGraphicsFill{
			if(_mouseOverFill){
				return _mouseOverFill;
			}else{
				return parentItem.mouseOverFill;
			}
		}
		public function set mouseOverFill(value:IGraphicsFill):void{
			_mouseOverFill = value;
		}
		
		
		/**
		 * override function to get/Set mouse down fill
		 * get it from parentItem if dont have own
		 * */
		private var _mouseDownFill:IGraphicsFill;
		public function get mouseDownFill():IGraphicsFill{
			if(_mouseDownFill){
				return _mouseDownFill;
			}else{
				return parentItem.mouseDownFill;
			}
		}
		public function set mouseDownFill(value:IGraphicsFill):void{
			_mouseDownFill = value;
		}
		
		
		/**
		 * to get this items subItems
		 * */
		private var _subItems:ArrayCollection = new ArrayCollection();
		public function get subItems():ArrayCollection{
			return _subItems;
		}
		
		public function get getOpenSubItem():MenuItem{
			return _openSubItem;
		}
		
		
		/**
		 * to get width/height
		 * this group dont have width/height but _arcComponent have
		 * */
		public override function get width():Number{
			return _arcComponent.width;
		}
		public override function get height():Number{
			return _arcComponent.width;
		}
		
		
		/**
		 * icon
		 * */
		private var _icon:Object;
		public function set icon(value:IGraphic):void{
			
			if(value){
				_icon = value;
				
				if(value is GraphicImage){
					_icon.scaleX = 0;
					_icon.scaleY = 0;
					_icon.mouseEnabled = false;
				
				}else if(value is GraphicText){
					_icon.mouseEnabled = false;
										
				}
			}
		}
		public function get icon():IGraphic{
			return _icon as IGraphic;
		}
		
		/**
		 * to refresh this geometryGroup
		 * all changes to _arcComponent apply
		 * */
		public function refresh():void{
			this.draw(null,null);
		}
		
		/**
		 * for arcComponent change event listener
		 * */
		private function refreshArc(event:PropertyChangeEvent):void{
			refresh();
			if(parentItem.getOpenSubItem == this){
				refreshItems();
			}
		}
		
		private function mouseDownHandler(event:MouseEvent):void{
			_arcComponent.fill = this.mouseDownFill;
			refresh();
		}
		
		private function mouseUpHandler(event:MouseEvent):void{
			_arcComponent.fill = this.mouseOverFill;
			refresh();
		}
		
		private function mouseOverHandler(event:MouseEvent):void{
			//change fill if mouse over
			if(_tweening == false){
				_arcComponent.fill = this.mouseOverFill;
				refresh();
				this.addEventListener(MouseEvent.MOUSE_OUT,mouseOutHandler);
				_parentItem.openSubItem(this);
			}
		}
		
		private function mouseOutHandler(event:MouseEvent):void{
			//change fill back
			_arcComponent.fill = this.fill;
			refresh();
		}
		
		/**
		 * to add new item to subItems-arraycollection
		 * */
		public function addSubItem(item:MenuItem):void{
			_subItems.addItem(item);
			
		}
		
		public function refreshItems():void{
			
			for(var i:int=0;i<_subItems.length;i++){
				var item:MenuItem = _subItems.getItemAt(i) as MenuItem;
				item.refreshItem();
			}
			
			
		}
		
		
		/**
		 * to recalculate arcComponent values
		 * */	
		private var _tweening:Boolean = false;
		public function refreshItem():void{
			
			if(_openSubItem){
				this.refreshItems();
			}
			
			var newArc:Number;
			var newStartAngle:Number;
			var thisParentItem:Object;
			
			var newWidth:Number;
			var newHeight:Number;
			
			var newInnerRadius:Number;
			
			var startAngleFix:Number;
			
			if(this.parentItem is MenuItem){
				
				thisParentItem = this.parentItem as MenuItem;

				newWidth = thisParentItem.width+thisParentItem.width*(this.innerRadius);
				newHeight = thisParentItem.height+thisParentItem.height*(this.innerRadius);
				
				newInnerRadius = 1-(_parentItem.width/newWidth);
				
				
				newArc = (thisParentItem.arc-this.gap*(thisParentItem.subItems.length-1))/thisParentItem.subItems.length;
				//newStartAngle = (newArc+this.gap)+(newArc+this.gap)*thisParentItem.subItems.getItemIndex(this);
			
				newStartAngle = thisParentItem.startAngle+((newArc+this.gap)*thisParentItem.subItems.getItemIndex(this));
				
				
			}else if(this.parentItem is PieMenu){
				
				thisParentItem = this.parentItem as PieMenu;
				
				newWidth = thisParentItem.width;
				newHeight = thisParentItem.height;
				
				newInnerRadius = thisParentItem.innerRadius;
				
				newArc = (thisParentItem.arc-this.gap*thisParentItem.subItems.length)/thisParentItem.subItems.length;
				//newStartAngle = (newArc+this.gap)+(newArc+this.gap)*thisParentItem.subItems.getItemIndex(this)-(this.gap/2);
				
				
				newStartAngle = thisParentItem.arc+this.gap-(this.gap/2)+((newArc+this.gap)*thisParentItem.subItems.getItemIndex(this));
				
			}	
			
			//newArc = (thisParentItem.arc-this.gap*(thisParentItem.subItems.length-1))/thisParentItem.subItems.length;
			//newStartAngle = thisParentItem.startAngle+(newArc+newStartAngleCapFix)*thisParentItem.subItems.getItemIndex(this);
			
			
			_arc = newArc;
			_startAngle = newStartAngle;
			
			
			var newX:Number = -newWidth/2;
			var newY:Number = -newHeight/2;

			_tweening = true;
			Tweener.addTween(_arcComponent,{arc:newArc,
											startAngle:newStartAngle,
											innerRadius:newInnerRadius,
											width:newWidth,
											height:newHeight,
											x:newX,
											y:newY,
											onComplete:showIcon,
											time:0.5,transition:"easeInOutCirc"});
				
			
			if(_icon){
				//update icon position
				var _testX:Number = (newWidth/2)*Math.cos((newStartAngle+newArc/2)*Math.PI/180);
				var _testY:Number = (newWidth/2)*Math.sin((newStartAngle+newArc/2)*Math.PI/180);
				
				var _testX2:Number = (_testX+newWidth/2)/newWidth;
				var _testY2:Number = (_testY+newWidth/2)/newWidth;
				
				
				_testX = _testX-(_icon.width*_testX2);
				_testY = _testY-(_icon.width*_testY2);
				
				//temp. fix
				if(_icon is GraphicText){
					_testY += 4;
				}
				
				_icon.x = _testX;
				_icon.y = _testY;
			}
			refresh();
			
			_arcComponent.fill = this.fill;
			_arcComponent.stroke = this.stroke;
		}
		
		
		public function showIcon():void{
			_tweening = false;
			
			Tweener.addTween(_icon,{scaleX:1,scaleY:1,time:0.3,transition:"linear"});
		}
		
		
		
		/**
		 * to set what subItem is open
		 * if other subItem is open we have to close it first
		 * */
		 private var _openSubItem:MenuItem;
		 private var _newOpenSubItem:MenuItem;
		 private var _isOpening:Boolean = false;
		 public function openSubItem(item:MenuItem):void{
		 	
		 	if(_openSubItem != item){
			 	_isOpening = true;
			 	//add item to memory
			 	_newOpenSubItem = item;
			 	//if there is item open close it
			 	closeSubItem();
			 }
		 }
		 
		 /**
		 *to close subItem
		 * if there is openSubItem ->close it
		 * */
		 public function closeSubItem():void{
		 	if(_openSubItem){
		 		_openSubItem.addEventListener(PieMenuEvent.ITEM_CLOSED,thisSubItemsClosed);
		 		_openSubItem.closeSubItem();
		 	}else{
		 		closeThisSubItems();
		 	}
		 }
		 
		 /**
		 * 
		 * */
		 private function thisSubItemsClosed(event:PieMenuEvent):void{
		 	_openSubItem.removeEventListener(PieMenuEvent.ITEM_CLOSED, thisSubItemsClosed);
		 	_openSubItem = null;
			closeThisSubItems();
			
		 }
		 
		 /**
		 * to close all this MenuItem's subItems
		 * */
		 private function closeThisSubItems():void{
		 	
		 	if(_isOpening == true){
		 		
				_openSubItem = _newOpenSubItem;
				_openSubItem.refreshItems();
				_isOpening = false;
				
		 	}else{
				for(var i:int=0;i<_subItems.length;i++){
					var item:MenuItem = _subItems.getItemAt(i) as MenuItem;
					item.closeSelf();
				}
				this.dispatchEvent(new PieMenuEvent(PieMenuEvent.ITEM_CLOSED));
		 	}
		 }
		 
		 /**
		 * to close this item
		 * */
		 public function closeSelf():void{
		 	Tweener.addTween(icon,{scaleX:0,scaleY:0,time:0.3,transition:"linear"});
			_arc = 0;
			Tweener.addTween(_arcComponent,{arc:0,delay:0.3,time:0.5,transition:"easeInOutCirc"});
			_startAngle = 0;
			Tweener.addTween(_arcComponent,{startAngle:0,delay:0.3,time:0.5,transition:"easeInOutCirc"});
			
			Tweener.addTween(_arcComponent,{innerRadius:0,delay:0.3,time:0.5,transition:"easeInOutCirc"});
			
			Tweener.addTween(_arcComponent,{width:0,height:0,x:0,y:0,delay:0.5,time:0});
		 }
		
	}
	
	
}
