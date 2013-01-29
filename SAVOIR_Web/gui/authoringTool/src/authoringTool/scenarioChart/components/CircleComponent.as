// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package authoringTool.scenarioChart.components{
	import com.degrafa.geometry.Circle;
	import com.degrafa.paint.LinearGradientFill;
	import com.degrafa.paint.GradientStop;
	import com.degrafa.paint.SolidFill;
	import com.degrafa.paint.SolidStroke;
	
	import authoringTool.scenarioChart.ComponentGroup;
	import authoringTool.scenarioChart.LinkPoint;
	import authoringTool.scenarioChart.SettingsObject;
	
	import flash.display.DisplayObject;
	import flash.events.MouseEvent;
	
	import mx.events.PropertyChangeEvent;
	import mx.styles.StyleManager;
	
	public class CircleComponent extends ComponentGroup{
		
		private var _defaultCircle:Circle;
		private var _closeCircle:Circle;
		
		private var _defaultStroke:SolidStroke;
		private var _defaultFill:LinearGradientFill;
		private var _selectedFill:LinearGradientFill;
		private var _closeStroke:SolidStroke;
		private var _closeFill:SolidFill;
						
		private var fillGradStop1:GradientStop;
		
		public function CircleComponent(){
			//set type
			componentType = "circle";
			settingsData.addItem(new SettingsObject("Nimi","input","cirlcenimi"));

          	//stroke for default circle
            _defaultStroke = new SolidStroke();
            _defaultStroke.color = 0x000000;
            _defaultStroke.alpha = 0.2;
            _defaultStroke.weight = 4;
                        
			//fill for default circle
            _defaultFill = new LinearGradientFill();
            _defaultFill.angle = 90;
            fillGradStop1 = new GradientStop(StyleManager.getColorName("#A1E42C"), 1,0.3);
            var fillGradStop2:GradientStop = new GradientStop(StyleManager.getColorName("#527529"), 1);
            _defaultFill.gradientStopsCollection.addItem(fillGradStop1);
            _defaultFill.gradientStopsCollection.addItem(fillGradStop2);
			
			//when circle is selected this fill is used
            _selectedFill = new LinearGradientFill();
            _selectedFill.angle = 90;
            var selectedGradStop1:GradientStop = new GradientStop(StyleManager.getColorName("#88b5f2"), 1,0.3);
            var selectedGradStop2:GradientStop = new GradientStop(StyleManager.getColorName("#4b78b5"), 1);
            _selectedFill.gradientStopsCollection.addItem(selectedGradStop1);
            _selectedFill.gradientStopsCollection.addItem(selectedGradStop2);
			
			
			//create link points where user can create link
          	var linkPointBack:LinkPoint = new LinkPoint(this,"left",-25,0);
          //	linkPointBack.target = this;
          	linkPoints.addItem(linkPointBack);
          	geometryCollection.addItem(linkPointBack);
          	geometryCollection.setItemIndex(linkPointBack,0);
          	
          	var linkPointFront:LinkPoint = new LinkPoint(this,"right",25,0);
         // 	linkPointFront.target = this;
          	linkPoints.addItem(linkPointFront);
          	geometryCollection.addItem(linkPointFront);
          	geometryCollection.setItemIndex(linkPointFront,0);
          	this.mask = linkPointFront as DisplayObject;
          	
			//create default(main) circle
			_defaultCircle = new Circle(0,0,25);
          	_defaultCircle.fill = _defaultFill;
			_defaultCircle.stroke = _defaultStroke;
          	geometryCollection.addItem(_defaultCircle);
          	
          	          	          	
          	refresh();
          	
          	addEventListener(MouseEvent.MOUSE_OVER, mouseOverHandler);
          	addEventListener(MouseEvent.MOUSE_OUT, mouseOutHandler);
          	
          	fillGradStop1.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, propertyChangeHandler);
          	
		}
		
		private function propertyChangeHandler(event:PropertyChangeEvent):void{
			refresh();
		}
		
		private function mouseOverHandler(event:MouseEvent):void{
			//not yet in use
			//_closeStroke.alpha = 0.7;
			//_closeFill.alpha = 1;
			//refresh();
			//Tweener.addTween(fillGradStop1,{color:0x000000,time:1});
			
			
			
		}
		
		private function mouseOutHandler(event:MouseEvent):void{
			//not yet in use
			//_closeStroke.alpha = 0;
			//_closeFill.alpha = 0;
			//refresh();
		}
		
		public function getDefaultCircle():Circle{
			return _defaultCircle;
		}
		public function getCloseCircle():Circle{
			return _closeCircle;
		}
				
		public override function set selected(value:Boolean):void{
			if(value == true){
				_defaultCircle.fill = _selectedFill;
				_selected = true;
				refresh();
			}else{
				_defaultCircle.fill = _defaultFill;
				_selected = false;
				refresh();
			}
		}
				
	}
}
