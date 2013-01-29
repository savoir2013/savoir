/*
 * Copyright 2008 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.gc.nrc.iit.oauth.provider.validator;

import ca.gc.nrc.iit.oauth.common.OAuthConsumer;
import ca.gc.nrc.iit.oauth.common.OAuthParams;
import ca.gc.nrc.iit.oauth.common.OAuthToken;
import ca.gc.nrc.iit.oauth.common.exception.OAuthException;


/**
 * An algorithm to determine whether a message has a valid signature, a correct
 * version number, a fresh timestamp, etc.
 *
 * @author Dirk Balfanz, Google
 * @author John Kristian, Netflix
 */
public interface OAuthValidator {

    /**
     * Check that the parameters validate against the given token secrets
     * @param params	Parameters of OAuth message
     * @param token		Claimed token of OAuth message
     * @param consumer	Claimed consumer of OAuth message
     * 
     * @throws OAuthException
     *             the message doesn't conform to OAuth. The exception contains
     *             information that conforms to the OAuth <a
     *             href="http://wiki.oauth.net/ProblemReporting">Problem
     *             Reporting extension</a>.
     */
    public void validateMessage(OAuthParams params, OAuthToken token, OAuthConsumer consumer)
            throws OAuthException;

}
