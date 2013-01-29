// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package authoringTool.scenarioChart{
	import com.degrafa.GeometryGroup;
	import com.degrafa.GraphicImage;
	import com.degrafa.GraphicText;
	import com.degrafa.geometry.Circle;
	import com.degrafa.geometry.CubicBezier;
	import com.degrafa.geometry.Line;
	import com.degrafa.geometry.Polyline;
	import com.degrafa.paint.SolidStroke;
	
	import authoringTool.scenarioChart.components.ComponentEvent;
	import authoringTool.scenarioChart.components.LinkCurveEvent;
	import authoringTool.pieMenu.MenuItem;
	import authoringTool.pieMenu.PieMenu;
	import authoringTool.pieMenu.PieMenuEvent;
	
	import flash.events.MouseEvent;
	import flash.geom.Point;
	import flash.utils.Timer;
	
	import mx.styles.StyleManager;
	
	[Event(name="mouseOverLink", type="erno.components.LinkCurveEvent")]
	[Event(name="mouseOutLink", type="erno.components.LinkCurveEvent")]
	
	
	public class LinkComponent extends GeometryGroup{
		
		private var _linkFrom:LinkPoint;
		private var _linkTo:LinkPoint;
		private var _curve:CubicBezier;
		private var _line:Polyline;
		private var _straightLine:Line;
		private var _curveStroke:SolidStroke;
		private var _lineStroke:SolidStroke;
		private var _straightLineStroke:SolidStroke;
		private var _controlStroke:SolidStroke;
		private var _showMenu:Boolean = false;
		private var _timer:Timer= new Timer(2000,1);
		private var _controlPoint:Circle;
		private var _controlPosition:Point;
		
		function LinkComponent(fromPoint:LinkPoint, toPoint:LinkPoint){
			
			
            _curveStroke = new SolidStroke();
            _curveStroke.color = StyleManager.getColorName("#0d4267");
            _curveStroke.weight = 5;
            
            _lineStroke = new SolidStroke();
            _lineStroke.color = StyleManager.getColorName("#0d4267");
            _lineStroke.weight = 5;
            
            _straightLineStroke = new SolidStroke();
            _straightLineStroke.color = StyleManager.getColorName("#0d4267");
            _straightLineStroke.weight = 5;
            
            _controlStroke = new SolidStroke();
            _controlStroke.color = StyleManager.getColorName("#0d4267");
            _controlStroke.weight = 5;
			
			this._linkFrom = fromPoint;
			this._linkTo = toPoint;
			
			_curve = new CubicBezier();
			_curve.stroke = _curveStroke;
			
			this.geometryCollection.addItem(_curve);
			
			var test1:Point = new Point(0,0);
			_line = new Polyline([test1]);
			_line.stroke = _lineStroke;
			
			this.geometryCollection.addItem(_line);
			
			_straightLine = new Line(0,0,0,0);
			_straightLine.stroke = _straightLineStroke;
			
			this.geometryCollection.addItem(_straightLine);
			
			_controlPosition = Point.interpolate(new Point(fromPoint.stageX,fromPoint.stageY),new Point(toPoint.stageX, toPoint.stageY),0.5);
			
			_controlPoint = new Circle(_controlPosition.x,_controlPosition.y,5);
			_controlPoint.stroke = _controlStroke;
			
			this.geometryCollection.addItem(_controlPoint);
			
			refresh();
			
			this.addEventListener(MouseEvent.CLICK,mouseClickHandler);
			
			
	        _linkFrom.parentComponent.addEventListener(ComponentEvent.COMPONENT_MOVE,componentMoveHandler);
	        _linkTo.parentComponent.addEventListener(ComponentEvent.COMPONENT_MOVE,componentMoveHandler);
			
		}
		
		private var _lineStyle:String = "curve";
		public function set lineStyle(value:String):void{
			
			_curveStroke.alpha = 0;
			_lineStroke.alpha = 0;
			_straightLineStroke.alpha = 0;

			if(_lineStyle != value){
				_lineStyle = value;
			}
			
			refresh();
		}
		public function get lineStyle():String{
			return _lineStyle;
		}
		
		public function get controlPoint():Point{
			var returnPoint:Point = new Point();
			//returnPoint.x = this.x + _controlPoint.centerX + this.parent.x;
			//returnPoint.y = this.y + _controlPoint.centerY + this.parent.y;
			returnPoint.x = this.x + _controlPoint.centerX + this.parent.x;
			returnPoint.y = this.y + _controlPoint.centerY + this.parent.y;
			return returnPoint;
		}
		
		
		private function get showMenu():Boolean{
			return _showMenu;
		}
		
		private function set showMenu(value:Boolean):void{
			_showMenu = value;
		}
		
		private var _menuVisible:Boolean = false;
		private var _menu:PieMenu;
		
		private function mouseClickHandler(event:MouseEvent):void{
			
			var surfaceComp:SurfaceComponent = this.target as SurfaceComponent;
				
			if(surfaceComp.selectedObject != null){
				
				surfaceComp.unFocus(surfaceComp.selectedObject);
				
			}else{
			
				var mousePosition:Point = new Point(event.localX,event.localY);
				
				if(_menuVisible == false && mouseOverControlPoint(mousePosition)){
					this.dispatchEvent(new LinkCurveEvent(LinkCurveEvent.MOUSE_OVER_LINK));
					
					_menu = new PieMenu();
					//_menu.x = controlPoint.x*surfaceComp.scaleX;
					//_menu.y = controlPoint.y*surfaceComp.scaleY;
					_menu.x = controlPoint.x;
					_menu.y = controlPoint.y;
					_menu.width = 60;
					_menu.height = 60;
					_menu.innerRadius = 0.5;
					_menu.arc = 360;
					_menu.startAngle = 0;
					_menu.gap = 10;
					_menu.addEventListener(PieMenuEvent.MENU_CLOSED, pieMenuCloseHandler);
					
					//this.target.parent.addChild(_menu);
					this.target.addChild(_menu);
						
					var removeButton:MenuItem = new MenuItem();
					removeButton.parentItem = _menu;
					removeButton.addEventListener(MouseEvent.CLICK, removeButtonHandler);
					_menu.addItem(removeButton);
					
					var removeIcon:GraphicImage = new GraphicImage();
					removeIcon.source = "assets/icons/trash.png";
					removeIcon.target = _menu;
					removeIcon.width = 32;
					removeIcon.height = 32;
					_menu.graphicsCollection.addItem(removeIcon);
					removeButton.icon = removeIcon;
					
					var lineStyleButton:MenuItem = new MenuItem();
					lineStyleButton.parentItem = _menu;
					_menu.addItem(lineStyleButton);
					
					
					var test:GraphicText = new GraphicText();
					test.text = "Line style";
					lineStyleButton.icon = test;
					
					_menu.graphicsCollection.addItem(test);
					
					var straightLineButton:MenuItem = new MenuItem();
					straightLineButton.parentItem = lineStyleButton;
					straightLineButton.gap = 0;
					straightLineButton.addEventListener(MouseEvent.CLICK,changeToStraightLine);
					_menu.addItem(straightLineButton);
					
					var straightLineIcon:GraphicImage = new GraphicImage();
					straightLineIcon.source = "assets/icons/straightLineIcon.png";
					straightLineIcon.target = _menu;
					straightLineIcon.width = 29;
					straightLineIcon.height = 29;
					_menu.graphicsCollection.addItem(straightLineIcon);
					straightLineButton.icon = straightLineIcon;
						
					var curveButton:MenuItem = new MenuItem();
					curveButton.parentItem = lineStyleButton;
					curveButton.gap = 0;
					curveButton.addEventListener(MouseEvent.CLICK, changeToCurve);
					_menu.addItem(curveButton);
	
					
					var curveIcon:GraphicImage = new GraphicImage();
					curveIcon.source = "assets/icons/curveIcon.png";
					curveIcon.target = _menu;
					curveIcon.width = 43;
					curveIcon.height = 22;
					_menu.graphicsCollection.addItem(curveIcon);
					curveButton.icon = curveIcon;
					
					var lineButton:MenuItem = new MenuItem();
					lineButton.parentItem = lineStyleButton;
					lineButton.gap = 0;
					lineButton.addEventListener(MouseEvent.CLICK, changeToLine);
					_menu.addItem(lineButton);
					
					
					var lineIcon:GraphicImage = new GraphicImage();
					lineIcon.source = "assets/icons/lineIcon.png";
					lineIcon.target = _menu;
					lineIcon.width = 30;
					lineIcon.height = 30;
					_menu.graphicsCollection.addItem(lineIcon);
					lineButton.icon = lineIcon;
					
					_menuVisible = true;
				}
			}
		}
		private function changeToStraightLine(event:MouseEvent):void{
			this.lineStyle = "straightLine";
			refresh();
		}
		private function changeToCurve(event:MouseEvent):void{
			this.lineStyle = "curve";
			refresh();
		}
		private function changeToLine(event:MouseEvent):void{
			this.lineStyle = "line";
			refresh();
		}
		
		private function pieMenuCloseHandler(event:PieMenuEvent):void{
			
			_menuVisible = false;
			
		}
		
		private function removeButtonHandler(event:MouseEvent):void{
			_menu.closePie();
			this.remove();
		}
		
		private function mouseOverControlPoint(position:Point):Boolean {
			if(Point.distance(position,_controlPosition) <= (_controlPoint.radius+_curveStroke.weight)){
				return true;
			}else{
				return false;
			}
			
		}
		
		private function componentMoveHandler(event:ComponentEvent):void{
			refresh();
		}
		
		private function refresh():void{
			if(_lineStyle == "curve"){
				_curveStroke.alpha = 1;
	        	_curve.x = _linkFrom.stageX;
	        	_curve.y = _linkFrom.stageY;
	        	_curve.x1 = _linkTo.stageX+1;
	        	_curve.y1= _linkTo.stageY+1;
		        	
				_curve.cx = _linkTo.stageX;
	        	_curve.cy = _linkFrom.stageY;
	        	_curve.cx1 = _linkFrom.stageX;
	        	_curve.cy1 = _linkTo.stageY;
		        
				
				_controlPosition = Point.interpolate(new Point(_linkFrom.stageX,_linkFrom.stageY),new Point(_linkTo.stageX, _linkTo.stageY),0.5);
				_controlPoint.centerX = _controlPosition.x;
				_controlPoint.centerY = _controlPosition.y;
				
			}else if(_lineStyle == "line"){
				
				_lineStroke.alpha = 1;
				
				var point1:Point = new Point(_linkFrom.stageX,_linkFrom.stageY);
				var point2:Point = new Point((_linkTo.stageX-_linkFrom.stageX)/2+_linkFrom.stageX,_linkFrom.stageY);
				var point3:Point = new Point((_linkTo.stageX-_linkFrom.stageX)/2+_linkFrom.stageX,_linkTo.stageY);
				var point4:Point = new Point(_linkTo.stageX,_linkTo.stageY);
				
				_line.points = new Array(point1,point2,point3,point4);
				
				_controlPosition = Point.interpolate(new Point(_linkFrom.stageX,_linkFrom.stageY),new Point(_linkTo.stageX, _linkTo.stageY),0.5);
				_controlPoint.centerX = _controlPosition.x;
				_controlPoint.centerY = _controlPosition.y;
				
			}else if(_lineStyle == "straightLine"){
				
				_straightLineStroke.alpha = 1;
				
				_straightLine.x = _linkFrom.stageX;
				_straightLine.y = _linkFrom.stageY;
				_straightLine.x1 = _linkTo.stageX;
				_straightLine.y1 = _linkTo.stageY;
				
				_controlPosition = Point.interpolate(new Point(_linkFrom.stageX,_linkFrom.stageY),new Point(_linkTo.stageX, _linkTo.stageY),0.5);
				_controlPoint.centerX = _controlPosition.x;
				_controlPoint.centerY = _controlPosition.y;
				
			}
			
			
			this.draw(null,null);
			//this.refresh();
		}
		
		public function get linkFrom():LinkPoint{
			return _linkFrom;
		}
		public function get linkTo():LinkPoint{
			return _linkTo;
		}
		public function remove():void{
        	        	
        	//update linkPoints
        	_linkFrom.removeLinks();
        	_linkTo.removeLinks();
        	
        	this.parent.removeChild(this);
		}
		
	}
}
