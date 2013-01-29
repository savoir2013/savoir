// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package socketproxy.systemtray;
//import socketproxy.messages.InitListenerRespMessage;
import socketproxy.messages.InitListenerMessage;
/**
 *
 * @author youy
 */
public class InitListenerThread extends Thread{
    private InitListenerMessage initParas;
    public InitListenerThread(){
        super("InitListenerThread");
    }

    public void setInitParas(InitListenerMessage paras){
        initParas = paras;
    }
    @Override
    public void run() {
//        InitListenerMessage resMsg = new InitListenerMessage(InitListenerMessage.INITLISTENER);
        try {
            SystemTrayMsgListener.getInstance().iniMsgListener(initParas.sessionID, initParas.userSessionID, initParas.url, initParas.isListenLogInfo, initParas.server);
//            resMsg.success = true;
//            resMsg.send();
            
        } catch (Exception ex) {
            ex.printStackTrace();
//            resMsg.success = false;
        }
//        SystemTrayMsgListener.initRespMsgQueue.add(resMsg);
//        if(!SystemTrayMsgListener.initRespMsgThrd.isAlive()){
//            SystemTrayMsgListener.initRespMsgThrd.start();
//        }

        
    }

}
