/*
 Copyright 2009 Revolution Systems Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

package ca.gc.nrc.iit.oauth.provider.spring;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.ui.AuthenticationEntryPoint;
import org.springframework.security.ui.FilterChainOrder;
import org.springframework.security.ui.SpringSecurityFilter;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.util.Assert;

import ca.gc.nrc.iit.oauth.common.OAuthParams;
import ca.gc.nrc.iit.oauth.provider.OAuthProvider;
import ca.gc.nrc.iit.oauth.provider.servlet.OAuthServletUtils;


/**
 * Filter to support OAuth authentication in Spring Security.
 * 
 * To wire this in Spring Security, add to its bean definition a "custom-filter" element from the
 * Spring Security namespace, with attribute "after" with value "BASIC_PROCESSING_FILTER". Other 
 * bean properties that must be set include consumerDetailsService, a reference to the Spring 
 * Security UserDetailsService containing the necessary details for these consumers, and realmName, 
 * a string naming this filter's authentication realm. Optionally, an "oauthProvider" property 
 * can be set, containing a reference to the OAuthProvider to use for this filter (the default
 * value is the OAuthProvider configured for this servlet).
 * 
 * Based on com.revolsys.security.oauth.OAuthProcessingFilter,
 * and com.revolsys.security.oauth.OAuthProcessingFilterEntryPoint
 * distributed at http://open.revolsys.com/svn/repos/com.revolsys.grid/trunk/com.revolsys.security/src/main/java/
 * 
 * @author Paul Austin, Revolution Systems
 * @author Aaron Moss, NRC-IIT
 */
public class OAuthProcessingFilter extends SpringSecurityFilter implements
		AuthenticationEntryPoint, InitializingBean {
	
	private UserDetailsService consumerDetailsService;
	private OAuthProvider oAuthProvider = null;
	private String realmName;
	
	@Override
	protected void doFilterHttp(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
		throws IOException, ServletException {
		
		String consumerKey = null;
		SecurityContext context = SecurityContextHolder.getContext();
		
		//checks to see that this method is OAuth secured
		String header = request.getHeader("Authorization");
		if (header != null) {
			String authType = header.split("\\s+")[0];
			if ("OAuth".equals(authType)) {
				try {
					OAuthParams params = null;
					try {
						params = OAuthServletUtils.getTokenParams(request);
					} catch (URISyntaxException ignored) {
					}
					consumerKey = params.getConsumerKey();
					UserDetails consumerDetails = getConsumerDetails(consumerKey);
					
					if (consumerDetails != null) {
						//validate access token (will throw exception for invalid token)
						oAuthProvider.validateMessage(params);
						
						//authorize this request to Spring Security
						GrantedAuthority[] authorities = consumerDetails.getAuthorities();
						Authentication authResult = 
							new OAuthAuthenticationToken(consumerKey, authorities);
						authResult.setAuthenticated(true);
						
						if (logger.isDebugEnabled()) {
							logger.debug("Authentication success: " + authResult);
						}
						
						context.setAuthentication(authResult);
					}
				} catch (Exception e) {
					AuthenticationException authE = 
						new BadCredentialsException("Signature validation failed", e);
					logger.debug("Authentication request for user : " + consumerKey + " failed", e);
					
					/* DEBUG CODE */
//					java.io.PrintStream debug = new java.io.PrintStream("oAuth.log");
//					debug.println("Authn request from '" + consumerKey + "' failed.");
//					String[][] tp = new String[][] {
//							{"token", accessor.accessToken},
//							{"tokenSecret", accessor.tokenSecret},
//							{"consumerKey", accessor.consumer.consumerKey},
//							{"consumerSecret", accessor.consumer.consumerSecret},
//							{"sigMethod", message.getSignatureMethod()},
//							{"timestamp", message.getParameter(OAuth.OAUTH_TIMESTAMP)},
//							{"nonce", message.getParameter(OAuth.OAUTH_NONCE)},
//							{"version", message.getParameter(OAuth.OAUTH_VERSION)},
//							{"httpMethod", message.method},
//							{"url", message.URL},
//							{"signature", message.getSignature()}
//					};
//					for (String[] a : tp) {
//						debug.println(a[0] + ":\t'" + a[1] + "'");
//					}
//					debug.println("OAuth params:");
//					for (Map.Entry<String, String> entry: message.getParameters()) {
//						debug.println(entry.getKey() + ":\t'" + entry.getValue() + "'");
//					}
//					e.printStackTrace(debug);
					/* END DEBUG CODE */
					
					context.setAuthentication(null);
					onUnsuccessfulAuthentication(request, response, authE);
					this.commence(request, response, authE);
				}
			}
		}
		
		
		chain.doFilter(request, response);
	}

	@Override
	public void commence(ServletRequest request, ServletResponse response, AuthenticationException authException)
		throws IOException, ServletException {
		HttpServletResponse httpResponse = (HttpServletResponse)response;
		httpResponse.addHeader("WWW-Authenticate", "OAuth realm=\"" + realmName + "\"");
		httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
	}
	
	private UserDetails getConsumerDetails(String consumerKey) {
		return consumerKey == null ? null : consumerDetailsService.loadUserByUsername(consumerKey);
	}
	
	protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response) 
		throws IOException {}
	
	protected void onUnsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, 
			AuthenticationException failed) throws IOException {}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(consumerDetailsService, 
				"A consumerDetailsService UserDetailsService property must be set");
		if (oAuthProvider == null) {
			oAuthProvider = OAuthProvider.getOAuthProvider();
		}
		Assert.hasText(realmName, "realmName must be specified");
	}

	@Override
	public int getOrder() {
		return FilterChainOrder.BASIC_PROCESSING_FILTER;
	}
	
	public UserDetailsService getConsumerDetailsService() {
		return consumerDetailsService;
	}

	public void setConsumerDetailsService(UserDetailsService consumerDetailsService) {
		this.consumerDetailsService = consumerDetailsService;
	}
	
	public OAuthProvider getOauthProvider() {
		return this.oAuthProvider;
	}
	
	public void setOauthProvider(OAuthProvider provider) {
		this.oAuthProvider = provider;
	}
	
	public String getRealmName() {
		return this.realmName;
	}

	public void setRealmName(String realmName) {
		this.realmName = realmName;
	}

}
