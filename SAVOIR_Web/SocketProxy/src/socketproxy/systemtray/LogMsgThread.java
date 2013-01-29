// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package socketproxy.systemtray;
import socketproxy.messages.LogMessage;

/**
 *
 * @author youy
 */
public class LogMsgThread extends Thread {

    public LogMsgThread() {
        super("LogMsgThread");
    }

    @Override
    public void run() {
        while (true) {
            if (!SystemTrayLogMsgListener.logMsgQueue.isEmpty()) {
                LogMessage sentMsg = (LogMessage) SystemTrayLogMsgListener.logMsgQueue.poll();
                if (sentMsg != null) {
                    sentMsg.send();
                }
            }
            try {
                Thread.sleep(100);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
