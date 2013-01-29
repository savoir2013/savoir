// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy;

import socketproxy.systemtray.SystemTrayManager;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * SystemTrayManagerTest.
 *  Invoke the SystemTray Manager for testing notification system, etc...
 * @author Bryan Copeland 09/09/09
 * @version 2.0
 */
public class SystemTrayManagerTest extends TestCase
{

    public SystemTrayManagerTest(String tray)
    {
        super(tray);
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
     * Test of getInstance method, of class SystemTrayManager.
     */
    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
        SystemTrayManager result = SystemTrayManager.getInstance();
        assertNotNull(result);
    }

    /**
     * Test of loadMenuProperties method, of class SystemTrayManager.
     */
    @Test
    public void testLoadMenuProperties() {
        System.out.println("loadMenuProperties");
       // SystemTrayManager instance = new SystemTrayManager();
       // instance.loadMenuProperties();
       // assertNotNull(instance);
    }

    /**
     * Test of setSocketServer method, of class SystemTrayManager.
     */
    @Test
    public void testSetSocketServer() {
        System.out.println("setSocketServer");
        //SocketServer server = null;
        //SystemTrayManager instance = new SystemTrayManager();
        //instance.setSocketServer(server);
        //assertNotNull(instance);
    }

    /**
     * Test of showInfoMessage method, of class SystemTrayManager.
     */
    @Test
    public void testShowInfoMessage() {
        System.out.println("showInfoMessage");
        String message = "";
        //SystemTrayManager instance = new SystemTrayManager();
        //instance.showInfoMessage(message);
        //assertNotNull(instance);
    }

    /**
     * Test of showErrorMessage method, of class SystemTrayManager.
     */
    @Test
    public void testShowErrorMessage() {
        System.out.println("showErrorMessage");
        String message = "";
        //SystemTrayManager instance = new SystemTrayManager();
        //instance.showErrorMessage(message);
        ///assertNotNull(instance);
    }

    /**
     * Test of showWarningMessage method, of class SystemTrayManager.
     */
    @Test
    public void testShowWarningMessage() {
        System.out.println("showWarningMessage");
        String message = "";
        //SystemTrayManager instance = new SystemTrayManager();
        //instance.showWarningMessage(message);
        //assertNotNull(instance);
    }


    
    public static void main(String[] args) throws Exception
    {
        //SystemTrayManager systray = new SystemTrayManager(); //initialize the SystemTray (note: this test requires the SystemTray to be "public", but in live deployment should be "private")

        if (args.length == 1)
        {
            //systray.showInfoMessage(args[0]);
            Thread.sleep(5000);
            //systray.showWarningMessage(args[0]);
            Thread.sleep(5000);
            //systray.showErrorMessage(args[0]);
        }
        else
        {
            System.err.println("Expected an argument to pass to each SystemTray notification type... \n\nUsing defaults instead:");
            /*Run some simple tests such as showing messages, warnings, errors */
            //systray.showInfoMessage("A-Ok");
            Thread.sleep(7500);
            //systray.showWarningMessage("Something's not quite right...");
            Thread.sleep(7500);
            //systray.showErrorMessage("Something is quite wrong!");
        }
    }
}
