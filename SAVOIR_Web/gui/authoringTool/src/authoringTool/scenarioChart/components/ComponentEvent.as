// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package authoringTool.scenarioChart.components
{
    //events/myEvents/EnableChangeEventConst.as
    import authoringTool.scenarioChart.ComponentGroup;
    
    import flash.events.Event;

    public class ComponentEvent extends Event
    {   
        // Public constructor. 
        public function ComponentEvent(type:String, component:ComponentGroup=null) {
                // Call the constructor of the superclass.
                super(type);
                
                // Set the new property.
                this.component = component;
        }

        // Define static constant.
        public static const ADD_COMPONENT:String = "addComponent";
        public static const COMPONENT_MOVE:String = "componentMove";
        public static const COMPONENT_SELECTED:String = "componentSelected";
        public static const COMPONENT_UNSELECTED:String = "componentUnselected";
        public static const REMOVE_COMPONENT:String = "removeComponent";
                   
        // Define a public variable to hold the state of the enable property.
        public var component:ComponentGroup;

        // Override the inherited clone() method. 
        override public function clone():Event {
            return new ComponentEvent(type, component);
        }
    }
}
