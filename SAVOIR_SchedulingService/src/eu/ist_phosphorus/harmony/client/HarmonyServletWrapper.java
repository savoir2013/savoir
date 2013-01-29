// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package eu.ist_phosphorus.harmony.client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.log4j.Logger;
import org.apache.muse.ws.addressing.soap.SoapFault;

import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.Activate;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.ActivateResponseType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.CancelReservationResponseType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.CancelReservationType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.CompleteJob;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.CompleteJobResponseType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.CreateReservation;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.CreateReservationResponseType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.CreateReservationType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.GetReservationsResponseType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.GetStatusResponseType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.GetStatusType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.IsAvailable;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.IsAvailableResponseType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.jaxb.IsAvailableType;
import eu.ist_phosphorus.harmony.common.serviceinterface.databinding.utils.JaxbSerializer;

/**
 * @author harzallahy
 * 
 */
public class HarmonyServletWrapper {

	private static final Logger logger = Logger
			.getLogger(HarmonyServletWrapper.class);

	public HarmonyServletWrapper() {

	}

	public static CompleteJobResponseType completeJob(CompleteJob completeJob)
			throws SoapFault, IOException, ClassNotFoundException {

		String[] parameters = { "i" };
		String[] values = { completeJob.getCompleteJob().getJobID() + "" };
		Object res = sendRequestToServlet("completejob", parameters, values);
		try {
			CompleteJobResponseType success = (CompleteJobResponseType) res;
			return success;
		} catch (ClassCastException e) {
			return null;
		}
	}

	public static GetReservationsResponseType getReservations()
			throws SoapFault, IOException, ClassNotFoundException {

		Object res = sendRequestToServlet("getreservations", null, null);
		try {
			GetReservationsResponseType success = (GetReservationsResponseType) res;
			return success;
		} catch (ClassCastException e) {
			return null;
		}
	}

	public static CancelReservationResponseType cancelReservation(
			CancelReservationType request) throws SoapFault, IOException,
			ClassNotFoundException {

		String[] parameters = { "i" };
		String[] values = { request.getReservationID() };
		Object res = sendRequestToServlet("cancel", parameters, values);
		try {
			CancelReservationResponseType success = (CancelReservationResponseType) res;
			return success;
		} catch (ClassCastException e) {
			return null;
		}
	}

	//TODO: harmony does not have a delete reservation method... Cancel should do the
	// same but this needs to be tested... Canceling a cancelled reservation
	// might throw an exception
	public static CancelReservationResponseType deleteReservation(
			CancelReservationType request) throws SoapFault, IOException,
			ClassNotFoundException {

		String[] parameters = { "i" };
		String[] values = { request.getReservationID() };
		Object res = sendRequestToServlet("cancel", parameters, values);
		try {
			CancelReservationResponseType success = (CancelReservationResponseType) res;
			return success;
		} catch (ClassCastException e) {
			return null;
		}
	}

	public static IsAvailableResponseType isAvailable(IsAvailableType isAvReq)
			throws SoapFault, IOException, ClassNotFoundException {
		logger.info("checking network availability");
		String[] parameters = { "xml" };
		IsAvailable isAv = new IsAvailable();
		isAv.setIsAvailable(isAvReq);
		String value = "";
		try {
			value = JaxbSerializer.getInstance().objectToXml(isAv);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		String[] values = { value };
		IsAvailableResponseType response = (IsAvailableResponseType) sendRequestToServlet(
				"isavailable", parameters, values);
		return response;
	}

	/**
	 * @param options
	 * @throws DatatypeConfigurationException
	 * @throws SoapFault
	 * @throws IOException
	 * @throws JAXBException
	 * @throws ClassNotFoundException
	 */
	public static CreateReservationResponseType createReservation(
			CreateReservationType request) throws SoapFault, IOException,
			ClassNotFoundException {

		String[] parameters = { "xml" };
		CreateReservation resv = new CreateReservation();
		resv.setCreateReservation(request);

		String value = "";
		try {
			value = JaxbSerializer.getInstance().objectToXml(resv);
			//added for test 09-22-9
			logger.info("sent out xml is::: " + value);
			//end add
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		String[] values = { value };

		Object res = sendRequestToServlet("create", parameters, values);
		try {
			CreateReservationResponseType result = (CreateReservationResponseType) res;
			return result;
		} catch (ClassCastException e) {
			return null;
		}
	}

	public static ActivateResponseType activate(Activate activate)
			throws IOException, ClassNotFoundException {
		String[] parameters = { "xml" };
		String value = "";
		try {
			value = JaxbSerializer.getInstance().objectToXml(activate);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		String[] values = { value };

		Object res = sendRequestToServlet("activate", parameters, values);
		try {
			ActivateResponseType result = (ActivateResponseType) res;
			return result;
		} catch (ClassCastException e) {
			return null;
		}
	}

	public static GetStatusResponseType getReservationStatus(
			GetStatusType request) throws SoapFault, IOException,
			ClassNotFoundException {

		String[] parameters = { "i" };
		String[] values = { request.getReservationID() };

		Object res = sendRequestToServlet("getstatus", parameters, values);
		try {
			GetStatusResponseType status = (GetStatusResponseType) res;
			return status;
		} catch (ClassCastException e) {
			return null;
		}
	}

	private static Object sendRequestToServlet(String command,
			String[] parameters, String[] values) throws IOException,
			ClassNotFoundException {

		StringBuilder sb = new StringBuilder("?a=");
		sb.append(command);

		System.out.println(sb.toString());

		URL url = new URL("http://10.1.3.4:8080/SAVOIR_HARMONY/HarmonyClient"
				+ sb.toString());

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

		ObjectInputStream in = new ObjectInputStream(conn.getInputStream());

		Object response = in.readObject();

		in.close();
		System.out.println(response);

		return response;
	}

	public static void main(String[] args) {
		// String[] parameters = { "i" };
		// String[] values = { "1357@idb" };
		//
		String[] parameters = { "i" };
		String[] values = { "988@idb" };

		// String[] parameters = { "s","t" };
		// String[] values = { "128.0.0.1","128.0.0.2" };

		// Activate activate = new Activate();
		// ActivateType type = new ActivateType();
		// type.setReservationID("983@idb");
		// type.setServiceID(1);
		// activate.setActivate(type);
		//
		// ActivateResponseType resp = null;
		//
		// try {
		// resp = activate(activate);
		// System.out.println(resp.isSuccess());
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (ClassNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		try {
			GetStatusResponseType status = (GetStatusResponseType) sendRequestToServlet(
					"getstatus", parameters, values);
			System.out.println(status.getServiceStatus().get(0).getStatus()
					.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// try {
		// CancelReservationResponseType success =
		// (CancelReservationResponseType) sendRequestToServlet(
		// "cancel", parameters, values);
		// System.out.println(success.isSuccess());
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (ClassNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// try {
		// GetReservationsResponseType success = getReservations();
		// for (GetReservationsComplexType cpType : success.getReservation()) {
		// System.out.println(cpType.getReservationID()
		// + " ------ "
		// + cpType.getReservation().getJobID()
		// + " ----- "
		// + ((GetStatusResponseType) sendRequestToServlet(
		// "getstatus", new String[] { "i" },
		// new String[] { cpType.getReservationID() }))
		// .getServiceStatus().get(0).getStatus()
		// .toString());
		// }
		//
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (ClassNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (SoapFault e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// CompleteJobType type = new CompleteJobType();
		// type.setJobID(1);
		// CompleteJob job = new CompleteJob();
		// job.setCompleteJob(type);
		// try {
		// System.out.println(((CompleteJob)
		// JaxbSerializer.getInstance().xmlToObject
		// (JaxbSerializer.getInstance().
		// objectToXml(job))).getCompleteJob().getJobID());
		// } catch (JAXBException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

}
