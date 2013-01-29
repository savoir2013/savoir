// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author yonghuayou
 */
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class EKGTutorialService {

    public final String EDGE_DEVICE_ID = "99";
    public final String EDGE_DEVICE_NAME = "VaderWeb_NRC";
    public final String EDGE_DEVICE_ABBREVIATION = "VaderWeb"; //random abbreviation used in the JMS topic name

    public final String EDGE_DEVICE_MESSAGE_CACHE = "http://198.164.40.210:8080/" + EDGE_DEVICE_NAME + "/MessageCache"; //QUICKFIX: message cache for setParameter messages
    //Bus Interface settings
    public final String BI_ENDPOINT = "http://198.164.40.210:8080/" + EDGE_DEVICE_NAME + "/";
    public static final String EDGE_DEVICE_TO_SAVOIR_PROFILE_TRANSFORM = "WEB-INF/businterface/template/device-profile.xsl";
    //Savoir settings
    public final String SAVOIR_ENDPOINT = "http://198.164.40.171:8891";
    public static final String SAVOIR_PROFILE_CACHE = "99_VaderWeb_NRC.xml"; //TODO: profile should then get updated dynamically based on the device profile (see above)
    //////////////////////////////////////////////////////////////////////////////
    //MESSAGES (Savoir-to-Device)
    public static final String DEVICE_AUTH_MESSAGE = "authenticate";
    public static final String DEVICE_LOAD_MESSAGE = "load";
    public static final String DEVICE_START_MESSAGE = "start";
    public static final String DEVICE_PROFILE_MESSAGE = "getProfile";
    //special case... save setParameter messages for client
    public static final String DEVICE_PARAMETER_MESSAGE = "setParameter";
    //MESSAGE RESPONSES (Device-to-Savoir)
    public static final String SAVOIR_ACK_MESSAGE = "acknowledge";
    public static final String SAVOIR_START_MESSAGE = "startResponse";
    public static final String SAVOIR_PROFILE_MESSAGE = "reportProfile";
    public static final String SAVOIR_STATUS_RESPONSE = "reportStatus";


    public static final String DEVICE_PAUSE_MESSAGE = "pause";
    public static final String DEVICE_RESUME_MESSAGE = "resume";
    public static final String DEVICE_STOP_MESSAGE = "stop";
    public static final String DEVICE_STATUS_MESSAGE = "getStatus";
    public static final String DEVICE_NOTIFY_MESSAGE = "notify";
    public static final String DEVICE_END_MESSAGE = "endSession";

    private EKGTutorialServiceAdapter ekgServiceAdapter;
    private String lastModified = "";
    public EKGTutorialService() {
    }

    public void initService(EKGTutorialServiceAdapter serviceAdapter){
        ekgServiceAdapter = serviceAdapter;
    }

    public void processMessageFromBus(String msgString) {
        System.out.println("Consuming message: " + msgString.trim());
        try {
            String savoirMsg;

            //Message data, fetched using XPath parser
            String messageID = getAttribute(msgString, "//message/@ID");
            String messageAction = getAttribute(msgString, "//message/@action");
            String sessionID = getAttribute(msgString, "//message/@sessionID");
            String serviceID = getAttribute(msgString, "//message/service/@ID");
            String serviceName = getAttribute(msgString, "//message/service/@name");
            String activityID = getAttribute(msgString, "//message/service/@activityID");
            String activityName = getAttribute(msgString, "//message/service/@activityName");
            if (activityName.equals("") || activityName == null) {
                String filePath = this.ekgServiceAdapter.getEKGProfileFile();
                String xml = loadFileAsString(filePath);
                String xpathActivityName = "//profileMessage/service/activities/activity[@ID=" + activityID + "]/name";
                //        logger.info("Trying the following XPath evaluation: " + xpath);
                //        System.out.println("Trying the following XPath evaluation: " + xpath);
                activityName = getAttribute(xml, xpathActivityName);
            }
            String serviceUserID = getAttribute(msgString, "//message/service/@serviceUserID");

            if (messageAction.equalsIgnoreCase(DEVICE_AUTH_MESSAGE)) {
                String servicePassword = getAttribute(msgString, "//message/service/@servicePassword");
                int success = (authenticateDevice(serviceUserID, servicePassword)) ? 1 : 0; //AUTH was successful "1" or not "0"
                savoirMsg = "<message action=\"" + SAVOIR_ACK_MESSAGE + "\" sessionID=\"" + sessionID + "\"><service ID=\"" + serviceID + "\" name=\"" + serviceName + "\" activityID=\"" + activityID + "\" activityName=\"" + URLEncoder.encode(activityName, "UTF-8") + "\"><notification messageID=\"" + messageID + "\" messageAction=\"" + DEVICE_AUTH_MESSAGE + "\" success=\"" + success + "\" /></service></message>";
                ekgServiceAdapter.sendMessageToSavoir(savoirMsg);
            } else if (messageAction.equalsIgnoreCase(DEVICE_LOAD_MESSAGE)) {
                int success = (loadDevice()) ? 1 : 0; //LOAD was successful "1" or not "0"
                savoirMsg = "<message action=\"" + SAVOIR_ACK_MESSAGE + "\" sessionID=\"" + sessionID + "\"><service ID=\"" + serviceID + "\" name=\"" + serviceName + "\" activityID=\"" + activityID + "\" activityName=\"" + URLEncoder.encode(activityName, "UTF-8") + "\"><notification messageID=\"" + messageID + "\" messageAction=\"" + DEVICE_LOAD_MESSAGE + "\" success=\"" + success + "\" /></service></message>";
                ekgServiceAdapter.sendMessageToSavoir( savoirMsg);
            } else if (messageAction.equalsIgnoreCase(DEVICE_START_MESSAGE)) {
                String path = startDevice(sessionID, serviceID, serviceUserID, activityID, activityName);
                int success = (path != null && !path.equals("")) ? 1 : 0; //START was successful "1" or not "0"
                savoirMsg = "<message action=\"" + SAVOIR_START_MESSAGE + "\" sessionID=\"" + sessionID + "\"><service ID=\"" + serviceID + "\" name=\"" + serviceName + "\" activityID=\"" + activityID + "\" activityName=\"" + URLEncoder.encode(activityName, "UTF-8") + "\" path=\"" + path + "\"><notification messageID=\"" + messageID + "\" messageAction=\"" + DEVICE_START_MESSAGE + "\" success=\"" + success + "\" /></service></message>";
                System.out.println("Sent Start Response From VaderWeb::" + savoirMsg);
                ekgServiceAdapter.sendMessageToSavoir( savoirMsg);
                
            } else if (messageAction.equalsIgnoreCase(DEVICE_PARAMETER_MESSAGE)) {
                setParameterDevice(sessionID, msgString);
                String parameterID = this.getAttribute(msgString, "//message/service/activityParameters/activityParameter/@ID");
                String heartBeat = this.getAttribute(msgString, "//message/service/activityParameters/activityParameter/@value");
                String parameterStr = "[CID=" + parameterID +  ",V=" + heartBeat + "]";
                savoirMsg = "<message action=\"" + this.SAVOIR_ACK_MESSAGE + "\" sessionID=\"" + sessionID + "\"><service ID=\"" + EDGE_DEVICE_ID + "\" name=\"" + this.EDGE_DEVICE_NAME + "\" activityID=\"" + activityID + "\" activityName=\"" + URLEncoder.encode(activityName, "UTF-8") + "\"><activityParameters>" + parameterStr + "</activityParameters><notification messageID=\"" + messageID + "\" messageAction=\"" + DEVICE_PARAMETER_MESSAGE + "\" success=\"1\" /></service></message>";
                ekgServiceAdapter.sendMessageToSavoir(savoirMsg);
            }else if (messageAction.equalsIgnoreCase(this.DEVICE_STOP_MESSAGE)) {
                stopEKGTutorial(sessionID, msgString);
                savoirMsg = "<message action=\"" + this.SAVOIR_ACK_MESSAGE + "\" sessionID=\"" + sessionID + "\"><service ID=\"" + EDGE_DEVICE_ID + "\" name=\"" + this.EDGE_DEVICE_NAME + "\" activityID=\"" + activityID + "\" activityName=\"" + URLEncoder.encode(activityName, "UTF-8") + "\">" + "<notification messageID=\"" + messageID + "\" messageAction=\"" + DEVICE_STOP_MESSAGE + "\" success=\"1\" /></service></message>";
                ekgServiceAdapter.sendMessageToSavoir(savoirMsg);
            }else if (messageAction.equalsIgnoreCase(this.DEVICE_PAUSE_MESSAGE)) {
                stopEKGTutorial(sessionID, msgString);
                savoirMsg = "<message action=\"" + this.SAVOIR_ACK_MESSAGE + "\" sessionID=\"" + sessionID + "\"><service ID=\"" + EDGE_DEVICE_ID + "\" name=\"" + this.EDGE_DEVICE_NAME + "\" activityID=\"" + activityID + "\" activityName=\"" + URLEncoder.encode(activityName, "UTF-8") + "\">" + "<notification messageID=\"" + messageID + "\" messageAction=\"" + DEVICE_PAUSE_MESSAGE + "\" success=\"1\" /></service></message>";
                ekgServiceAdapter.sendMessageToSavoir(savoirMsg);
            }else if (messageAction.equalsIgnoreCase(this.DEVICE_RESUME_MESSAGE)) {
                resumeEKGTutorial(sessionID, msgString);
                savoirMsg = "<message action=\"" + this.SAVOIR_ACK_MESSAGE + "\" sessionID=\"" + sessionID + "\"><service ID=\"" + EDGE_DEVICE_ID + "\" name=\"" + this.EDGE_DEVICE_NAME + "\" activityID=\"" + activityID + "\" activityName=\"" + URLEncoder.encode(activityName, "UTF-8") + "\">" + "<notification messageID=\"" + messageID + "\" messageAction=\"" + DEVICE_RESUME_MESSAGE + "\" success=\"1\" /></service></message>";
                ekgServiceAdapter.sendMessageToSavoir(savoirMsg);
            }else if (messageAction.equalsIgnoreCase(this.DEVICE_END_MESSAGE)) {
                endEKGTutorial(sessionID, msgString);
                savoirMsg = "<message action=\"" + this.SAVOIR_ACK_MESSAGE + "\" sessionID=\"" + sessionID + "\"><service ID=\"" + EDGE_DEVICE_ID + "\" name=\"" + this.EDGE_DEVICE_NAME + "\" activityID=\"" + activityID + "\" activityName=\"" + URLEncoder.encode(activityName, "UTF-8") + "\">" + "<notification messageID=\"" + messageID + "\" messageAction=\"" + DEVICE_END_MESSAGE + "\" success=\"1\" /></service></message>";
                ekgServiceAdapter.sendMessageToSavoir(savoirMsg);
            }else if (messageAction.equalsIgnoreCase(this.DEVICE_STATUS_MESSAGE)){
                savoirMsg = "<message action=\"'" +SAVOIR_STATUS_RESPONSE +"\" sessionID=\""+sessionID + "\"><service ID=\"" + EDGE_DEVICE_ID + "\" name=\"" + EDGE_DEVICE_NAME + "\" activityID=\"" + activityID + "\" activityName=\"" + activityName + "\"><activityParameters /><notification messageID=\"00\" messageAction=\"" + SAVOIR_STATUS_RESPONSE + "\" success=\"1\" /></service></message>";
                ekgServiceAdapter.sendMessageToSavoir(savoirMsg);
            }else {
//                logger.debug("Ignoring msg...");
                System.out.println("Ignoring msg...");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    // SERVER-SIDE: SAVOIR Device Message Handlers (messages sent before Client launch)
    /////////////////////////////////////////////////////////////////////////////////
    /**
     * authenticateDevice
     *  Authenticate a device
     *  NOTE: Secure Authentication may require significant reworking in the
     *        spec and Bus Interface, but BASIC authentication is accomplished
     *        by passing a username and password (ideally at least over SSL)
     * @param username String representing the serviceUserID from SAVOIR-NEP spec
     * @param password String representing the servicePassword from SAVOIR-NEP spec
     * @return authenticated boolean true/false authentication status
     * @throws IOException
     */
    private boolean authenticateDevice(String username, String password){
        boolean authenticated = true;
       
        return authenticated;
    }

    /**
     * startDevice
     *  Create a Start message for SAVOIR to launch the device (Bus Interface)
     *  by building and returning the path (endpoint) indicating WHERE to launch
     *  the Edge Device.
     *  NOTE: Because of the nature of the AJAX portion of the hybrid AJAX-JAVA
     *      Bus Interface, we must pass all the required data via the URL (path)
     * @param sessionID String SAVOIR's sessionID
     * @param serviceUserID String the Edge Device's User ID (serviceUserID)
     * @param activityID String passed in activityID (used to lookup any activityParameters required to build the launch path)
     * @return path String endpoint to tell SAVOIR to launch via a startResponse
     */
    private String startDevice(String sessionID, String serviceID, String serviceUserID, String activityID, String activityName) {
        //add later for the front end flex page to load
        String encodedActName = "";
        try{
            encodedActName = URLEncoder.encode(activityName, "UTF-8");
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return ekgServiceAdapter.getEKGClientPath(sessionID, encodedActName);
    }

    /**
     * loadDevice
     *   Checks whether or not the device has been loaded (or, is ready to be
     *   run).
     * @return true/false boolean specifying success or failure of device load
     */
    private boolean loadDevice() throws HttpException, IOException {
        // Create an instance of HttpClient.
       

        return true;
    }

    /**
     * setParameterDevice
     *  Set the specified parameter(s) to the values passed in via
     *  the setParameter message
     * @param sessionID
     * @param xmlMsg
     * @throws HttpException
     * @throws IOException
     */
    private void setParameterDevice(String sessionID, String xmlMsg) throws HttpException, IOException {
       this.ekgServiceAdapter.sendMessageToClient(xmlMsg);
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * setHeaders
     *  set headers for an HTTP request
     * @param method
     * @return method HttpMethod
     */
    private void setHeaders(HttpMethod method) {
//      method.setRequestHeader(new Header("If-None-Match", entityTag));
        method.setRequestHeader(new Header("If-Modified-Since", lastModified));
        method.addRequestHeader(new Header("Accept-Encoding", "gzip"));
        method.addRequestHeader(new Header("User-Agent", "" + EDGE_DEVICE_NAME + "/2.0 (http://142.51.75.11/" + EDGE_DEVICE_NAME + "/)"));
    }

    /**
     * loadFileAsString
     *  load a File as a String
     *  (used to load the cached Device Profile XML)
     * @param filePath
     * @return buffer String
     * @throws java.io.IOException
     */
    public String loadFileAsString(String filePath) throws IOException {
        byte[] buffer = new byte[(int) new File(filePath).length()];
        BufferedInputStream f = new BufferedInputStream(new FileInputStream(filePath));
        f.read(buffer);

        return new String(buffer);
    }

    /**
     * getAttribute
     *  gets an attribute from an XML Message string via XPath expression
     * @param msg String XML Message (or Element as String) to get attribute of
     * @param xpathExpr String XPath Expression to apply to get the attribute
     * @return result String value of the attribute
     */
    public String getAttribute(String msg, String xpathExpr) throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource xml = new InputSource();
        xml.setCharacterStream(new StringReader(msg));
        Document dom = db.parse(xml);

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile(xpathExpr);

        Object result = expr.evaluate(dom, XPathConstants.STRING);
        //DEBUG
//            System.out.println((String) result);

        return (String) result;
    }

    /**
     * dateToLong
     *  Convert a typical server timestamp to a long representation
     * EXAMPLE:
     *      dateToLong("1 Sep 2010 17:00:00 GMT") should return 1283360400000
     * @param dateString Date string with the format "dd MMM yyyy hh:mm:ss zzz"
     * @return longDate long (time since epoch) representation of a date
     * @throws ParseException
     */
    public long dateToLong(String timestamp, String format) throws ParseException {
      long longDate = 0;
      String serverDateTimeFormat = (format != null && !format.equals("")) ? format : "dd MMM yyyy hh:mm:ss zzz";
      String d = timestamp;
      if (d != null && !d.equals("")) {
          DateFormat formatter = new SimpleDateFormat(serverDateTimeFormat);
          Date date = (Date) formatter.parse(d);
          longDate = date.getTime();
          // logger.info("Date as long: " +longDate);
          System.out.println("Date as long: " +longDate);
      }
      return longDate;
    }

    public void stopEKGTutorial(String sessionID, String xmlMsg){
       this.ekgServiceAdapter.sendMessageToClient(xmlMsg);
    }

    public void resumeEKGTutorial(String sessionID, String xmlMsg){
       this.ekgServiceAdapter.sendMessageToClient(xmlMsg);
    }

    public void endEKGTutorial(String sessionID, String xmlMsg){
       this.ekgServiceAdapter.sendMessageToClient(xmlMsg);
    }


}
