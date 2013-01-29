// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.mgmtUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

public class BusTools {

	private static ResourceBundle resources = 
		ResourceBundle.getBundle("mgmtservices", Locale.getDefault());
		
	private static String webUriPrefix = resources.getString("repos.webPrefix");

	public static void sendMessage(String xmlMessage, List<String> parameters,
			List<String> values) {
		
		String webUri = webUriPrefix.replaceFirst("/$", "");
		
		StringBuilder sb = new StringBuilder(webUri + ":8890");	

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("savoirmsg", xmlMessage));

		System.out.println(sb.toString());

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpost = new HttpPost(sb.toString());
		
		httpost.addHeader("contentType", "text/html;charset=UTF-8");	

		try {
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

			System.out.println("executing request " + httpost.getURI());

			// Create a response handler
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody;

			responseBody = httpclient.execute(httpost, responseHandler);
			System.out.println(responseBody);

			System.out.println("----------------------------------------");

			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
