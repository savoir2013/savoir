// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.oauth.consumer.cxf;

import java.net.URL;

import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.HttpAuthSupplier;

/**
 * A HttpAuthSupplier with provided authentication header.
 * 
 * @author Aaron Moss, NRC-IIT
 */
public class RelayHttpAuthSupplier extends HttpAuthSupplier {

	/**
	 * The authorization header to use to sign the request
	 */
	protected String authHeader;
	
	public String getAuthHeader() {
		return authHeader;
	}

	public void setAuthHeader(String authHeader) {
		this.authHeader = authHeader;
	}

	@Override
	public String getAuthorizationForRealm(HTTPConduit arg0, URL arg1,
			Message arg2, String arg3, String arg4) {
		return getAuthHeader();
	}

	@Override
	public String getPreemptiveAuthorization(HTTPConduit arg0, URL arg1,
			Message arg2) {
		return getAuthHeader();
	}

}
