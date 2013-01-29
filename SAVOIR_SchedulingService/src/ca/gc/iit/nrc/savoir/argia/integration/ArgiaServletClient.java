// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.argia.integration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import ca.gc.iit.nrc.savoir.domain.Resource;
import ca.gc.nrc.iit.savoir.dao.impl.DAOFactory;
import ca.gc.nrc.iit.savoir.utils.ClientException;

public class ArgiaServletClient {

	
	private ResourceBundle clientRsc = ResourceBundle.getBundle("client",
			Locale.getDefault());

	public ArgiaServletClient() throws IOException, ClientException {
		
	}

	public String setLightPath(String scenarioID) throws IOException,
			ClientException {
		String[] parameters = { "scID" };
		String[] values = { scenarioID };
		String result = sendRequestToServlet("setScenario", parameters, values);
		//(new DaemonNotificationThread(result, "501", "106")).start();
		return result;
	}

	public String unSetLightPath(String scenarioID) throws IOException,
			ClientException {
		String[] parameters = { "scID" };
		String[] values = { scenarioID };
		String result = sendRequestToServlet("unSetScenario", parameters,
				values);
		//(new DaemonNotificationThread(result, "502", "106")).start();
		return result;
	}

	public String queryScenario() throws IOException, ClientException {
		return sendRequestToServlet("queryScenarios", null, null);
	}

	public String getCurrentScenarioID() throws IOException, ClientException {
		return sendRequestToServlet("queryConfiguredScenario", null, null);
	}

	private String sendRequestToServlet(String command, String[] parameters,
			String[] values) throws IOException, ClientException {

		List<Resource> r = DAOFactory.getDAOFactoryInstance().getResourceDAO()
				.getResourcesByType("LP_APN");

		Resource apnResource = null;

		if (r != null && r.get(0) != null) {
			apnResource = r.get(0);
		} else {
			throw new ClientException(
					"Could not retrieve APN data from database");
		}

		StringBuilder sb = new StringBuilder("?rq=");
		sb.append(command);

		sb.append("&key=");
		sb.append(apnResource.getParameterValue("LP_APN_EPR"));

		sb.append("&user=");
		sb.append(apnResource.getParameterValue("LP_APN_USER"));

		sb.append("&pass=");
		sb.append(apnResource.getParameterValue("LP_APN_PASSWORD"));

		sb.append("&org=");
		sb.append(apnResource.getParameterValue("LP_APN_ORGANIZATION"));

		System.out.println(sb.toString());

		URL url = new URL("http://" + clientRsc.getString("server") + ":"
				+ clientRsc.getString("port") + "/"
				+ clientRsc.getString("servletPath") + sb.toString());

		System.out.println(url.toString());

		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);

		if (parameters != null && values != null) {

			for (int i = 0; i < parameters.length; i++) {
				sb.append("&");
				sb.append(parameters[i]);
				sb.append("=");
				sb.append(values[i]);
			}
		}

		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(conn
				.getOutputStream()));
		out.write(sb.toString());
		out.flush();
		out.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(conn
				.getInputStream()));

		String line, response = "";
		while ((line = in.readLine()) != null) {
			response += line;
		}
		in.close();
		System.out.println(response);
		return response;
	}
}
