// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package org.iotashan.oauth
{
	public interface IOAuthSignatureMethod
	{
		public function IOAuthSignatureMethod()
		
		function get name():String
		
		function signRequest(request:OAuthRequest):String
	}
}
