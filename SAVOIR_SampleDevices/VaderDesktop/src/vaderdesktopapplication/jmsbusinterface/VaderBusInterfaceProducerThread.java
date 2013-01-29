// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package vaderdesktopapplication.jmsbusinterface;


import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.util.IndentPrinter;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A simple tool for publishing messages
 * 
 * @version $Revision: 1.2 $
 */
public class VaderBusInterfaceProducerThread extends Thread {

    private Destination destination;
    private int messageCount = 10;
    private long sleepTime = 100;
    private long threadSleepTime = 100;
    private boolean verbose = true;
    private int messageSize = 255;
    private static int parallelThreads = 1;
    private long timeToLive = 0;
    private String user = ActiveMQConnection.DEFAULT_USER;
    private String password = ActiveMQConnection.DEFAULT_PASSWORD;
    private String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    private String subject = "TOOL.DEFAULT";
    private boolean topic = true;
    private boolean transacted;
    private boolean persistent = false;
    private static Object lockResults = new Object();
    private Connection connection;
    private  ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
    private Session session;
    private MessageProducer producer;
    public boolean running = false;

//    public static void main(String[] args) {
//        ArrayList<VaderBusInterfaceProducerThread> threads = new ArrayList();
//        VaderBusInterfaceProducerThread producerTool = new VaderBusInterfaceProducerThread();
//        String[] unknown = CommandLineSupport.setOptions(producerTool, args);
//        if (unknown.length > 0) {
//            System.out.println("Unknown options: " + Arrays.toString(unknown));
//            System.exit(-1);
//        }
//        producerTool.showParameters();
//        for (int threadCount = 1; threadCount <= parallelThreads; threadCount++) {
//            producerTool = new VaderBusInterfaceProducerThread();
//            CommandLineSupport.setOptions(producerTool, args);
//            producerTool.start();
//            threads.add(producerTool);
//        }
//
//        while (true) {
//            Iterator<VaderBusInterfaceProducerThread> itr = threads.iterator();
//            int running = 0;
//            while (itr.hasNext()) {
//                VaderBusInterfaceProducerThread thread = itr.next();
//                if (thread.isAlive()) {
//                    running++;
//                }
//            }
//            if (running <= 0) {
//                System.out.println("All threads completed their work");
//                break;
//            }
//            try {
//                Thread.sleep(1000);
//            } catch (Exception e) {
//            }
//        }
//    }

    public void initProducer(String theUrl, String topic){
        url =  theUrl;
        subject = topic;

    }

    public void showParameters() {
        System.out.println("Connecting to URL: " + url);
        System.out.println("Publishing a Message with size " + messageSize + " to " + (topic ? "topic" : "queue") + ": " + subject);
        System.out.println("Using " + (persistent ? "persistent" : "non-persistent") + " messages");
        System.out.println("Sleeping between publish " + sleepTime + " ms");
        System.out.println("Running " + parallelThreads + " parallel threads");

        if (timeToLive != 0) {
            System.out.println("Messages time to live " + timeToLive + " ms");
        }
    }

    public void run() {
        connection = null;
        try {
            // Create the connection.
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory( url);
            connection = connectionFactory.createConnection();
            connection.start();

            // Create the session
            session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
            if (topic) {
                destination = session.createTopic(subject);
            } else {
                destination = session.createQueue(subject);
            }

            // Create the producer.
            producer = session.createProducer(destination);
            if (persistent) {
                producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            } else {
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            }
            if (timeToLive != 0) {
                producer.setTimeToLive(timeToLive);
            }

            // Start sending messages
            while (running) {
                sendLoop(session, producer);
                Thread.sleep(threadSleepTime);
            }
            System.out.println("[" + this.getName() + "] Done.");

//            synchronized (lockResults) {
//                ActiveMQConnection c = (ActiveMQConnection) connection;
//                System.out.println("[" + this.getName() + "] Results:\n");
//                c.getConnectionStats().dump(new IndentPrinter());
//            }

        } catch (Exception e) {
            System.out.println("[" + this.getName() + "] Caught: " + e);
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (Throwable ignore) {
            }
        }
    }

    protected void sendLoop(Session session, MessageProducer producer) throws Exception {

        messageCount = this.queue.size();
        while (!this.queue.isEmpty()) {

            TextMessage message = session.createTextMessage(this.queue.poll());

            if (verbose) {
                String msg = message.getText();
//                if (msg.length() > 50) {
//                    msg = msg.substring(0, 50) + "...";
//                }
                System.out.println("[" + this.getName() + "] Sending message: '" + msg + "'");
            }

            producer.send(message);

            if (transacted) {
                System.out.println("[" + this.getName() + "] Committing " + messageCount + " messages");
                session.commit();
            }
            Thread.sleep(sleepTime);
        }
    }

//    private String createMessageText(int index) {
//        StringBuffer buffer = new StringBuffer(messageSize);
//        buffer.append("Message: " + index + " sent at: " + new Date());
//        if (buffer.length() > messageSize) {
//            return buffer.substring(0, messageSize);
//        }
//        for (int i = buffer.length(); i < messageSize; i++) {
//            buffer.append(' ');
//        }
//        return buffer.toString();
//    }

    public void setPersistent(boolean durable) {
        this.persistent = durable;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public void setMessageSize(int messageSize) {
        this.messageSize = messageSize;
    }

    public void setPassword(String pwd) {
        this.password = pwd;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    public void setParallelThreads(int parallelThreads) {
        if (parallelThreads < 1) {
            parallelThreads = 1;
        }
        this.parallelThreads = parallelThreads;
    }

    public void setTopic(boolean topic) {
        this.topic = topic;
    }

    public void setQueue(boolean queue) {
        this.topic = !queue;
    }

    public void setTransacted(boolean transacted) {
        this.transacted = transacted;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean terminateConnenction(){
        if(connection != null){
            try{
                this.producer.close();
                this.session.close();
             connection.stop();
             connection.close();
            }catch(Exception ex){
                ex.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    public void addMsgIntoQueue(String Msg){
        this.queue.add(Msg);
    }

    public void sendMessageToSavoir(String msg){
        this.addMsgIntoQueue(msg);
    }
}
