// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.utils;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.example.apnSchema.XmlResponseDocument;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ca.gc.nrc.iit.savoir.servletClient.APN;

public class ResponseParser {

	// public static void main(String[] args) {
	// String xmlString = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
	// "<xml-response type=\"list\">"
	// + "<resource>fa8a5e10-c53a-11dd-9dce-97bb50658a8d</resource>" +
	// "<scenario id=\"A\" status=\"unset\">"
	// + "<connection id=\"1228749379101\">" +
	// "<endpoint>ome-ottawa3</endpoint>" +
	// "<endpoint>hdxc-pacificwave</endpoint>"
	// + "</connection>" + "<connection id=\"1228749379102\">" +
	// "<endpoint>ome-ottawa3</endpoint>"
	// + "<endpoint>ome-fredricton</endpoint>" + "</connection>" + "</scenario>"
	// + "<scenario id=\"B\" status=\"unset\">"
	// + "<connection id=\"1228749379103\">" +
	// "<endpoint>ome-fredricton</endpoint>" +
	// "<endpoint>hdxc-pacificwave</endpoint>"
	// + "</connection>" + "</scenario>" + "</xml-response>";
	//
	// APN scenario = (APN) parse(xmlString);
	// }

	public static Object parse(String xmlString) {

		try {

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource inStream = new InputSource();
			inStream.setCharacterStream(new StringReader(xmlString));

			Document doc = db.parse(inStream);

			Node rootNode = doc.getDocumentElement();

			if (rootNode.getNodeType() == Node.ELEMENT_NODE) {

				Element rootElement = (Element) rootNode;

				if (rootElement.getAttribute("type").equals(XMLTags.LIST_TYPE)) {
					XmlResponseDocument xmlResponse = XmlResponseDocument.Factory
							.parse(xmlString);
					return new APN(xmlResponse.getXmlResponse());

				} else if (rootElement.getAttribute("type").equals(
						XMLTags.BOOLEAN_TYPE)) {
					return new Boolean(getCharacterDataFromElement(rootElement));
				} else if (rootElement.getAttribute("type").equals(
						XMLTags.CONFIG_RESPONSE_TYPE)) {
					Boolean done = null;
					String scenario = "";

					NodeList nodeList = doc.getElementsByTagName("result");

					Node node = nodeList.item(0);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						if (!getCharacterDataFromElement(element).equals(null)) {
							done = new Boolean(
									getCharacterDataFromElement(element));
						} else {
							done = null;
						}
					}

					nodeList = doc.getElementsByTagName("scenario");

					node = nodeList.item(0);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						scenario = getCharacterDataFromElement(element);
					}
					return new ConfigResponse(done, scenario);
				} else if (rootElement.getAttribute("type").equals(
						XMLTags.RESULT_TYPE)) {
					return getCharacterDataFromElement(rootElement);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String getResponseType(String xmlString) {

		System.out.println(xmlString);

		try {

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource inStream = new InputSource();
			inStream.setCharacterStream(new StringReader(xmlString));

			Document doc = db.parse(inStream);

			Node rootNode = doc.getDocumentElement();

			if (rootNode.getNodeType() == Node.ELEMENT_NODE) {

				Element rootElement = (Element) rootNode;

				return rootElement.getAttribute("type");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if (child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			return cd.getData();
		}
		return "?";
	}

	public static ClientException parseError(String xmlString) {
		String message = "";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();

			InputSource inStream = new InputSource();
			inStream.setCharacterStream(new StringReader(xmlString));

			Document doc = db.parse(inStream);

			Node rootNode = doc.getDocumentElement();

			if (rootNode.getNodeType() == Node.ELEMENT_NODE) {

				NodeList nodeList = doc.getElementsByTagName("faultString");
				if (nodeList != null && nodeList.item(0) != null) {
					Node node = nodeList.item(0);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						message = getCharacterDataFromElement(element);
					}
				}
			}

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new ClientException(message);
	}
}
