// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package socketproxy.systemtray;
import socketproxy.messages.InitListenerMessage;
/**
 *
 * @author youy
 */
public class InitListenerRespThread extends Thread{
    public InitListenerRespThread(){
        super("InitListenerRespThread");
    }

    @Override
    public void run() {
        while (true) {
            if (!SystemTrayMsgListener.initRespMsgQueue.isEmpty()) {
                InitListenerMessage sentMsg = (InitListenerMessage) SystemTrayMsgListener.initRespMsgQueue.poll();
                if (sentMsg != null) {
                    sentMsg.send();
                    System.out.println("Send back init resp msg with res = " + sentMsg.success);
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
