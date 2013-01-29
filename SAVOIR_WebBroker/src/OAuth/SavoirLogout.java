// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

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
import ca.gc.nrc.iit.savoir.sessionMgmt.SessionMgr;

public class SavoirLogout extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SavoirLogout.class);
	static String[] CONFIG_FILES = new String[] { "classpath:cxf.xml" };

	static ApplicationContext ac = new ClassPathXmlApplicationContext(
			CONFIG_FILES);
	private OAuthProviderService oaService;
	private SessionMgr sesMgr;
	
	public SavoirLogout(){
		super();
		oaService =  (OAuthProviderService)ac.getBean("oauthProviderClient");
		sesMgr = (SessionMgr) ac.getBean("sessionMgrClient");
	}

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
			WebServiceOAuthParams wsOAuthParams = new WebServiceOAuthParams(requestParams);
			oaService.logout(wsOAuthParams);
			//end default session for user
			sesMgr.endSessionAgnostic(requestParams.getConsumerKey());
			
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
