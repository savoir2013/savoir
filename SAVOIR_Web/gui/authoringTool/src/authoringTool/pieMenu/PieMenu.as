// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package authoringTool.pieMenu{
	import com.degrafa.GraphicText;
	import com.degrafa.Surface;
	import com.degrafa.core.IGraphicsFill;
	import com.degrafa.core.IGraphicsStroke;
	import com.degrafa.paint.LinearGradientFill;
	import com.degrafa.paint.GradientStop;
	import com.degrafa.paint.SolidStroke;
	
	import flash.events.MouseEvent;
	import flash.events.TimerEvent;
	import flash.utils.Timer;
	
	import mx.collections.ArrayCollection;
	import mx.styles.StyleManager;
	
	[Event(name="menuClosed",type="erno.menus.PieMenuEvent")]
	[Event(name="itemClosed",type="erno.menus.PieMenuEvent")]
	
	public class PieMenu extends Surface{
		
		private var _subItems:ArrayCollection = new ArrayCollection();
		private var _closeTimer:Timer = new Timer(300);
		
		public function PieMenu(width:Number=0,height:Number=0,Arc:Number=360,startAngle:Number=0,gap:Number=0):void{
			
			//make default stroke
			
			var defaultStroke:SolidStroke = new SolidStroke();
			defaultStroke.color = 0x000000;
			defaultStroke.weight = 2;
			defaultStroke.alpha = 0.5;
			
			this.stroke = defaultStroke;
		
			
			//make default fill
            var defaultFill:LinearGradientFill = new LinearGradientFill();
            defaultFill.angle = 90;
            var fillGradStop1:GradientStop = new GradientStop(StyleManager.getColorName("#a0c1ed"), 1,0.5);
            var fillGradStop2:GradientStop = new GradientStop(StyleManager.getColorName("#cbe1fe"), 1,1);
            defaultFill.gradientStopsCollection.addItem(fillGradStop1);
            defaultFill.gradientStopsCollection.addItem(fillGradStop2);
			
			this.fill = defaultFill;
			
			//make default mouse over fill
            var defaultOverFill:LinearGradientFill = new LinearGradientFill();
            defaultOverFill.angle = 90;
            var overFillGradStop1:GradientStop = new GradientStop(StyleManager.getColorName("#cbe1fe"), 1,0.3);
            var overFillGradStop2:GradientStop = new GradientStop(StyleManager.getColorName("#ffffff"), 1);
            defaultOverFill.gradientStopsCollection.addItem(overFillGradStop1);
            defaultOverFill.gradientStopsCollection.addItem(overFillGradStop2);
			
			this.mouseOverFill = defaultOverFill;
			
			//make default mouse over fill
            var defaultDownFill:LinearGradientFill = new LinearGradientFill();
            defaultDownFill.angle = 90;
            var downFillGradStop1:GradientStop = new GradientStop(StyleManager.getColorName("#6184a9"), 1,0.3);
            var downFillGradStop2:GradientStop = new GradientStop(StyleManager.getColorName("#a0c1ed"), 1);
            defaultDownFill.gradientStopsCollection.addItem(downFillGradStop1);
            defaultDownFill.gradientStopsCollection.addItem(downFillGradStop2);
			
			this.mouseDownFill = defaultDownFill;
			
			this.width = width;
			this.height = height;
			this.arc = Arc;
			this.startAngle = startAngle;
			this.gap = gap;
			
			this.addEventListener(MouseEvent.ROLL_OVER, mouseRollOverHandler);
			
		}
				
		
				
		
		/**
		 * to set/get default stroke
		 * */
		private var _stroke:IGraphicsStroke;
		public function set stroke(value:IGraphicsStroke):void{
			if(_stroke != value){
				if(value){
					_stroke = value;
				}
			}
		}
		public function get stroke():IGraphicsStroke{
			return _stroke;
		}
		
		/**
		 * to set/get default mouseOver stroke
		 * */
		private var _mouseOverStroke:IGraphicsStroke;
		public function set mouseOverStroke(value:IGraphicsStroke):void{
			if(_mouseOverStroke != value){
				if(value){
					_mouseOverStroke = value;
				}
			}
		}
		public function get mouseOverStroke():IGraphicsStroke{
			return _mouseOverStroke;
		}
		
		
		/**
		 * to set/get default mouse down stroke
		 * */
		private var _mouseDownStroke:IGraphicsStroke;
		public function set mouseDownStroke(value:IGraphicsStroke):void{
			if(_mouseDownStroke != value){
				if(value){
					_mouseDownStroke = value;
				}
			}
		}
		public function get mouseDownStroke():IGraphicsStroke{
			return _mouseDownStroke;
		}
		
		
		/**
		 * to set/get default fill
		 * */
		private var _fill:IGraphicsFill;
		public function set fill(value:IGraphicsFill):void{
			if(_fill != value){
				if(value){
					_fill = value;
				}
			}
		}
		public function get fill():IGraphicsFill{
			return _fill;
		}
		
		
		/**
		 * to set/get default mouse over fill
		 * */
		private var _mouseOverFill:IGraphicsFill;
		public function set mouseOverFill(value:IGraphicsFill):void{
			if(_mouseOverFill != value){
				if(value){
					_mouseOverFill = value;
				}
			}
		}
		public function get mouseOverFill():IGraphicsFill{
			return _mouseOverFill;
		}
		
		
		/**
		 * to set/get default mouse out fill
		 * */
		private var _mouseDownFill:IGraphicsFill;
		public function set mouseDownFill(value:IGraphicsFill):void{
			if(_mouseDownFill != value){
				if(value){
					_mouseDownFill = value;
				}
			}
		}
		public function get mouseDownFill():IGraphicsFill{
			return _mouseDownFill;
		}
		
		
		/**
		 * to set how big is whole menus arc
		 * value 1-360
		 * */
		private var _arc:Number=0;
		public function set arc(value:Number):void{
			if(_arc != value){
				_arc = value;
				refreshItems();
			}
		}
		public function get arc():Number{
			return _arc;
		}
		
		/**
		 * to set/get start angle
		 * angle where menu starts
		 * */
		private var _startAngle:Number=0;
		public function set startAngle(value:Number):void{
			if(_startAngle != value){
				_startAngle = value;
				refreshItems();
			}
		}
		public function get startAngle():Number{
			return _startAngle;
				refreshItems();
		}
		
		/**
		 * to set/get innerRadius
		 * how many % of full width is middle hole
		 * if value is 0 there is hole at all
		 * if value is 0.99 item width is only 0.01% of full width
		 * value 1 cant be. 
		 * */
		private var _innerRadius:Number=0;
		public function set innerRadius(value:Number):void{
			if(_innerRadius != value){
				_innerRadius = value;
				refreshItems();
			}
		}
		public function get innerRadius():Number{
			return _innerRadius;
		}
		
		/**
		 * to set/get gap between items
		 * */
		private var _gap:Number=0;
		public function set gap(value:Number):void{
			if(_gap != value){
				_gap = value;
				refreshItems();
			}
		}
		public function get gap():Number{
			return _gap;
		}
		
		/**
		 * temporary testing
		 * */
		private var _iconRotate:Number = 0;
		public function get iconRotate():Number{
			return _iconRotate;
		}
		public function set iconRotate(value:Number):void{
			_iconRotate = value;
			refreshItems();
		}
		
		
		/**
		 * to get all subItems
		 * */
		public function get subItems():ArrayCollection{
			return _subItems;
		}
		
		public function get getOpenSubItem():MenuItem{
			return _openSubItem;
		}
		
		
		/**
		 * add listener when mouse rolls out from menu
		 * */
		private function mouseRollOverHandler(event:MouseEvent):void{
			if(_rollOutTimer.running){
				_rollOutTimer.reset();
			}
			this.addEventListener(MouseEvent.ROLL_OUT,mouseRollOutHandler);
		}
		
		
		/**
		 * close this menu
		 * */
		private var _rollOutTimer:Timer = new Timer(1000,1);
		private function mouseRollOutHandler(event:MouseEvent):void{
			
		 	if(!_rollOutTimer.running){
		 		_rollOutTimer.addEventListener(TimerEvent.TIMER_COMPLETE,rollOutTimerHandler);
		 		_rollOutTimer.start();
		 	}
		 	
		}
		
		private function rollOutTimerHandler(event:TimerEvent):void{	 	
		 	closePie();
		}
		
		public function closePie():void{
			
			for(var i:int=0;i<_subItems.length;i++){
		 		var item:MenuItem = _subItems.getItemAt(i) as MenuItem;
		 		item.closeSubItem();
		 		item.closeSelf();
		 	}
		 	
		 	var closeTimer:Timer = new Timer(500,1);
		 	closeTimer.addEventListener(TimerEvent.TIMER_COMPLETE,removeFromStage);
		 	closeTimer.start();
		}
		
		public function removeFromStage(event:TimerEvent):void{
		 	this.dispatchEvent(new PieMenuEvent(PieMenuEvent.MENU_CLOSED));
		 	if(this.parent){
		 		this.parent.removeChild(this);
		 	}
		}
		
		/**
		 * to add new menuItem
		 * */
		public function addItem(item:MenuItem):void{
			
			//add it to surface			
			this.graphicsCollection.addItem(item);
			
			item.target = this;
						
			//add it to parentItem's subitems-arraycollection
			item.parentItem.addSubItem(item);
			refreshItems();
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
		 * to set what subItem is open
		 * if other subItem is open we have to close it first
		 * */
		 private var _openSubItem:MenuItem;
		 private var _newOpenSubItem:MenuItem;
		 public function openSubItem(item:MenuItem):void{
		 	
		 	if(_openSubItem != item){
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
		 		_openSubItem.addEventListener(PieMenuEvent.ITEM_CLOSED,openNewSubItem);
		 		_openSubItem.closeSubItem();
		 	}else{
		 		openNewSubItem(new PieMenuEvent(PieMenuEvent.ITEM_CLOSED));
		 	}
		 }
		 
		 private function openNewSubItem(event:PieMenuEvent):void{
		 	_openSubItem = _newOpenSubItem as MenuItem;
		 	_openSubItem.refreshItems();
		 	
		 }
		
				
	}
}
