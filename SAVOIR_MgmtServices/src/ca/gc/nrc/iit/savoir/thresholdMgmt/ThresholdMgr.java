// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.thresholdMgmt;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * Incoming bus interface for SAVOIR. 
 * The XML content of all incoming messages to SAVOIR should be passed to the 
 * {@link #handleIncoming(String)} method of this service, to be acted on as 
 * appropriate.
 */
@WebService
public interface ThresholdMgr {

	/**
	 * Endpoint for incoming bus messages. All messages routed to SAVOIR on the 
	 * message bus should be passed to this method.
	 * 
	 * @param message	The bus message sent to SAVOIR
	 */
	public void handleIncoming(@WebParam(name = "message")String message);
}
