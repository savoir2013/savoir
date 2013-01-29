// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.oauth.common.exception;

import ca.gc.nrc.iit.oauth.common.OAuth;

public class SanitizedOAuthException extends OAuthException {

	private static final long serialVersionUID = 1L;
	
	public static enum Problem {
		NONE, BAD_REQUEST, REQUEST_DENIED, SERVICE_UNAVAILABLE, OTHER;
		
		public static Problem fromHttpCode(int httpCode) {
			switch (httpCode) {
			case 200:	//success
				return NONE;
			case 400:	//Bad Request
				return BAD_REQUEST;
			case 401:	//Unauthorized
				return REQUEST_DENIED;
			case 404:	//Not Found
			case 503:	//Service Unavailable
				return SERVICE_UNAVAILABLE;
			default:
				return OTHER;
			}
		}
		
		public int toHttpCode() {
			switch(this) {
			case BAD_REQUEST:
				return 400;	//Bad Request
			case REQUEST_DENIED:
				return 401;	//Unauthorized
			case SERVICE_UNAVAILABLE:
				return 503;	//Service Unavailable
			case NONE:
			case OTHER:
			default:
				return 500;	//Internal Error
			}
		}
	}
	
	private Problem problem;
	
	public SanitizedOAuthException() {
		super();
		problem = Problem.NONE;
	}
	
	public SanitizedOAuthException(OAuthProblemException e) {
		super();
		//check HTTP status code
		Integer httpCode = e.getHttpStatusCode();
		if (httpCode == 200) {
			httpCode = (Integer)e.getParameters().get(OAuth.HTTP_STATUS_CODE);
	        if (httpCode == null) {
	            httpCode = OAuth.Problems.TO_HTTP_CODE.get(e.getProblem());
	        }
		}
		problem = Problem.fromHttpCode(httpCode);
	}
	
	public SanitizedOAuthException(Problem p) {
		super();
		problem = p;
	}
	
	public Problem getProblem() {
		return this.problem;
	}
	
	public void setProblem(Problem p) {
		this.problem = p;
	}
}
