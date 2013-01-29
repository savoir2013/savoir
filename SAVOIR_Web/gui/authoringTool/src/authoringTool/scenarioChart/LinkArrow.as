// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package  authoringTool.scenarioChart{
	import com.degrafa.geometry.Path;
	import com.degrafa.paint.SolidFill;
	
	public class LinkArrow extends Path{
		
		private var _whiteFill:SolidFill;
		
		function LinkArrow():void{
			super.data = "M25,0 L0,25 L0,10 L-25,10 L-25,-10 L0,-10 L0,-25 L25,0z";
			
			
            _whiteFill = new SolidFill();
            _whiteFill.color = 0x0d4267;
            _whiteFill.alpha = 0.7;
			
			this.fill = _whiteFill;
		}
		
		
	}
}
