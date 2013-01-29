// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.oauth.provider.validator;

import ca.gc.nrc.iit.oauth.common.OAuthParams;
import ca.gc.nrc.iit.oauth.common.exception.OAuthProblemException;

/**
 * Implements logic for validating the timestamp of an OAuthParams object
 * 
 * @author Aaron Moss, NRC-IIT
 */
public interface TimestampValidator {
	/**
	 * Validates the timestamp of an OAuthParams object
	 * @param params			The OAuth parameters containing the timestamp
	 * @param currentTimeMsec	The current time in milliseconds
	 * @throws OAuthProblemException on invalid timestamp
	 */
	public void validateTimestamp(OAuthParams params, long currentTimeMsec) 
		throws OAuthProblemException;
}
