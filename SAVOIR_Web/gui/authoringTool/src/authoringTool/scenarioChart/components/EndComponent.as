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
	
	import mx.collections.ArrayCollection;
	import mx.core.Application;
	import mx.styles.StyleManager;
	
	
	public class EndComponent extends ComponentGroup{
		
		private var _defaultRectangle:RoundedRectangle;
		private var _defaultStroke:SolidStroke;
		private var _defaultFill:LinearGradientFill;
		private var _selectedFill:LinearGradientFill;
		private var _text:GraphicText;
		private var _endBmpfill:BitmapFill;
		
		private var _endBMPData:BitmapData;
		[Bindable]
		[Embed(source="/assets/widgets/end.png")]
		private var _EndImage:Class;
		private var _linkPointUp:LinkPoint;
		public function get linkPointUp():LinkPoint{
			return _linkPointUp;
		}
		
		private var _endNodeId:Number = -1;
		public function get nodeID():Number{
			var maxValue:Number = Application.application.maxNodeID;
			_endNodeId = maxValue + 1;
			return _endNodeId;
		}
		public function set nodeID(value:Number):void{
			Application.application.maxNodeID = value - 1;
			_endNodeId = value;
		}
				
		public function EndComponent(){
			_endBMPData = new _EndImage().bitmapData;
					
			_endBmpfill = new BitmapFill();
			_endBmpfill.source = new Bitmap(_endBMPData);
			_endBmpfill.smooth = true;
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
          	_linkPointUp = new LinkPoint(this,"up",0,-25);
         // 	linkPointBack.target = this;
          	linkPoints.addItem(_linkPointUp);
          	geometryCollection.addItem(_linkPointUp);
          	geometryCollection.setItemIndex(_linkPointUp,0);
          	
//          	var linkPointFront:LinkPoint = new LinkPoint(this,"right",25,0);
//        //  	linkPointFront.target = this;
//          	linkPoints.addItem(linkPointFront);
//          	geometryCollection.addItem(linkPointFront);
//          	geometryCollection.setItemIndex(linkPointFront,0);
			
			//create default(main) rectangle
			_defaultRectangle = new RoundedRectangle(-25,-25,50,50);
			_defaultRectangle.stroke = _defaultStroke;
          	_defaultRectangle.fill = _endBmpfill;
          	_defaultRectangle.cornerRadius = 15;
          	geometryCollection.addItem(_defaultRectangle);
          	_text =  new GraphicText();
          	_text.text = "End";
          	_text.fontSize = 11;
//          	_text.verticalCenter = 0;
//          	_text.left = 0;
//          	_text.right = 0;
          	geometryCollection.addItem(_text);
          	
          	          	
          	this.refresh();
          	
          	addEventListener(MouseEvent.MOUSE_OVER, mouseOverHandler);
          	addEventListener(MouseEvent.MOUSE_OUT, mouseOutHandler);
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
				_defaultRectangle.fill = _endBmpfill;
				_selected = false;
				refresh();
			}
		}
		
		public function getUpperNodes():ArrayCollection{
			var upperNodesAry:ArrayCollection = new ArrayCollection();
			for each(var item:Object in _linkPointUp.targetLinks){
				var res:EdgeDeviceComponent = (EdgeDeviceComponent(item.component));
				upperNodesAry.addItem(res);
			}
			return upperNodesAry;
//			var res:EdgeDeviceComponent = null;
//			if(_linkPointUp.linked){
//				var targetComp:Object = _linkPointUp.targetLinks.getItemAt(0);
//				res = (EdgeDeviceComponent(targetComp.component));
//			}
//			return res;
		}
		
		
	}
}
// ActionScript file
// ActionScript file
