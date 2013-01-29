// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package savoirAutoComplete.utils
{
	public class KeyboardUtil
	{
		public static function charCodeToChar( num:int ):String 
		{
			if (num >= 0 && num <= 26)
			{
				var letters:String = "abcdefghijklmnopqrstuvwxyz";
				return letters.charAt(num - 1);
			}
			else if (num >= 49 && num <= 59)
			{
				return String( num - 48 );
			}
			else
			{
				return num.toString();
			}
        }
	}
}
