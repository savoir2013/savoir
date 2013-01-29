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
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ca.gc.nrc.iit.oauth.common.OAuthParams;
import ca.gc.nrc.iit.oauth.provider.servlet.OAuthServletUtils;
import ca.gc.nrc.iit.savoir.oAuthProvider.OAuthProviderService;
import ca.gc.nrc.iit.savoir.oAuthProvider.WebServiceOAuthParams;

/**
 * Servlet to handle OAuth request token authorization requests.
 * This is quite possibly non-spec, as any valid request token will be authorized.
 * The reason for this is that we are dealing with 2-legged OAuth, so the user already
 * authorized the login process to produce the request token.
 * 
 * Based on net.oauth.example.provider.servlets.AuthorizationServlet,
 * distributed with the Java OAuth implementation at http://oauth.googlecode.com/svn/code/java/
 * 
 * @author Praveen Alavilli, AOL
 * @author Aaron Moss, NRC-IIT
 */
public class SavoirRequestAuthorization extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(SavoirRequestAuthorization.class);
	static String[] CONFIG_FILES = new String[] { "classpath:cxf.xml" };

	static ApplicationContext ac = new ClassPathXmlApplicationContext(
			CONFIG_FILES);
	private OAuthProviderService oaService;
	
	public SavoirRequestAuthorization(){
		super();
		oaService =  (OAuthProviderService)ac.getBean("oauthProviderClient");
	}


	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
		try {
			OAuthParams requestParams = OAuthServletUtils.getTokenParams(request, null);
			WebServiceOAuthParams wsOAuthParams = new WebServiceOAuthParams(requestParams);
			//check records for this request token
			oaService.authorizeRequestToken(wsOAuthParams);
			
			//return to consumer, ignoring any callback URLs
			response.setContentType("text/plain");
            PrintWriter out = response.getWriter();
            out.println("You have successfully authorized '" 
            		+ requestParams.getConsumerKey() + "'.");
            out.close();
            
//			//return to consumer, using callback URL if available
//			//NOTE: none of our consumers (users) have callback URLs, 
//			// but I thought it'd be better to properly implement the spec.
//          //Then this didn't work, and I didn't want to waste time debugging it
//			String callback = request.getParameter("oauth_callback");
//	        if("none".equals(callback) 
//	            && accessor.consumer.callbackURL != null 
//	                && accessor.consumer.callbackURL.length() > 0){
//	            // first check if we have something in our properties file
//	            callback = accessor.consumer.callbackURL;
//	        }
//	        
//	        if( "none".equals(callback) ) {
//	            // no call back it must be a client
//	            response.setContentType("text/plain");
//	            PrintWriter out = response.getWriter();
//	            out.println("You have successfully authorized '" 
//	                    + accessor.consumer.getProperty("description") + "'.");
//	            out.close();
//	        } else {
//	            // if callback is not passed in, use the callback from config
//	            if(callback == null || callback.length() <=0 )
//	                callback = accessor.consumer.callbackURL;
//	            String token = accessor.requestToken;
//	            if (token != null) {
//	                callback = OAuth.addParameters(callback, "oauth_token", token);
//	            }
//
//	            response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
//	            response.setHeader("Location", callback);
//	        }
			
		} catch (Exception e) {
			OAuthServletUtils.handleException(e, request, response);
		}
	}
}
