// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package authoringTool.pieMenu
{
    //events/myEvents/EnableChangeEventConst.as
    import authoringTool.scenarioChart.ComponentGroup;
    
    import flash.events.Event;

    public class PieMenuEvent extends Event
    {   
        // Public constructor. 
        public function PieMenuEvent(type:String) {
                // Call the constructor of the superclass.
                super(type);
                
        }

        // Define static constant.
        public static const ITEM_CLOSED:String = "itemClosed";
        public static const MENU_CLOSED:String = "menuClosed";
        
        // Override the inherited clone() method. 
        override public function clone():Event {
            return new PieMenuEvent(type);
        }
    }
}
