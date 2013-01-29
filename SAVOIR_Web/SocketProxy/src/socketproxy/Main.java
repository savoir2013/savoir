// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy;
import merapi.PolicyServer;
import merapi.Bridge;
import socketproxy.handlers.InitListenerMessageHandler;
import socketproxy.handlers.LaunchMessageHandler;
import socketproxy.handlers.HeartbeatMessageHandler;
import socketproxy.messages.InitListenerMessage;
import socketproxy.handlers.LogoutMessageHandler;
import socketproxy.systemtray.InitListenerThread;
/**
 * Run the SystemTray directly from command-line or IDE (ideal for testing)
 * @author youy
 * @author copelandb
 */
public class Main
{
        /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {

        PolicyServer ps=new PolicyServer(12345, null);
		ps.start();
		while (ps.isAlive()) {
			try {
				Thread.sleep(100);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			// System.out.println(ps.getId());
		}
//        try {
//            Thread.sleep(500);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
		Bridge.open();
        new InitListenerMessageHandler();
        new LaunchMessageHandler();
        new HeartbeatMessageHandler();
        new LogoutMessageHandler();
//        InitListenerMessage initMsg = new InitListenerMessage(InitListenerMessage.INITLISTENER);
//        initMsg.success = false;
////            System.out.println("initMsg success = " + initMsg.success);
//
//        try {
//
////                listener.iniMsgListener(initMsg.sessionID, initMsg.userSessionID,
////                        initMsg.url, initMsg.isListenLogInfo,initMsg.server);
////                Thread.sleep(100);
//            initMsg.send();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }

    }

}
