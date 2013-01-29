// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author copelandb
 */
public class SocketProxyTestSuite extends TestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(CallServiceTest.class);
        suite.addTestSuite(ClientInfoTest.class);
        suite.addTestSuite(DownloadTest.class);
        suite.addTestSuite(LaunchBrowserTest.class);
        suite.addTestSuite(LaunchDesktopTest.class);
        suite.addTestSuite(LaunchTest.class);
        suite.addTestSuite(RESTClientTest.class);
        suite.addTestSuite(SOAPClientTest.class);
        suite.addTestSuite(SOAPFromFileClientTest.class);
        suite.addTestSuite(SocketServerTest.class);
        suite.addTestSuite(SocketServerThreadTest.class);
        suite.addTestSuite(SystemTrayManagerTest.class);
        suite.addTestSuite(SystemTrayMsgListenerTest.class);
        return suite;
    }
}
