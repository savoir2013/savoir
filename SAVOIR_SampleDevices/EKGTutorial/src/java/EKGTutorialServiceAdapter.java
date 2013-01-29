// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author yonghuayou
 */
import java.io.*;
import java.util.*;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;
import flex.messaging.services.MessageService;
import flex.messaging.services.ServiceAdapter;
import flex.messaging.util.UUIDUtils;

public class EKGTutorialServiceAdapter extends ServiceAdapter {
private static EKGTutorialBusInterfaceConsumerThread consumerThread ;
private static EKGTutorialBusInterfaceProducerThread producerThread;
private static EKGTutorialService ekgService;
private static String url;
private static String topicToSavoir;
private static String topicFromSavoir;
private static String EKGClientPath;
private static String EKGProfileFile;
private ResourceBundle resource = ResourceBundle.getBundle("EKGTutorialBI",Locale.getDefault());
    
    //If thread is null, allocate memory to thread, and call its start method
 private class MessageLoop implements Runnable {
        public void run() {
            String importantInfo[] = {
                "Mares eat oats",
                "Does eat oats",
                "Little lambs eat ivy",
                "A kid will eat ivy too"
            };
            try {
                while (true) {
                    //Pause for 4 seconds
                    Thread.sleep(4000);
                    //Print a message
                    sendMessageToClient(importantInfo[0]);
                }
            } catch (InterruptedException e) {
                sendMessageToClient("I wasn't done!");
            }
        }
    }

    @Override
    public void start() {
        super.start();
        url = resource.getString("SavoirJMSURL");
        topicToSavoir = resource.getString("topicToSavoir");
        topicFromSavoir = resource.getString("topicFromSavoir");
        EKGClientPath = resource.getString("EKGClientPath");
        EKGProfileFile = resource.getString("EKGProfileFilePath");
        System.out.println("Url:" + url +"\n" + "toSavoir:" + topicToSavoir + "\n" + "fromSavoir:" + topicFromSavoir + "EKGClientPath:" + EKGClientPath);
//        try {
//            File f = new File("..\\EKGTutorialBI.properties");
//            if (f.exists()) {
//                Properties pro = new Properties();
//                FileInputStream in = new FileInputStream(f);
//                pro.load(in);
//                System.out.println("All key are given: " + pro.keySet());
//
//                url = pro.getProperty("SavoirJMSURL");
//                topicToSavoir = pro.getProperty("topicToSavoir");
//                topicFromSavoir = pro.getProperty("topicFromSavoir");
//
//            } else {
//
//                System.out.println("File not found!");
//            }
//        } catch (IOException ex) {
//            System.out.println(ex.toString());
//        }
        if(ekgService == null){
            ekgService =  new EKGTutorialService();
            ekgService.initService(this);
        }
        if (consumerThread == null) {
            consumerThread = new EKGTutorialBusInterfaceConsumerThread();
            consumerThread.initConsumer(url, topicFromSavoir, this, ekgService);
            consumerThread.start();
        }
        if(producerThread ==  null){
            producerThread = new EKGTutorialBusInterfaceProducerThread();
            producerThread.initProducer(url, topicToSavoir);
            producerThread.start();
        }
//        Thread t = new Thread(new MessageLoop());
//        t.start();

        
    }
    @Override
    public void stop(){
        if (consumerThread != null) {
            consumerThread.terminateConnenction();
        }
        if(producerThread != null){
            producerThread.running = false;
            producerThread.terminateConnenction();
        }
    }
    public static void main(String arg[]){
        EKGTutorialServiceAdapter busInterface = new EKGTutorialServiceAdapter();
        busInterface.start();
    }

    @Override
    public Object invoke(Message msg) {
        //throw new UnsupportedOperationException("Not supported yet.");
        String msgFromClient = (String)(msg.getBody());
        System.out.println("Savoir EKG Web Message:" + msgFromClient);
        this.sendMessageToSavoir(msgFromClient);
        return null;
    }

    public void sendMessageToClient(String msg){
        Message pushMsg = this.createMessage(msg);
        ((MessageService)this.getDestination().getService()).pushMessageToClients(pushMsg, false);
    }

    public void sendMessageToSavoir(String msg){
        producerThread.addMsgIntoQueue(msg);
    }

    public String getEKGClientPath(String sessionIDStr, String activityName){
        return this.EKGClientPath + "#sessionID=" + sessionIDStr + ";activityName=" + activityName;
    }

    public String getEKGProfileFile(){
        return this.EKGProfileFile;
    }

    private Message createMessage(String msg){
        String clientID = UUIDUtils.createUUID();
        final AsyncMessage pushMsg = new AsyncMessage();
        pushMsg.setDestination("ekgTutorialSADestination");
        pushMsg.setClientId(clientID);
        pushMsg.setMessageId(UUIDUtils.createUUID());
        pushMsg.setTimestamp(System.currentTimeMillis());
        pushMsg.setBody(msg);


        return pushMsg;
    }

    


}
