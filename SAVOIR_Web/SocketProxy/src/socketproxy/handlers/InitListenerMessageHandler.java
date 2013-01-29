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
import socketproxy.messages.InitListenerMessage;
import socketproxy.systemtray.SystemTrayMsgListener;
import socketproxy.systemtray.InitListenerThread;
/**
 *
 * @author youy
 */
public class InitListenerMessageHandler extends MessageHandler{

    private SystemTrayMsgListener listener;
    public InitListenerMessageHandler(){
        super(InitListenerMessage.INITLISTENER);
        listener = SystemTrayMsgListener.getInstance();
        System.out.println("Init Listener Handler Created");
    }


    @Override 
    public void handleMessage(IMessage message){
        System.out.println("Received Message is" + message.getType());
        if (message instanceof InitListenerMessage) {
            InitListenerMessage initMsg = (InitListenerMessage) message;
            initMsg.success = true;
//            System.out.println("initMsg success = " + initMsg.success);
            
            try {

//                listener.iniMsgListener(initMsg.sessionID, initMsg.userSessionID,
//                        initMsg.url, initMsg.isListenLogInfo,initMsg.server);
//                Thread.sleep(100);
                InitListenerThread initThrd = new InitListenerThread();
                initThrd.setInitParas(initMsg);
                initThrd.start();
            } catch (Exception ex) {
                ex.printStackTrace();
//                initMsg.success = false;
//                initMsg.send();
            }
           initMsg.send();
        }
    }
}
