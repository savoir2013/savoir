// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package authoringTool.scenarioChart.components
{
    //events/myEvents/EnableChangeEventConst.as
    import authoringTool.scenarioChart.LinkComponent;
    
    import flash.events.Event;

    public class LinkCurveEvent extends Event
    {   
        // Public constructor. 
        public function LinkCurveEvent(type:String, component:LinkComponent=null) {
                // Call the constructor of the superclass.
                super(type);
                
                // Set the new property.
                this.component = component;
        }

        // Define static constant.
        
        public static const LINK_MENU_SHOW:String = "linkMenuShow";
        public static const LINK_MENU_HIDE:String = "linkMenuHide";
        
        public static const MOUSE_OVER_LINK:String = "mouseOverLink";
        public static const MOUSE_OUT_LINK:String = "mouseOutLink";
            
        // Define a public variable to hold the state of the enable property.
        public var component:LinkComponent;

        // Override the inherited clone() method. 
        override public function clone():Event {
            return new LinkCurveEvent(type, component);
        }
    }
}
