// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package authoringTool.extenders
{
	public class Connection
	{
		public var srcSiteSelectIndex:Number;
		public var srcSite:String;
		public var srcSiteId:String;
		public var destSiteSelectIndex:Number;
		public var destSite:String;
		public var destSiteId:String;
		public var maxBW:String;
		public var minBW:String;
		public function Connection()
		{
		}
		public function toConnectionString():String{
			return "Source site " + srcSite 
			       + " connect to destination site " + destSite
			       + ": MaxBW = " + maxBW
			       + " MinBW = " + minBW + "\n";
		}
		
		public function toConnectionXMLString( connId:uint):String{
			return "<connection id=\"" + connId + "\"" 
			        + " minBandwidth=\"" + minBW + "\"" + " maxBandwidth=\"" + maxBW + "\">"
			        + "<sourceSite id=\"" + srcSiteId + "\""  
    			    + " name=\"" + srcSite + "\"/>"
    			    + "<destSite id=\"" + destSiteId + "\""
    			    + " name=\"" + destSite + "\"/>"
    			    
    			    + "</connection>"; 
		}

	}
}
