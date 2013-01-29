// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package socketproxy.systemtray;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import java.util.concurrent.ConcurrentLinkedQueue;

import socketproxy.messages.LogMessage;

/**
 *
 * @author youy
 */
public class SystemTrayLogMsgListener implements MessageListener{

    
    static SystemTrayLogMsgListener instance = null;
    static ConcurrentLinkedQueue logMsgQueue =  new ConcurrentLinkedQueue();
    public static LogMsgThread logMsgThrd = new LogMsgThread();

    public static SystemTrayLogMsgListener getInstance(){
        if(instance != null){
            return instance;
        }else{
            instance = new SystemTrayLogMsgListener();
            return instance;
        }
    }

    public void onMessage(Message message) {
        try {
            System.out.println("Receiving Log Msg ID is " + message.getJMSMessageID());
            if (message instanceof TextMessage) {

                String xmlStr = ((TextMessage) message).getText();
                System.out.println(xmlStr);
                LogMessage logMsg = new LogMessage(LogMessage.LOGINFO);
                logMsg.logMessage = xmlStr;
                boolean res = logMsgQueue.add(logMsg);
                if(!res){
                    System.out.println("Log msg has been failed to add into buffer!");
                }
                if(!logMsgThrd.isAlive()){
                    logMsgThrd.start();
                }
//                logMsg.send();
//                Thread.sleep(100);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
