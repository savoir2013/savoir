// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package extenders
{
	import mx.controls.LinkButton;

	public class SessionLinkButton extends LinkButton
	{
		[Bindable]
		public var sessionID:String;
		public function SessionLinkButton()
		{
			super();
		}
		
	}
}
