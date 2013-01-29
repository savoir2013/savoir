// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package authoringTool.scenarioChart.components{
	import authoringTool.scenarioChart.ComponentGroup;
	import authoringTool.scenarioChart.LinkPoint;
	
	import com.degrafa.geometry.RoundedRectangle;
	import com.degrafa.paint.BitmapFill;
	import com.degrafa.paint.GradientStop;
	import com.degrafa.paint.LinearGradientFill;
	import com.degrafa.paint.SolidStroke;
	
	import flash.display.Bitmap;
	import flash.display.BitmapData;
	import flash.events.MouseEvent;
	import flash.filters.ColorMatrixFilter;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	
	import mx.collections.ArrayCollection;
	import mx.events.CollectionEvent;
	import mx.managers.PopUpManager;
	import mx.styles.StyleManager;
	
	import savoirComp.InfoPopupWindow;
	/**
	 * BooleanComponent
	 * element where is true and false linkpoints
	 * 
	 * maybe trueFalseComponent is better name?
	 * */
	public class BooleanComponent extends ComponentGroup{
		
		//private
		private var _defaultBoolean:RoundedRectangle;
		private var _defaultStroke:SolidStroke;
		private var _defaultFill:LinearGradientFill;
		private var _selectedFill:LinearGradientFill;
		private var _booleanBmpfill:BitmapFill;
		private var _booleanBmpSelectedFill:BitmapFill;
		private var _booleanBMPData:BitmapData;
		private var _booleanBMPSelectedData:BitmapData;
		
		[Bindable]
		[Embed(source="/assets/widgets/medium_dimandRule.png")]
		private var _booleanImage:Class;
		//changed by yyh
		//upper link point
		private var _linkPointTrue:LinkPoint;
		//lower link point
		private var _linkPointFalse:LinkPoint; 
		
		public function get linkPointUp():LinkPoint{
			return _linkPointTrue;
		}
		
		public function get linkPointDown():LinkPoint{
			return _linkPointFalse;
		}
		public var rules:ArrayCollection = null;
		
		public var rulesXMLList:XMLList = null;
		
		private var matrix:Array = [
							1, 0, 0, 0, 0, 
							0, 1, 0, 0, 0, 
							0, 0, 1, 0, 0, 
							0, 0, 0, 0.6, 0
							];
				
		private var colorMatrix:ColorMatrixFilter;
		
		private var selectedFilter:Boolean = false;
						
		public function BooleanComponent(){
			
			_booleanBMPData = new _booleanImage().bitmapData;
			_booleanBmpfill = new BitmapFill();
			_booleanBmpfill.source = new Bitmap(_booleanBMPData);
			_booleanBmpfill.rotation = 0;
			_booleanBmpfill.smooth = true;
			
			//add for selection filtering
			
			colorMatrix = new ColorMatrixFilter(matrix);
			
			_booleanBMPSelectedData = new _booleanImage().bitmapData;
			
			_booleanBmpSelectedFill = new BitmapFill();
			_booleanBmpSelectedFill.source = new Bitmap(_booleanBMPSelectedData);
			_booleanBmpSelectedFill.smooth = true;
			_booleanBmpSelectedFill.rotation = 0;
		    
		    
		    componentType = "boolean";
		    
          	//stroke for default Boolean
            _defaultStroke = new SolidStroke();
            _defaultStroke.color = 0x000000;
            _defaultStroke.alpha = 0.2;
            _defaultStroke.weight = 4;
           
			//fill for default Boolean
            _defaultFill = new LinearGradientFill();
            _defaultFill.angle = 45;
            var gradStop1:GradientStop = new GradientStop(StyleManager.getColorName("#ffce79"), 1,0.3);
            var gradStop2:GradientStop = new GradientStop(StyleManager.getColorName("#bc8629"), 1);
            _defaultFill.gradientStopsCollection.addItem(gradStop1);
            _defaultFill.gradientStopsCollection.addItem(gradStop2);
			
			//when circle is selected this fill is used
            _selectedFill = new LinearGradientFill();
            _selectedFill.angle = 45;

            var selectedGradStop1:GradientStop = new GradientStop(StyleManager.getColorName("#88b5f2"), 1,0.3);
            var selectedGradStop2:GradientStop = new GradientStop(StyleManager.getColorName("#4b78b5"), 1);
            _selectedFill.gradientStopsCollection.addItem(selectedGradStop1);
            _selectedFill.gradientStopsCollection.addItem(selectedGradStop2);
				
			//create link points where user can create link
          	_linkPointTrue = new LinkPoint(this,"up",-35,-35,12,true,0,-40);
          	_linkPointTrue.targetLinks.addEventListener(CollectionEvent.COLLECTION_CHANGE,handleLinkChangeInTrue);
          	linkPoints.addItem(_linkPointTrue);
          	geometryCollection.addItem(_linkPointTrue);
          	geometryCollection.setItemIndex(_linkPointTrue,0);
          	
          	_linkPointFalse = new LinkPoint(this,"down",35,35,12,true,0,40);
          	_linkPointFalse.targetLinks.addEventListener(CollectionEvent.COLLECTION_CHANGE,handleLinkChangeInFalse);
          	linkPoints.addItem(_linkPointFalse);
          	geometryCollection.addItem(_linkPointFalse);
          	geometryCollection.setItemIndex(_linkPointFalse,0);
			
//commented out right and left by yyh 08-30-10 because currently doesn't support			
//          	var linkPointBack:LinkPoint = new LinkPoint(this,"left",-23,23,12,true,-40,0);
//          	linkPoints.addItem(linkPointBack);
//          	geometryCollection.addItem(linkPointBack);
//          	geometryCollection.setItemIndex(linkPointBack,0);
//          	
//          	var linkPointForward:LinkPoint = new LinkPoint(this,"right",23,-23,12,true,0,40);
//          	linkPoints.addItem(linkPointForward);
//          	geometryCollection.addItem(linkPointForward);
//          	geometryCollection.setItemIndex(linkPointForward,0);
			
			//create default(main) Boolean
			_defaultBoolean = new RoundedRectangle(-37,-37,74,74);
			_defaultBoolean.stroke = _defaultStroke;
          	_defaultBoolean.fill = _booleanBmpfill;
          	_defaultBoolean.cornerRadius = 15;
          	geometryCollection.addItem(_defaultBoolean);
          	
          	
          	this.rotation = 45;
          	
          	this.refresh();
          	
          	addEventListener(MouseEvent.MOUSE_OVER, mouseOverHandler);
          	addEventListener(MouseEvent.MOUSE_OUT, mouseOutHandler);
		}
		
		private function handleLinkChangeInTrue(event:CollectionEvent):void{
			if(this._linkPointTrue.targetLinks.length > 1){
   				var infoWindow:InfoPopupWindow = InfoPopupWindow(PopUpManager.createPopUp(this, InfoPopupWindow, true));
                infoWindow.title = "Warning: You cannot link with 2 more upstream nodes of edge device";
                infoWindow.setMessage("Please remove extra upstream edge device nodes before you edit any transition rule!");
                infoWindow.formatInfoBox();
                PopUpManager.centerPopUp(infoWindow);
			}
		}
		
		private function handleLinkChangeInFalse(event:CollectionEvent):void{
			if(this._linkPointFalse.targetLinks.length > 1){
   				var infoWindow:InfoPopupWindow = InfoPopupWindow(PopUpManager.createPopUp(this, InfoPopupWindow, true));
                infoWindow.title = "Warning: You cannot link with 2 more downstream nodes of edge device";
                infoWindow.setMessage("Please remove extra downstream edge device nodes before you edit any transition rule!");
                infoWindow.formatInfoBox();
                PopUpManager.centerPopUp(infoWindow);
			}
		}
		
		private function mouseOverHandler(event:MouseEvent):void{
			//not yet in use
			//_closeStroke.alpha = 0.7;
			//_closeFill.alpha = 1;
			//refresh();
		}
		
		private function mouseOutHandler(event:MouseEvent):void{
			//not yet in use
			//_closeStroke.alpha = 0;
			//_closeFill.alpha = 0;
			//refresh();
		}
		
		public function getDefaultBoolean():RoundedRectangle{
			return _defaultBoolean;
		}
		
		public override function set selected(value:Boolean):void{
			if(value == true){
				//use the same bmp pictrue
				
				var pt:Point = new Point(-37,-37);
				_defaultBoolean.fill = _booleanBmpSelectedFill;
				var width:Number = _defaultBoolean.bounds.width * 1.5;
				var height:Number = _defaultBoolean.bounds.height * 1.5;
				var rect:Rectangle = new Rectangle(-37,-37,width,height);
				//Alert.show("width:" + width + "height:" + height);
				
				_selected = true;
				refresh();
				if(!selectedFilter){
					this._booleanBMPSelectedData.applyFilter(_booleanBMPSelectedData, rect, pt, colorMatrix);
					selectedFilter = true;
				}
				
			}else{
//				this._booleanBMPData.applyFilter(;
//				_booleanBmpfill.source = new Bitmap(_booleanBMPData);
                
//				var newmatrix:Array = [
//							1, 0, 0, 0, 0, 
//							0, 1, 0, 0, 0, 
//							0, 0, 1, 0, 0, 
//							0, 0, 0, 1, 0
//							];
//				
//				var newcolorMatrix:ColorMatrixFilter;
//				newcolorMatrix = new ColorMatrixFilter(newmatrix);
//				var width:Number = _defaultBoolean.bounds.width * 1.5;
//				var height:Number = _defaultBoolean.bounds.height * 1.5;
//				var rect:Rectangle = new Rectangle(-37,-37,width,height);
//                this._booleanBMPSelectedData.applyFilter(_booleanBMPSelectedData, rect, pt, newcolorMatrix);
				_defaultBoolean.fill = _booleanBmpfill;
				_selected = false;
				refresh();
			}
		}
		
		public function getUpperNode():EdgeDeviceComponent{
			var res:EdgeDeviceComponent = null;
			if(_linkPointTrue.linked){
				var targetComp:Object = _linkPointTrue.targetLinks.getItemAt(0);
				res = (EdgeDeviceComponent(targetComp.component));
			}
			return res;
		}
		
		public function getLowerNode():EdgeDeviceComponent{
			var res:EdgeDeviceComponent = null;
			if(_linkPointFalse.linked){
				var targetComp:Object = _linkPointFalse.targetLinks.getItemAt(0);
				res = (EdgeDeviceComponent(targetComp.component));
			}
			return res;
		}
			
	}
}
