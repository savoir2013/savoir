// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.oauth.provider.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.gc.nrc.iit.oauth.common.OAuthParams;
import ca.gc.nrc.iit.oauth.provider.OAuthProvider;


public class LogoutServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
		OAuthServletUtils.log.trace("Incoming GET Message to logout servlet");
		processRequest(request, response);
    }
    
	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
		OAuthServletUtils.log.trace("Incoming POST Message to logout servlet");
		processRequest(request, response);
    }
        
    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
    	try {
			OAuthParams requestParams = OAuthServletUtils.getTokenParams(request, null);
			OAuthProvider.getOAuthProvider().logout(requestParams);
			
			//return to consumer
			response.setContentType("text/plain");
            PrintWriter out = response.getWriter();
            out.println("You have successfully logged out '" + requestParams.getConsumerKey() + "'.");
            out.close();
            OAuthServletUtils.log.trace("Logout: message sent");
		} catch (Exception e) {
			OAuthServletUtils.log.trace("Exception in logout servlet", e);
			OAuthServletUtils.handleException(e, request, response);
			OAuthServletUtils.log.trace("Logout: exception sent");
		}
    }
}
