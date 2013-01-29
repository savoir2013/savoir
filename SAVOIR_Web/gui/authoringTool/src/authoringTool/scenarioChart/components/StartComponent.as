// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package authoringTool.scenarioChart.components{
	import authoringTool.scenarioChart.ComponentGroup;
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
	
	import mx.styles.StyleManager;
	
	
	public class StartComponent extends ComponentGroup{
		
		private var _defaultRectangle:RoundedRectangle;
		private var _defaultStroke:SolidStroke;
		private var _defaultFill:LinearGradientFill;
		private var _selectedFill:LinearGradientFill;
		
		private var _text:GraphicText;
		
		private var _startBmpfill:BitmapFill;
		
		private var _startBMPData:BitmapData;
		[Bindable]
		[Embed(source="/assets/widgets/start.png")]
		private var _StartImage:Class;
		
		public var nodeID:Number = 0;
		private var _linkPointDown:LinkPoint;
		
		public function get linkPointDown():LinkPoint{
			return _linkPointDown;
		}
				
		public function StartComponent(){
			
			_startBMPData = new _StartImage().bitmapData;
					
			_startBmpfill = new BitmapFill();
			_startBmpfill.source = new Bitmap(_startBMPData);
			_startBmpfill.smooth = true;
			
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
				
			
			
			//create link points where user can create link
          	_linkPointDown = new LinkPoint(this,"down",0,25);
         // 	linkPointBack.target = this;
          	linkPoints.addItem(_linkPointDown);
          	geometryCollection.addItem(_linkPointDown);
          	geometryCollection.setItemIndex(_linkPointDown,0);
          	
//          	var linkPointFront:LinkPoint = new LinkPoint(this,"right",25,0);
//        //  	linkPointFront.target = this;
//          	linkPoints.addItem(linkPointFront);
//          	geometryCollection.addItem(linkPointFront);
//          	geometryCollection.setItemIndex(linkPointFront,0);
			
			
			
			//create default(main) rectangle
			//_defaultRectangle = new RoundedRectangle(-25,-25,50,50);
			_defaultRectangle = new RoundedRectangle(-25,-25,50,50);
			_defaultRectangle.stroke = _defaultStroke;
          	_defaultRectangle.fill = _startBmpfill;
          	_defaultRectangle.cornerRadius = 15;
          	
          	geometryCollection.addItem(_defaultRectangle);
          	
          	_text =  new GraphicText();
          	_text.target = this;
          	//_text.stroke = _defaultStroke;
          	//_text.fill = _defaultFill;
          	_text.text = "start";
          	_text.embedFonts = true;
          	//_text.align = "center";
          	_text.fontSize = 11;
          	_text.autoSizeField = true;
          	_text.color = 0xffffff;
          	//_text.x = this.width/2 -_text.width/2;
          	//_text.y = this.height/2 - _text.height/2;
          	_text.width = 50;
          	_text.height = 50;
          	_text.x = -50;
          	_text.y = 50;
          	_text.textColor = 0xfffff;
          	_text.backgroundColor = 0xffffff;
          	_text.visible = true;
          	
          	
          	//_text.verticalCenter = 0;
          	//_text.left = 0;
          	//_text.right = 0;
          	geometryCollection.addItem(_text);
          	geometryCollection.setItemIndex(_text,0);
          	
          	          	
          	this.refresh();
          	
          	addEventListener(MouseEvent.MOUSE_OVER, mouseOverHandler);
          	addEventListener(MouseEvent.MOUSE_OUT, mouseOutHandler);
          	//this.draw(this.graphics,this.getBounds(this));
          	this.draw(null,null);
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
				_defaultRectangle.fill = _selectedFill;
				_selected = true;
				refresh();
			}else{
				_defaultRectangle.fill = _startBmpfill;
				_selected = false;
				refresh();
			}
		}
		
		public function getLowerNode():EdgeDeviceComponent{
			var res:EdgeDeviceComponent = null;
			if(_linkPointDown.linked){
				var targetComp:Object = _linkPointDown.targetLinks.getItemAt(0);
				res = (EdgeDeviceComponent(targetComp.component));
			}
			return res;
		}
		
		
		
		
	}
}
// ActionScript file
