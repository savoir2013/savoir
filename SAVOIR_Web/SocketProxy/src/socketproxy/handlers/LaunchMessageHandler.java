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
import socketproxy.messages.LaunchMessage;
import socketproxy.systemtray.SystemTrayManager;
import socketproxy.LaunchHandler;

/**
 *
 * @author youy
 */
public class LaunchMessageHandler extends MessageHandler{
    private  SystemTrayManager trayManager;

    

    public LaunchMessageHandler(){
        super(LaunchMessage.LAUNCH);
        trayManager = SystemTrayManager.getInstance();
        
    }

    public void handleMessage(IMessage message){
        if(message instanceof LaunchMessage){
            LaunchMessage launchMsg = (LaunchMessage) message;
            LaunchHandler.launch(launchMsg.launchURI);
        }
    }
}
