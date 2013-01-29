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
 * Servlet to handle OAuth request token requests.
 * 
 * Based on net.oauth.example.provider.servlets.RequestTokenServlet,
 * distributed with the Java OAuth implementation at http://oauth.googlecode.com/svn/code/java/
 * 
 * @author Praveen Alavilli, AOL
 * @author Aaron Moss, NRC-IIT
 */
public class RequestTokenServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
		OAuthServletUtils.log.trace("Incoming GET Message to request token servlet");
		processRequest(request, response);
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
    	OAuthServletUtils.log.trace("Incoming POST Message to request token servlet");
    	processRequest(request, response);
    }
    
    public void processRequest(HttpServletRequest request, HttpServletResponse response)
    	throws IOException, ServletException {
    	try {
    		OAuthParams requestParams = OAuthServletUtils.getTokenParams(request, null);
            
            OAuthToken token = OAuthProvider.getOAuthProvider().newRequestToken(requestParams);
            
            //send request token as response
            response.setContentType("text/plain");
            OutputStream out = response.getOutputStream();
            OAuth.formEncode(OAuth.newList(OAuthParams.OAUTH_TOKEN, token.getToken(),
                    OAuthParams.OAUTH_TOKEN_SECRET, token.getTokenSecret(),
                    OAuthParams.OAUTH_CALLBACK_CONFIRMED, Boolean.TRUE.toString()),
                    out);
            out.close();
            OAuthServletUtils.log.trace("Req_token: message sent");
    		
    	} catch (Exception e) {
    		OAuthServletUtils.log.trace("Exception in request token servlet", e);
    		OAuthServletUtils.handleException(e, request, response);
    		OAuthServletUtils.log.trace("Req_token: exception sent");
    	}
//    	//testing code
//    	response.setContentType("text/html");
//    	OutputStream outStream = response.getOutputStream();
//    	//OutputStreamWriter out = new OutputStreamWriter(outStream);
//    	PrintStream out = new PrintStream(outStream);
//    	out.println("<html>" + OAuthProvider.getOAuthProvider().sayHello() + "</html>");
//    	out = null;
//    	outStream.close();
    }
}
