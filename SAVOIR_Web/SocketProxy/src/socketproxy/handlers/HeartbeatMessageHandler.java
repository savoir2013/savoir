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
import socketproxy.messages.HeartbeatMessage;
import socketproxy.systemtray.SystemTrayManager;
/**
 *
 * @author youy
 */
public class HeartbeatMessageHandler extends MessageHandler{

    public HeartbeatMessageHandler(){
        super(HeartbeatMessage.HEARTBEAT);
    }

    @Override
    public void handleMessage(IMessage message){
        //System.out.println("Received Message is " + message.getType());
        if (message instanceof HeartbeatMessage) {
            //HeartbeatMessage heartBeatMsg = (HeartbeatMessage) message;
            //System.out.println("Received heartbeat message!");
            SystemTrayManager.getInstance().decreaseOneOfheartbeatCounter();
            //System.out.println("Received heartbeat message!" );
            if(!SystemTrayManager.heartbeatThrd.isAlive()){
                SystemTrayManager.heartbeatThrd.start();
            }
            //heartBeatMsg.message = "Are you still there!";
            //heartBeatMsg.send();
        }
    }

}
