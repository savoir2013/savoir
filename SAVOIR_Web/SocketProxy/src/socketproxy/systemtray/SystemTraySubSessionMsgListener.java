// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package socketproxy.systemtray;

import java.awt.Desktop;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Message;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
import socketproxy.LaunchHandler;
/**
 *
 * @author youy
 */
public class SystemTraySubSessionMsgListener implements MessageListener {

    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                System.out.println(((TextMessage) message).getText());
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
                    System.out.println("launching " + action);
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
                }

            }
            System.out.println("In SubSession Message consumer!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
