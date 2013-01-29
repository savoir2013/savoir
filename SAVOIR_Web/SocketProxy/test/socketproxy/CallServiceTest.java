// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import junit.framework.TestCase;

/**
 *
 * @author copelandb
 */
public class CallServiceTest extends TestCase  {

    public CallServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of CallService method, of class CallService.
     */
    @Test
    public void testCallService() {
        System.out.println("CallService");
        String serviceType = "REST";
        String endpoint = "http://www.flickr.com/services/rest/";
        String query = "?method=flickr.test.echo&format=rest&foo=bar&api_key=57d30be449caf9032b1027d468885a40";
        CallService instance = new CallService();
        String expResult = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<rsp stat=\"ok\">\n<method>flickr.test.echo</method>\n<format>rest</format>\n<foo>bar</foo>\n<api_key>57d30be449caf9032b1027d468885a40</api_key>\n<launchData>?method=flickr.test.echo&amp;format=rest&amp;foo=bar&amp;api_key=57d30be449caf9032b1027d468885a40</launchData>\n</rsp>\n";
        String result = instance.CallService(serviceType, endpoint, query);
        System.out.println("RESTful Web Service call RESULT:\n-------------------------------------------\n"+result+"\n\nEXPECTED RESULT:\n-------------------------------------------\n"+expResult+"\n\n\n");
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of callSOAP method, of class CallService.
     */
    @Test
    public void testCallSOAP() {
        System.out.println("callSOAP");
        String endpoint = "http://198.164.40.210:8080/SAVOIR_MgmtServices/services/UserManagerWS";
        String query = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:user=\"http://userMgmt.savoir.iit.nrc.gc.ca/\"> <soapenv:Body> <user:getUsers/> </soapenv:Body> </soapenv:Envelope>";
        CallService instance = new CallService();
        String expResult = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns1:getUsersResponse xmlns:ns1=\"http://userMgmt.savoir.iit.nrc.gc.ca/\"><UserIDName><userID>17</userID><givenName>test</givenName><surname></surname></UserIDName><UserIDName><userID>6</userID><givenName>BRYAN</givenName><surname>COPELAND</surname></UserIDName><UserIDName><userID>16</userID><givenName>RACHEL</givenName><surname>ELLAWAY</surname></UserIDName><UserIDName><userID>34</userID><givenName>Rachel</givenName><surname>Ellaway</surname></UserIDName><UserIDName><userID>56</userID><givenName>Herve</givenName><surname>Guy</surname></UserIDName><UserIDName><userID>1</userID><givenName>YOSRI</givenName><surname>HARZALLAH</surname></UserIDName><UserIDName><userID>5</userID><givenName>JUSTIN</givenName><surname>HICKEY</surname></UserIDName><UserIDName><userID>14</userID><givenName>Bobby</givenName><surname>Ho</surname></UserIDName><UserIDName><userID>37</userID><givenName>Bobby</givenName><surname>Ho</surname></UserIDName><UserIDName><userID>57</userID><givenName>j. Bassett</givenName><surname>Hound</surname></UserIDName><UserIDName><userID>7</userID><givenName>FERRER</givenName><surname>JORDI</surname></UserIDName><UserIDName><userID>3</userID><givenName>SANDY</givenName><surname>LIU</surname></UserIDName><UserIDName><userID>15</userID><givenName>JORDAN</givenName><surname>MACDONALD</surname></UserIDName><UserIDName><userID>8</userID><givenName>AARON</givenName><surname>MOSS</surname></UserIDName><UserIDName><userID>51</userID><givenName>alison</givenName><surname>P</surname></UserIDName><UserIDName><userID>4</userID><givenName>RENE</givenName><surname>RICHARD</surname></UserIDName><UserIDName><userID>2</userID><givenName>BRUCE</givenName><surname>SPENCER</surname></UserIDName><UserIDName><userID>59</userID><givenName>Bruce</givenName><surname>Spencer</surname></UserIDName><UserIDName><userID>58</userID><givenName>Julie</givenName><surname>Totten</surname></UserIDName><UserIDName><userID>11</userID><givenName>Hanxi</givenName><surname>Zhang</surname></UserIDName><UserIDName><userID>9</userID><givenName>martin</givenName><surname>brooks</surname></UserIDName><UserIDName><userID>24</userID><givenName>alison</givenName><surname>peek</surname></UserIDName></ns1:getUsersResponse></soap:Body></soap:Envelope>";
        String result = instance.callSOAP(endpoint, query);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of callSOAPFromFile method, of class CallService.
     */
    @Test
    public void testCallSOAPFromFile() {
        System.out.println("callSOAPFromFile");
        String endpoint = "http://198.164.40.210:8080/SAVOIR_MgmtServices/services/UserManagerWS";
        String file = "C:\\workspace\\SocketProxy\\test\\socketproxy\\getUsers.xml";
        CallService instance = new CallService();
        String expResult = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns1:getUsersResponse xmlns:ns1=\"http://userMgmt.savoir.iit.nrc.gc.ca/\"><UserIDName><userID>17</userID><givenName>test</givenName><surname></surname></UserIDName><UserIDName><userID>6</userID><givenName>BRYAN</givenName><surname>COPELAND</surname></UserIDName><UserIDName><userID>16</userID><givenName>RACHEL</givenName><surname>ELLAWAY</surname></UserIDName><UserIDName><userID>34</userID><givenName>Rachel</givenName><surname>Ellaway</surname></UserIDName><UserIDName><userID>56</userID><givenName>Herve</givenName><surname>Guy</surname></UserIDName><UserIDName><userID>1</userID><givenName>YOSRI</givenName><surname>HARZALLAH</surname></UserIDName><UserIDName><userID>5</userID><givenName>JUSTIN</givenName><surname>HICKEY</surname></UserIDName><UserIDName><userID>14</userID><givenName>Bobby</givenName><surname>Ho</surname></UserIDName><UserIDName><userID>37</userID><givenName>Bobby</givenName><surname>Ho</surname></UserIDName><UserIDName><userID>57</userID><givenName>j. Bassett</givenName><surname>Hound</surname></UserIDName><UserIDName><userID>7</userID><givenName>FERRER</givenName><surname>JORDI</surname></UserIDName><UserIDName><userID>3</userID><givenName>SANDY</givenName><surname>LIU</surname></UserIDName><UserIDName><userID>15</userID><givenName>JORDAN</givenName><surname>MACDONALD</surname></UserIDName><UserIDName><userID>8</userID><givenName>AARON</givenName><surname>MOSS</surname></UserIDName><UserIDName><userID>51</userID><givenName>alison</givenName><surname>P</surname></UserIDName><UserIDName><userID>4</userID><givenName>RENE</givenName><surname>RICHARD</surname></UserIDName><UserIDName><userID>2</userID><givenName>BRUCE</givenName><surname>SPENCER</surname></UserIDName><UserIDName><userID>59</userID><givenName>Bruce</givenName><surname>Spencer</surname></UserIDName><UserIDName><userID>58</userID><givenName>Julie</givenName><surname>Totten</surname></UserIDName><UserIDName><userID>11</userID><givenName>Hanxi</givenName><surname>Zhang</surname></UserIDName><UserIDName><userID>9</userID><givenName>martin</givenName><surname>brooks</surname></UserIDName><UserIDName><userID>24</userID><givenName>alison</givenName><surname>peek</surname></UserIDName></ns1:getUsersResponse></soap:Body></soap:Envelope>";
        String result = instance.callSOAPFromFile(endpoint, file);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of callREST method, of class CallService.
     */
    @Test
    public void testCallREST() {
        System.out.println("callREST");
        String endpoint = "http://www.flickr.com/services/rest/";
        String query = "?method=flickr.test.echo&format=rest&foo=bar&api_key=57d30be449caf9032b1027d468885a40";
        CallService instance = new CallService();
        String expResult = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<rsp stat=\"ok\">\n<method>flickr.test.echo</method>\n<format>rest</format>\n<foo>bar</foo>\n<api_key>57d30be449caf9032b1027d468885a40</api_key>\n<launchData>?method=flickr.test.echo&amp;format=rest&amp;foo=bar&amp;api_key=57d30be449caf9032b1027d468885a40</launchData>\n</rsp>\n";
        String result = instance.callREST(endpoint, query);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of copy method, of class CallService.
     */
    @Test
    public void testCopy() throws Exception {
        System.out.println("copy");        
        FileInputStream in = new FileInputStream("C:\\workspace\\SocketProxy\\test\\socketproxy\\getUsers.xml");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Copy the SOAP file to the open connection.
        CallService.copy(in, out);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

}
