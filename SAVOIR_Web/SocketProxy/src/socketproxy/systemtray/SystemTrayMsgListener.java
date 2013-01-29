// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy.systemtray;

import java.awt.Desktop;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.Calendar;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ca.gc.nrc.iit.savoir.message.bindings.util.JaxbTool;

import org.w3c.dom.*; //import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.io.StringReader;
import org.xml.sax.InputSource;
import java.util.concurrent.ConcurrentLinkedQueue;
import socketproxy.LaunchHandler;

public class SystemTrayMsgListener implements MessageListener {
	// added for JMS
	private Connection connection = null;
	// private MessageProducer producer;
	private Session session;
	// private int count;
	// private long start;
	private Topic allUsersBroadcastTopic;
    private Topic allUsersInSessionBroadcastTopic;
    private Topic oneToOneUserTopic;
    private Topic oneToOneUserInSessionTopic;
    private Topic logInfoTopic;

	// private Topic control;
	//changed at 09-09-09 url will get from properties file
	//private String testUrl = "http://198.164.40.210:8891";
	//private String url = "";

	private String msg = "";

	static SystemTrayMsgListener instance = null;

	private ca.gc.nrc.iit.savoir.message.bindings.Message message = null;

    private int currentSessionId = 0;
    private String currentServer = "";
    private int userSessionId = 0;
    private String preUrl = "";

    private boolean isInitOnce = false;

    static ConcurrentLinkedQueue initRespMsgQueue =  new ConcurrentLinkedQueue();
    public static InitListenerRespThread initRespMsgThrd = new InitListenerRespThread();

    private HashSet preSubSessionIdSet = new HashSet();

	public SystemTrayMsgListener() {

	}

    public static SystemTrayMsgListener getInstance(){
        if(instance != null){
            return instance;
        }else{
            instance = new SystemTrayMsgListener();
            return instance;
        }
    }




	public void iniMsgListener(int sessionID, int userSessionID, String url, Boolean isListenLogInfo, String server) throws JMSException {

        long startTime = Calendar.getInstance().getTimeInMillis();

        if(isInitJMSAlready(url.trim(),sessionID, server)){
            //SystemTrayManager.getInstance().showInfoMessage("Please check whether the JMS server URL: " + url + " is Right?\n"
            //                                                + "If not please exit the current system try manager and login again!!");
            return;
        }else{
            this.terminatePrivousConnenction();
            String debugInfo = "Setup new connction for topics";
            System.out.println(debugInfo);
            SystemTrayManager.getInstance().showInfoMessage(debugInfo);
            currentSessionId = sessionID;
            currentServer = server;
        }
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
		connection = factory.createConnection();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        if (server.equalsIgnoreCase("Dev")) {
            allUsersBroadcastTopic = session.createTopic("savoirCommonDev");
            oneToOneUserTopic = session.createTopic("savoirDev" + String.valueOf(userSessionID));
            if (sessionID != -1) {
                allUsersInSessionBroadcastTopic = session.createTopic("savoirDev" + String.valueOf(sessionID));
                oneToOneUserInSessionTopic = session.createTopic("savoirDev" + String.valueOf(sessionID) + "-" + String.valueOf(userSessionID));
            }
        } else {
            allUsersBroadcastTopic = session.createTopic("savoirCommon");
            oneToOneUserTopic = session.createTopic("savoir" + String.valueOf(userSessionID));
            if (sessionID != -1) {
                allUsersInSessionBroadcastTopic = session.createTopic("savoir" + String.valueOf(sessionID));
                oneToOneUserInSessionTopic = session.createTopic("savoir" + String.valueOf(sessionID) + "-" + String.valueOf(userSessionID));
            }
        }
        if(isListenLogInfo ==  true && sessionID != -1){
            if(server.equalsIgnoreCase("Dev")){
            logInfoTopic = session.createTopic("savoirLogDev" + sessionID);
            }else{
                logInfoTopic = session.createTopic("savoirLog" + sessionID);
            }
        }
		MessageConsumer consumerForAllUserMessage = session.createConsumer(allUsersBroadcastTopic);
		consumerForAllUserMessage.setMessageListener(this);
        MessageConsumer consumerFortheUserMessage = session.createConsumer(oneToOneUserTopic);
		consumerFortheUserMessage.setMessageListener(this);
        if (sessionID != -1) {
            MessageConsumer consumerForAllUserMessageInSession = session.createConsumer(allUsersInSessionBroadcastTopic);
            consumerForAllUserMessageInSession.setMessageListener(this);
            MessageConsumer consumerFortheUserMessageInSession = session.createConsumer(oneToOneUserInSessionTopic);
            consumerFortheUserMessageInSession.setMessageListener(this);
        }
           
        if(isListenLogInfo == true && sessionID != -1){
            MessageConsumer consumerForSavoirLogMessage = session.createConsumer(logInfoTopic);
            consumerForSavoirLogMessage.setMessageListener(SystemTrayLogMsgListener.getInstance());
        }
        String debugTopicStr = "";
        if(allUsersBroadcastTopic != null){
            debugTopicStr = debugTopicStr + allUsersBroadcastTopic.getTopicName();
        }
        if(oneToOneUserTopic != null){
            debugTopicStr = debugTopicStr + "and\n" + oneToOneUserTopic.getTopicName();
        }
        if(allUsersInSessionBroadcastTopic != null){
            debugTopicStr = debugTopicStr + "and\n" + allUsersInSessionBroadcastTopic.getTopicName();
        }
        if(oneToOneUserInSessionTopic != null){
            debugTopicStr = debugTopicStr + "and\n" + oneToOneUserInSessionTopic.getTopicName();
        }
        String debuInfo = "System Tray is listen to: " + debugTopicStr;
        System.out.println(debuInfo);
        SystemTrayManager.getInstance().showInfoMessage(debuInfo);
        connection.start();

        System.out.println("Init Listener spend seconds" + (Calendar.getInstance().getTimeInMillis() - startTime)/1000);
    }



    private boolean isInitJMSAlready(String url, int sessionID, String server){
        if (connection != null && preUrl.equalsIgnoreCase(url)
                && (currentSessionId == sessionID)
                && (this.currentServer.equalsIgnoreCase(server))){
            return true;
        }else{
            preUrl = url;
            return false;
        }
    }
    
    private boolean terminatePrivousConnenction(){
        if(connection != null){
            try{
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
    
    private boolean isHasSubScribeForSubSession(String subSessionStr){
    	boolean result = false;
    	String trimedOne = subSessionStr.trim();
    	if(preSubSessionIdSet.contains(trimedOne)){
    		result = true;
    	}
    	return result;
    }


	public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
//				setMessage((ca.gc.nrc.iit.savoir.message.bindings.Message) JaxbTool
//						.XMLStringToObject(((TextMessage) message).getText()));
                String xmlStr = ((TextMessage) message).getText();
                System.out.println(xmlStr);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new InputSource(new StringReader(xmlStr)));
                doc.getDocumentElement().normalize();
                XPathFactory factory = XPathFactory.newInstance();
                XPath xpath = factory.newXPath();
                XPathExpression expr = xpath.compile("//message/@action");

                Object result = expr.evaluate(doc, XPathConstants.NODESET);
                NodeList nodes = (NodeList) result;
                String action = "";
                if (nodes.getLength() == 1) {
                     action = nodes.item(0).getNodeValue().trim();
                }
                System.out.println("Current received action is: " + action);

                if (action.equalsIgnoreCase("launch")) {
                    XPathExpression exprUrl = xpath.compile("//message/service/@path");

                    result = exprUrl.evaluate(doc, XPathConstants.NODESET);
                    nodes = (NodeList) result;
                    String pathStr = "";
                    if (nodes.getLength() == 1) {
                        pathStr = nodes.item(0).getNodeValue();
                        System.out.println(pathStr);
                    }
                    if (!pathStr.equals("")) {
                        SystemTrayManager.getInstance().showInfoMessage(pathStr);
                        LaunchHandler.launch(pathStr);
                    }
                } else if (action.equalsIgnoreCase("notify")) {
                    XPathExpression exprNot = xpath.compile("//message/service/notification");

                    result = exprNot.evaluate(doc, XPathConstants.STRING);
                    //nodes = (NodeList) result;
                    msg = (String)result;
//                    if (nodes.getLength() == 1) {
//                        msg = nodes.item(0).getNodeValue();
//                    }
                     if (!msg.equals("")) {
                        SystemTrayManager.getInstance().showInfoMessage(msg);
                    }
                } else if(action.equalsIgnoreCase("subscribe")){
                    XPathExpression exprServ = xpath.compile("//message/@sessionID");

                    result = exprServ.evaluate(doc, XPathConstants.NODESET);
                    nodes = (NodeList) result;
                    String sessionIdsStr = "";
                    if (nodes.getLength() == 1) {
                        sessionIdsStr = nodes.item(0).getNodeValue();
                    }
                    System.out.println("Current server is: " + this.currentServer);
                    System.out.println("Subcribe sessionID is: " + sessionIdsStr);
                    if(isHasSubScribeForSubSession(sessionIdsStr)){
                    	return;
                    }else{
                    	preSubSessionIdSet.add(sessionIdsStr.trim());
                    }
                    if(!sessionIdsStr.equalsIgnoreCase("")){
                        String[] ids = sessionIdsStr.split("-");
                        System.out.println("Subcribe sessionID length is: " + ids.length);
                        if(ids.length != 3){
                            SystemTrayManager.getInstance().showInfoMessage("sessionID in subscribe message is wrong!");
                        }else{
                            String allUsersInSubsessionTopicStr = ids[0] + "-" + ids[1];
                            String oneToOneUserInSubsessionTopicStr = sessionIdsStr.trim();
                            Topic allUsersInSubsessionTopic;
                            Topic oneToOneUserInSubsessionTopic;
                            Session jmsSessionForSubsessionTopic = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                            if (currentServer.equalsIgnoreCase("Dev")) {
                                allUsersInSubsessionTopic = jmsSessionForSubsessionTopic.createTopic("savoirDev" + allUsersInSubsessionTopicStr);
                                oneToOneUserInSubsessionTopic = jmsSessionForSubsessionTopic.createTopic("savoirDev" + oneToOneUserInSubsessionTopicStr);
                                System.out.println("creating topics: "
                                        + "savoirDev" + allUsersInSubsessionTopicStr
                                        + " " + "savoirDev" + oneToOneUserInSubsessionTopicStr);
                            }else{
                                allUsersInSubsessionTopic = jmsSessionForSubsessionTopic.createTopic("savoir" + allUsersInSubsessionTopicStr);
                                oneToOneUserInSubsessionTopic = jmsSessionForSubsessionTopic.createTopic("savoir" + oneToOneUserInSubsessionTopicStr);
                            }
                            SystemTraySubSessionMsgListener subSessionListener = new SystemTraySubSessionMsgListener();
                            MessageConsumer consumerForAllUsersInSubsessionMessage = jmsSessionForSubsessionTopic.createConsumer(allUsersInSubsessionTopic);
                            consumerForAllUsersInSubsessionMessage.setMessageListener(subSessionListener);
                            MessageConsumer consumerForOneToOneUserInSubsessionMessage = jmsSessionForSubsessionTopic.createConsumer(oneToOneUserInSubsessionTopic);
		                    consumerForOneToOneUserInSubsessionMessage.setMessageListener(subSessionListener);
                            String debuInfo = "System Tray is listening to added topics after received subscribe message: " + allUsersInSubsessionTopic.getTopicName()
                                    + "and\n" +  oneToOneUserInSubsessionTopic.getTopicName();
                            System.out.println(debuInfo);
                            SystemTrayManager.getInstance().showInfoMessage(debuInfo);
                        }
                    }
                    
                }else if (action.equalsIgnoreCase("startResponse")) {
                    SystemTrayManager.getInstance().showInfoMessage(xmlStr);
                    XPathExpression exprPath = xpath.compile("//message/service/@path");

                    result = exprPath.evaluate(doc, XPathConstants.NODESET);
                    nodes = (NodeList) result;
                    String pathStr = "";
                    if (nodes.getLength() == 1) {
                        pathStr = nodes.item(0).getNodeValue();
                    }
                     if (!pathStr.equals("")) {
                        SystemTrayManager.getInstance().showInfoMessage(pathStr);
                        LaunchHandler.launch(pathStr);
                    }
                }

               

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }





	public synchronized void setMessage(
			ca.gc.nrc.iit.savoir.message.bindings.Message message) {
		this.message = message;
	}

	public synchronized ca.gc.nrc.iit.savoir.message.bindings.Message getMessage() {
		return message;
	}

}
