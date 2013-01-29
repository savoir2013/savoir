/*
 * Copyright 2007, 2008 Netflix, Inc.
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

package ca.gc.nrc.iit.oauth.consumer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.gc.nrc.iit.oauth.common.OAuth;
import ca.gc.nrc.iit.oauth.common.OAuthConsumer;
import ca.gc.nrc.iit.oauth.common.OAuthParams;
import ca.gc.nrc.iit.oauth.common.OAuthServiceProvider;
import ca.gc.nrc.iit.oauth.common.OAuthToken;
import ca.gc.nrc.iit.oauth.common.OAuth.ParameterStyle;
import ca.gc.nrc.iit.oauth.common.OAuthParams.Parameter;
import ca.gc.nrc.iit.oauth.common.exception.OAuthException;
import ca.gc.nrc.iit.oauth.common.exception.OAuthProblemException;
import ca.gc.nrc.iit.oauth.common.signature.OAuthSignatureMethod;
import ca.gc.nrc.iit.oauth.consumer.http.HttpClient;
import ca.gc.nrc.iit.oauth.consumer.http.HttpMessage;
import ca.gc.nrc.iit.oauth.consumer.http.HttpMessageDecoder;
import ca.gc.nrc.iit.oauth.consumer.http.HttpResponseMessage;
import ca.gc.nrc.iit.oauth.consumer.http.httpclient4.HttpClient4;
import ca.gc.nrc.iit.oauth.consumer.http.httpclient4.OAuthSchemeFactory;


/**
 * An HTTP-based OAuth client.
 * 
 * Provides convenience methods for performing OAuth login (request token, authorize token, 
 * request access token) and logout operations, as well as useful client operations such as 
 * generation of the HTTP Authorization header.
 * 
 * <b>Known bugs:</b>
 * <ul>
 * <li>Due to HTTP state maintenance issues, this client may not successfully perform
 * 	   login calls after logout has been called. The apparent issue is that the instance of
 * 	   org.apache.http.client.HttpClient configured by this client fails to perform calls to 
 * 	   a URI it has called once, after calling a different URI on the same host. Instantiating 
 *     a new OAuthClient instance will not solve this issue, as it will use the same HttpClient
 *     instance. Only known workaround: terminate and restart the calling Java class, thus 
 *     refreshing the JVM and instantiating a new HttpClient. As logout is typically associated 
 *     with quitting the client app, the author considers this fix acceptable.</li>
 * </ul>
 * 
 * @author John Kristian, Netflix
 * @author Aaron Moss, NRC-IIT
 */
public class OAuthClient {
	
	public OAuthClient() {
		this(new HttpClient4());
	}
	
	public OAuthClient(HttpClient http) {
        this.http = http;
        httpParameters.put(HttpClient.FOLLOW_REDIRECTS, Boolean.FALSE);
    }

    private HttpClient http;
    protected final Map<String, Object> httpParameters = new HashMap<String, Object>();
    private ParameterStyle parameterStyle;

    public void setHttpClient(HttpClient http) {
        this.http = http;
    }

    public HttpClient getHttpClient() {
        return http;
    }
    
    public void setParameterStyle(ParameterStyle parameterStyle) {
    	this.parameterStyle = parameterStyle;
    }
    
    public ParameterStyle getParameterStyle() {
    	if (this.parameterStyle == null) {
    		this.parameterStyle = ParameterStyle.AUTHORIZATION_HEADER;
    	}
    	return this.parameterStyle;
    }
    
    /**
     * HTTP client parameters, as a map from parameter name to value.
     * 
     * @see HttpClient for parameter names.
     */
    public Map<String, Object> getHttpParameters() {
        return httpParameters;
    }
	
	/**
	 * Logs into an OAuth-secured service using the provided credentials
	 * @param username		The username to log in with (consumer key)
	 * @param password		The password to log in with (consumer secret)
	 * @param provider		The OAuth addresses of the provider
	 * 						Note that this assumes a 2-legged model, requiring
	 * 						no user interaction at the authorization stage.
	 * @return an OAuth token containing the relevant shared secrets.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws OAuthException 
	 */
	public OAuthToken login(String username, String password, OAuthServiceProvider provider) 
			throws OAuthException, IOException, URISyntaxException {
		OAuthToken token;
		OAuthConsumer consumer = new OAuthConsumer(username, password);
		
		token = getRequestToken(consumer, provider);
		authorizeToken(token, consumer, provider);
		token = getAccessToken(token, consumer, provider);
		
		return token;
	}
	
	/**
	 * Log out of an OAuth-secured session associated with the provided credentials
	 * @param token			The token describing the session
	 * @param provider		The service provider.
	 * @throws IOException 
	 * @throws OAuthProblemException 
	 * @throws URISyntaxException 
	 */
	public void logout(OAuthConsumer consumer, OAuthToken token, OAuthServiceProvider provider) 
			throws OAuthProblemException, IOException, URISyntaxException {
		OAuthParams params = new OAuthParams(token, consumer, HttpClient.GET, provider.logoutURL);
		invoke(params, token, consumer);
	}
	
	/**
	 * Gets an OAuth request token from the given provider
	 * @param consumer			The OAuth consumer to authenticate as
	 * @param provider			The OAuth provider to use
	 * @return The request token
	 * @throws OAuthException
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public OAuthToken getRequestToken(OAuthConsumer consumer, OAuthServiceProvider provider)
			throws OAuthException, IOException, URISyntaxException {
		OAuthParams params = 
			new OAuthParams(consumer, HttpClient.POST, provider.requestTokenURL);
		params.setCallback("oob");
		
		return getToken(params, new OAuthToken(), consumer);
	}
	
	/**
	 * Authorizes an OAuth request token
	 * @param token		The token to authorize
	 * @param provider	The provider that supplied the request token
	 * @return the URL to navigate to for user authorization
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws OAuthProblemException 
	 */
	public String authorizeToken(OAuthToken token, OAuthConsumer consumer,
			OAuthServiceProvider provider) 
			throws URISyntaxException, OAuthProblemException, IOException {
		OAuthParams params = 
			new OAuthParams(token, consumer, HttpClient.GET, provider.userAuthorizationURL);
		HttpResponseMessage response = invoke(params, token, consumer);
		
		return response.url.toExternalForm();
	}
	
	/**
	 * Use the given request token to procure an OAuth access token
	 * @param token		The request token to provide (should be authorized)
	 * @param provider	The OAuth provider that supplied the request token
	 * @return an access token to the same provider
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws OAuthProblemException 
	 */
	public OAuthToken getAccessToken(OAuthToken token, OAuthConsumer consumer,
			OAuthServiceProvider provider) 
			throws URISyntaxException, OAuthProblemException, IOException {
		OAuthParams params = 
			new OAuthParams(token, consumer, HttpClient.POST, provider.accessTokenURL);
		return getToken(params, token, consumer);
	}
	
	protected OAuthToken getToken(OAuthParams params, OAuthToken token, OAuthConsumer consumer) 
			throws OAuthProblemException, IOException {
		//get request token values
		HttpResponseMessage response = invoke(params, token, consumer);
		List<Parameter> respParams = 
			OAuth.decodeForm(readToString(response.getBody(), HttpMessage.getBodyEncoding()));
		
		for (Parameter p : respParams) {
			if (p.getName().equals(OAuthParams.OAUTH_TOKEN)) {
				token.setToken(p.getValue());
			} else if (p.getName().equals(OAuthParams.OAUTH_TOKEN_SECRET)) {
				token.setTokenSecret(p.getValue());
			}
		}
		
		return token;
	}
	
	/**
     * Send a request message to the service provider and get the response.
     * 
     * @return the response
	 * @throws IOException 
     *                 failed to communicate with the service provider
     * @throws OAuthProblemException
     *             the HTTP response status code was not 200 (OK)
     */
	public HttpResponseMessage invoke(OAuthParams params, OAuthToken token, OAuthConsumer consumer) 
			throws IOException, OAuthProblemException {
		HttpResponseMessage response = access(params, token, consumer);
		//not a success code
		if (response.getStatusCode() / 100 != 2) {
			throw toOAuthProblemException(response);
		}
		return response;
	}
	
	/**
     * Send a request and return the response. Don't try to decide whether the
     * response indicates success; merely return it.
	 * @throws IOException 
     */
	public HttpResponseMessage access(OAuthParams params, OAuthToken token, OAuthConsumer consumer) 
			throws IOException {
		HttpMessage httpRequest = 
			HttpMessage.newRequest(params, token, consumer, getParameterStyle());
		HttpResponseMessage httpResponse = http.execute(httpRequest, httpParameters);
		return HttpMessageDecoder.decode(httpResponse);
	}
	
	/**
     * Encapsulate this message as an exception.
	 * @throws IOException 
     */
	protected static OAuthProblemException toOAuthProblemException(HttpResponseMessage m) 
			throws IOException {
		OAuthProblemException problem = new OAuthProblemException();
		OAuthParams params = null;
        try {
            params = getOAuthParams(m);
        } catch (IOException ignored) {
        } catch (URISyntaxException ignored) {
		}

        Map<String, Object> problemParams = problem.getParameters();
        params.dump(problemParams);
        m.dump(problemParams);
        
        return problem;
	}
	
	/**
	 * Gets the HTTP "Authorization:" header for the given parameters
	 * @param params	The header parameters
	 * @param token		The secret data required to produce the header
	 * @return	A string describing the authorization header
	 */
	public static String getAuthorizationHeader(OAuthParams params) {
		//add parameters to header
		StringBuilder into = new StringBuilder();
		into.append(OAuthSchemeFactory.SCHEME_NAME);
		boolean first = true;
		for (Parameter p : params.getOAuthParameters()) {
			if (first) {
				first = false;
			} else {
				into.append(",");
			}
			
			into.append(" ").append(OAuth.percentEncode(p.getName())).append("=\"");
			into.append(OAuth.percentEncode(p.getValue())).append("\"");
		}
		
		return into.toString();
	}
	
	public static void ensureSignature(OAuthParams params, OAuthToken token, OAuthConsumer consumer) 
			throws OAuthException, IOException, URISyntaxException {
		if (params.getSignature() == null) {
			if (token != null) {
				OAuthSignatureMethod.newMethod(params.getSignatureMethod())
					.sign(params, token, consumer);
			} else {
				OAuthProblemException problem =
					new OAuthProblemException(OAuth.Problems.PARAMETER_ABSENT);
				problem.setParameter(OAuth.Problems.OAUTH_PARAMETERS_ABSENT,
						OAuth.percentEncode(OAuthParams.OAUTH_SIGNATURE));
				throw problem;
			}
		}
	}
	
    /**
     * Read all the data from the given stream, and close it.
     * 
     * @return null if from is null, or the data from the stream converted to a
     *         String
     */
    public static String readToString(InputStream from, String encoding) throws IOException {
        if (from == null) {
            return null;
        }
        try {
            StringBuilder into = new StringBuilder();
            Reader r = new InputStreamReader(from, encoding);
            char[] s = new char[512];
            for (int n; 0 < (n = r.read(s));) {
                into.append(s, 0, n);
            }
            return into.toString();
        } finally {
            from.close();
        }
    }
    
    /**
     * Extracts OAuth parameters from an HttpResponseMessage
     * @param m	The message
     * @return The OAuth parameters encapsulated in this message
     * @throws URISyntaxException
     * @throws IOException
     */
    public static OAuthParams getOAuthParams(HttpResponseMessage m) 
    		throws URISyntaxException, IOException {
    	OAuthParams params = new OAuthParams();
    	
    	params.setHttpMethod(m.method);
    	params.setUrl(m.url.toExternalForm());
    	
    	params.setParameters(
			OAuth.decodeForm(readToString(m.getBody(), HttpMessage.getBodyEncoding())));
    	
    	return params;
    }
}
