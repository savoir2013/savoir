// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package authoringTool.scenarioChart.components{
	import com.degrafa.geometry.RoundedRectangle;
	import com.degrafa.paint.LinearGradientFill;
	import com.degrafa.paint.GradientStop;
	import com.degrafa.paint.SolidStroke;
	
	import authoringTool.scenarioChart.ComponentGroup;
	import authoringTool.scenarioChart.LinkPoint;
	
	import flash.events.MouseEvent;
	
	import mx.styles.StyleManager;
	
	
	public class RectangleComponent extends ComponentGroup{
		
		private var _defaultRectangle:RoundedRectangle;
		private var _defaultStroke:SolidStroke;
		private var _defaultFill:LinearGradientFill;
		private var _selectedFill:LinearGradientFill;
				
		public function RectangleComponent(){
			
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
            var gradStop1:GradientStop = new GradientStop(StyleManager.getColorName("#f75757"), 1,0.3);
            var gradStop2:GradientStop = new GradientStop(StyleManager.getColorName("#9e2828"), 1);
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
          	var linkPointBack:LinkPoint = new LinkPoint(this,"left",-25,0);
         // 	linkPointBack.target = this;
          	linkPoints.addItem(linkPointBack);
          	geometryCollection.addItem(linkPointBack);
          	geometryCollection.setItemIndex(linkPointBack,0);
          	
          	var linkPointFront:LinkPoint = new LinkPoint(this,"right",25,0);
        //  	linkPointFront.target = this;
          	linkPoints.addItem(linkPointFront);
          	geometryCollection.addItem(linkPointFront);
          	geometryCollection.setItemIndex(linkPointFront,0);
			
			//create default(main) rectangle
			_defaultRectangle = new RoundedRectangle(-25,-25,50,50);
			_defaultRectangle.stroke = _defaultStroke;
          	_defaultRectangle.fill = _defaultFill;
          	_defaultRectangle.cornerRadius = 15;
          	geometryCollection.addItem(_defaultRectangle);
          	          	
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
				_defaultRectangle.fill = _defaultFill;
				_selected = false;
				refresh();
			}
		}
		
		
		
		
	}
}
