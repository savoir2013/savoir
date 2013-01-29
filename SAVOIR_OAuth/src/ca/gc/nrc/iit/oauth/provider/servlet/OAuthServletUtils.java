/*
 * Copyright 2007 Netflix, Inc.
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

package ca.gc.nrc.iit.oauth.provider.servlet;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import ca.gc.nrc.iit.oauth.common.OAuth;
import ca.gc.nrc.iit.oauth.common.OAuthParams;
import ca.gc.nrc.iit.oauth.common.OAuthParams.Parameter;
import ca.gc.nrc.iit.oauth.common.exception.OAuthProblemException;
import ca.gc.nrc.iit.oauth.common.exception.SanitizedOAuthException;


/**
 * Utility methods for servlets that implement OAuth.
 * 
 * @author John Kristian, Netflix
 * @author Aaron Moss, NRC-IIT
 */
public class OAuthServletUtils {

	public static final String AUTH_SCHEME = "OAuth";
	public static final Logger log = Logger.getLogger(OAuthServletUtils.class);
	
	/**
     * Convenience method for getTokenParams(request, null);
     * 
	 * @throws IOException 
	 * @throws URISyntaxException 
     */
	public static OAuthParams getTokenParams(HttpServletRequest request) 
			throws IOException, URISyntaxException {
		return getTokenParams(request, null);
	}
	
	/**
     * Extract the parts of the given request that are relevant to OAuth.
     * Parameters include OAuth Authorization headers and the usual request
     * parameters in the query string and/or form encoded body.
     * 
     * @param request
     * 			the request made to this servlet
     * 
     * @param url
     * 			the official URL of this service; that is the URL a legitimate
     *          client would use to compute the digital signature. If this
     *          parameter is null, this method will try to reconstruct the URL
     *          from the HTTP request; which may be wrong in some cases.
	 * @throws IOException 
	 * @throws URISyntaxException 
     */
	@SuppressWarnings("unchecked")
	public static OAuthParams getTokenParams(HttpServletRequest request, String url) 
			throws IOException, URISyntaxException {
		
		OAuthParams params = new OAuthParams();
		
		params.setHttpMethod(request.getMethod());
		if (url == null) {
			url = request.getRequestURL().toString();
		}
		params.setUrl(url);
		
		Map<String, String[]> paramMap = request.getParameterMap();
		List<Parameter> requestParams = new ArrayList<Parameter>(paramMap.size());
		
		for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            String name = entry.getKey();
            for (String value : entry.getValue()) {
            	requestParams.add(new Parameter(name, value));
            }
		}
		
		params.setParameters(requestParams);
		
		String authzHeader = request.getHeader("Authorization");
		if (authzHeader != null) {
			params.decodeAuthorization(authzHeader);
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Decoded Request Parameters:\n" + params);
		}
		
		return params;
	}
	
    public static void handleException(Exception e, HttpServletRequest request, 
    		HttpServletResponse response) 
    		throws IOException, ServletException {
    	String realm = (request.isSecure())?"https://":"http://";
    	realm += request.getLocalName();
    	
    	if (e instanceof OAuthProblemException) {
            OAuthProblemException problem = (OAuthProblemException) e;
            Object httpCode = problem.getParameters().get(OAuth.HTTP_STATUS_CODE);
            if (httpCode == null) {
                httpCode = OAuth.Problems.TO_HTTP_CODE.get(problem.getProblem());
            }
            if (httpCode == null) {
                httpCode = HttpServletResponse.SC_FORBIDDEN;
            }
            response.reset();
            response.setStatus(Integer.parseInt(httpCode.toString()));
			response.addHeader("WWW-Authenticate", getAuthorizationHeader(realm));
            sendForm(response, problem.getParameterList());
    	} else if (e instanceof SanitizedOAuthException) {
    		SanitizedOAuthException problem = (SanitizedOAuthException) e;
    		response.reset();
            response.setStatus(problem.getProblem().toHttpCode());
			response.addHeader("WWW-Authenticate", getAuthorizationHeader(realm));
    	} else if (e instanceof IOException) {
            throw (IOException) e;
        } else if (e instanceof ServletException) {
            throw (ServletException) e;
        } else if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else {
            throw new ServletException(e);
        }
    }
    
    /** Send the given parameters as a form-encoded response body. */
    public static void sendForm(HttpServletResponse response,
            Iterable<? extends Map.Entry<String,String>> parameters) 
    		throws IOException {
        response.resetBuffer();
        response.setContentType(OAuth.FORM_ENCODED + ";charset="
                + OAuth.ENCODING);
        OAuth.formEncode(parameters, response.getOutputStream());
    }
    
    /**
     * Construct a WWW-Authenticate header value, containing the given realm.
     */
    public static String getAuthorizationHeader(String realm) {
    	return AUTH_SCHEME + " realm=\"" + OAuth.percentEncode(realm) + "\"";
    }
}
