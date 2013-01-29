// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy;

import socketproxy.systemtray.SystemTrayMsgListener;
import java.util.List;

import ca.gc.nrc.iit.savoir.message.bindings.Message;
import ca.gc.nrc.iit.savoir.message.bindings.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.jms.JMSException;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * SystemTrayMsgListenerTest.
 *  Invoke the SystemTray JMS Messaging (queue/topic) listener for testing push messages
 * @author Bryan Copeland 09/12/09
 * @version 2.0
 */
public class SystemTrayMsgListenerTest extends TestCase
{
    /*
     * SystemTrayMsgListenerTest constructor
     */
    public SystemTrayMsgListenerTest(String msg)
    {
       super(msg);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {    }

    @AfterClass
    public static void tearDownClass() throws Exception {    }

    @Before
    public void setUp() {    }

    @After
    public void tearDown() {    }

    /**
     * Test of iniMsgListener method, of class SystemTrayMsgListener.
     */
    @Test(expected=NoClassDefFoundError.class)
    public void testIniMsgListener() throws Exception
    {
        System.out.println("iniMsgListener");
        String msg = "";
        String xml = "";
        final String ACTION = "launch";
        final int SESSION_ID = 0;
        final String RESOURCE_ID = "notepad";

        try
        {
           SystemTrayMsgListener instance = new SystemTrayMsgListener();
//           instance.iniMsgListener(TEST_SESSION);
           Message testmsg = new Message();
           /********************************************************************
            * we create the following message using the JAXB Message Binding:
            * 
            *    <message action="launch" sessionID="999">
            *       <resource id="notepad" endpoint="C:/WINDOWS/system32/notepad.exe\">
            *       </resource>
            *    </message>
            *******************************************************************/
           testmsg.setAction(ACTION);
           testmsg.setSessionID(Integer.toString(SESSION_ID));
           testmsg.setId(RESOURCE_ID);
           instance.setMessage(testmsg);           

           String action = instance.getMessage().getAction();
           String sessionID = instance.getMessage().getSessionID();
           String id = instance.getMessage().getId();

           System.out.println("ACTION: " + action);
           System.out.println("SESSION: " + sessionID);
           System.out.println("ID: " + id);
           List<Service> service = instance.getMessage().getService();
           while (service.listIterator().hasNext())
               System.out.println("SERVICE: " + service);
           
           xml = "<message action=\"launch\" sessionID=\"0\"><service id=\"notepad\"></service></message>";
           msg = instance.getMessage().toString();

           assertEquals(xml,msg);
        }
        catch (Exception jmsEx)
        {
          jmsEx.printStackTrace();
        }
        catch (NoClassDefFoundError noclassError)
        {
            noclassError.printStackTrace();
        }
    }

    /**
     * Test of onMessage method, of class SystemTrayMsgListener.
     */
    @Test(expected=NoClassDefFoundError.class)
    public void testOnMessage()
    {
        System.out.println("onMessage");        
        javax.jms.Message message = null;
        SystemTrayMsgListener instance = new SystemTrayMsgListener();
        instance.onMessage(message);
        assertNotNull(instance);
    }

    /**
     * Test of setMessage method, of class SystemTrayMsgListener.
     */
    @Test
    public void testSetMessage()
    {
        System.out.println("setMessage");
        ca.gc.nrc.iit.savoir.message.bindings.Message message = null;
        SystemTrayMsgListener instance = new SystemTrayMsgListener();
        instance.setMessage(message);
        assertNotNull(instance);
    }

    /**
     * Test of getMessage method, of class SystemTrayMsgListener.
     */
    @Test
    public void testGetMessage()
    {
        System.out.println("getMessage");
        SystemTrayMsgListener instance = new SystemTrayMsgListener();
        ca.gc.nrc.iit.savoir.message.bindings.Message expResult = null;
        ca.gc.nrc.iit.savoir.message.bindings.Message result = instance.getMessage();
        assertEquals(expResult, result);
    }



   public static String LOCAL_SOCKET_CONNECTION = "http://localhost:9000/?";
   public static String programName;
   public static String launchArgs;
   public static String downloadURL;
    public static final int TEST_SESSION = 999;

    public static void main(String[] args) throws Exception
    {
        if (args.length < 3)
        {
            programName = "RSV";
            launchArgs = "v3 138.49.134.235 27491 Hand-Multi";
            downloadURL = "";
          //find the name of the OS the Edge Device is trying to be run on
          String osName = System.getProperty("os.name");
           if (osName.startsWith("Windows"))
           {
               downloadURL = "http://visu.uwlax.edu/InfoTools/Win.zip";
           }
           else if (osName.startsWith("Mac"))
           {
               downloadURL = "http://visu.uwlax.edu/InfoTools/Mac.zip";
           }
        }
        else
        {
            programName = args[0];
            launchArgs = args[1];
            downloadURL = args[2];
        }
       try
       {
           URL url = new URL(LOCAL_SOCKET_CONNECTION + "programname=" + programName);
//                   + "&lanchargs=" + launchArgs
//                   + "&download=" + downloadURL);
           try
           {
               BufferedReader in = new BufferedReader(new
                       InputStreamReader(url.openStream()));
               String str;
               while ((str = in.readLine()) != null)
               {
                   System.out.println(str);
               }
               in.close();
            }
            catch (IOException ioEx) { ioEx.printStackTrace(); }
        }
        catch (MalformedURLException mEx)
        {
            mEx.printStackTrace();
        }
    }
}
