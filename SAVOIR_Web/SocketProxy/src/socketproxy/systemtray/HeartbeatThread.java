// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package socketproxy.systemtray;
import socketproxy.messages.HeartbeatMessage;
import java.io.*;
import merapi.Bridge;
/**
 *
 * @author youy
 */
public class HeartbeatThread extends Thread {

    
    public HeartbeatThread() {
        super("HeartbeatThread");
    }

    @Override
    public void run() {
        while(true){
            //HeartbeatMessage heartbeatMsg = new HeartbeatMessage();
            //heartbeatMsg.message = "Are you still there!";
            //heartbeatMsg.send();

            SystemTrayManager.getInstance().increaseOneOfHeartbeatCounter();
            try {
                if (!SystemTrayManager.getInstance().isHeartbeatOk()) {
                    SystemTrayManager.getInstance().showErrorMessage("SystemTray lose the connection with the savoir page!\n SystemTray will exit now!");
                    Bridge.close();
                    Thread.sleep(2000);
                    System.gc();
                    System.exit(0);
                    
                    //Runtime.getRuntime().halt(0);
                }
                Thread.sleep(3000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
