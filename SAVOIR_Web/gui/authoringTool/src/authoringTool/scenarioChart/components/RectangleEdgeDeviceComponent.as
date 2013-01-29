// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package authoringTool.scenarioChart.components
{
	import authoringTool.scenarioChart.LinkPoint;
	
	import com.degrafa.GraphicText;
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
	
	import mx.styles.StyleManager;
	import flash.display.DisplayObject;
	import mx.controls.Alert;
	import mx.core.Application;
	public class RectangleEdgeDeviceComponent extends EdgeDeviceComponent
	{
		private var _defaultRectangle:RoundedRectangle;
		private var _defaultStroke:SolidStroke;
		private var _defaultFill:LinearGradientFill;
		private var _selectedFill:LinearGradientFill;
		private var _rectEDBmpfill:BitmapFill;
		
		//private var _rectEDBMPData:BitmapData;
		
		private var _text:GraphicText;
		
		public var selectedChannelIndex:uint = 0;
		
		private var matrix:Array = [
							1, 0, 0, 0, 0, 
							0, 1, 0, 0, 0, 
							0, 0, 1, 0, 0, 
							0, 0, 0, 0.6, 0
							];
				
		private var colorMatrix:ColorMatrixFilter;
		
		private var selectedFilter:Boolean = false;
		
		private var _rectEDBmpSelectedFill:BitmapFill;
		private var _rectEDBMPSelectedData:BitmapData;
		public function RectangleEdgeDeviceComponent(resourceProfile:XML)
		{
			super(resourceProfile);
		}
		
		override public function updateImageData(val:DisplayObject):void{
			if(val == null){
				//Alert.show("Null");
				
				return;
			}
			//Alert.show("Not Null");
			_rectEDBMPData = Bitmap(val).bitmapData;
			_rectEDBmpfill = new BitmapFill();
			_rectEDBmpfill.source = new Bitmap(_rectEDBMPData);
			_rectEDBmpfill.smooth = true;
			
			//add for selection filtering
			
			colorMatrix = new ColorMatrixFilter(matrix);
			
			_rectEDBMPSelectedData = Bitmap(val).bitmapData;
			
			_rectEDBmpSelectedFill = new BitmapFill();
			_rectEDBmpSelectedFill.source = new Bitmap(_rectEDBMPSelectedData);
			_rectEDBmpSelectedFill.smooth = true;
			_rectEDBmpSelectedFill.rotation = 0;
			
			
			_text =  new GraphicText();
          	_text.text = "start";
          	_text.embedFonts = true;
          	//_text.align = "center";
          	_text.fontSize = 11;
          	_text.autoSizeField = true;
          	_text.color = 0xffffff;
          	_text.x = this.width/2 -_text.width/2;
          	_text.y = this.height/2 - _text.height/2;
          	//_text.width = 10;
          	//_text.height = 10;
          	_text.textColor = 0xffffff;
          	
          	
          	//_text.verticalCenter = 0;
          	//_text.left = 0;
          	//_text.right = 0;
          	geometryCollection.addItem(_text);
			
			//set type
			componentType = "rectangle";
			
          	//stroke for default rectangle
            _defaultStroke = new SolidStroke();
            _defaultStroke.color = 0x000000;
            _defaultStroke.alpha = 0.2;
            _defaultStroke.weight = 4;
           
			//fill for default rectangle
            _defaultFill = new LinearGradientFill();
            _defaultFill.angle = 90;
            var gradStop1:GradientStop = new GradientStop(StyleManager.getColorName("#444545"), 1,0.3);
            var gradStop2:GradientStop = new GradientStop(StyleManager.getColorName("#444545"), 1);
            _defaultFill.gradientStopsCollection.addItem(gradStop1);
            _defaultFill.gradientStopsCollection.addItem(gradStop2);
			
			//when circle is selected this fill is used
            _selectedFill = new LinearGradientFill();
            _selectedFill.angle = 90;
            var selectedGradStop1:GradientStop = new GradientStop(StyleManager.getColorName("#88b5f2"), 1,0.3);
            var selectedGradStop2:GradientStop = new GradientStop(StyleManager.getColorName("#4b78b5"), 1);
            _selectedFill.gradientStopsCollection.addItem(selectedGradStop1);
            _selectedFill.gradientStopsCollection.addItem(selectedGradStop2);
				
				var linkPointFront:LinkPoint = new LinkPoint(this,"up",10,-25);
        //  	linkPointFront.target = this;
          	linkPoints.addItem(linkPointFront);
          	geometryCollection.addItem(linkPointFront);
          	geometryCollection.setItemIndex(linkPointFront,0);

			
			//create link points where user can create link
          	var linkPointBack:LinkPoint = new LinkPoint(this,"down",10,50);
         // 	linkPointBack.target = this;
          	linkPoints.addItem(linkPointBack);
          	geometryCollection.addItem(linkPointBack);
          	geometryCollection.setItemIndex(linkPointBack,0);
          	
          			
			
			
			//create default(main) rectangle
			_defaultRectangle = new RoundedRectangle(-25,-25,75,75);
			//_defaultRectangle = new RoundedRectangle(-50,-50,100,100);
			_defaultRectangle.stroke = _defaultStroke;
          	_defaultRectangle.fill = _rectEDBmpfill;
          	_defaultRectangle.cornerRadius = 15;
          	geometryCollection.addItem(_defaultRectangle);
          	
          	
          	          	
          	this.refresh();
          	
          	addEventListener(MouseEvent.MOUSE_OVER, mouseOverHandler);
          	addEventListener(MouseEvent.MOUSE_OUT, mouseOutHandler);
          	this.draw(null,null);
          	//Alert.show("linkPoints length " + linkPoints.length);
          	if(Application.application.isConstructScenarioFromFile == true){
	          	Application.application.componentProgressIndicator = Application.application.componentProgressIndicator + 1;
	          	if(Application.application.componentProgressIndicator == 0){
	          		Application.application.drawLinks();
	          	}
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
		
		public function getDefaultRectangle():RoundedRectangle{
			return _defaultRectangle;
		}
		
		public override function set selected(value:Boolean):void{
			if(value == true){
				//use the same bmp pictrue
				//_defaultRectangle.fill = _selectedFill;
				var pt:Point = new Point(-25,-25);
				_defaultRectangle.fill = _rectEDBmpSelectedFill;
				var width:Number = _defaultRectangle.bounds.width * 1.5;
				var height:Number = _defaultRectangle.bounds.height * 1.5;
				var rect:Rectangle = new Rectangle(-25,-25,width,height);
				
				_selected = true;
				refresh();
				if(!selectedFilter){
					this._rectEDBMPSelectedData.applyFilter(_rectEDBMPSelectedData, rect, pt, colorMatrix);
					selectedFilter = true;
				}
			}else{
				_defaultRectangle.fill = _rectEDBmpfill;
				_selected = false;
				refresh();
			}
		}
		
	}
}
