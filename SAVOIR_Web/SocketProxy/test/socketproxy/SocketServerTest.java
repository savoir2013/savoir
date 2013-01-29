// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * SocketServerTest
 *   Test the actual Service which opens up a port on the local host
 * NOTE:
 *   Later, this should provide test cases for:          STATUS
 *    - Starting/Stopping,                                 y
 *    - Faults/Error recovery,                             --
 *    - Security (SSL)                                     n
 *    - Security (Certificates)                            n
 *    - Authentication                                     n
 *    - Authorization                                      n
 * @author copelandb
 */
public class SocketServerTest extends TestCase 
{
//    private static SocketServer server = new SocketServer();

    public SocketServerTest(String port)
    {
        super(port);
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }

    /**
     * Test of startSocketServer method, of class SocketServer.
     */
    @Test
    public static void StartSocketServer()
    {
        System.out.println("startSocketServer");
//        server.startSocketSever();
//        assertTrue(server.listening);
    }

    /**
     * Test of stopSocketServer method, of class SocketServer.
     */
    @Test
    public static void StopSocketServer()
    {
        System.out.println("stopSocketServer");
//        server.stopSocketServer();
//        assertFalse(server.listening);
    }

    /**
     * Test of startLoginPage method, of class SocketServer.
     */
    @Test
    public static void testStartLoginPage()
    {
        System.out.println("startLoginPage");
//        server.startLoginPage();
//        assertNotNull(server);
    }

    /**
     * Test of startSystemTray method, of class SocketServer.
     */
    @Test
    public static void StartSystemTray()
    {
//        System.out.println("startSystemTray");
//        server.startSystemTray();
        //assertEquals(server.,9000);
    }



    /* What happens if we Start then Stop right away? (prefix with "test" to include in Coverage tests)*/
    @Test
    public static void StartStop()
    {
        StartSocketServer();
        Wait(5000);
        StopSocketServer();
        //assertNull(server.port);
    }

    @Test
    /* What happens if we Stop before its started? */
    public static void StopStart()
    {
        StopSocketServer();
        Wait(5000);
        StartSocketServer();
        //assertNull(server.port);
    }

    /* Generic Wait method to pause between Start/Stop and other method calls */
    @Test
    public static void Wait(int t)
    {
        try
        {
            Thread.sleep(t);
        }
        catch(InterruptedException intEx)
        {
            intEx.printStackTrace();
        }
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        StartStop();   // SUCCEEDS
//        StopStart(); // FAILS
    }
}
