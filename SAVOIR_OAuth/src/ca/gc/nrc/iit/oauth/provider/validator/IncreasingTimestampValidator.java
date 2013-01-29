// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.oauth.provider.validator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ca.gc.nrc.iit.oauth.common.OAuth;
import ca.gc.nrc.iit.oauth.common.OAuthParams;
import ca.gc.nrc.iit.oauth.common.exception.OAuthProblemException;

/**
 * Timestamp validator that simply verifies that the timestamp is >= the last timestamp
 * supplied by this consumer.
 * 
 * Note: based on a synchronized, in-memory map.
 * 
 * @author Aaron Moss, NRC-IIT
 */
public class IncreasingTimestampValidator implements TimestampValidator {

	private final Map<String, Long> timestamps = 
		Collections.synchronizedMap(new HashMap<String, Long>());
	
	@Override
	public void validateTimestamp(OAuthParams params, long currentTimeMsec)
			throws OAuthProblemException {
		String consumerKey = params.getConsumerKey();
		long timestamp = params.getTimestamp();
		Long lastTimestamp = timestamps.get(consumerKey);
		if (lastTimestamp != null && timestamp < lastTimestamp) {
			//if we have a previous timestamp from this consumer which is later than this one
			throw new OAuthProblemException(OAuth.Problems.TIMESTAMP_REFUSED);
		}
		//otherwise, accept the timestamp, and store it
		timestamps.put(consumerKey, timestamp);
	}

}
