/*
 * Copyright 2009 Paul Austin
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

package ca.gc.nrc.iit.oauth.consumer.http.httpclient4;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.auth.RFC2617Scheme;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.message.BasicHeader;

import ca.gc.nrc.iit.oauth.common.OAuthParams;
import ca.gc.nrc.iit.oauth.consumer.OAuthClient;


/**
 * @author Paul Austin
 * @author John Kristian
 * @author Aaron Moss, NRC-IIT
 */
class OAuthScheme extends RFC2617Scheme {

    private final String defaultRealm;
    /** Whether the authentication process is complete (for the current context) */
    private boolean complete;

    OAuthScheme(String defaultRealm) {
        this.defaultRealm = defaultRealm;
    }

    @Override
    public String getRealm() {
        String realm = super.getRealm();
        if (realm == null) {
            realm = defaultRealm;
        }
        return realm;
    }

    public String getSchemeName() {
        return OAuthSchemeFactory.SCHEME_NAME;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean isConnectionBased() {
        return false;
    }

    /**
     * Handle a challenge from an OAuth server.
     */
    @Override
    public void processChallenge(Header challenge) throws MalformedChallengeException {
        super.processChallenge(challenge);
        complete = true;
    }

    public Header authenticate(Credentials credentials, HttpRequest request) 
    		throws AuthenticationException {
        String uri;
        String method;
        HttpUriRequest uriRequest = getHttpUriRequest(request);
        if (uriRequest != null) {
            uri = uriRequest.getURI().toString();
            method = uriRequest.getMethod();
        } else {
            // Some requests don't include the server name in the URL.
            RequestLine requestLine = request.getRequestLine();
            uri = requestLine.getUri();
            method = requestLine.getMethod();
        }
        try {
            OAuthCredentials oAuth = getCredentials(credentials);
            OAuthParams params = 
            	new OAuthParams(oAuth.getToken(), oAuth.getConsumer(), method, uri);
            OAuthClient.ensureSignature(params, oAuth.getToken(), oAuth.getConsumer());
            String authorization = OAuthClient.getAuthorizationHeader(params);
            return new BasicHeader("Authorization", authorization);
        } catch (Exception e) {
            throw new AuthenticationException(null, e);
        }
    }

    private HttpUriRequest getHttpUriRequest(HttpRequest request) {
        while (request instanceof RequestWrapper) {
            HttpRequest original = ((RequestWrapper) request).getOriginal();
            if (original == request) {
                break;
            }
            request = original;
        }
        if (request instanceof HttpUriRequest) {
            return (HttpUriRequest) request;
        }
        return null;
    }

    private OAuthCredentials getCredentials(Credentials credentials) {
        if (credentials instanceof OAuthCredentials) {
            return (OAuthCredentials) credentials;
        }
        return new OAuthCredentials(credentials.getUserPrincipal().getName(),
        		credentials.getPassword());
    }

}
