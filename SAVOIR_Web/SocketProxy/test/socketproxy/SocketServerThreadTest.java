// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy;

import java.net.ServerSocket;
import java.net.Socket;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * SocketServerThreadTest.
 *   Should test
 *    - Thread state
 *    - Thread count
 *    - Deadlocks
 *    - Interruption/recovery
 * @author copelandb
 */
public class SocketServerThreadTest extends TestCase
{

    public SocketServerThreadTest() {
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
     * Test of run method, of class SocketServerThread.
     */
    @Test
    public void testRun()
    {
        System.out.println("run");
//        Socket socket = new Socket();
//        SocketServerThread server = new SocketServerThread(socket);
//        Thread.State t = server.getState();
//        assertNotNull(t);
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        Socket socket = new Socket();
//        SocketServerThread server = new SocketServerThread(socket);
//        server.start();
//        Thread.State t = server.getState();
//        System.out.print(t.toString());
    }
}
