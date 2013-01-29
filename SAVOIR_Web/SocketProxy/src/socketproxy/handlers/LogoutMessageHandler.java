// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package socketproxy.handlers;

import merapi.handlers.MessageHandler;
import merapi.messages.IMessage;
import socketproxy.messages.LogoutMessage;
import socketproxy.systemtray.SystemTrayManager;
import merapi.Bridge;

/**
 *
 * @author youy
 */
public class LogoutMessageHandler extends MessageHandler {

    public LogoutMessageHandler() {
        super(LogoutMessage.LOGOUT);
    }

    @Override
    public void handleMessage(IMessage message) {
        //System.out.println("Received Message is " + message.getType());
        if (message instanceof LogoutMessage) {
            SystemTrayManager.getInstance().showErrorMessage("SystemTray will exit now!");
//            Bridge.close();
//            if(SystemTrayLogMsgListener.logMsgThrd.isAlive()){
//                SystemTrayLogMsgListener.logMsgThrd.stop();
//            }
//            if(SystemTrayMsgListener.initRespMsgThrd.isAlive()){
//                SystemTrayMsgListener.initRespMsgThrd.stop();
//            }
//            if(SystemTrayManager.heartbeatThrd.isAlive()){
//                SystemTrayManager.heartbeatThrd.heartbeatStopFlag = true;
//            }
            //System.gc();
            System.exit(0);
        }
    }
}
