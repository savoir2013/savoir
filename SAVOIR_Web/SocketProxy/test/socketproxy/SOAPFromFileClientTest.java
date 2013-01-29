// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy;

import junit.framework.TestCase;
import org.junit.Test;


/**
 * SOAPFromFileClientTest.
 *  Read the SOAP envelope file passed as the second parameter, pass it to the
 *  SOAP endpoint passed as the first parameter, and print out the SOAP envelope
 *  passed as a response.
 * @author Bryan Copeland 09/09/09
 * @author Michael Brennan 05/23/01
 * @author Bob DuCharme 03/09/01
 * @version 2.0
*/
public class SOAPFromFileClientTest extends TestCase
{
    String serviceURL = "";     //Web Service URL
    String soapXML = "";        //XML (File) to send
    String soapAction = "";     //SOAP Aciton (optional)
    
    /*
     * SOAPFromFileClientTest constructor
     * @param   serviceURL   URL of SOAP Endpoint to send request to.
     * @param   soapXML      A file with an XML document of the request.
     * @param   soapAction  (optional) Action header of SOAP Web Service call.
     */
    public SOAPFromFileClientTest (String file)
    {
        super(file);
    }

    public static void main(String[] args) throws Exception
    {
        String serviceURL = "";     //Web Service URL
        String soapXML = "";        //XML (File) to send
        String soapAction = "";     //SOAP Aciton (optional)

        if (args.length  > 2)
        {
            serviceURL = args[0];
            soapXML = args[1];
            soapAction = args[2];
        }
        else if (args.length == 2)
        {
            serviceURL = args[0];
            soapXML = args[1];
        }
        else
        {
            System.err.println("Usage:  java SOAPClient http://soapURL soapEnvelopefile.xml [SOAPAction]");
	    System.err.println("SOAPAction is optional... \n\nSample call:");
            serviceURL = "http://198.164.40.210:8080/SAVOIR_MgmtServices/services/UserManagerWS";
            soapXML = "C:\\workspace\\SocketProxy\\test\\socketproxy\\getUsers.xml";
        }
        
        CallService cs = new CallService();
        cs.callSOAPFromFile(serviceURL, soapXML);
    }

  @Test
  public void testCallSOAPFile()
  {
     serviceURL = "http://198.164.40.210:8080/SAVOIR_MgmtServices/services/UserManagerWS";
     soapXML = "C:\\workspace\\SocketProxy\\test\\socketproxy\\getUsers.xml";
     String expResult = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns1:getUsersResponse xmlns:ns1=\"http://userMgmt.savoir.iit.nrc.gc.ca/\"><UserIDName><userID>17</userID><givenName>test</givenName><surname></surname></UserIDName><UserIDName><userID>6</userID><givenName>BRYAN</givenName><surname>COPELAND</surname></UserIDName><UserIDName><userID>16</userID><givenName>RACHEL</givenName><surname>ELLAWAY</surname></UserIDName><UserIDName><userID>34</userID><givenName>Rachel</givenName><surname>Ellaway</surname></UserIDName><UserIDName><userID>56</userID><givenName>Herve</givenName><surname>Guy</surname></UserIDName><UserIDName><userID>1</userID><givenName>YOSRI</givenName><surname>HARZALLAH</surname></UserIDName><UserIDName><userID>5</userID><givenName>JUSTIN</givenName><surname>HICKEY</surname></UserIDName><UserIDName><userID>14</userID><givenName>Bobby</givenName><surname>Ho</surname></UserIDName><UserIDName><userID>37</userID><givenName>Bobby</givenName><surname>Ho</surname></UserIDName><UserIDName><userID>57</userID><givenName>j. Bassett</givenName><surname>Hound</surname></UserIDName><UserIDName><userID>7</userID><givenName>FERRER</givenName><surname>JORDI</surname></UserIDName><UserIDName><userID>3</userID><givenName>SANDY</givenName><surname>LIU</surname></UserIDName><UserIDName><userID>15</userID><givenName>JORDAN</givenName><surname>MACDONALD</surname></UserIDName><UserIDName><userID>8</userID><givenName>AARON</givenName><surname>MOSS</surname></UserIDName><UserIDName><userID>51</userID><givenName>alison</givenName><surname>P</surname></UserIDName><UserIDName><userID>4</userID><givenName>RENE</givenName><surname>RICHARD</surname></UserIDName><UserIDName><userID>2</userID><givenName>BRUCE</givenName><surname>SPENCER</surname></UserIDName><UserIDName><userID>59</userID><givenName>Bruce</givenName><surname>Spencer</surname></UserIDName><UserIDName><userID>58</userID><givenName>Julie</givenName><surname>Totten</surname></UserIDName><UserIDName><userID>11</userID><givenName>Hanxi</givenName><surname>Zhang</surname></UserIDName><UserIDName><userID>9</userID><givenName>martin</givenName><surname>brooks</surname></UserIDName><UserIDName><userID>24</userID><givenName>alison</givenName><surname>peek</surname></UserIDName></ns1:getUsersResponse></soap:Body></soap:Envelope>";
     CallService cs = new CallService();
     assertEquals(expResult, cs.callSOAPFromFile(serviceURL, soapXML));
  }
}
