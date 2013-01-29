/*
 * Copyright 2007 AOL, LLC.
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
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.gc.nrc.iit.oauth.common.OAuth;
import ca.gc.nrc.iit.oauth.common.OAuthParams;
import ca.gc.nrc.iit.oauth.common.OAuthToken;
import ca.gc.nrc.iit.oauth.provider.OAuthProvider;



/**
 * Servlet to handle OAuth access token requests.
 * 
 * Based on net.oauth.example.provider.servlets.AccessTokenServlet,
 * distributed with the Java OAuth implementation at 
 * http://oauth.googlecode.com/svn/code/java/
 * 
 * @author Praveen Alavilli, AOL
 * @author Aaron Moss, NRC-IIT
 */
public class AccessTokenServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        processRequest(request, response);
    }
    
	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        processRequest(request, response);
    }
        
    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
    	try {
    		OAuthParams requestParams = OAuthServletUtils.getTokenParams(request, null);
    		OAuthToken token = OAuthProvider.getOAuthProvider().newAccessToken(requestParams);
			
			
			response.setContentType("text/plain");
			OutputStream out = response.getOutputStream();
            OAuth.formEncode(OAuth.newList(OAuthParams.OAUTH_TOKEN, token.getToken(),
            		OAuthParams.OAUTH_TOKEN_SECRET, token.getTokenSecret()),
            		out);
			
            out.close();
		} catch (Exception e) {
			OAuthServletUtils.handleException(e, request, response);
		}
    }
	
}