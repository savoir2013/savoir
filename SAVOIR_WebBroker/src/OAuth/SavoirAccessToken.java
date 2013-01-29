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

package OAuth;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ca.gc.nrc.iit.oauth.common.OAuth;
import ca.gc.nrc.iit.oauth.common.OAuthParams;
import ca.gc.nrc.iit.oauth.common.OAuthToken;
import ca.gc.nrc.iit.oauth.provider.servlet.OAuthServletUtils;

import ca.gc.nrc.iit.savoir.oAuthProvider.OAuthProviderService;
import ca.gc.nrc.iit.savoir.oAuthProvider.WebServiceOAuthParams;


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
public class SavoirAccessToken extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SavoirAccessToken.class);
	static String[] CONFIG_FILES = new String[] { "classpath:cxf.xml" };

	static ApplicationContext ac = new ClassPathXmlApplicationContext(
			CONFIG_FILES);
	private OAuthProviderService oaService;
	
	public SavoirAccessToken(){
		super();
		oaService =  (OAuthProviderService)ac.getBean("oauthProviderClient");
	}

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
    		WebServiceOAuthParams wsOAuthParams = new WebServiceOAuthParams(requestParams);
    		OAuthToken token = oaService.newAccessToken(wsOAuthParams);
			
			
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