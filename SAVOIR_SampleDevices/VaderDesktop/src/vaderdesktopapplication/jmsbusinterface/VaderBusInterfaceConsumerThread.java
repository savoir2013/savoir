// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package vaderdesktopapplication.jmsbusinterface;


import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * A simple tool for consuming messages
 * 
 * @version $Revision: 1.1.1.1 $
 */
public class VaderBusInterfaceConsumerThread extends Thread implements MessageListener, ExceptionListener {

    private boolean running;

    private Session session;
    private Destination destination;
    private MessageProducer replyProducer;

    private boolean pauseBeforeShutdown = false;
    private boolean verbose = true;
    private int maxiumMessages = 0;
    private static int parallelThreads = 1;
    private String subject = "TOOL.DEFAULT";
    private boolean topic = true;
    private String user = ActiveMQConnection.DEFAULT_USER;
    private String password = ActiveMQConnection.DEFAULT_PASSWORD;
    private String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    private boolean transacted = false;
    private boolean durable = false;
    private String clientId;
    private int ackMode = Session.AUTO_ACKNOWLEDGE;
    private String consumerName = "James";
    private long sleepTime;
    private long receiveTimeOut = 0;

    private Connection connection;
    private MessageConsumer consumer;
    
    private VaderMsgProcessor ekgController;

//    public static void main(String[] args) {
//        ArrayList<VaderBusInterfaceConsumerThread> threads = new ArrayList();
//        VaderBusInterfaceConsumerThread consumerTool = new VaderBusInterfaceConsumerThread();
//        String[] unknown = CommandLineSupport.setOptions(consumerTool, args);
//        if (unknown.length > 0) {
//            System.out.println("Unknown options: " + Arrays.toString(unknown));
//            System.exit(-1);
//        }
//        consumerTool.showParameters();
//        for (int threadCount = 1; threadCount <= parallelThreads; threadCount++) {
//            consumerTool = new VaderBusInterfaceConsumerThread();
//            CommandLineSupport.setOptions(consumerTool, args);
//            consumerTool.start();
//            threads.add(consumerTool);
//        }
//
//        while (true) {
//            Iterator<VaderBusInterfaceConsumerThread> itr = threads.iterator();
//            int running = 0;
//            while (itr.hasNext()) {
//                VaderBusInterfaceConsumerThread thread = itr.next();
//                if (thread.isAlive()) {
//                    running++;
//                }
//            }
//
//            if (running <= 0) {
//                System.out.println("All threads completed their work");
//                break;
//            }
//
//            try {
//                Thread.sleep(1000);
//            } catch (Exception e) {
//            }
//        }
//        Iterator<VaderBusInterfaceConsumerThread> itr = threads.iterator();
//        while (itr.hasNext()) {
//            VaderBusInterfaceConsumerThread thread = itr.next();
//        }
//    }
    public void initConsumer(String theUrl, String topic, VaderMsgProcessor controller){
        url =  theUrl;
        subject = topic;
        
        ekgController = controller;

    }
    public void showParameters() {
        System.out.println("Connecting to URL: " + url);
        System.out.println("Consuming " + (topic ? "topic" : "queue") + ": " + subject);
        System.out.println("Using a " + (durable ? "durable" : "non-durable") + " subscription");
        System.out.println("Running " + parallelThreads + " parallel threads");
    }

    public void run() {
        try {
            running = true;

            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            connection = connectionFactory.createConnection();
            if (durable && clientId != null && clientId.length() > 0 && !"null".equals(clientId)) {
                connection.setClientID(clientId);
            }
            connection.setExceptionListener(this);
            connection.start();

            session = connection.createSession(transacted, ackMode);
            if (topic) {
                destination = session.createTopic(subject);
            } else {
                destination = session.createQueue(subject);
            }

//            replyProducer = session.createProducer(null);
//            replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            consumer = null;
            if (durable && topic) {
                consumer = session.createDurableSubscriber((Topic) destination, consumerName);
            } else {
                consumer = session.createConsumer(destination);
            }

            if (maxiumMessages > 0) {
                consumeMessagesAndClose(connection, session, consumer);
            } else {
                if (receiveTimeOut == 0) {
                    consumer.setMessageListener(this);
                } else {
                    consumeMessagesAndClose(connection, session, consumer, receiveTimeOut);
                }
            }

        } catch (Exception e) {
            System.out.println("[" + this.getName() + "] Caught: " + e);
            e.printStackTrace();
            running = false;
        }
        running = false;
    }

    public void onMessage(Message message) {
        try {

            if (message instanceof TextMessage) {
                TextMessage txtMsg = (TextMessage) message;
                if (verbose) {

                    String msg = txtMsg.getText();
                    int length = msg.length();
//                    if (length > 50) {
//                        msg = msg.substring(0, 50) + "...";
//                    }
                    System.out.println("[" + this.getName() + "] Received: '" + msg + "' (length " + length + ")");
                }
                TextMessage msg = (TextMessage) message;

//                logger.info("Reading message: " + msg.getText());
                String msgString = msg.getText();
                this.ekgController.processMessageFromBus(msgString);
            } else {
                if (verbose) {
                    System.out.println("[" + this.getName() + "] Received: '" + message + "'");
                }
            }

//            if (message.getJMSReplyTo() != null) {
//                replyProducer.send(message.getJMSReplyTo(), session.createTextMessage("Reply: " + message.getJMSMessageID()));
//            }

            if (transacted) {
                session.commit();
            } else if (ackMode == Session.CLIENT_ACKNOWLEDGE) {
                message.acknowledge();
            }

        } catch (JMSException e) {
            System.out.println("[" + this.getName() + "] Caught: " + e);
            e.printStackTrace();
        } finally {
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public synchronized void onException(JMSException ex) {
        System.out.println("[" + this.getName() + "] JMS Exception occured.  Shutting down client.");
        running = false;
    }

    public synchronized boolean isRunning() {
        return running;
    }

    protected void consumeMessagesAndClose(Connection connection, Session session, MessageConsumer consumer) throws JMSException,
            IOException {
        System.out.println("[" + this.getName() + "] We are about to wait until we consume: " + maxiumMessages
                + " message(s) then we will shutdown");

        for (int i = 0; i < maxiumMessages && isRunning();) {
            Message message = consumer.receive(1000);
            if (message != null) {
                i++;
                onMessage(message);
            }
        }
        System.out.println("[" + this.getName() + "] Closing connection");
        consumer.close();
        session.close();
        connection.close();
        if (pauseBeforeShutdown) {
            System.out.println("[" + this.getName() + "] Press return to shut down");
            System.in.read();
        }
    }

    protected void consumeMessagesAndClose(Connection connection, Session session, MessageConsumer consumer, long timeout)
            throws JMSException, IOException {
        System.out.println("[" + this.getName() + "] We will consume messages while they continue to be delivered within: " + timeout
                + " ms, and then we will shutdown");

        Message message;
        while ((message = consumer.receive(timeout)) != null) {
            onMessage(message);
        }

        System.out.println("[" + this.getName() + "] Closing connection");
        consumer.close();
        session.close();
        connection.close();
        if (pauseBeforeShutdown) {
            System.out.println("[" + this.getName() + "] Press return to shut down");
            System.in.read();
        }
    }

    public void setAckMode(String ackMode) {
        if ("CLIENT_ACKNOWLEDGE".equals(ackMode)) {
            this.ackMode = Session.CLIENT_ACKNOWLEDGE;
        }
        if ("AUTO_ACKNOWLEDGE".equals(ackMode)) {
            this.ackMode = Session.AUTO_ACKNOWLEDGE;
        }
        if ("DUPS_OK_ACKNOWLEDGE".equals(ackMode)) {
            this.ackMode = Session.DUPS_OK_ACKNOWLEDGE;
        }
        if ("SESSION_TRANSACTED".equals(ackMode)) {
            this.ackMode = Session.SESSION_TRANSACTED;
        }
    }

    public void setClientId(String clientID) {
        this.clientId = clientID;
    }

    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public void setMaxiumMessages(int maxiumMessages) {
        this.maxiumMessages = maxiumMessages;
    }

    public void setPauseBeforeShutdown(boolean pauseBeforeShutdown) {
        this.pauseBeforeShutdown = pauseBeforeShutdown;
    }

    public void setPassword(String pwd) {
        this.password = pwd;
    }

    public void setReceiveTimeOut(long receiveTimeOut) {
        this.receiveTimeOut = receiveTimeOut;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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
    
    public boolean terminateConnenction() {
        this.running = false;
        if (connection != null) {
            try {
                consumer.close();
                session.close();

                connection.stop();
                connection.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }
}
